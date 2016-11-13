/*
 * uart.h
 *
 *  Created on: 13. Nov. 2016
 *      Author: Tobias
 */

#ifndef UART_H_
#define UART_H_
#include <xdc/runtime/System.h>

#include "Board.h"
#include <ti/drivers/UART.h>

extern UART_Handle uart;

void uart_init(void);

#endif /* UART_H_ */
