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
static PIN_State led_pin_state;
static PIN_State buttonPinState;

/*
 * Application LED pin configuration table:
 *   - All LEDs board LEDs are off.
 */
PIN_Config led_pin_table[] = {
    Board_LED1 | PIN_GPIO_OUTPUT_EN | PIN_GPIO_LOW | PIN_PUSHPULL | PIN_DRVSTR_MAX,
    Board_LED2 | PIN_GPIO_OUTPUT_EN | PIN_GPIO_LOW | PIN_PUSHPULL | PIN_DRVSTR_MAX,
// Pin einbinden für Toggel
    PIN_TERMINATE
};

PIN_Config buttonPinTable[] = {
    Board_BUTTON0  | PIN_INPUT_EN | PIN_PULLUP | PIN_IRQ_NEGEDGE,
    Board_BUTTON1  | PIN_INPUT_EN | PIN_PULLUP | PIN_IRQ_NEGEDGE,
    PIN_TERMINATE
};

Task_Struct task0Struct;
Char task0_stack[TASKSTACKSIZE];

// variables for i2c communication
static MPU9150_Handle MPU_handel;
static I2C_Handle      i2c;


/*
 *  ======== echoFxn ========
 *  Task for this function is created statically. See the project's .cfg file.
 */
Void taskFxn(UArg arg0, UArg arg1)
{
    unsigned int    i;
    uint8_t         tx_buffer[2];
    uint8_t         rx_buffer[2];

    I2C_Params      I2C_params;
    I2C_Transaction I2C_transaction;

    /* Create I2C for usage */
    I2C_Params_init(&I2C_params);
    I2C_params.bitRate = I2C_400kHz;
    i2c = I2C_open(Board_I2C_TMP, &I2C_params);
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


    //TODO Send routine

    /* Point to the T ambient register and read its 2 bytes */
    // Slave adresse 1 bit shift nach rechts machen !
    I2C_transaction.slaveAddress = 0x44;//Board_TMP007_ADDR, Lightsensor read slave adress ; 136 = schreiben 137 lesen
    I2C_transaction.writeBuf = tx_buffer;
    I2C_transaction.writeCount = 1;
    I2C_transaction.readBuf = rx_buffer;
    I2C_transaction.readCount = 4;


    int flag = 1;
    /* Take 20 samples and print them out onto the console */
    while(1) {

        tx_buffer[0] = 0x02; // test register
        //txBuffer[1] = 0xaa; // test register
        if (I2C_transfer(i2c, &I2C_transaction)) {

        	// pin toggle

            System_printf("Sample %u: %d , %d (RAW)\n", i, rx_buffer[0], rx_buffer[1]);
            if( rx_buffer[1] >= 2 &&  flag  ){
            	flag = 0;
            	if (!MPU9150_read(MPU_handel)) {
            		System_abort("Could not extract data registers from the MPU9150");
            	}else{
            		System_abort("Could extract data registers from the MPU9150");
            	}

            }
        }
        else {
            System_printf("I2C Bus fault \n" );
            System_printf("I2C Werte Slave Adresse %d ",I2C_transaction.slaveAddress );
            System_printf("I2C Werte TX %d, %d \n", tx_buffer[0], tx_buffer[1] );
            System_printf("I2C Werte RX %d, %d \n", rx_buffer[0], rx_buffer[1] );
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

/* we should read the gyro manuel, because we don't know when the light value is big enough and the gyro interrupt is
 * received.
 */
void buttonCallbackFxn(){

	if (!MPU9150_read(MPU_handel)) {
		System_abort("Could not extract data registers from the MPU9150");
	}else{
		System_abort("Could extract data registers from the MPU9150");
	}
}

/*
 *  ======== main ========
 */
int main(void)
{
    PIN_Handle led_pin_handle;
    PIN_Handle button_pin_handle;

    Task_Params task_params;

    /* Call board init functions */
    Board_initGeneral();
    Board_initI2C();

    /* Construct tmp007 Task thread */
    Task_Params_init(&task_params);
    task_params.stackSize = TASKSTACKSIZE;
    task_params.stack = &task0_stack;
    Task_construct(&task0Struct, (Task_FuncPtr)taskFxn, &task_params, NULL);


    // init i2c of the gyro sensor
    MPU_handel = MPU9150_init(0, &i2c, MPU9150_I2C_ADDRESS);

    // config pin of the Interrupt of the gyro
    button_pin_handle = PIN_open(&buttonPinState, buttonPinTable);
    if(!button_pin_handle) {
    	System_abort("Error initializing button pins\n");
    }
    /* Setup callback for button pins */
    if (PIN_registerIntCb(button_pin_handle, &buttonCallbackFxn) != 0) {
    	System_abort("Error registering button callback function");
    }


    /* Open LED pins */
    led_pin_handle = PIN_open(&led_pin_state, led_pin_table);
    if(!led_pin_handle) {
        System_abort("Error initializing board LED pins\n");
    }

    PIN_setOutputValue(led_pin_handle, Board_LED0, 1);
    PIN_setOutputValue(led_pin_handle, Board_LED1, 1);


    System_printf("Starting the I2C example\nSystem provider is set to SysMin."
                  " Halt the target to view any SysMin contents in ROV.\n");
    /* SysMin will only print to the console when you call flush or exit */
    System_flush();

    /* Start BIOS */
    BIOS_start();

    return (0);
}
