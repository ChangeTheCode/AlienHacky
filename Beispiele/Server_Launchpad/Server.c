/*
 * Copyright (c) 2015-2016, Texas Instruments Incorporated
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * *  Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * *  Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 * *  Neither the name of Texas Instruments Incorporated nor the names of
 *    its contributors may be used to endorse or promote products derived
 *    from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

/***** Includes *****/
#include <stdlib.h>
#include <xdc/std.h>
#include <xdc/cfg/global.h>
#include <xdc/runtime/System.h>

#include <ti/sysbios/BIOS.h>
#include <ti/sysbios/knl/Task.h>

/* Drivers */
#include <ti/drivers/rf/RF.h>
#include <ti/drivers/PIN.h>
#include <driverlib/rf_prop_mailbox.h>

#include <inc/hw_fcfg1.h>

/* Board Header files */
#include "Board.h"

#include "RFQueue.h"
#include "smartrf_settings/smartrf_settings.h"

#include <ti/drivers/UART.h>

#include <stdlib.h>

/* Pin driver handle */
static PIN_Handle ledPinHandle;
static PIN_State ledPinState;

/*
 * Application LED pin configuration table:
 *   - All LEDs board LEDs are off.
 */
PIN_Config pinTable[] =
{
    Board_LED2 | PIN_GPIO_OUTPUT_EN | PIN_GPIO_LOW | PIN_PUSHPULL | PIN_DRVSTR_MAX,
    PIN_TERMINATE
};


/***** Defines *****/
#define RX_TASK_STACK_SIZE 1024
#define RX_TASK_PRIORITY   2

#define UART_TASK_STACK_SIZE     1024

/* Packet RX Configuration */
#define DATA_ENTRY_HEADER_SIZE 8  /* Constant header size of a Generic Data Entry */
#define MAX_LENGTH             30 /* Max length byte the radio will accept */
#define NUM_DATA_ENTRIES       2  /* NOTE: Only two data entries supported at the moment */
#define NUM_APPENDED_BYTES     2  /* The Data Entries data field will contain:
                                   * 1 Header byte (RF_cmdPropRx.rxConf.bIncludeHdr = 0x1)
                                   * Max 30 payload bytes
                                   * 1 status byte (RF_cmdPropRx.rxConf.bAppendStatus = 0x1) */

/* Packet TX Configuration */
#define PAYLOAD_LENGTH      19

/***** Prototypes *****/
static void rxTaskFunction(UArg arg0, UArg arg1);
void uartTask(UArg arg0, UArg arg1);
static void callback(RF_Handle h, RF_CmdHandle ch, RF_EventMask e);

/***** Variable declarations *****/
static Task_Params rxTaskParams;
Task_Struct rxTask;    /* not static so you can see in ROV */
static uint8_t rxTaskStack[RX_TASK_STACK_SIZE];

static Task_Params uartTaskParams;
Task_Struct taskUartStruct;
uint8_t taskUartStack[UART_TASK_STACK_SIZE];

static RF_Object rfObject;
static RF_Handle rfHandle;

Semaphore_Struct semTxStruct;
Semaphore_Handle semTxHandle;

Semaphore_Struct semRxStruct;
Semaphore_Handle semRxHandle;


/* Buffer which contains all Data Entries for receiving data.
 * Pragmas are needed to make sure this buffer is 4 byte aligned (requirement from the RF Core) */
#if defined(__TI_COMPILER_VERSION__)
    #pragma DATA_ALIGN (rxDataEntryBuffer, 4);
        static uint8_t rxDataEntryBuffer[RF_QUEUE_DATA_ENTRY_BUFFER_SIZE(NUM_DATA_ENTRIES,
                                                                 MAX_LENGTH,
                                                                 NUM_APPENDED_BYTES)];
#elif defined(__IAR_SYSTEMS_ICC__)
    #pragma data_alignment = 4
        static uint8_t rxDataEntryBuffer[RF_QUEUE_DATA_ENTRY_BUFFER_SIZE(NUM_DATA_ENTRIES,
                                                                 MAX_LENGTH,
                                                                 NUM_APPENDED_BYTES)];
#elif defined(__GNUC__)
        static uint8_t rxDataEntryBuffer [RF_QUEUE_DATA_ENTRY_BUFFER_SIZE(NUM_DATA_ENTRIES,
            MAX_LENGTH, NUM_APPENDED_BYTES)] __attribute__ ((aligned (4)));
