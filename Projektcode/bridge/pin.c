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
    PIN_TERMINATE
};

void LED_init(void)
{
	LED_pin_handle = PIN_open(&LED_pin_state, LED_pin_table);
    if(!LED_pin_handle)
    {
        System_abort("Error initializing board LED pins\n");
    }
}
