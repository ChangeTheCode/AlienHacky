/*
 * kick_contoller.c
 *
 *  Created on: 02.12.2016
 *      Author: Jim
 */
#include "kick_controller.h"
#include "tx_node_interface.h"
#include "timer.h"

kick_vectors_t transport_kick_struct;

void alien_init_i2c_task(void){
	/* Construct tmp007 Task thread */
	Task_Params_init(&sensor_task_params);
	sensor_task_params.stackSize = TASKSTACKSIZE;
	sensor_task_params.stack = &sensor_task_stack;
	sensor_task_params.priority = 1;
	Task_construct(&sensor_task, (Task_FuncPtr)sensor_task_fn, &sensor_task_params, NULL);
}


// function to calculate average of the values.
int calculate_average (int* p_values, int new_value, int avarage){

	avarage = ((avarage * MAX_AVARAGE_COUNT) - *p_values) + new_value;

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

	//
	// Now drop back to using the data as a single array for the
	// purpose of decomposing the float into a integer part and a
	// fraction (decimal) part.
	//
	for(ui32Idx = 0; ui32Idx < 3; ui32Idx++)
	{
		//
		// Conver float value to a integer truncating the decimal part.
		//
		i32IPart[ui32Idx] = (int32_t) newAccel[ui32Idx][0];

		//
		// Multiply by 1000 to preserve first three decimal values.
		// Truncates at the 3rd decimal place.
		//
		i32FPart[ui32Idx] = (int32_t) (newAccel[ui32Idx][0] * 1000.0f);

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

	// i32Ipart is the integer part of the value und i32FPart float with 3 numbers after the point
	//system prints only for tests
	System_printf("\n Gyro %.3f, %.3f, %.3f\n", newAccel[0][0], newAccel[1][0], newAccel[2][0]);
	System_flush();

	transport_kick_struct._kick_int_high_x = (i32IPart[0] & 0x000000ff);
	transport_kick_struct._kick_float_high_x  = (i32FPart[0] & 0x0000ff00) >> 8;
	transport_kick_struct._kick_float_low_x =(i32FPart[0] & 0x000000ff);

	transport_kick_struct._kick_int_high_y = (i32IPart[1] & 0x000000ff);
	transport_kick_struct._kick_float_high_y  = (i32FPart[1] & 0x0000ff00) >> 8;
	transport_kick_struct._kick_float_low_y =(i32FPart[1] & 0x000000ff);

	transport_kick_struct._kick_int_high_z = (i32IPart[2] & 0x000000ff);
	transport_kick_struct._kick_float_high_z  = (i32FPart[2] & 0x0000ff00) >> 8;
	transport_kick_struct._kick_float_low_z =(i32FPart[2] & 0x000000ff);

	//set_new_kick_event_value(transport_kick_struct);
}

// byte array is an 3 item big array
void get_byte_value(int_fast32_t value, uint8_t* byte_array){
	*byte_array = (value & 0x00ff0000) >> 16;  byte_array++;
	*byte_array = (value & 0x0000ff00) >> 8;   byte_array++;
	*byte_array =(value & 0x000000ff);
}

// main function of the task
void sensor_task_fn(UArg arg0, UArg arg1){
	I2C_Params      I2C_params;
	/* Create I2C for usage */
	I2C_Params_init(&I2C_params);
	I2C_params.bitRate = I2C_400kHz;
	i2c = I2C_open(Board_I2C_TMP, &I2C_params);
	if (i2c == NULL) {
		Alien_log("Error Initializing I2C\n");
	}
	else {
		Alien_log("I2C Initialized!\n");
	}

	// init i2c of the gyro sensor
	MPU_handel = MPU9150_init(0, i2c, MPU9150_I2C_ADDRESS);
	if(MPU_handel == NULL){
		GPTimerCC26XX_start(timer_error_handle);
		Alien_log("MPU 9150 Init failed \n");
		while(1)		// blink red led forever
		{
		}
	}
	else {
		Alien_log("Gyro Initialized!\n");
	}

	if( ! config_light_sensor(i2c) ){
		GPTimerCC26XX_start(timer_error_handle);
		Alien_log("Error Initializing  light 1\n");
		while(1)		// blink red led forever
		{
		}
	}
	else {
		Alien_log("Light sensor Initialized!\n");
	}

	if( ! config_light_sensor_reg2(i2c) ){
		GPTimerCC26XX_start(timer_error_handle);
		Alien_log("Error Initializing light 2\n");
		while(1)		// blink red led forever
		{
		}
	}
	else {
		Alien_log("Light sensor configured!\n");
	}


	int light_transaction_values[4];
	int current_16b_light = 0;
	int old_light_average = 0;

	CompDCMInit(&g_sCompDCMInst, 1.0f / 50.0f, 0.2f, 0.6f, 0.2f);

	ui32CompDCMStarted = 0;

	// average light array init
	int i = 0;
	int start_sum_light_values = 0;
	for(i = 0; i < MAX_AVARAGE_COUNT; i++){
		if(read_light_sensor_values(i2c, &light_transaction_values[0])){
			light_values[i] = light_transaction_values[0];
			start_sum_light_values += light_transaction_values[0];
		}
	}
	light_average = start_sum_light_values / MAX_AVARAGE_COUNT;

	if(light_average == 0){
		GPTimerCC26XX_start(timer_error_handle);
		Alien_log("Error Initializing light average\n");
		while(1)		// blink red led forever
		{
		}
	}
	else {
		Alien_log("Light average array Initialized!\n");
	}


	while(1) {

		if(read_light_sensor_values(i2c, &light_transaction_values[0])){ // needs 250 µs

			// filtering light values and check if the delta is big enough
			current_16b_light = light_transaction_values[0];
			old_light_average = light_average; // save old value of light to see how big are the difference

			if (light_pos >= MAX_AVARAGE_COUNT){
				light_pos = 0;
			}
			light_average = calculate_average(  &light_values[light_pos] ,current_16b_light, light_average);

			light_pos ++;

			if( (light_average * 100) / old_light_average >= LIGHT_LEVEL_IN_PROCENT ){
				gyro_to_do();
			}

		}else{
			Alien_log("Read light sensor failed \n");
		}
	}
}

void gyro_to_do(){
	MPU9150_Data mpu_data;
	gyro_value_t new_com_values;

	if (! MPU9150_read(MPU_handel, i2c)){ // needs 750 µs
		Alien_log("Gyro read failed \n");
		return;
	}

	// Get floating point version of the Accel Data in m/s^2.
	MPU9150_getAccelFloat(MPU_handel, &mpu_data);
	new_com_values._accel_x = mpu_data.xFloat;
	new_com_values._accel_y = mpu_data.yFloat;
	new_com_values._accel_z = mpu_data.zFloat;

	// get floating point version of angular velocities in rad/sec
	MPU9150_getGyroFloat(MPU_handel, &mpu_data);
	new_com_values._gyro_x = mpu_data.xFloat;
	new_com_values._gyro_y = mpu_data.yFloat;
	new_com_values._gyro_z = mpu_data.zFloat;

	//Get floating point version of magnetic fields strength in tesla
	MPU9150_getMagnetoFloat(MPU_handel, &mpu_data);
	new_com_values._magneto_x = mpu_data.xFloat;
	new_com_values._magneto_y = mpu_data.yFloat;
	new_com_values._magneto_z = mpu_data.zFloat;

	calc_in_world_coordinates(new_com_values);

}