#else
    #error This compiler is not supported.
#endif

/* Receive dataQueue for RF Core to fill in data */
static dataQueue_t dataQueue;
static rfc_dataEntryGeneral_t* currentDataEntry;
static uint8_t packetLength;
static uint8_t* packetDataPointer;

static PIN_Handle pinHandle;

static uint8_t packet[MAX_LENGTH + NUM_APPENDED_BYTES - 1]; /* The length byte is stored in a separate variable */

uint8_t input = 0;

uint8_t packetTX[PAYLOAD_LENGTH];

RF_CmdHandle rx_cmd;

/***** Function definitions *****/
void RxTask_init(PIN_Handle ledPinHandle) {
    pinHandle = ledPinHandle;

    Task_Params_init(&rxTaskParams);
    rxTaskParams.stackSize = RX_TASK_STACK_SIZE;
    rxTaskParams.priority = RX_TASK_PRIORITY;
    rxTaskParams.stack = &rxTaskStack;
    rxTaskParams.arg0 = (UInt)1000000;

    Task_construct(&rxTask, rxTaskFunction, &rxTaskParams, NULL);
}

static void rxTaskFunction(UArg arg0, UArg arg1)
{
	//rx init
    RF_Params rfParams;
    RF_Params_init(&rfParams);

    rfParams.nInactivityTimeout = 200; // 200us

    if( RFQueue_defineQueue(&dataQueue,
                            rxDataEntryBuffer,
                            sizeof(rxDataEntryBuffer),
                            NUM_DATA_ENTRIES,
                            MAX_LENGTH + NUM_APPENDED_BYTES))
    {
        /* Failed to allocate space for all data entries */
        while(1);
    }

    /* Modify CMD_PROP_RX command for application needs */
    RF_cmdPropRx.pQueue = &dataQueue;           /* Set the Data Entity queue for received data */
    RF_cmdPropRx.rxConf.bAutoFlushIgnored = 1;  /* Discard ignored packets from Rx queue */
    RF_cmdPropRx.rxConf.bAutoFlushCrcErr = 1;   /* Discard packets with CRC error from Rx queue */
    RF_cmdPropRx.maxPktLen = MAX_LENGTH;        /* Implement packet length filtering to avoid PROP_ERROR_RXBUF */
    RF_cmdPropRx.pktConf.bRepeatOk = 0;
    RF_cmdPropRx.pktConf.bRepeatNok = 0;

    if (!rfHandle) {
		/* Request access to the radio */
		rfHandle = RF_open(&rfObject, &RF_prop, (RF_RadioSetup*)&RF_cmdPropRadioDivSetup, &rfParams);

		/* Set the frequency */
		RF_postCmd(rfHandle, (RF_Op*)&RF_cmdFs, RF_PriorityNormal, NULL, 0);
	}

    while(1)
    {
		/* Enter RX mode and stay forever in RX */
    	rx_cmd = RF_postCmd(rfHandle, (RF_Op*)&RF_cmdPropRx, RF_PriorityNormal, &callback, IRQ_RX_ENTRY_DONE);
		Semaphore_pend(semRxHandle, BIOS_WAIT_FOREVER);
    }

}

void callback(RF_Handle h, RF_CmdHandle ch, RF_EventMask e)
{
    if (e & RF_EventRxEntryDone)
    {
        /* Toggle pin to indicate RX */
        PIN_setOutputValue(pinHandle, Board_LED2,!PIN_getOutputValue(Board_LED2));

        /* Get current unhandled data entry */
        currentDataEntry = RFQueue_getDataEntry();

        /* Handle the packet data, located at &currentDataEntry->data:
         * - Length is the first byte with the current configuration
         * - Data starts from the second byte */
        packetLength      = *(uint8_t*)(&currentDataEntry->data);
        packetDataPointer = (uint8_t*)(&currentDataEntry->data + 1);

        /* Copy the payload + the status byte to the packet variable */
        memcpy(packet, packetDataPointer, (packetLength + 1));

        Semaphore_post(semTxHandle);

        RFQueue_nextEntry();
    }
}

