/*
 * light_sensor.h
 *
 *  Created on: 05.11.2016
 *      Author: Jim
 */

#ifndef SENSOR_LIB_LIGHT_SENSOR_H_
#define SENSOR_LIB_LIGHT_SENSOR_H_

#include "../AlienHacky_datatypes.h"

BOOL config_light_sensor(I2C_Handle i2c);

BOOL config_light_sensor_reg2(I2C_Handle i2c);

I2C_Transaction single_read_light_sensor_value(I2C_Handle i2c, uint8_t reg_address);

I2C_Transaction single_write_light_sensor_value(I2C_Handle i2c, uint8_t reg_address, uint8_t write_value );


#endif /* SENSOR_LIB_LIGHT_SENSOR_H_ */
