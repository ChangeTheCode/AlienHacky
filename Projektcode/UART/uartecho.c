/*
 * Bridge.c
 *
 */
#include "AlienUART.h"
#include "queue.h"

#include "Board.h"
#include <ti/sysbios/BIOS.h>
#include <xdc/runtime/System.h>  // This is needed as Board_initGeneral () uses System_abort but didn't include it?

int main(void) {

    // Start message
    System_printf ("AlienBridge starting. Running version 28.11.2016\n\n");
    System_flush();

    // Initialise the Board
    Board_initGeneral();

    // Initialise the UART
    Alien_UART_init ();

    // Now start BIOS
    BIOS_start();

    return (0);
}
