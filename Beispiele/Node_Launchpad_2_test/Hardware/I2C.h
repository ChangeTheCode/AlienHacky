/*
 * I2C.h
 *
 *  Created on: 02.12.2016
 *      Author: Jim
 */

#ifndef HARDWARE_I2C_H_
#define HARDWARE_I2C_H_


#include <xdc/runtime/System.h>


#include <ti/sysbios/BIOS.h>
#include <ti/sysbios/knl/Task.h>
#include <ti/sysbios/knl/Semaphore.h>

// include alien types
#include "../AlienTypes.h"

#include <math.h>

void Alien_i2c_init(void);
extern alien_init_i2c_task(void);


#endif /* HARDWARE_I2C_H_ */
