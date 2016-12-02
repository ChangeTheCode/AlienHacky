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

#include <ti/sysbios/BIOS.h>
#include <ti/sysbios/knl/Task.h>


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
#include "Standard_lib/comp_dcm.h"
#include <math.h>

//extern void gyro_worker(I2C_Handle *i2c);

#define TASKSTACKSIZE       1024
#define TMP007_OBJ_TEMP     0x0003  /* Object Temp Result Register */
#define MAX_AVARAGE_COUNT 10
#define LIGHT_LEVEL_IN_PROCENT 20 // 20 % bigger then old value, so read gyro !

/* Global memory storage for a PIN_Config table */
static PIN_State led_pin_state;
static PIN_State buttonPinState;

PIN_Handle led_pin_handle;


/*
 * Application LED pin configuration table:
 *   - All LEDs board LEDs are off.
 */
PIN_Config led_pin_table[] = {
    Board_LED1 | PIN_GPIO_OUTPUT_EN | PIN_GPIO_LOW | PIN_PUSHPULL | PIN_DRVSTR_MAX,
    //Board_LED2 | PIN_GPIO_OUTPUT_EN | PIN_GPIO_LOW | PIN_PUSHPULL | PIN_DRVSTR_MAX,
	Board_DIO14 | PIN_GPIO_OUTPUT_EN | PIN_GPIO_LOW | PIN_PUSHPULL | PIN_DRVSTR_MAX,
    PIN_TERMINATE
};

PIN_Config buttonPinTable[] = {
    Board_BUTTON0  | PIN_INPUT_EN | PIN_PULLUP | PIN_IRQ_NEGEDGE,
	Board_LIGHT_int  | PIN_INPUT_EN | PIN_PULLUP | PIN_IRQ_NEGEDGE,
    PIN_TERMINATE
};

Task_Struct task0Struct;
Char task0_stack[TASKSTACKSIZE];

// variables for i2c communication
static MPU9150_Handle MPU_handel;
static I2C_Handle      i2c;
static int light_values[MAX_AVARAGE_COUNT];
static int light_pos = 0, light_avarage = 0;

//*****************************************************************************
//
// Global Instance structure to manage the DCM state.
//
//*****************************************************************************
	tCompDCM g_sCompDCMInst;
	int_fast32_t i32IPart[20], i32FPart[20];
    uint_fast32_t ui32Idx, ui32CompDCMStarted;
    float pfData[20];
    float *pfAccel, *pfGyro, *pfMag, *pfEulers, *pfQuaternion;

    float eYaw, ePitch, eRoll;
    float radVal = 3.14159265f/180.0f;

    int c, d, k;
    float sum = 0.0f;
    float transMatrix[3][3], accelMatrix[3][1], newAccel[3][1];

    float *pfAccel2;

	#define PRINT_SKIP_COUNT        10
    uint32_t g_ui32PrintSkipCounter;

typedef struct{
	float _accel_x;
	float _accel_y;
	float _accel_z;

	float _magneto_x;
	float _magneto_y;
	float _magneto_z;

	float _gyro_x;
	float _gyro_y;
	float _gyro_z;

	float _euler_x;
	float _euler_y;
	float _euler_z;


}gyro_value_t;

// function to calculate average of the values.
int calculate_avarage (int* p_values, int new_value, int avarage){

	avarage = ((avarage* MAX_AVARAGE_COUNT) - *p_values) + new_value;

	avarage = avarage / MAX_AVARAGE_COUNT;

	*p_values = new_value;

	return avarage;
}


