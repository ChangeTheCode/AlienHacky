/*
 * kick_controller.h
 *
 *  Created on: 02.12.2016
 *      Author: Jim
 */

#ifndef KICK_CONTROLLER_H_
#define KICK_CONTROLLER_H_

#include <xdc/runtime/System.h>

#include <ti/sysbios/BIOS.h>
#include <ti/sysbios/knl/Task.h>

/* TI-RTOS Header files */
#include <ti/drivers/PIN.h>
#include <ti/drivers/I2C.h>

#include "Board.h"
#include "AlienTypes.h"

#include "Hardware/Standard_lib/mpu9150.h"
#include "Hardware/Standard_lib/comp_dcm.h"
#include <math.h>


#define LIGHT_LEVEL_IN_PROCENT 20 // 20 % bigger then old value, so read gyro !

#define PRINT_SKIP_COUNT        10

#define MAX_AVARAGE_COUNT 10 // filter size for the light array

#define TASKSTACKSIZE       1024

int MPU9150_I2C_ADDRESS = 0x68; // the AD0 is logic level Zero(GND), thats fix on the Sensor board

Task_Struct sensor_task;
static Task_Params sensor_task_params;
Char sensor_task_stack[TASKSTACKSIZE];



// variables for i2c communication
static MPU9150_Handle MPU_handel;
static I2C_Handle      i2c;
static int light_values[MAX_AVARAGE_COUNT];
static int light_pos = 0, light_avarage = 0;

//*****************************************************************************
// Global Instance structure to manage the DCM state.
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


uint32_t g_ui32PrintSkipCounter;


// this struct is use, to convert the sensor values in the world coordination system
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



/* Function Headers */
void alien_init_i2c_task(void);

int calculate_avarage (int* p_values, int new_value, int avarage);

void calc_in_world_coordinates( gyro_value_t new_ComDCM);

Void sensor_task_fn(UArg arg0, UArg arg1);

void gyro_to_do(MPU9150_Data mpu_data);


#endif /* KICK_CONTROLLER_H_ */
