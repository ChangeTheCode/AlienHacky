/*
 *    ======== mpu9150.h ========
 */

#ifndef MPU9150_H_
#define MPU9150_H_

#include <stdint.h>
#include <ti/sysbios/BIOS.h>
#include <ti/sysbios/gates/GateMutex.h>
#include <ti/drivers/I2C.h>

#define MPU9150_COUNT	                    1
#define MPU9150_SENSOR_REGISTER_SET_SIZE    22
#define MPU9150_INT_PIN 6

/*
 * MPU9150 data structure is used by the MPU9150_get* APIs to extract the
 * requested data from the previously captured data read using MPU9150_read()
 */
typedef struct MPU9150_Data {
	union {
		struct {
			int16_t	x;
			int16_t	y;
			int16_t	z;
		};
		struct {
			float	xFloat;
			float	yFloat;
			float	zFloat;
		};
		struct {
			int16_t temperature;
		};
		struct {
			float   temperatureCFloat;
			float   temperatureFFloat;
		};
	};
} MPU9150_Data;

typedef struct MPU9150_Object {
	I2C_Handle       i2c;
	uint8_t	         i2cAddr;
	GateMutex_Handle dataAccess;
	uint8_t          data[MPU9150_SENSOR_REGISTER_SET_SIZE];
} MPU9150_Object, *MPU9150_Handle;

/*
 *  ======== MPU9150_init ========
 *  Function opens the I2C controller and initializes the MPU9150
 *  It returns a non-zero MPU9150 handle if the MPU9150 initialized
 *  successfully.
 *  This is a one time call
 */
MPU9150_Handle MPU9150_init(unsigned int mpu9105_index,
							I2C_Handle  *i2c,
                            uint8_t i2c_addr);

/*
 *  ======== MPU9150_read ========
 *  Function reads all the MPU9150 data registers.
 *  The data registers read are: Accelerometer, Gyroscope, Magnetometer, and
 *  Temperature.
 *  This function should be called when the MPU9150 has sensor data ready;
 *  which is typically notified via a GPIO interrupt.
 */
bool MPU9150_read(MPU9150_Handle handle, I2C_Handle i2c);

/*
 *  ======== MPU9150_getAccelRaw ========
 *  Function returns the raw Acceleration register values for X,Y,and Z axes.
 *  Returns true if successful.
 */
bool MPU9150_getAccelRaw(MPU9150_Handle handle, MPU9150_Data *data);

/*
 *  ======== MPU9150_getAccelFloat ========
 *  Function returns a processed float values of the accelerometer in (m/s^2).
 *  Returns true if successful.
 */
bool MPU9150_getAccelFloat(MPU9150_Handle handle, MPU9150_Data *data);

/*
 *  ======== MPU9150_getGyroRaw ========
 *  Function returns the raw Gyroscope register values for X,Y,and Z axes.
 *  Returns true if successful.
 */
bool MPU9150_getGyroRaw(MPU9150_Handle handle, MPU9150_Data *data);

/*
 *  ======== MPU9150_getGyroFloat ========
 *  Function returns a processed float values of the gyroscope in (rad/s).
 *  Returns true if successful.
 */
bool MPU9150_getGyroFloat(MPU9150_Handle handle, MPU9150_Data *data);

/*
 *  ======== MPU9150_getMagnetoRaw ========
 *  Function returns the raw Magnetometer register values for X,Y,and Z axes.
 *  Returns true if successful.
 */
bool MPU9150_getMagnetoRaw(MPU9150_Handle handle, MPU9150_Data *data);

/*
 *  ======== MPU9150_getMagnetoFloat ========
 *  Function returns a processed float values of the Magnetometer in (uT).
 *  Returns true if successful.
 */
bool MPU9150_getMagnetoFloat(MPU9150_Handle handle, MPU9150_Data *data);

/*
 *  ======== MPU9150_getTemperatureRaw ========
 *  Function returns the raw Temperature register values.
 *  Returns true if successful.
 */
bool MPU9150_getTemperatureRaw(MPU9150_Handle handle, MPU9150_Data *data);

/*
 *  ======== MPU9150_getTemperatureFloat ========
 *  Function returns a processed float values of the Temperature in (C) & (F).
 *  Returns true if successful.
 */
bool MPU9150_getTemperatureFloat(MPU9150_Handle handle, MPU9150_Data *data);

#endif /* MPU9150_H_ */
