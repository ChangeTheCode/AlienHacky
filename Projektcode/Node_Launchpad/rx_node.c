/*
 * rx.c
 *
 *  Created on: 13. Nov. 2016
 *      Author: Tobias
 */

#include "RF.h"
#include "timer.h"

static Task_Params rx_task_params;
Task_Struct rx_task;    /* not static so you can see in ROV */
static uint8_t rx_task_stack[RX_TASK_STACK_SIZE];

/* Receive dataQueue for RF Core to fill in data */
static dataQueue_t rx_data_queue;
static rfc_dataEntryGeneral_t* current_data_entry;
uint8_t packet_rx_length;
static uint8_t* packet_rx_data_pointer;
uint8_t packet_rx[MAX_PACKET_LENGTH]; /* The length byte is stored in a separate variable */

static void rx_task_function(UArg arg0, UArg arg1);
static void rx_callback(RF_Handle h, RF_CmdHandle ch, RF_EventMask e);


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

void rx_task_init(void) {
    Task_Params_init(&rx_task_params);
    rx_task_params.stackSize = RX_TASK_STACK_SIZE;
    rx_task_params.priority = RX_TASK_PRIORITY;
    rx_task_params.stack = &rx_task_stack;
    rx_task_params.arg0 = (UInt)1000000;

    Task_construct(&rx_task, rx_task_function, &rx_task_params, NULL);
}

static void rx_task_function(UArg arg0, UArg arg1)
{
	//rx init
    RF_Params rf_params;
    RF_Params_init(&rf_params);

    rf_params.nInactivityTimeout = 200; // 200us

    if( RFQueue_defineQueue(&rx_data_queue,
                            rxDataEntryBuffer,
                            sizeof(rxDataEntryBuffer),
                            NUM_DATA_ENTRIES,
                            MAX_LENGTH + NUM_APPENDED_BYTES))
    {
        /* Failed to allocate space for all data entries */
    	System_abort("Failed to allocate space for all RX data entries");
    }

    /* Modify CMD_PROP_RX command for application needs */
    RF_cmdPropRx.pQueue = &rx_data_queue;           /* Set the Data Entity queue for received data */
    RF_cmdPropRx.rxConf.bAutoFlushIgnored = 1;  /* Discard ignored packets from Rx queue */
    RF_cmdPropRx.rxConf.bAutoFlushCrcErr = 1;   /* Discard packets with CRC error from Rx queue */
    RF_cmdPropRx.maxPktLen = MAX_LENGTH;        /* Implement packet length filtering to avoid PROP_ERROR_RXBUF */
    RF_cmdPropRx.pktConf.bRepeatOk = 0;
    RF_cmdPropRx.pktConf.bRepeatNok = 1;
    RF_cmdPropRx.pktConf.bChkAddress = 1;
    RF_cmdPropRx.address0 = 0xaa;
    RF_cmdPropRx.address1 = 0xaa;
    RF_cmdPropRx.rxConf.bAppendStatus = 0;

    if (!RF_handle) {
		/* Request access to the radio */
		RF_handle = RF_open(&RF_object, &RF_prop, (RF_RadioSetup*)&RF_cmdPropRadioDivSetup, &rf_params);

		/* Set the frequency */
		RF_postCmd(RF_handle, (RF_Op*)&RF_cmdFs, RF_PriorityNormal, NULL, 0);
	}

    while(1)
    {
		/* Enter RX mode and stay in RX until a packet arrives */
    	rx_cmd = RF_postCmd(RF_handle, (RF_Op*)&RF_cmdPropRx, RF_PriorityNormal, &rx_callback, IRQ_RX_ENTRY_DONE);
		Semaphore_pend(sem_rx_handle, BIOS_WAIT_FOREVER);
    }

}

void rx_callback(RF_Handle h, RF_CmdHandle ch, RF_EventMask e)
{
    if (e & RF_EventRxEntryDone)
    {
        /* Toggle pin to indicate RX */
        //PIN_setOutputValue(LED_pin_handle, Board_LED2, !PIN_getOutputValue(Board_LED2));	// Red LED

        /* Get current unhandled data entry */
        current_data_entry = RFQueue_getDataEntry();

        /* Handle the packet data, located at &currentDataEntry->data:
         * - Length is the first byte with the current configuration
         * - Data starts from the second byte */
        packet_rx_length      = *(uint8_t*)(&current_data_entry->data);
        packet_rx_data_pointer = (uint8_t*)(&current_data_entry->data + 1);

        /* Copy the payload + the status byte to the packet variable */
        memcpy(packet_rx, packet_rx_data_pointer, (packet_rx_length + 1));

        switch(packet_rx[1]) 		//byte 0 is the address of the sender (bridge: 0xaa)
        {
			case 2:
				// login OK
				// to measure the roundtrip time of a packet
				//PIN_setOutputValue(LED_pin_handle, Board_DIO15, 1);

				/* Toggle pin to indicate OK */  // only for debug purposes ( later only turn on the green LED )
				PIN_setOutputValue(LED_pin_handle, Board_LED1, 1);	// Green LED
				login_ok = TRUE;
				Semaphore_post(sem_tx_handle);
				break;
			case 3:
				// login not OK
				PIN_setOutputValue(LED_pin_handle, Board_LED0, 1); 	// Red LED
				login_ok = FALSE;
				GPTimerCC26XX_start(timer_login_handle); // try again after 30 seconds
				break;
			default:
				break;
        }

        RFQueue_nextEntry();
    }
}
