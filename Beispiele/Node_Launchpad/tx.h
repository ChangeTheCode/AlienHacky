/*
 * tx.h
 *
 *  Created on: 13. Nov. 2016
 *      Author: Tobias
 */

#ifndef TX_H_
#define TX_H_

#include <ti/drivers/rf/RF.h>
#include <ti/drivers/PIN.h>
#include <ti/drivers/UART.h>

#include <ti/sysbios/BIOS.h>
#include <ti/sysbios/knl/Task.h>

#include <inc/hw_fcfg1.h>

#include "Board.h"
#include "smartrf_settings/smartrf_settings.h"

#define TX_TASK_STACK_SIZE 1024
#define TX_TASK_PRIORITY   2

/* Packet TX Configuration */
#define PAYLOAD_LENGTH      19

extern PIN_Handle ledPinHandle;
extern PIN_Handle buttonPinHandle;

extern uint8_t button_pressed;

extern RF_Object rfObject;
extern RF_Handle rfHandle;

extern RF_CmdHandle rx_cmd;

extern Semaphore_Handle semTxHandle;
extern Semaphore_Handle semRxHandle;

extern UART_Handle uart;

void TxTask_init(PIN_Handle inPinHandle);

#endif /* TX_H_ */