void uartTask_init (PIN_Handle inPinHandle)
{
	pinHandle = ledPinHandle;

    Task_Params_init(&uartTaskParams);
    uartTaskParams.stackSize = UART_TASK_STACK_SIZE;
    uartTaskParams.stack = &taskUartStack;
    uartTaskParams.priority = 1;
    Task_construct(&taskUartStruct, (Task_FuncPtr)uartTask, &uartTaskParams, NULL);
}

void uartTask(UArg arg0, UArg arg1)
{
	// uart init
    UART_Handle uart;
    UART_Params uartParams;

    const char uartOk[] = "UART init ok\n";

	/* Create a UART with data processing off. */
	UART_Params_init(&uartParams);
	uartParams.writeDataMode = UART_DATA_BINARY;
	uartParams.readDataMode = UART_DATA_BINARY;
	uartParams.readReturnMode = UART_RETURN_FULL;
	uartParams.readEcho = UART_ECHO_OFF;
	uartParams.baudRate = 9600;
	uart = UART_open(Board_UART0, &uartParams);

	if (uart == NULL) {
		System_abort("Error opening the UART");
	}

	UART_write(uart, uartOk, sizeof(uartOk));

	// rf init
	RF_Params rfParams;
	RF_Params_init(&rfParams);
	rfParams.nInactivityTimeout = 200; // 200us

	RF_cmdPropTx.pktLen = PAYLOAD_LENGTH;
	RF_cmdPropTx.pPkt = packetTX;
	RF_cmdPropTx.startTrigger.triggerType = TRIG_NOW;
	RF_cmdPropTx.startTrigger.pastTrig = 1;
	RF_cmdPropTx.startTime = 0;

	if (!rfHandle) {
		/* Request access to the radio */
		rfHandle = RF_open(&rfObject, &RF_prop, (RF_RadioSetup*)&RF_cmdPropRadioDivSetup, &rfParams);

		/* Set the frequency */
		RF_postCmd(rfHandle, (RF_Op*)&RF_cmdFs, RF_PriorityNormal, NULL, 0);
	}

    // get MAC-Address
	uint64_t macAddressInt = *((uint64_t *)(FCFG1_BASE + FCFG1_O_MAC_15_4_0)) & 0xFFFFFFFFFFFF;
	uint8_t macAddress[8];
	int y;
	for(y = 0; y < 8; y++) macAddress[y] = macAddressInt >> (8-1-y)*8;

    while(1)
    {
    	Semaphore_pend(semTxHandle, BIOS_WAIT_FOREVER);

		UART_write(uart, &packet, packetLength);
		if(packet[0] == 1)
		{
			packetTX[0] = 2;
			/* Send packet */
			// stop RX CMD
			RF_Stat r = RF_cancelCmd(rfHandle, rx_cmd, 1);

			// post TX CMD
			RF_CmdHandle tx_cmd = RF_postCmd(rfHandle, (RF_Op*)&RF_cmdPropTx, RF_PriorityHighest, NULL, 0);

			// wait for TX CMD to complete
			RF_EventMask tx2 = RF_pendCmd(rfHandle, tx_cmd, (RF_EventLastCmdDone | RF_EventCmdAborted | RF_EventCmdStopped | RF_EventCmdCancelled));
			UART_write(uart, "OK gesendet\n", 13);
		}
		Semaphore_post(semRxHandle);
    }
}

/*
 *  ======== main ========
 */
int main(void)
{
	Semaphore_Params semParams;

    /* Call board init functions. */
    Board_initGeneral();
    Board_initUART();

    /* Open LED pins */
    ledPinHandle = PIN_open(&ledPinState, pinTable);
    if(!ledPinHandle)
    {
        System_abort("Error initializing board LED pins\n");
    }

    /* Construct a Semaphore object to be used as a resource lock, inital count 0 */
    Semaphore_Params_init(&semParams);
    Semaphore_construct(&semTxStruct, 0, &semParams);
    Semaphore_construct(&semRxStruct, 0, &semParams);


    /* Obtain instance handle */
    semTxHandle = Semaphore_handle(&semTxStruct);
    semRxHandle = Semaphore_handle(&semRxStruct);

    /* Initialize task */
    RxTask_init(ledPinHandle);
    uartTask_init(ledPinHandle);

    /* Start BIOS */
    BIOS_start();

    return (0);
}
