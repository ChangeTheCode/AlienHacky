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
BOOLEAN Alien_UART_send ();
BOOLEAN Alien_UART_receive ();

#endif /* ALIENUART_H_ */
