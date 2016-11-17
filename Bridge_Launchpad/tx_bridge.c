/*
 * tx_bridge.c
 *
 *  Created on: 14. Nov. 2016
 *      Author: Tobias
 */

#include "RF.h"
#include "uart.h"

/***** Variable declarations *****/
static Task_Params txTaskParams;
Task_Struct txTask;    /* not static so you can see in ROV */
static uint8_t txTaskStack[TX_TASK_STACK_SIZE];

uint8_t payload[] = "HelloBigWord";
uint8_t packetTx[PAYLOAD_LENGTH];

uint8_t uart_text[] = "packet gesendet\n";

static void txTaskFunction(UArg arg0, UArg arg1);

void TxTask_init (PIN_Handle ledPinHandle)
{
	//pinHandle = ledPinHandle;

    Task_Params_init(&txTaskParams);
    txTaskParams.stackSize = TX_TASK_STACK_SIZE;
    txTaskParams.stack = &txTaskStack;
    txTaskParams.priority = TX_TASK_PRIORITY;
    Task_construct(&txTask, (Task_FuncPtr)txTaskFunction, &txTaskParams, NULL);
}

void txTaskFunction(UArg arg0, UArg arg1)
{
	uart_init();

	// rf init
	RF_Params rfParams;
	RF_Params_init(&rfParams);
	rfParams.nInactivityTimeout = 200; // 200us

	RF_cmdPropTx.pktLen = PAYLOAD_LENGTH;
	RF_cmdPropTx.pPkt = packetTx;
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

    	// write the packet payload to the uart
		UART_write(uart, &packetRx, packetRxLength);
		if(packetRx[0] == 1)
		{
			packetTx[0] = 0xaa;
			packetTx[1] = 2;

			/* Send packet */
			// stop RX CMD
			RF_Stat r = RF_cancelCmd(rfHandle, rx_cmd, 1);

			PIN_setOutputValue(ledPinHandle, Board_DIO15, 0);

			// post TX CMD
			RF_CmdHandle tx_cmd = RF_postCmd(rfHandle, (RF_Op*)&RF_cmdPropTx, RF_PriorityHighest, NULL, 0);

			// wait for TX CMD to complete
			//RF_EventMask tx2 = RF_pendCmd(rfHandle, tx_cmd, (RF_EventLastCmdDone | RF_EventCmdAborted | RF_EventCmdStopped | RF_EventCmdCancelled));

			UART_write(uart, "OK gesendet\n", 13);
		}
		Semaphore_post(semRxHandle);
    }
}
