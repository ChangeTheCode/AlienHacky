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

// UART init
void Alien_UART_init (void) {

	uint8_t length = 5;
	uint8_t length1 = 6;
	uint8_t length2 = 7;
	uint8_t data [MAX_PACKET_LENGTH]  = "aaaaa";
	uint8_t data1 [MAX_PACKET_LENGTH] = "bbbbbb";
	uint8_t data2 [MAX_PACKET_LENGTH] = "ccccccc";
	enqueue (data,  length);
	enqueue (data1, length1);
	enqueue (data2, length2);

	dequeue (data2, &length2);




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
