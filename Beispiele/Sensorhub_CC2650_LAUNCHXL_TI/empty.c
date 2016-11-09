/*
 * Copyright (c) 2015-2016, Texas Instruments Incorporated
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * *  Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * *  Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 * *  Neither the name of Texas Instruments Incorporated nor the names of
 *    its contributors may be used to endorse or promote products derived
 *    from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

/*
 *  ======== empty.c ========
 */
/* XDCtools Header files */
#include <xdc/std.h>
#include <xdc/runtime/System.h>

/* BIOS Header files */
#include <ti/sysbios/BIOS.h>
#include <ti/sysbios/knl/Clock.h>
#include <ti/sysbios/knl/Task.h>

/* TI-RTOS Header files */
#include <ti/drivers/I2C.h>
#include <ti/drivers/PIN.h>
// #include <ti/drivers/SPI.h>
 #include <ti/drivers/UART.h>
// #include <ti/drivers/Watchdog.h>

/* Board Header files */
#include "Board.h"

#include "sensorlib/mpu9150.h"
#include "sensorlib/hw_mpu9150.h"

#include <stdint.h>


//for the mpu9150
tMPU9150 g_sMPU9150Inst;

#define UART_TASK_STACK_SIZE   768

Task_Struct task0Struct;
Char task0Stack[UART_TASK_STACK_SIZE];

Task_Struct taskUartStruct;
Char taskUartStack[UART_TASK_STACK_SIZE];

/* Pin driver handle */
static PIN_Handle ledPinHandle;
static PIN_State ledPinState;

/*
 * Application LED pin configuration table:
 *   - All LEDs board LEDs are off.
 */
PIN_Config ledPinTable[] = {
    Board_LED0 | PIN_GPIO_OUTPUT_EN | PIN_GPIO_LOW | PIN_PUSHPULL | PIN_DRVSTR_MAX,
    Board_LED1 | PIN_GPIO_OUTPUT_EN | PIN_GPIO_LOW | PIN_PUSHPULL | PIN_DRVSTR_MAX,
    PIN_TERMINATE
};

/*
 *  ======== heartBeatFxn ========
 *  Toggle the Board_LED0. The Task_sleep is determined by arg0 which
 *  is configured for the heartBeat Task instance.
 */
Void heartBeatFxn(UArg arg0, UArg arg1)
{
    while (1) {
        Task_sleep((UInt)arg0);
        PIN_setOutputValue(ledPinHandle, Board_LED0,
                           !PIN_getOutputValue(Board_LED0));
    }
}


/*
 *  ======== echoFxn ========
 *  Task for this function is created statically. See the project's .cfg file.
 */
Void echoFxn(UArg arg0, UArg arg1)
{
    char input;
    UART_Handle uart;
    UART_Params uartParams;
    I2C_Params i2cParam;
	I2C_Handle i2c;
    const char echoPrompt[] = "\fEchoing characters:\r\n";

    /* Create a UART with data processing off. */
    UART_Params_init(&uartParams);
    uartParams.writeDataMode = UART_DATA_BINARY;
    uartParams.readDataMode = UART_DATA_BINARY;
    uartParams.readReturnMode = UART_RETURN_FULL;
    uartParams.readEcho = UART_ECHO_OFF;
    uartParams.baudRate = 9600;
    uart = UART_open(Board_UART0, &uartParams);

    if (uart == NULL) {
        System_abort("Error opening the UART");
    }

    UART_write(uart, echoPrompt, sizeof(echoPrompt));

    /* Open I2C bus driver */
    I2C_Params_init(&i2cParam);
    i2cParam.transferMode = I2C_MODE_BLOCKING;
    i2cParam.transferCallbackFxn = NULL;
    i2c = I2C_open(0, &i2cParam);
    if (i2c == NULL) {
        System_printf("I2C0: Failure opening port\n");
        System_flush();

        BIOS_exit(1);
    }

    System_printf("Successfully initialized I2C0\n");
    System_flush();

    // I2C transaction - Setup MPU9150 system registers at 0x68
	I2C_Transaction txn;
	UChar bmeRegAddr[2];
	UChar readData[32];
	txn.writeBuf = &bmeRegAddr;
	txn.writeCount = 2;
	txn.slaveAddress = 0x68;  // MPU9150

	bmeRegAddr[0] = MPU9150_O_PWR_MGMT_1;
	bmeRegAddr[1] = MPU9150_PWR_MGMT_1_DEVICE_RESET;

	txn.readBuf = readData;
	txn.readCount = 1; // only 1 byte worth of data in this register

	if (!I2C_transfer(i2c, &txn)) {
		System_printf("I2C0 mpu config failed\n");
		System_flush();
	} else {
		System_printf("Received CONFIG: 0x%02X\r\n", readData[0]);
		System_flush();
	}

	// I2C transaction - Read MPU9150 system registers at 0x68
	txn.writeBuf = &bmeRegAddr;
	txn.writeCount = 1;
	txn.slaveAddress = 0x68;  // MPU9150

	bmeRegAddr[0] = MPU9150_O_ACCEL_XOUT_H;

	txn.readBuf = readData;
	txn.readCount = 22; // only 1 byte worth of data in this register

	if (!I2C_transfer(i2c, &txn)) {
		System_printf("I2C0 mpu read failed\n");
		System_flush();
	} else {
		System_printf("Received CONFIG: 0x%02X\r\n", readData[0]);
		System_flush();
	}

    /* Loop forever echoing */
    while (1) {
        UART_read(uart, &input, 1);
        UART_write(uart, &input, 1);
    }
}

/*
 *  ======== main ========
 */
int main(void)
{
    Task_Params taskParams;
    Task_Params taskUartParams;

    /* Call board init functions */
    Board_initGeneral();
    Board_initI2C();
    // Board_initSPI();
    Board_initUART();
    // Board_initWatchdog();

    /* Construct heartBeat Task  thread */
    Task_Params_init(&taskParams);
    taskParams.arg0 = 1000000 / Clock_tickPeriod;
    taskParams.stackSize = UART_TASK_STACK_SIZE;
    taskParams.stack = &task0Stack;
    Task_construct(&task0Struct, (Task_FuncPtr)heartBeatFxn, &taskParams, NULL);

    /* Construct uart Task  thread */
	Task_Params_init(&taskUartParams);
	taskUartParams.stackSize = UART_TASK_STACK_SIZE;
	taskUartParams.stack = &taskUartStack;
	Task_construct(&taskUartStruct, (Task_FuncPtr)echoFxn, &taskUartParams, NULL);

//    //
//    // Initialize the MPU9150 Driver.
//    //
//    MPU9150Init(&g_sMPU9150Inst, &g_sI2CInst, MPU9150_I2C_ADDRESS,
//                MPU9150AppCallback, &g_sMPU9150Inst);


    /* Open LED pins */
    ledPinHandle = PIN_open(&ledPinState, ledPinTable);
    if(!ledPinHandle) {
        System_abort("Error initializing board LED pins\n");
    }

    PIN_setOutputValue(ledPinHandle, Board_LED1, 1);

    System_printf("Starting the example\nSystem provider is set to SysMin. "
                  "Halt the target to view any SysMin contents in ROV.\n");
    /* SysMin will only print to the console when you call flush or exit */
    System_flush();

    /* Start BIOS */
    BIOS_start();

    return (0);
}
