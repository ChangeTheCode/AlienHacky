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

    /* Init the Board */
    Board_initGeneral();

    // Init the UART
    Alien_UART_init ();

    /* Start BIOS */
    BIOS_start();

    return (0);
}
