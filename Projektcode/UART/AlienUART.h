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
#define MAX_LOG_ENTRY 256

void Alien_UART_init (void);
BOOLEAN Alien_UART_send (uint8_t * data, uint8_t length);
BOOLEAN Alien_UART_receive (uint8_t * data, uint8_t * length, BOOLEAN * buffer_overflow);
void Alien_log (char * to_log);

#endif /* ALIENUART_H_ */
