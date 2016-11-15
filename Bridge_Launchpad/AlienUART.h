/*
 * AlienUART.h
 *
 *  Created on: 13. Nov. 2016
 *      Author: ursus
 */

#ifndef ALIENUART_H_ 
#define ALIENUART_H_

#include "AlienTypes.h"

void Alien_UART_init (void);
BOOLEAN Alien_UART_queue (uint8_t * data, uint8_t length);
BOOLEAN Alien_UART_dequeue (uint8_t * data, uint8_t length);

#endif /* ALIENUART_H_ */
