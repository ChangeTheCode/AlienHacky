/* XDCtools Header files */
#include <xdc/runtime/System.h>

/* BIOS Header files */
#include <ti/sysbios/BIOS.h>
#include <ti/sysbios/knl/Task.h>

/* TI-RTOS Header files */
#include <ti/drivers/UART.h>

/* Example/Board Header files */
#include "Board.h"

#define TASKSTACKSIZE     768

Task_Struct task0Struct;
Char task0Stack[TASKSTACKSIZE];

/*
 *  ======== echoFxn ========
 *  Task for this function is created statically. See the project's .cfg file.
 *
 */
Void UART_run (UArg arg0, UArg arg1) {

    char input;
    UART_Handle uart;
    UART_Params uartParams;
    const char echoPrompt[] = "\fEchoing characters:\r\n";

    /* Create a UART with data processing off. */
    UART_Params_init(&uartParams);
    uartParams.writeDataMode = UART_DATA_BINARY;
    uartParams.readDataMode = UART_DATA_BINARY;
    uartParams.readReturnMode = UART_RETURN_FULL;
    uartParams.readEcho = UART_ECHO_OFF;
    uartParams.baudRate = 115200;
    uart = UART_open (Board_UART0, &uartParams);

    if (uart == NULL) {
        System_abort("Error opening the UART");
    }

    UART_write(uart, echoPrompt, sizeof(echoPrompt));

    /* Loop forever echoing */
    while (1) {
        UART_read(uart, &input, 1);
        if (input == (char) 13)
        	UART_write(uart, echoPrompt, sizeof(echoPrompt));
        else
        	UART_write(uart, &input, 1);
    }
}
/*
 *  ======== main ========
 */
int main(void) {
    /* Call board init functions */
    Board_initGeneral();
    Board_initUART();

    /* Construct BIOS objects */
    Task_Params taskParams;
    Task_Params_init(&taskParams);
    taskParams.stackSize = TASKSTACKSIZE;
    taskParams.stack = &task0Stack;
    Task_construct(&task0Struct, (Task_FuncPtr)UART_run, &taskParams, NULL);

    // Start message
    System_printf ("UART Test AlienRFServer Version 1.00\n");
    System_flush();

    /* Start BIOS */
    BIOS_start();

    // fin
    return (0);
}
