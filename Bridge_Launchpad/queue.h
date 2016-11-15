/*
 * queue.h
 *
 *  Created on: 13. Nov. 2016
 *      Author: ursus
 */

#ifndef QUEUE_H_
#define QUEUE_H_

#include "AlienTypes.h"
#include <stdint.h>
#include "RF.h"

#define SEND_QUEUE 1
#define RECEIVE_QUEUE 2

BOOLEAN queue (int queue, uint8_t * data, uint8_t length);
BOOLEAN dequeue (int queue, uint8_t * data, uint8_t * length);

#endif /* QUEUE_H_ */