void calc_in_world_coordinates( gyro_value_t new_ComDCM){
	//
	// Check if this is our first data ever.
	//
	if(ui32CompDCMStarted == 0)
	{
		//
		// Set flag indicating that DCM is started.
		// Perform the seeding of the DCM with the first data set.
		//
		ui32CompDCMStarted = 1;
		CompDCMMagnetoUpdate(&g_sCompDCMInst, new_ComDCM._magneto_x, new_ComDCM._magneto_y,
				new_ComDCM._magneto_z);
		CompDCMAccelUpdate(&g_sCompDCMInst, new_ComDCM._accel_x, new_ComDCM._accel_y,
				new_ComDCM._gyro_z);
		CompDCMGyroUpdate(&g_sCompDCMInst, new_ComDCM._gyro_x, new_ComDCM._gyro_y,
				new_ComDCM._gyro_z);
		CompDCMStart(&g_sCompDCMInst);
	}
	else
	{
		//
		// DCM Is already started.  Perform the incremental update.
		//
		CompDCMMagnetoUpdate(&g_sCompDCMInst, new_ComDCM._magneto_x, new_ComDCM._magneto_y,
				new_ComDCM._magneto_z);
		CompDCMAccelUpdate(&g_sCompDCMInst, new_ComDCM._accel_x, new_ComDCM._accel_y,
				new_ComDCM._accel_z);
		CompDCMGyroUpdate(&g_sCompDCMInst, -new_ComDCM._gyro_x, -new_ComDCM._gyro_y,
				-new_ComDCM._gyro_z);
		CompDCMUpdate(&g_sCompDCMInst);
	}

	//
	// Increment the skip counter.  Skip counter is used so we do not
	// overflow the UART with data.
	//
	g_ui32PrintSkipCounter++;
	if(g_ui32PrintSkipCounter >= PRINT_SKIP_COUNT)
	{
		//
		// Reset skip counter.
		//
		g_ui32PrintSkipCounter = 0;

		//
		// Get Euler data. (Roll Pitch Yaw)
		//
		CompDCMComputeEulers(&g_sCompDCMInst, &new_ComDCM._euler_x, &new_ComDCM._euler_y,
				&new_ComDCM._euler_z);


		//
		// convert mag data to micro-tesla for better human interpretation.
		//
		new_ComDCM._magneto_x *= 1e6;
		new_ComDCM._magneto_y *= 1e6;
		new_ComDCM._magneto_z *= 1e6;

		//
		// Convert Eulers to degrees. 180/PI = 57.29...
		// Convert Yaw to 0 to 360 to approximate compass headings.
		//
		new_ComDCM._euler_x *= 57.295779513082320876798154814105f;
		new_ComDCM._euler_y *= 57.295779513082320876798154814105f;
		new_ComDCM._euler_z *= 57.295779513082320876798154814105f;
		if(new_ComDCM._euler_y < 0)
		{
			new_ComDCM._euler_y += 360.0f;
		}

		//
		// Transform the Acceleration of the board coordinates to the world coordinate
		//
		accelMatrix[0][0] = new_ComDCM._accel_x;
		accelMatrix[1][0] = new_ComDCM._accel_y;
		accelMatrix[2][0] = new_ComDCM._accel_z;

		// Psi 		= 	Yaw
		// Theta 	= 	Pitch
		// Phi 		= 	Roll

		eRoll = new_ComDCM._euler_x * radVal;
		ePitch = new_ComDCM._euler_y * radVal;
		eYaw = new_ComDCM._euler_z * radVal;

		transMatrix[0][0] = cosf(ePitch)*cosf(eYaw);
		transMatrix[0][1] = sinf(eRoll)*sinf(ePitch)*cosf(eYaw) - cosf(eRoll)*sinf(eYaw);
		transMatrix[0][2] = cosf(eRoll)*sinf(ePitch)*cosf(eYaw) + sinf(eRoll)*sinf(eYaw);

		transMatrix[1][0] = cosf(ePitch)*sinf(eYaw);
		transMatrix[1][1] = sinf(eRoll)*sinf(ePitch)*sinf(eYaw) + cosf(eRoll)*cosf(eYaw);
		transMatrix[1][2] = cosf(eRoll)*sinf(ePitch)*sinf(eYaw) - sinf(eRoll)*cosf(eYaw);

		transMatrix[2][0] = sinf(ePitch)*(-1);
		transMatrix[2][1] = sinf(eRoll)*cosf(ePitch);
		transMatrix[2][2] = cosf(eRoll)*cosf(ePitch);


		for (c = 0; c < 3; c++)
		{
			for (d = 0; d < 1; d++)
			{
				for (k = 0; k < 3; k++)
				{
					sum = sum + transMatrix[c][k]*accelMatrix[k][d];
				}

				newAccel[c][d] = sum;
				sum = 0;
			}
		}

		// send values, of the current kick
		pfAccel2[0] = newAccel[0][0];
		pfAccel2[1] = newAccel[1][0];
		pfAccel2[2] = newAccel[2][0];


		System_printf("\n Berechnete Wert: x: %d, y: %d, z: %d \n" ,pfAccel2[0], pfAccel2[1], pfAccel2[2] );
		System_flush();

		//
		// Now drop back to using the data as a single array for the
		// purpose of decomposing the float into a integer part and a
		// fraction (decimal) part.
		//
		for(ui32Idx = 0; ui32Idx < 19; ui32Idx++)
		{
			//
			// Conver float value to a integer truncating the decimal part.
			//
			i32IPart[ui32Idx] = (int32_t) pfData[ui32Idx];

			//
			// Multiply by 1000 to preserve first three decimal values.
			// Truncates at the 3rd decimal place.
			//
			i32FPart[ui32Idx] = (int32_t) (pfData[ui32Idx] * 1000.0f);

			//
			// Subtract off the integer part from this newly formed decimal
			// part.
			//
			i32FPart[ui32Idx] = i32FPart[ui32Idx] -
					(i32IPart[ui32Idx] * 1000);

			//
			// make the decimal part a positive number for display.
			//
			if(i32FPart[ui32Idx] < 0)
			{
				i32FPart[ui32Idx] *= -1;
			}
		}
	}
}




