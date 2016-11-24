/*
 * pin.c
 *
 *  Created on: 22. Nov. 2016
 *      Author: Tobias
 */

#include <RF.h>

/* Pin driver handles */
PIN_Handle button_pin_handle;
PIN_Handle LED_pin_handle;

/* Global memory storage for a PIN_Config table */
PIN_State button_pin_state;
PIN_State LED_pin_state;

/*
 * Initial LED pin configuration table
 *   - LEDs Board_LED0 is on.
 *   - LEDs Board_LED1 is off.
 */
PIN_Config LED_pin_table[] = {
    Board_LED0 | PIN_GPIO_OUTPUT_EN | PIN_GPIO_HIGH | PIN_PUSHPULL | PIN_DRVSTR_MAX,
    Board_LED1 | PIN_GPIO_OUTPUT_EN | PIN_GPIO_LOW  | PIN_PUSHPULL | PIN_DRVSTR_MAX,
	Board_DIO15 | PIN_GPIO_OUTPUT_EN | PIN_GPIO_LOW | PIN_PUSHPULL | PIN_DRVSTR_MAX,
    PIN_TERMINATE
};

/*
 * Application button pin configuration table:
 *   - Buttons interrupts are configured to trigger on falling edge.
 */
PIN_Config button_pin_table[] = {
    Board_BUTTON0  | PIN_INPUT_EN | PIN_PULLUP | PIN_IRQ_NEGEDGE,
    Board_BUTTON1  | PIN_INPUT_EN | PIN_PULLUP | PIN_IRQ_NEGEDGE,
    PIN_TERMINATE
};

uint8_t button_pressed = 0;

void button_callback(PIN_Handle handle, PIN_Id pinId);

void LED_init(void)
{
	LED_pin_handle = PIN_open(&LED_pin_state, LED_pin_table);
    if(!LED_pin_handle)
    {
        System_abort("Error initializing board LED pins\n");
    }
}

void button_init(void)
{
    button_pin_handle = PIN_open(&button_pin_state, button_pin_table);
	if(!button_pin_handle) {
		System_abort("Error initializing button pins\n");
	}

	/* Setup callback for button pins */
	if (PIN_registerIntCb(button_pin_handle, &button_callback) != 0) {
		System_abort("Error registering button callback function");
	}
}

void button_callback(PIN_Handle handle, PIN_Id pinId) {
    uint32_t currVal = 0;

    /* Debounce logic, only toggle if the button is still pushed (low) */
    CPUdelay(8000*50);
    if (!PIN_getInputValue(pinId)) {
        /* Toggle LED based on the button pressed */
        switch (pinId) {
            case Board_BUTTON0:
                currVal =  PIN_getOutputValue(Board_LED0);
                PIN_setOutputValue(LED_pin_handle, Board_LED0, !currVal);

                // to measure the roundtrip time of a packet
				//PIN_setOutputValue(ledPinHandle, Board_DIO15, 1);

                button_pressed = 1;
    			Semaphore_post(sem_tx_handle);
                break;

            case Board_BUTTON1:
                currVal =  PIN_getOutputValue(Board_LED1);
                PIN_setOutputValue(LED_pin_handle, Board_LED1, !currVal);
                break;

            default:
                /* Do nothing */
                break;
        }
    }
}
