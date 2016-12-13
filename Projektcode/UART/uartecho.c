/*
 * Bridge.c
 *
 */
#include "AlienUART.h"
#include "RF.h"

#include "Board.h"
#include <ti/sysbios/BIOS.h>
#include <xdc/runtime/System.h>  // This is needed as Board_initGeneral () uses System_abort but didn't include it?

#include "button.h"

BOOLEAN debug;

int main(void) {

	// are we displaying DEBUG messages
	debug = TRUE;

    // Start message
	Alien_log ("AlienBridge starting. Running version 09.12.2016\n\n");

    // Initialise the Board
    Board_initGeneral();
    button_init();

    // Initialise the UART
    Alien_UART_init ();

    // Initialise the RF
    Alien_RF_init ();

    // Now start BIOS
    BIOS_start();
    return (0);
}
