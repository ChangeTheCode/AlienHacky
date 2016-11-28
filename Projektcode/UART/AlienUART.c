/*
 * Alien_UART.c
 *
 *  Created on: 13. Nov. 2016
 *      Author: Ursus Schneider
 */
#include "RF.h"
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
void UART_read_callback (UART_Handle UART, void * data, size_t length);

#define UART_TASK_STACK_SIZE 1024

// UART send Task
static Task_Params UART_send_task_params;
Task_Struct task_UART_send_struct;
uint8_t task_UART_send_stack [UART_TASK_STACK_SIZE];

// UART receive Task
static Task_Params UART_receive_task_params;
Task_Struct task_UART_receive_struct;
uint8_t task_UART_receive_stack [UART_TASK_STACK_SIZE];

// UART structure
UART_Handle UART;
UART_Params UART_params;

// semaphore for the send semaphore
Semaphore_Struct semaphore_struct;
Semaphore_Handle semaphore_handle;
Semaphore_Params semaphore_params;

// we are reading byte wise from the UART so we need a temporary array to keep the bytes in
uint8_t temp_pos = 0;
uint8_t temp_data [MAX_PACKET_LENGTH] = {""};

// if we receive more chars than MAX_PACKET_LENGTH we need to tell the caller
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

// UART initialise
void Alien_UART_init (void) {

	System_printf ("UART initialise starting\n");
	System_flush();

	// initialise the UART
	Board_initUART();

	/* Create a UART with data processing off. */
	UART_Params_init (&UART_params);
	UART_params.writeDataMode = UART_DATA_BINARY;

	UART_params.readDataMode = UART_DATA_BINARY;
	UART_params.readMode = UART_MODE_CALLBACK;
	UART_params.readCallback = &UART_read_callback;
	UART_params.readReturnMode = UART_RETURN_NEWLINE;
	UART_params.readEcho = UART_ECHO_OFF;

	UART_params.baudRate = 115200;
	UART = UART_open (Board_UART0, &UART_params);
	if (UART == NULL)
		System_abort ("Error opening the UART");

	System_printf ("UART port setup complete\n");
	System_flush();

	// create the UART send task
	Task_Params_init (&UART_send_task_params);
	UART_send_task_params.stackSize = UART_TASK_STACK_SIZE;
	UART_send_task_params.stack = &task_UART_send_stack;
	UART_send_task_params.priority = 1;
	Task_construct (&task_UART_send_struct, (Task_FuncPtr) Alien_UART_send_task, &UART_send_task_params, NULL);

	System_printf ("UART send task setup complete\n");
	System_flush();

	// create the UART receive task
	Task_Params_init (&UART_receive_task_params);
	UART_receive_task_params.stackSize = UART_TASK_STACK_SIZE;
	UART_receive_task_params.stack = &task_UART_receive_stack;
	UART_receive_task_params.priority = 1;
	Task_construct (&task_UART_receive_struct, (Task_FuncPtr) Alien_UART_receive_task, &UART_receive_task_params, NULL);

	System_printf ("UART receive task setup complete\n");
	System_flush();

	/* Construct a Semaphore object to be used as a resource lock, initial count 0 */
	Semaphore_Params_init (&semaphore_params);
	Semaphore_construct (&semaphore_struct, 0, &semaphore_params);
	semaphore_handle = Semaphore_handle (&semaphore_struct);

	System_printf ("UART semaphore setup complete\n");
	System_printf ("UART initialise complete\n\n");
	System_flush();

}
// this gets called when you are doing the callback
void UART_read_callback (UART_Handle UART, void * data, size_t length) {

	System_printf ("In UART_read_callback function\n");
	System_flush();

	if (UART == 0) return;
	if (data == 0) return;

	// take what you read and place it in the receive queue
	BOOLEAN rc = queue (RECEIVE_QUEUE, data, length, buffer_overflow);

	// TODO: Call the RF read function
	// Semaphore_post (rftx_semaphore_handle);

	System_printf ("Leaving UART_read_callback function\n\n");
	System_flush();
}

// UART Task
void Alien_UART_send_task (UArg arg0, UArg arg1) {

	uint8_t length;
	uint8_t data [MAX_PACKET_LENGTH];

	// loop forever
	while (TRUE) {

		System_printf ("In Alien_UART_send_task function waiting for semaphore...\n");
		System_flush();

		// wait for something or someone to wake me
		Semaphore_pend (semaphore_handle, BIOS_WAIT_FOREVER);

		System_printf ("The semaphore in Alien_UART_send_task function just woke up\n");
		System_flush();

		// send everything in the send queue
		do {
			dequeue (SEND_QUEUE, data, &length, FALSE);
			if (length > 0) {
				UART_write (UART, data, length);
				data [length] = (uint8_t) '\0';
				System_printf ("Sent %s\n", data);
				System_flush();
			}
		} while (length > 0);

		System_printf ("Finished sending data to queue\n\n");
		System_flush();

	}

}

// UART Task
void Alien_UART_receive_task (UArg arg0, UArg arg1) {

	while (1) {
		System_printf ("Doing an Interrupt read...");
		System_flush();
		UART_read(UART, (void *) temp_data, MAX_PACKET_LENGTH);

		// message
		System_printf ("Received: #%s#\n", temp_data);
		System_flush();
	}

}
