/*
 * tx_bridge.c
 *
 *  Created on: 14. Nov. 2016
 *      Author: Tobias
 */

#include "RF.h"


/***** Variable declarations *****/
static Task_Params tx_task_params;
Task_Struct tx_task;    /* not static so you can see in ROV */
static uint8_t tx_task_stack[TX_TASK_STACK_SIZE];

static void tx_task_function(UArg arg0, UArg arg1);

void tx_task_init ()
{
    Task_Params_init(&tx_task_params);
    tx_task_params.stackSize = TX_TASK_STACK_SIZE;
    tx_task_params.stack = &tx_task_stack;
    tx_task_params.priority = TX_TASK_PRIORITY;
    Task_construct(&tx_task, (Task_FuncPtr)tx_task_function, &tx_task_params, NULL);
}

void tx_task_function(UArg arg0, UArg arg1)
{
	uint8_t send_packet[MAX_PACKET_LENGTH];
	uint8_t send_packet_length = 0;
	BOOLEAN send_packet_buffer_overflow = TRUE;

	// rf init
	RF_Params rf_params;
	RF_Params_init(&rf_params);
	rf_params.nInactivityTimeout = 200; // 200us

	RF_cmdPropTx.pktLen = PAYLOAD_LENGTH;
	RF_cmdPropTx.pPkt = send_packet;
	RF_cmdPropTx.startTrigger.triggerType = TRIG_NOW;
	RF_cmdPropTx.startTrigger.pastTrig = 1;
	RF_cmdPropTx.startTime = 0;

	if (!RF_handle) {
		/* Request access to the radio */
		RF_handle = RF_open(&RF_object, &RF_prop, (RF_RadioSetup*)&RF_cmdPropRadioDivSetup, &rf_params);

		/* Set the frequency */
		RF_postCmd(RF_handle, (RF_Op*)&RF_cmdFs, RF_PriorityNormal, NULL, 0);
	}

    while(1)
    {
    	Semaphore_pend(sem_tx_handle, BIOS_WAIT_FOREVER);

		System_printf ("In RF send\n");
		System_flush();

		// if something was sent from the server send it
    	if(Alien_UART_receive (send_packet, &send_packet_length, &send_packet_buffer_overflow))
    	{
    		send_packet [send_packet_length] ='\0';
    		System_printf ("RTS: %s\n", send_packet);
    		System_flush();

			if ((!send_packet_buffer_overflow) && (send_packet_length > 0))
			{
				/* Send packet */
				// stop RX CMD
				RF_Stat r = RF_cancelCmd(RF_handle, rx_cmd, 1);

				// post TX CMD
				RF_CmdHandle tx_cmd = RF_postCmd(RF_handle, (RF_Op*)&RF_cmdPropTx, RF_PriorityHighest, NULL, 0);
			}
			// bufferoverflow or invalid packet length
    	}
    	else	// else send ok for login
    	{
    		// send login ok
			if(packet_rx[0] == 1)
			{
				send_packet[0] = 0xaa; 	// TODO: Abkl�ren, was der Server ins packet schreibt und was die Bridge
				send_packet[1] = 2;

				/* Send packet */
				// stop RX CMD
				RF_Stat r = RF_cancelCmd(RF_handle, rx_cmd, 1);

				// post TX CMD
				RF_CmdHandle tx_cmd = RF_postCmd(RF_handle, (RF_Op*)&RF_cmdPropTx, RF_PriorityHighest, NULL, 0);
			}
    	}


		Semaphore_post(sem_rx_handle);
    }
}
