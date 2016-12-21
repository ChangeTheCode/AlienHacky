/*
 * RF.h
 *
 *  Created on: 14. Nov. 2016
 *      Author: Tobias
 */

#ifndef RF_H_
#define RF_H_

#include <ti/drivers/rf/RF.h>
#include <ti/drivers/PIN.h>
#include <ti/drivers/UART.h>

#include <xdc/runtime/System.h>

#include <ti/sysbios/BIOS.h>
#include <ti/sysbios/knl/Task.h>

#include <inc/hw_fcfg1.h>

#include "Board.h"
#include "RFQueue.h"
#include "smartrf_settings/smartrf_settings.h"

#include "AlienTypes.h"
#include "AlienUART.h"

/* RX and TX Task Config */
#define RX_TASK_STACK_SIZE 1024
#define RX_TASK_PRIORITY   2

#define TX_TASK_STACK_SIZE 1024
#define TX_TASK_PRIORITY   1

/* Packet RX Configuration */
#define DATA_ENTRY_HEADER_SIZE 8  /* Constant header size of a Generic Data Entry */
#define MAX_LENGTH             30 /* Max length byte the radio will accept */
#define NUM_DATA_ENTRIES       2  /* NOTE: Only two data entries supported at the moment */
#define NUM_APPENDED_BYTES     2  /* The Data Entries data field will contain:
                                   * 1 Header byte (RF_cmdPropRx.rxConf.bIncludeHdr = 0x1)
                                   * Max 30 payload bytes
                                   * 1 status byte (RF_cmdPropRx.rxConf.bAppendStatus = 0x1) */
#define MAX_PACKET_LENGTH (MAX_LENGTH + NUM_APPENDED_BYTES - 1)

/* Packet TX Configuration */
#define PACKET_LENGTH      19


extern PIN_Handle LED_pin_handle;
extern PIN_Handle button_pin_handle;

extern RF_Object RF_object;
extern RF_Handle RF_handle;

extern Semaphore_Handle sem_tx_handle;
extern Semaphore_Handle sem_rx_handle;

extern uint8_t kick;

extern RF_CmdHandle rx_cmd;

extern uint8_t packet_rx[MAX_PACKET_LENGTH];
extern uint8_t packet_rx_length;

extern BOOLEAN login_ok;
extern BOOLEAN login_sent;

extern char node_address;

void Alien_RF_init(void);
void rx_task_init(void);
void tx_task_init(void);

#endif /* RF_H_ */