/*
 *  ======== echoFxn ========
 *  Task for this function is created statically. See the project's .cfg file.
 */
Void taskFxn(UArg arg0, UArg arg1)
{

	//
	// Initialize convenience pointers that clean up and clarify the code
	// meaning. We want all the data in a single contiguous array so that
	// we can make our pretty printing easier later.
	//
	pfAccel = pfData;
	pfGyro = pfData + 3;
	pfMag = pfData + 6;
	pfEulers = pfData + 9;
	pfQuaternion = pfData + 12;

	pfAccel2 = pfData + 16;


    I2C_Params      I2C_params;
    //I2C_Transaction I2C_transaction;

    /* Create I2C for usage */
    I2C_Params_init(&I2C_params);
    I2C_params.bitRate = I2C_400kHz;
    i2c = I2C_open(Board_I2C_TMP, &I2C_params);
    if (i2c == NULL) {
        System_abort("Error Initializing I2C\n");
        System_flush();
    }
    else {
        System_printf("I2C Initialized!\n");
    }
    // init i2c of the gyro sensor
    MPU_handel = MPU9150_init(0, i2c, MPU9150_I2C_ADDRESS);

    if( ! config_light_sensor(i2c) ){
    	return;   // config of the light sensor failed Break
    }
    Task_sleep(100);
    if( ! config_light_sensor_reg2(i2c) ){
		return;   // config of the light sensor failed Break
	}
    Task_sleep(100);
    /*480 is approximently  HI : 1 LOW: 195 */
    if( ! config_light_int_threshold(i2c, 280 , 0) ){
		return;   // config of the light sensor failed Break
    }
    Task_sleep(100);


    /* Take 20 samples and print them out onto the console */
    int light_transaction_values[4];
    int current_16b_light = 0;
    int old_light_avarage = 0;


    MPU9150_Data mpu_data;
    gyro_value_t new_com_values;
    CompDCMInit(&new_com_values, 1.0f / 50.0f, 0.2f, 0.6f, 0.2f);

    while(1) {
    	read_light_sensor_values(i2c, &light_transaction_values[0]); // needs 250 µs


    	System_printf("\n Main Light value : %d (RAW)", light_transaction_values[0] );

    	current_16b_light = light_transaction_values[0] << 8 | light_transaction_values[1] ;
    	old_light_avarage = light_avarage; // save old value of light to see how big are the difference

    	if (light_pos >= MAX_AVARAGE_COUNT){
    		light_pos = 0;
    	}
    	light_avarage = calculate_avarage(  &light_values[light_pos] ,current_16b_light, light_avarage);

    	// if the difference between old an new bigger then 20 % so send the gyro values
    	//if( (light_avarage * 100) / old_light_avarage >= LIGHT_LEVEL_IN_PROCENT ){ // to do a test, comment this if block out
    		Task_sleep(100);

    		if (! MPU9150_read(MPU_handel, i2c)){ // needs 750 µs
    			System_printf("\n Read failed ");
    			System_flush();
    		}

    		// Get floating point version of the Accel Data in m/s^2.
    		MPU9150_getAccelFloat(MPU_handel, &mpu_data);
    		new_com_values._accel_x = mpu_data.xFloat;
    		new_com_values._accel_y = mpu_data.yFloat;
    		new_com_values._accel_z =mpu_data.zFloat;

    		// get floating point version of angular velocities in rad/sec
    		MPU9150_getGyroFloat(MPU_handel, &mpu_data);
    		new_com_values._gyro_x = mpu_data.xFloat;
    		new_com_values._gyro_y = mpu_data.yFloat;
    		new_com_values._gyro_z =mpu_data.zFloat;

    		//Get floating point version of magnetic fields strength in tesla
    		MPU9150_getMagnetoFloat(MPU_handel, &mpu_data);
    		new_com_values._magneto_x = mpu_data.xFloat;
    		new_com_values._magneto_y = mpu_data.yFloat;
    		new_com_values._magneto_z =mpu_data.zFloat;

    		calc_in_world_coordinates(new_com_values);

    		//TODO: Calculate all necessary value like MagnetoGetFloat,Accel, gyrogetfloat and so on. Talk to Tobi and to it together
    		//TODO: how to transform the values to the world coordinates

    		//TOdo: if(ui32CompDCMStarted == 0) line 566 in tiva
    	//}

    }


    /* Deinitialized I2C */
    /*I2C_close(i2c);
    System_printf("I2C closed!\n");

    System_flush();*/
}

