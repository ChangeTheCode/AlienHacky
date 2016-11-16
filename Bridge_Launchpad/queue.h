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

typedef enum _queue_status {
	SEND_QUEUE = 1,
	RECEIVE_QUEUE = 2
} queue_status_t;

BOOLEAN queue (queue_status_t queue, uint8_t * data, uint8_t length);
BOOLEAN dequeue (queue_status_t queue, uint8_t * data, uint8_t * length);

#endif /* QUEUE_H_ */
