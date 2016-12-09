/*
 * button.c
 *
 *  Created on: 08. December 2016
 *      Author: Ursus Schneider
 */
/*
 * Application button pin configuration table:
 *   - Buttons interrupts are configured to trigger on falling edge.
 */

#include <ti/drivers/PIN.h>
#include <ti/drivers/pin/PINCC26XX.h>
#include <stdio.h>
#include "AlienUART.h"
#include "Board.h"
#include "RF.h"
#include "queue.h"
#include "button.h"

char curr_char = 'A';


void button_callback (PIN_Handle handle, PIN_Id pin_id);

PIN_Config button_pin_table [] = {
		Board_BUTTON0  | PIN_INPUT_EN | PIN_PULLUP | PIN_IRQ_NEGEDGE,
		Board_BUTTON1  | PIN_INPUT_EN | PIN_PULLUP | PIN_IRQ_NEGEDGE,
		PIN_TERMINATE

};

PIN_State button_pin_state;
PIN_Handle button_pin_handle;
int code_to_test;

void button_init (int to_test) {

	code_to_test = to_test;

	button_pin_handle = PIN_open(&button_pin_state, button_pin_table);
	if(!button_pin_handle) {
		Alien_log ("Error initialising button pins\n");
	}

	/* Setup callback for button pins */
	if (PIN_registerIntCb (button_pin_handle, &button_callback) != 0) {
		Alien_log ("Error registering button callback function");
	}
}
/*
 * button callback function -> add to the queue when button 0 is pressed, call
 *    Alien_UART_receive to clean out the queue
 *
 */
void button_callback (PIN_Handle handle, PIN_Id pin_id) {

	uint8_t length = 7;
	uint8_t data [MAX_PACKET_LENGTH];
	BOOLEAN overflow = FALSE;
	char temp_string [MAX_LOG_ENTRY];
	int i;

	/* Debounce logic, only toggle if the button is still pushed (low) */
	CPUdelay(8000*50);

	// check if button still pressed
	if (PIN_getInputValue(pin_id)) return;

	// Receive test
	if (code_to_test == RECEIVE_TEST) {
		if (pin_id == Board_BUTTON0) {
			for (i = 0; i < length; i++) {
				data [i] = curr_char++;
				if (curr_char > 'z')
					curr_char = 'A';
			}
			data [7] = '\0';
			queue (RECEIVE_QUEUE, data, length, overflow);
			sprintf (temp_string, "Added #%s# to the receive queue\n", data);
			Alien_log (temp_string);
		} if (pin_id == Board_BUTTON1) {
			do {
				Alien_UART_receive (data, &length, &overflow);
			} while (length != 0);
		}
	}

	// send test
	if (code_to_test == SEND_TEST) {
		if (pin_id == Board_BUTTON0) {
			for (i = 0; i < length; i++) {
				data [i] = curr_char++;
				if (curr_char > 'z')
					curr_char = 'A';
			}
			data [7] = '\0';
			queue (SEND_QUEUE, data, length, overflow);
			sprintf (temp_string, "Added #%s# to the receive queue\n", data);
			Alien_log (temp_string);
		} if (pin_id == Board_BUTTON1) {
			// Wake the UART send process
			Alien_start_send_task ();
		}
	}

}
