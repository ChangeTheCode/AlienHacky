/*
* Alien_UART.c
*
*  Created on: 13. Nov. 2016
*      Author: Ursus Schneider
*/
#include <RF_bridge.h>
#include "AlienUART.h"
#include "Board.h"
#include <ti/sysbios/BIOS.h>
#include <ti/drivers/UART.h>
#include <ti/sysbios/knl/Task.h>
#include <xdc/runtime/System.h>
#include <ti/sysbios/knl/Semaphore.h>
#include "queue.h"

void Alien_UART_send_task (UArg arg0, UArg arg1);
void Alien_UART_receive_task (UArg arg0, UArg arg1);

#define UART_TASK_STACK_SIZE 1024

// UART send Task
static Task_Params UART_send_task_params;
Task_Struct task_UART_send_struct;
uint8_t task_UART_send_stack [UART_TASK_STACK_SIZE];

// UART receive Task
static Task_Params UART_receive_task_params;
Task_Struct task_UART_receive_struct;
uint8_t task_UART_receive_stack [UART_TASK_STACK_SIZE];

// UART Struct
UART_Handle UART;
UART_Params UART_params;

// semaphore for the send semaphore
Semaphore_Struct semaphore_struct;
Semaphore_Handle semaphore_handle;
Semaphore_Params semaphore_params;

// we are reading bytewise from the UART so we need a temp array to keep the bytes in
uint8_t temp_pos = 0;
uint8_t temp_data [MAX_PACKET_LENGTH] = {""};
char char_read;
BOOLEAN buffer_overflow = FALSE;

// add to the top of the queue
BOOLEAN Alien_UART_send (uint8_t * data, uint8_t length) {

	// just add to the queue
	BOOLEAN rc = queue (SEND_QUEUE, data, length, FALSE);

	// let the UART Task know that you have something to send
	Semaphore_post (semaphore_handle);

	// finished
	return rc;
}

// read the next entry from the queue
BOOLEAN Alien_UART_receive (uint8_t * data, uint8_t * length, BOOLEAN * buffer_overflow) {

	// just get from the queue
	return dequeue (RECEIVE_QUEUE, data, length, buffer_overflow);
}
// this gets called when you are doing the callback

void UART_read_callback (UART_Handle UART, void * data, size_t length) {

	// check if it was our EOL char
	if (char_read == END_OF_RECORD) {
		// take what you read and place it in the receive queue
		BOOLEAN rc = queue (RECEIVE_QUEUE, temp_data, temp_pos, buffer_overflow);

		// point to the top of the list
		temp_pos = 0;
		buffer_overflow = FALSE;

		// TODO: Call the RF read function
		// Semaphore_post (rftx_semaphore_handle);
	} else {
		temp_data [temp_pos++] = char_read;
		if (temp_pos == MAX_PACKET_LENGTH) {
			// this should never happen, but if it does just go back on char and set the error flag
			buffer_overflow = TRUE;
			temp_pos--;
		}
	}
}

// UART init
void Alien_UART_init (void) {

	// init the UART
	Board_initUART();

	/* Create a UART with data processing off. */
	UART_Params_init (&UART_params);
	UART_params.writeDataMode = UART_DATA_BINARY;

	UART_params.readDataMode = UART_DATA_BINARY;
	UART_params.readMode = UART_MODE_CALLBACK;
	UART_params.readCallback = &UART_read_callback;
	UART_params.readReturnMode = UART_RETURN_FULL;
	UART_params.readEcho = UART_ECHO_OFF;

	UART_params.baudRate = 115200;
	UART = UART_open (Board_UART0, &UART_params);
	if (UART == NULL)
		System_abort ("Error opening the UART");

	// create the UART send task
	Task_Params_init (&UART_send_task_params);
	UART_send_task_params.stackSize = UART_TASK_STACK_SIZE;
	UART_send_task_params.stack = &task_UART_send_stack;
	UART_send_task_params.priority = 1;
	Task_construct (&task_UART_send_struct, (Task_FuncPtr) Alien_UART_send_task, &UART_send_task_params, NULL);

	// create the UART receive task
	Task_Params_init (&UART_receive_task_params);
	UART_receive_task_params.stackSize = UART_TASK_STACK_SIZE;
	UART_receive_task_params.stack = &task_UART_receive_stack;
	UART_receive_task_params.priority = 1;
	Task_construct (&task_UART_receive_struct, (Task_FuncPtr) Alien_UART_receive_task, &UART_receive_task_params, NULL);

	/* Construct a Semaphore object to be used as a resource lock, inital count 0 */
	Semaphore_Params_init (&semaphore_params);
	Semaphore_construct (&semaphore_struct, 0, &semaphore_params);
	Semaphore_construct (&semaphore_struct, 0, &semaphore_params);

}

// UART Task
void Alien_UART_send_task (UArg arg0, UArg arg1) {

    uint8_t length;
    uint8_t data [MAX_PACKET_LENGTH];

    // loop forever
	while (TRUE) {
		// wait for something or someone to wake me
		Semaphore_pend (semaphore_handle, BIOS_WAIT_FOREVER);

		// send everything in the send queue
		do {
			dequeue (SEND_QUEUE, data, &length, FALSE);
			if (length > 0) {
				   UART_write (UART, data, length);
			}
		} while (length > 0);

	}

}

// UART Task
void Alien_UART_receive_task (UArg arg0, UArg arg1) {

	// setup reading via interrupt -> one char at a time as we do not know the how long our records are!
	UART_read(UART, (void *) char_read, 1);

}
