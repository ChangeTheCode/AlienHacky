/*
 * I2C.c
 *
 *  Created on: 02.12.2016
 *      Author: Jim
 */

#include "I2C.h"

Semaphore_Handle sem_i2c_handle;
Semaphore_Struct sem_i2c_struct;


void Alien_i2c_init(void)
{
	Semaphore_Params sem_params;

    /* Construct a Semaphore object to be used as a resource lock, inital count 0 */
    Semaphore_Params_init(&sem_params);
    Semaphore_construct(&sem_i2c_struct, 0, &sem_params);


    /* Obtain instance handle */
    sem_i2c_handle = Semaphore_handle(&sem_i2c_struct);

    /* Initialize tasks */
	rx_task_init();
}

