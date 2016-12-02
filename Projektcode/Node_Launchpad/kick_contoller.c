/*
 * kick_contoller.c
 *
 *  Created on: 02.12.2016
 *      Author: Jim
 */
#include "kick_controller.h"

void alien_init_i2c_task(){
	/* Construct tmp007 Task thread */
	Task_Params_init(&sensor_task_params);
	sensor_task_params.stackSize = TASKSTACKSIZE;
	sensor_task_params.stack = &sensor_task_stack;
	Task_construct(&sensor_task, (Task_FuncPtr)sensor_task_fn, &sensor_task_params, NULL);
}


// function to calculate average of the values.
int calculate_avarage (int* p_values, int new_value, int avarage){

	avarage = ((avarage* MAX_AVARAGE_COUNT) - *p_values) + new_value;

	avarage = avarage / MAX_AVARAGE_COUNT;

	*p_values = new_value;

	return avarage;
}

//TODO: aufteilen der Funktion ist so unschön
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


// main function of the task
Void sensor_task_fn(UArg arg0, UArg arg1){

}




