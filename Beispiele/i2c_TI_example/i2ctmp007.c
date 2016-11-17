/*
 * Copyright (c) 2016, Texas Instruments Incorporated
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
 *    ======== i2ctmp007.c ========
 */

/* XDCtools Header files */
#include <xdc/std.h>
#include <xdc/runtime/System.h>

/* BIOS Header files */
#include <ti/sysbios/BIOS.h>
#include <ti/sysbios/knl/Clock.h>
#include <ti/sysbios/knl/Task.h>

/* TI-RTOS Header files */
#include <ti/drivers/PIN.h>
#include <ti/drivers/I2C.h>

/* Example/Board Header files */
#include "Board.h"

// include alien types
#include "AlienHacky_datatypes.h"
#include "Sensor_lib/light_sensor.h"
#include "Sensor_lib/task_mpu9150.h"
#include "Standard_lib/mpu9150.h"

//extern void gyro_worker(I2C_Handle *i2c);

#define TASKSTACKSIZE       1024
#define TMP007_OBJ_TEMP     0x0003  /* Object Temp Result Register */

/* Global memory storage for a PIN_Config table */
static PIN_State ledPinState;

/*
 * Application LED pin configuration table:
 *   - All LEDs board LEDs are off.
 */
PIN_Config ledPinTable[] = {
    Board_LED1 | PIN_GPIO_OUTPUT_EN | PIN_GPIO_LOW | PIN_PUSHPULL | PIN_DRVSTR_MAX,
    Board_LED2 | PIN_GPIO_OUTPUT_EN | PIN_GPIO_LOW | PIN_PUSHPULL | PIN_DRVSTR_MAX,
// Pin einbinden für Toggel
    PIN_TERMINATE
};

Task_Struct task0Struct;
Char task0Stack[TASKSTACKSIZE];


/*
 *  ======== echoFxn ========
 *  Task for this function is created statically. See the project's .cfg file.
 */
Void taskFxn(UArg arg0, UArg arg1)
{
    unsigned int    i;
    uint8_t         txBuffer[2];
    uint8_t         rxBuffer[2];
    I2C_Handle      i2c;
    I2C_Params      i2cParams;
    I2C_Transaction i2cTransaction;

    /* Create I2C for usage */
    I2C_Params_init(&i2cParams);
    i2cParams.bitRate = I2C_400kHz;
    i2c = I2C_open(Board_I2C_TMP, &i2cParams);
    if (i2c == NULL) {
        System_abort("Error Initializing I2C\n");
    }
    else {
        System_printf("I2C Initialized!\n");
    }

    if( ! config_light_sensor(i2c) ){
    	return;   // config of the light sensor failed Break
    }

    if( ! config_light_sensor_reg2(i2c) ){
		return;   // config of the light sensor failed Break
	}



    MPU9150_init(0, &i2c, MPU9150_I2C_ADDRESS);



    //TODO Send routine

    /* Point to the T ambient register and read its 2 bytes */
    // Slave adresse 1 bit shift nach rechts machen !
    i2cTransaction.slaveAddress = 0x44;//Board_TMP007_ADDR, Lightsensor read slave adress ; 136 = schreiben 137 lesen
    i2cTransaction.writeBuf = txBuffer;
    i2cTransaction.writeCount = 1;
    i2cTransaction.readBuf = rxBuffer;
    i2cTransaction.readCount = 4;

    /* Take 20 samples and print them out onto the console */
    while(1) {

        txBuffer[0] = 0x02; // test register
        //txBuffer[1] = 0xaa; // test register
        if (I2C_transfer(i2c, &i2cTransaction)) {

        	// pin toggle

            System_printf("Sample %u: %d , %d (RAW)\n", i, rxBuffer[0], rxBuffer[1]);
        }
        else {
            System_printf("I2C Bus fault \n" );
            System_printf("I2C Werte Slave Adresse %d ",i2cTransaction.slaveAddress );
            System_printf("I2C Werte TX %d, %d \n", txBuffer[0], txBuffer[1] );
            System_printf("I2C Werte RX %d, %d \n", rxBuffer[0], rxBuffer[1] );
            Task_sleep(20000);
        }

        System_flush();
        //Task_sleep(1000000 / Clock_tickPeriod);
    }


    /* Deinitialized I2C */
    I2C_close(i2c);
    System_printf("I2C closed!\n");

    System_flush();
}

/*
 *  ======== main ========
 */
int main(void)
{
    PIN_Handle ledPinHandle;

    Task_Params taskParams;

    /* Call board init functions */
    Board_initGeneral();
    Board_initI2C();

    /* Construct tmp007 Task thread */
    Task_Params_init(&taskParams);
    taskParams.stackSize = TASKSTACKSIZE;
    taskParams.stack = &task0Stack;
    Task_construct(&task0Struct, (Task_FuncPtr)taskFxn, &taskParams, NULL);


    /* Open LED pins */
    ledPinHandle = PIN_open(&ledPinState, ledPinTable);
    if(!ledPinHandle) {
        System_abort("Error initializing board LED pins\n");
    }

   /* green_Pin_handle  = PIN_open(&ledPinState, ledPinTable );
    if(!green_Pin_handle) {
		System_abort("Error initializing board green LED pins\n");
	}*/

    PIN_setOutputValue(ledPinHandle, Board_LED0, 1);
    PIN_setOutputValue(ledPinHandle, Board_LED1, 1);


    System_printf("Starting the I2C example\nSystem provider is set to SysMin."
                  " Halt the target to view any SysMin contents in ROV.\n");
    /* SysMin will only print to the console when you call flush or exit */
    System_flush();

    /* Start BIOS */
    BIOS_start();

    return (0);
}
