/*
 * tx.c
 *
 *  Created on: 13. Nov. 2016
 *      Author: Tobias
 */

#include "RF.h"
#include "timer.h"

/***** Variable declarations *****/
static Task_Params tx_task_params;
Task_Struct tx_task;    /* not static so you can see in ROV */
static uint8_t tx_task_stack[TX_TASK_STACK_SIZE];

uint8_t payload[] = "HelloBigWord";
uint8_t packet_tx[PAYLOAD_LENGTH];

BOOLEAN login_ok = FALSE;
BOOLEAN login_sent = FALSE;

static void tx_task_function(UArg arg0, UArg arg1);

void tx_task_init(void)
{
    Task_Params_init(&tx_task_params);
    tx_task_params.stackSize = TX_TASK_STACK_SIZE;
    tx_task_params.priority = TX_TASK_PRIORITY;
    tx_task_params.stack = &tx_task_stack;
    tx_task_params.arg0 = (UInt)1000000;

    Task_construct(&tx_task, tx_task_function, &tx_task_params, NULL);
}

static void tx_task_function(UArg arg0, UArg arg1)
{
	// rf init
    RF_Params rf_params;
    RF_Params_init(&rf_params);
    rf_params.nInactivityTimeout = 200; // 200us

    RF_cmdPropTx.pktLen = PAYLOAD_LENGTH;
    RF_cmdPropTx.pPkt = packet_tx;
    RF_cmdPropTx.startTrigger.triggerType = TRIG_NOW;
    RF_cmdPropTx.startTrigger.pastTrig = 1;
    RF_cmdPropTx.startTime = 0;

    if (!RF_handle) {
		/* Request access to the radio */
		RF_handle = RF_open(&RF_object, &RF_prop, (RF_RadioSetup*)&RF_cmdPropRadioDivSetup, &rf_params);

		/* Set the frequency */
		RF_postCmd(RF_handle, (RF_Op*)&RF_cmdFs, RF_PriorityNormal, NULL, 0);
	}

    // get MAC-Address
	uint64_t mac_address_int = *((uint64_t *)(FCFG1_BASE + FCFG1_O_MAC_15_4_0)) & 0xFFFFFFFFFFFF;
	uint8_t mac_address[8];
	int y;
	for(y = 0; y < 8; y++) mac_address[y] = mac_address_int >> (8-1-y)*8;

    while(1)
    {
    	// Login
    	if((login_ok == FALSE) && (login_sent == FALSE))
    	{
    		packet_tx[0] = (uint8_t)(1);	//Login

    		// add mac address to packet
			uint8_t j;
			for (j = 2; j < 8; j++)
			{
				packet_tx[j-1] = mac_address[j];
			}
			/* Send packet */
			// stop RX CMD
			RF_Stat r = RF_cancelCmd(RF_handle, rx_cmd, 1);

			// post TX CMD
			RF_CmdHandle tx_cmd = RF_postCmd(RF_handle, (RF_Op*)&RF_cmdPropTx, RF_PriorityHighest, NULL, 0);

			login_sent = TRUE;
    	}
    	else
    	{
        	Semaphore_pend(sem_tx_handle, BIOS_WAIT_FOREVER);

        	// Heartbeat
        	if(heartbeat == TRUE)
        	{
        		packet_tx[0] = (uint8_t)(4); 	//Heartbeat
        		// add mac address to packet
				uint8_t j;
				for (j = 2; j < 8; j++)
				{
					packet_tx[j-1] = mac_address[j];
				}
				/* Send packet */
				// stop RX CMD
				RF_Stat r = RF_cancelCmd(RF_handle, rx_cmd, 1);

				// post TX CMD
				RF_CmdHandle tx_cmd = RF_postCmd(RF_handle, (RF_Op*)&RF_cmdPropTx, RF_PriorityHighest, NULL, 0);

				heartbeat = FALSE;
        	}

        	// Send packet
			if(button_pressed == 1) 	// TODO: durch Variable die beim Tritt gesetzt wird ersetzen!
			{
				button_pressed = 0;

				/* Create packet with command number, 6 Byte Mac, max of 12 Byte payload */
				packet_tx[0] = (uint8_t)(6);  //Kick

				// add mac address to packet	//TODO: add address received at the login instead of mac
				uint8_t j;
				for (j = 2; j < 8; j++)
				{
					packet_tx[j-1] = mac_address[j];
				}

				// add payload to packet  // TODO: add the 3 Accel Floats
				uint8_t i;
				for (i = 7; i < PAYLOAD_LENGTH; i++)
				{
					//packet[i] = rand();
					packet_tx[i] = payload[i-7];
				}

				/* Send packet */
				// stop RX CMD
				RF_Stat r = RF_cancelCmd(RF_handle, rx_cmd, 1);

				PIN_setOutputValue(LED_pin_handle, Board_DIO15, 0);

				// post TX CMD
				RF_CmdHandle tx_cmd = RF_postCmd(RF_handle, (RF_Op*)&RF_cmdPropTx, RF_PriorityHighest, NULL, 0);

				PIN_setOutputValue(LED_pin_handle, Board_LED0, 1);	// Red LED  TODO: turn off after 3 seconds (maybe function in pin.c)
			}
    	}
		Semaphore_post(sem_rx_handle);
    }
}
