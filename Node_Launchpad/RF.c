/*
 * RF.c
 *
 *  Created on: 18.11.2016
 *      Author: Tobias
 */

#include <RF_node.h>

RF_Object RF_object;
RF_Handle RF_handle;

Semaphore_Struct sem_tx_struct;
Semaphore_Handle sem_tx_handle;

Semaphore_Struct sem_rx_struct;
Semaphore_Handle sem_rx_handle;

void Alien_RF_init(void)
{
	Semaphore_Params sem_params;

    /* Construct a Semaphore object to be used as a resource lock, inital count 0 */
    Semaphore_Params_init(&sem_params);
    Semaphore_construct(&sem_tx_struct, 0, &sem_params);
    Semaphore_construct(&sem_rx_struct, 0, &sem_params);


    /* Obtain instance handle */
    sem_tx_handle = Semaphore_handle(&sem_tx_struct);
    sem_rx_handle = Semaphore_handle(&sem_rx_struct);

    /* Initialize tasks */
	rx_task_init();
	tx_task_init();
}
