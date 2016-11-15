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

/* RX and TX Task Config */
#define RX_TASK_STACK_SIZE 1024
#define RX_TASK_PRIORITY   1

#define TX_TASK_STACK_SIZE 1024
#define TX_TASK_PRIORITY   2

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
#define PAYLOAD_LENGTH      19


extern PIN_Handle ledPinHandle;
extern PIN_Handle buttonPinHandle;

extern RF_Object rfObject;
extern RF_Handle rfHandle;

extern Semaphore_Handle semTxHandle;
extern Semaphore_Handle semRxHandle;

extern UART_Handle uart;

extern uint8_t button_pressed;

extern RF_CmdHandle rx_cmd;

extern uint8_t packetRx[MAX_PACKET_LENGTH];
extern uint8_t packetRxLength;

void RxTask_init(PIN_Handle ledPinHandle);
void TxTask_init(PIN_Handle ledPinHandle);

#endif /* RF_H_ */
