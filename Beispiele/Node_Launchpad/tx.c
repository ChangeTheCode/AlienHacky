/*
 * tx.c
 *
 *  Created on: 13. Nov. 2016
 *      Author: Tobias
 */

#include "tx.h"
#include "uart.h"

/***** Variable declarations *****/
static Task_Params txTaskParams;
Task_Struct txTask;    /* not static so you can see in ROV */
static uint8_t txTaskStack[TX_TASK_STACK_SIZE];

uint8_t payload[] = "HelloBigWord";
uint8_t packet[PAYLOAD_LENGTH];

uint8_t uart_text[] = "packet gesendet\n";

static void txTaskFunction(UArg arg0, UArg arg1);

void TxTask_init(PIN_Handle inPinHandle)
{
    //pinHandle = inPinHandle;

    Task_Params_init(&txTaskParams);
    txTaskParams.stackSize = TX_TASK_STACK_SIZE;
    txTaskParams.priority = TX_TASK_PRIORITY;
    txTaskParams.stack = &txTaskStack;
    txTaskParams.arg0 = (UInt)1000000;

    Task_construct(&txTask, txTaskFunction, &txTaskParams, NULL);
}

static void txTaskFunction(UArg arg0, UArg arg1)
{
	uart_init();

	// rf init
    RF_Params rfParams;
    RF_Params_init(&rfParams);
    rfParams.nInactivityTimeout = 200; // 200us

    RF_cmdPropTx.pktLen = PAYLOAD_LENGTH;
    RF_cmdPropTx.pPkt = packet;
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


    	if(button_pressed == 1)
    	{
			button_pressed = 0;

			/* Create packet with command number, 6 Byte Mac, max of 12 Byte payload */
			packet[0] = (uint8_t)(1);  //Login

			// add mac address to packet
			uint8_t j;
			for (j = 2; j < 8; j++)
			{
				packet[j-1] = macAddress[j];
			}

			// add payload to packet (normally the 3 Accel Floats)
			uint8_t i;
			for (i = 7; i < PAYLOAD_LENGTH; i++)
			{
				//packet[i] = rand();
				packet[i] = payload[i-7];
			}

			/* Send packet */
			// stop RX CMD
			RF_Stat r = RF_cancelCmd(rfHandle, rx_cmd, 1);

			PIN_setOutputValue(ledPinHandle, Board_DIO15, 0);

			// post TX CMD
			RF_CmdHandle tx_cmd = RF_postCmd(rfHandle, (RF_Op*)&RF_cmdPropTx, RF_PriorityHighest, NULL, 0);

			// wait for TX CMD to complete
			RF_EventMask tx2 = RF_pendCmd(rfHandle, tx_cmd, (RF_EventLastCmdDone | RF_EventCmdAborted | RF_EventCmdStopped | RF_EventCmdCancelled));

			UART_write(uart, &uart_text, 16);
    	}
		Semaphore_post(semRxHandle);
    }
}
