/*
 * light_sensor.h
 *
 *  Created on: 05.11.2016
 *      Author: Jim
 */

#ifndef SENSOR_LIB_LIGHT_SENSOR_H_
#define SENSOR_LIB_LIGHT_SENSOR_H_

#include "../../AlienTypes.h"
#include <ti/drivers/I2C.h>

#define INT_LIGHT_PIN 37 // Page 981 in CC2650 Technical Reference
#define INT_HT_LSB 0x06
#define INT_HT_MSB 0x07
#define DATA_LSB 0x02
#define DATA_MSB 0x03


BOOLEAN config_light_sensor(I2C_Handle i2c);

BOOLEAN config_light_sensor_reg2(I2C_Handle i2c);

BOOLEAN config_light_int_threshold(I2C_Handle i2c, int thresehold_top, int thresehold_down);

void read_light_sensor_values(I2C_Handle i2c, int* read_value);


#endif /* SENSOR_LIB_LIGHT_SENSOR_H_ */
