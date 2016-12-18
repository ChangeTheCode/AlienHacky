/*
 * Bridge.c
 *
 */
#include <RF.h>
#include "AlienUART.h"
#include "Board.h"
#include <ti/sysbios/BIOS.h>
#include <xdc/runtime/System.h>  // This is needed as Board_initGeneral () uses System_abort but didn't include it?


#include "pin.h"

BOOLEAN debug;

int main(void) {

	// are we displaying DEBUG messages
	debug = FALSE;

    // Start message
	Alien_log ("AlienBridge starting. Running version 18.12.2016\n\n");

    // Initialise the Board
    Board_initGeneral();

    // Initialise the LEDs
    LED_init();

    // Initialise the UART
    Alien_UART_init ();

    // Initialise the RF Module
    Alien_RF_init ();

    // Now start BIOS
    BIOS_start();
    return (0);
}
