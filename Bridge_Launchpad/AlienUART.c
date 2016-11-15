/*
* Alien_UART.c
*
*  Created on: 13. Nov. 2016
*      Author: Ursus Schneider
*/
#include "AlienUART.h"
#include "Board.h"
#include <ti/drivers/UART.h>
#include <ti/sysbios/knl/Task.h>
#include <xdc/runtime/System.h>
#include "RF.h"
#include "queue.h"

void Alien_UART_task (UArg arg0, UArg arg1);

#define UART_TASK_STACK_SIZE 1024

// UART Task
static Task_Params UART_task_params;
Task_Struct task_UART_struct;
uint8_t task_UART_stack [UART_TASK_STACK_SIZE];

// UART Struct
UART_Handle UART;
UART_Params UART_params;

// add to the top of the queue
BOOLEAN Alien_UART_send (uint8_t * data, uint8_t length) {

	// just add to the queue
	return queue (SEND_QUEUE, data, length);

}

// read the next entry from the queue
BOOLEAN Alien_UART_receive (uint8_t * data, uint8_t * length) {

	// just get from the queue
	return dequeue (RECEIVE_QUEUE, data, length);

}


// UART init
void Alien_UART_init (void) {

	// init the UART
	Board_initUART();

	/* Create a UART with data processing off. */
	UART_Params_init (&UART_params);
	UART_params.writeDataMode = UART_DATA_BINARY;
	UART_params.readDataMode = UART_DATA_BINARY;
	UART_params.readReturnMode = UART_RETURN_FULL;
	UART_params.readEcho = UART_ECHO_OFF;
	UART_params.baudRate = 115200;
	UART = UART_open (Board_UART0, &UART_params);
	//	if (UART == NULL) System_abort ("Error opening the UART");

	// create the UART task
	Task_Params_init (&UART_task_params);
	UART_task_params.stackSize = UART_TASK_STACK_SIZE;
	UART_task_params.stack = &task_UART_stack;
	UART_task_params.priority = 1;
	Task_construct (&task_UART_struct, (Task_FuncPtr) Alien_UART_task, &UART_task_params, NULL);
}

// UART Task
void Alien_UART_task (UArg arg0, UArg arg1) {

	System_printf("Starting the UART Task\n\n");

	while (1) {
		System_printf("in the UART Task\n");
	}

}
