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


#endif /* SENSOR_LIB_LIGHT_SENSOR_H_ */
