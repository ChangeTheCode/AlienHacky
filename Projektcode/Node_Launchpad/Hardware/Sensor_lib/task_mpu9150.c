/*
 * mpu9150.c
 *
 *  Created on: 09.11.2016
 *      Author: Jim
 */

#include "../../AlienTypes.h"
#include "../Standard_lib/mpu9150.h"
#include "../Standard_lib/hw_mpu9150.h"
#include "../Standard_lib/comp_dcm.h"

#include <xdc/std.h>
#include <xdc/runtime/System.h>
#include <math.h>


//*****************************************************************************
// Define MPU9150 I2C Address.
//*****************************************************************************
#define MPU9150_I2C_ADDRESS     0x68

//*****************************************************************************
// Global instance structure for the ISL29023 sensor driver.
//*****************************************************************************
//tMPU9150 g_sMPU9150Inst;

//*****************************************************************************
// Global Instance structure to manage the DCM state.
//*****************************************************************************
tCompDCM g_sCompDCMInst;

//*****************************************************************************
// Global counter to control and slow down the rate of data to the terminal.
//*****************************************************************************
#define PRINT_SKIP_COUNT  10

uint32_t g_ui32PrintSkipCounter;




void gyro_worker(I2C_Handle *i2c){
//	int_fast32_t i32IPart[20], i32FPart[20];
//	uint_fast32_t ui32Idx, ui32CompDCMStarted;
//	float pfData[20];
//	float *pfAccel, *pfGyro, *pfMag, *pfEulers, *pfQuaternion;
//
//	float eYaw, ePitch, eRoll;
//	float radVal = 3.14159265f/180.0f;
//
//	int c, d, k;
//	float sum = 0.0f;
//	float transMatrix[3][3], accelMatrix[3][1], newAccel[3][1];
//
//	float *pfAccel2;
//
//	int counter=0;
//
//	//
//	// Initialize convenience pointers that clean up and clarify the code
//	// meaning. We want all the data in a single contiguous array so that
//	// we can make our pretty printing easier later.
//	//
//	pfAccel = pfData;
//	pfGyro = pfData + 3;
//	pfMag = pfData + 6;
//	pfEulers = pfData + 9;
//	pfQuaternion = pfData + 12;
//
//	pfAccel2 = pfData + 16;
//
//
//	//
//	// Initialize the MPU9150 Driver.
//	//
//	MPU9150Init(&g_sMPU9150Inst, i2c, MPU9150_I2C_ADDRESS, &g_sMPU9150Inst);
//
//	//
//	// Write application specifice sensor configuration such as filter settings
//	// and sensor range settings.
//	//
//	g_sMPU9150Inst.pui8Data[0] = MPU9150_CONFIG_DLPF_CFG_94_98;
//	g_sMPU9150Inst.pui8Data[1] = MPU9150_GYRO_CONFIG_FS_SEL_250;
//	g_sMPU9150Inst.pui8Data[2] = (MPU9150_ACCEL_CONFIG_ACCEL_HPF_5HZ |
//			MPU9150_ACCEL_CONFIG_AFS_SEL_2G);
//	MPU9150Write(&g_sMPU9150Inst, MPU9150_O_CONFIG, g_sMPU9150Inst.pui8Data, 3);
//
//
//	//
//	// Configure the data ready interrupt pin output of the MPU9150.
//	//
//	g_sMPU9150Inst.pui8Data[0] = MPU9150_INT_PIN_CFG_INT_LEVEL |
//			MPU9150_INT_PIN_CFG_INT_RD_CLEAR |
//			MPU9150_INT_PIN_CFG_LATCH_INT_EN;
//	g_sMPU9150Inst.pui8Data[1] = MPU9150_INT_ENABLE_DATA_RDY_EN;
//	MPU9150Write(&g_sMPU9150Inst, MPU9150_O_INT_PIN_CFG,
//			g_sMPU9150Inst.pui8Data, 2);
//
//
//	while(1){  // TODO: Ask Tobi how it works and whats about the Interrupt.
//
//		if(counter >=1000){
//			MPU9150DataRead(&g_sMPU9150Inst);
//			counter = 0;
//		}
//		counter++;
//
//		//
//		// Get floating point version of the Accel Data in m/s^2.
//		//
//		MPU9150DataAccelGetFloat(&g_sMPU9150Inst, pfAccel, pfAccel + 1, pfAccel + 2);
//
//		//
//		// Get floating point version of angular velocities in rad/sec
//		//
//		MPU9150DataGyroGetFloat(&g_sMPU9150Inst, pfGyro, pfGyro + 1, pfGyro + 2);
//
//		//
//		// Get floating point version of magnetic fields strength in tesla
//		//
//		MPU9150DataMagnetoGetFloat(&g_sMPU9150Inst, pfMag, pfMag + 1,pfMag + 2);
//
//		//
//		// Check if this is our first data ever.
//		//
//		if(ui32CompDCMStarted == 0)
//		{
//			//
//			// Set flag indicating that DCM is started.
//			// Perform the seeding osf the DCM with the first data set.
//			//
//			ui32CompDCMStarted = 1;
//			CompDCMMagnetoUpdate(&g_sCompDCMInst, pfMag[0], pfMag[1],
//					pfMag[2]);
//			CompDCMAccelUpdate(&g_sCompDCMInst, pfAccel[0], pfAccel[1],
//					pfAccel[2]);
//			CompDCMGyroUpdate(&g_sCompDCMInst, pfGyro[0], pfGyro[1],
//					pfGyro[2]);
//			CompDCMStart(&g_sCompDCMInst);
//		}
//		else
//		{
//			//
//			// DCM Is already started.  Perform the incremental update.
//			//
//			CompDCMMagnetoUpdate(&g_sCompDCMInst, pfMag[0], pfMag[1],
//					pfMag[2]);
//			CompDCMAccelUpdate(&g_sCompDCMInst, pfAccel[0], pfAccel[1],
//					pfAccel[2]);
//			CompDCMGyroUpdate(&g_sCompDCMInst, -pfGyro[0], -pfGyro[1],
//					-pfGyro[2]);
//			CompDCMUpdate(&g_sCompDCMInst);
//		}
//
//		//
//		// Increment the skip counter.  Skip counter is used so we do not
//		// overflow the UART with data.
//		//
//		g_ui32PrintSkipCounter++;
//		if(g_ui32PrintSkipCounter >= PRINT_SKIP_COUNT)
//		{
//			//
//			// Reset skip counter.
//			//
//			g_ui32PrintSkipCounter = 0;
//
//			//
//			// Get Euler data. (Roll Pitch Yaw)
//			//
//			CompDCMComputeEulers(&g_sCompDCMInst, pfEulers, pfEulers + 1,
//					pfEulers + 2);
//
//			//
//			// Get Quaternions.
//			//
//			CompDCMComputeQuaternion(&g_sCompDCMInst, pfQuaternion);
//
//			//
//			// convert mag data to micro-tesla for better human interpretation.
//			//
//			pfMag[0] *= 1e6;
//			pfMag[1] *= 1e6;
//			pfMag[2] *= 1e6;
//
//			//
//			// Convert Eulers to degrees. 180/PI = 57.29...
//			// Convert Yaw to 0 to 360 to approximate compass headings.
//			//
//			pfEulers[0] *= 57.295779513082320876798154814105f;
//			pfEulers[1] *= 57.295779513082320876798154814105f;
//			pfEulers[2] *= 57.295779513082320876798154814105f;
//			if(pfEulers[2] < 0)
//			{
//				pfEulers[2] += 360.0f;
//			}
//
//			//
//			// Transform the Acceleration of the board coordinates to the world coordinate
//			//
//			accelMatrix[0][0] = pfAccel[0];
//			accelMatrix[1][0] = pfAccel[1];
//			accelMatrix[2][0] = pfAccel[2];
//
//			// Psi 		= 	Yaw
//			// Theta 	= 	Pitch
//			// Phi 		= 	Roll
//
//			eRoll = *pfEulers * radVal;
//			ePitch = *(pfEulers + 1) * radVal;
//			eYaw = *(pfEulers + 2) * radVal;
//
//			transMatrix[0][0] = cosf(ePitch)*cosf(eYaw);
//			transMatrix[0][1] = sinf(eRoll)*sinf(ePitch)*cosf(eYaw) - cosf(eRoll)*sinf(eYaw);
//			transMatrix[0][2] = cosf(eRoll)*sinf(ePitch)*cosf(eYaw) + sinf(eRoll)*sinf(eYaw);
//
//			transMatrix[1][0] = cosf(ePitch)*sinf(eYaw);
//			transMatrix[1][1] = sinf(eRoll)*sinf(ePitch)*sinf(eYaw) + cosf(eRoll)*cosf(eYaw);
//			transMatrix[1][2] = cosf(eRoll)*sinf(ePitch)*sinf(eYaw) - sinf(eRoll)*cosf(eYaw);
//
//			transMatrix[2][0] = sinf(ePitch)*(-1);
//			transMatrix[2][1] = sinf(eRoll)*cosf(ePitch);
//			transMatrix[2][2] = cosf(eRoll)*cosf(ePitch);
//
//
//			for (c = 0; c < 3; c++)
//			{
//				for (d = 0; d < 1; d++)
//				{
//					for (k = 0; k < 3; k++)
//					{
//						sum = sum + transMatrix[c][k]*accelMatrix[k][d];
//					}
//
//					newAccel[c][d] = sum;
//					sum = 0;
//				}
//			}
//
//			pfAccel2[0] = newAccel[0][0];
//			pfAccel2[1] = newAccel[1][0];
//			pfAccel2[2] = newAccel[2][0];
//
//
//
//			//
//			// Now drop back to using the data as a single array for the
//			// purpose of decomposing the float into a integer part and a
//			// fraction (decimal) part.
//			//
//			for(ui32Idx = 0; ui32Idx < 19; ui32Idx++)
//			{
//				//
//				// Conver float value to a integer truncating the decimal part.
//				//
//				i32IPart[ui32Idx] = (int32_t) pfData[ui32Idx];
//
//				//
//				// Multiply by 1000 to preserve first three decimal values.
//				// Truncates at the 3rd decimal place.
//				//
//				i32FPart[ui32Idx] = (int32_t) (pfData[ui32Idx] * 1000.0f);
//
//				//
//				// Subtract off the integer part from this newly formed decimal
//				// part.
//				//
//				i32FPart[ui32Idx] = i32FPart[ui32Idx] -
//						(i32IPart[ui32Idx] * 1000);
//
//				//
//				// make the decimal part a positive number for display.
//				//
//				if(i32FPart[ui32Idx] < 0)
//				{
//					i32FPart[ui32Idx] *= -1;
//				}
//			}
//
//
//			//
//			// Print the acceleration numbers in the table.
//			//
//			System_printf("\033[5;17H%3d.%03d", i32IPart[0], i32FPart[0]);
//			System_printf("\033[5;40H%3d.%03d", i32IPart[1], i32FPart[1]);
//			System_printf("\033[5;63H%3d.%03d", i32IPart[2], i32FPart[2]);
//
//			System_printf("\033[6;17H%3d.%03d", i32IPart[16], i32FPart[16]);
//			System_printf("\033[6;40H%3d.%03d", i32IPart[17], i32FPart[17]);
//			System_printf("\033[6;63H%3d.%03d", i32IPart[18], i32FPart[18]);
//
//			//
//			// Print the angular velocities in the table.
//			//
//			System_printf("\033[7;17H%3d.%03d", i32IPart[3], i32FPart[3]);
//			System_printf("\033[7;40H%3d.%03d", i32IPart[4], i32FPart[4]);
//			System_printf("\033[7;63H%3d.%03d", i32IPart[5], i32FPart[5]);
//
//			//
//			// Print the magnetic data in the table.
//			//
//			System_printf("\033[9;17H%3d.%03d", i32IPart[6], i32FPart[6]);
//			System_printf("\033[9;40H%3d.%03d", i32IPart[7], i32FPart[7]);
//			System_printf("\033[9;63H%3d.%03d", i32IPart[8], i32FPart[8]);
//
//			//
//			// Print the Eulers in a table.
//			//
//			System_printf("\033[14;17H%3d.%03d", i32IPart[9], i32FPart[9]);
//			System_printf("\033[14;40H%3d.%03d", i32IPart[10], i32FPart[10]);
//			System_printf("\033[14;63H%3d.%03d", i32IPart[11], i32FPart[11]);
//
//			//
//			// Print the quaternions in a table format.
//			//
//			System_printf("\033[19;14H%3d.%03d", i32IPart[12], i32FPart[12]);
//			System_printf("\033[19;32H%3d.%03d", i32IPart[13], i32FPart[13]);
//			System_printf("\033[19;50H%3d.%03d", i32IPart[14], i32FPart[14]);
//			System_printf("\033[19;68H%3d.%03d", i32IPart[15], i32FPart[15]);
//
//
//		}
//	}
}
