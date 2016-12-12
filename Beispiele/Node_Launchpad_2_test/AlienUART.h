/*
 * AlienUART.h
 *
 *  Created on: 13. Nov. 2016
 *      Author: ursus
 */

#ifndef ALIENUART_H_ 
#define ALIENUART_H_

#include "AlienTypes.h"
#include <stdint.h>

#define END_OF_RECORD (char) 13

void Alien_UART_init (void);
BOOLEAN Alien_UART_send (uint8_t * data, uint8_t length);
BOOLEAN Alien_UART_receive (uint8_t * data, uint8_t * length, BOOLEAN * buffer_overflow);

#endif /* ALIENUART_H_ */