void buttonCallbackFxn(){
	MPU9150_read(MPU_handel, i2c);
}


/*
 *  ======== main ========
 */
int main(void)
{

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


    // config pin of the Interrupt of the gyro
    button_pin_handle = PIN_open(&buttonPinState, buttonPinTable);
    if(!button_pin_handle) {
    	System_abort("Error initializing button pins\n");
    }
    /* Setup callback for button pins */
    /*if (PIN_registerIntCb(button_pin_handle, &buttonCallbackFxn) != 0) {
    	System_abort("Error registering button callback function");
    }*/


    /* Open LED pins */
    led_pin_handle = PIN_open(&led_pin_state, led_pin_table);
    if(!led_pin_handle) {
        System_abort("Error initializing board LED pins\n");
    }

    PIN_setOutputValue(led_pin_handle, Board_LED0, 1);
    //PIN_setOutputValue(led_pin_handle, Board_LED1, 1);
    PIN_setOutputValue(led_pin_handle, Board_DIO14, 0);


    System_printf("Starting the I2C example\nSystem provider is set to SysMin."
                  " Halt the target to view any SysMin contents in ROV.\n");
    /* SysMin will only print to the console when you call flush or exit */
    System_flush();

    /* Start BIOS */
    BIOS_start();

    return (0);
}
