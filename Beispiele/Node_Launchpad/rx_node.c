/*
 * rx.c
 *
 *  Created on: 13. Nov. 2016
 *      Author: Tobias
 */

#include "RF.h"

static Task_Params rxTaskParams;
Task_Struct rxTask;    /* not static so you can see in ROV */
static uint8_t rxTaskStack[RX_TASK_STACK_SIZE];

/* Receive dataQueue for RF Core to fill in data */
static dataQueue_t dataQueue;
static rfc_dataEntryGeneral_t* currentDataEntry;
uint8_t packetRxLength;
static uint8_t* packetRxDataPointer;
uint8_t packetRx[MAX_PACKET_LENGTH]; /* The length byte is stored in a separate variable */

static void rxTaskFunction(UArg arg0, UArg arg1);
static void callback(RF_Handle h, RF_CmdHandle ch, RF_EventMask e);


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

RF_CmdHandle rx_cmd;

void RxTask_init(PIN_Handle ledPinHandle) {
    //pinHandle = ledPinHandle;

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
        while(1); //TODO
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
		/* Enter RX mode and stay in RX until a packet arrives */
    	rx_cmd = RF_postCmd(rfHandle, (RF_Op*)&RF_cmdPropRx, RF_PriorityNormal, &callback, IRQ_RX_ENTRY_DONE);
		Semaphore_pend(semRxHandle, BIOS_WAIT_FOREVER);
    }

}

void callback(RF_Handle h, RF_CmdHandle ch, RF_EventMask e)
{
    if (e & RF_EventRxEntryDone)
    {
        /* Toggle pin to indicate RX */
        PIN_setOutputValue(ledPinHandle, Board_LED2,!PIN_getOutputValue(Board_LED2));	// Red LED

        /* Get current unhandled data entry */
        currentDataEntry = RFQueue_getDataEntry();

        /* Handle the packet data, located at &currentDataEntry->data:
         * - Length is the first byte with the current configuration
         * - Data starts from the second byte */
        packetRxLength      = *(uint8_t*)(&currentDataEntry->data);
        packetRxDataPointer = (uint8_t*)(&currentDataEntry->data + 1);

        /* Copy the payload + the status byte to the packet variable */
        memcpy(packetRx, packetRxDataPointer, (packetRxLength + 1));

        // TODO: maybe run mac check first

        switch(packetRx[0])
        {
			case 1:
				// login of other device (ignore)
				break;
			case 2:
				// login OK
				// to measure the roundtrip time of a packet
				PIN_setOutputValue(ledPinHandle, Board_DIO15, 1);

				/* Toggle pin to indicate OK */  // only for debug purposes ( later only turn on the green LED )
				PIN_setOutputValue(ledPinHandle, Board_LED1,!PIN_getOutputValue(Board_LED1));	// Green LED
				Semaphore_post(semTxHandle);
				break;
			case 3:
				// login not OK
				PIN_setOutputValue(ledPinHandle, Board_LED0, 1);
				// sleep(30); retry login
				break;
			case 4:
				// Heartbeat of other device (ignore)
				break;
			case 5:
				// Heartbeat ok
				// reset heartbeat packet counter;
				break;
			case 6:
				// Kick of other device (ignore)
				break;
			case 7:
				// Kick ok
				// reset kick packet counter + clock
				// light red + green LED for 3 seconds
				break;
			default:
				break;

        }

        RFQueue_nextEntry();
    }
}
