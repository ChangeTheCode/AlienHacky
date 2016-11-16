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

    // test
    uint8_t length = 5;
    uint8_t length1 = 6;
    uint8_t length2 = 7;
    uint8_t data [MAX_PACKET_LENGTH]  = "aaaaa";
    uint8_t data1 [MAX_PACKET_LENGTH] = "bbbbbb";
    uint8_t data2 [MAX_PACKET_LENGTH] = "ccccccc";

    Alien_UART_send (data,  length);
    Alien_UART_send (data1, length1);
    Alien_UART_send (data2, length2);

    // just add some stuff to the receive queue for testing purposes
    data [MAX_PACKET_LENGTH]  = "12345";
    data1 [MAX_PACKET_LENGTH] = "987654";
    data2 [MAX_PACKET_LENGTH] = "הצüהצüה";
	queue (RECEIVE_QUEUE, data, length);
	queue (RECEIVE_QUEUE, data, length);
	queue (RECEIVE_QUEUE, data, length);

    /* Start BIOS */
    BIOS_start();

    return (0);
}
