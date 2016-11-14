/*
 * uart.c
 *
 *  Created on: 13. Nov. 2016
 *      Author: Tobias
 */

#include "uart.h"

void uart_init(void)
{
	// uart init
	UART_Params uartParams;

	const char uartOk[] = "UART init ok\n";

	/* Create a UART with data processing off. */
	UART_Params_init(&uartParams);
	uartParams.writeDataMode = UART_DATA_BINARY;
	uartParams.readDataMode = UART_DATA_BINARY;
	uartParams.readReturnMode = UART_RETURN_FULL;
	uartParams.readEcho = UART_ECHO_OFF;
	uartParams.baudRate = 115200;
	uart = UART_open(Board_UART0, &uartParams);

	if (uart == NULL) {
		System_abort("Error opening the UART");
	}

	UART_write(uart, uartOk, sizeof(uartOk));
}
