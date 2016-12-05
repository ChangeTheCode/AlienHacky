/*
 * queue.c
 *
 *  Created on: 13. Nov. 2016
 *      Author: ursus
 */

#include <stdio.h>
#include <stdlib.h>
#include <stdint.h>
#include "queue.h"

struct node_t {

	uint8_t length;
	uint8_t * data;
	struct node_t * next;
	BOOLEAN buffer_overflow;

} node_t ;

// Two queues to store addresses of front and rear nodes.
struct node_t * send_front = NULL;
struct node_t * send_rear = NULL;

struct node_t * receive_front = NULL;
struct node_t * receive_rear = NULL;

// add to the end of the queue
BOOLEAN queue (queue_status_t queue, uint8_t * data, uint8_t length, BOOLEAN buffer_overflow) {

	// which queue are you using
	struct node_t ** front;
	struct node_t ** rear;
	if (queue == SEND_QUEUE) {
		front = &send_front;
		rear = &send_rear;
	} else {
		front = &receive_front;
		rear = &receive_rear;
	}

	// create the struct
	struct node_t * temp = (struct node_t *) malloc (sizeof(struct node_t));

	// copy the data
	temp->data = malloc (length);
	memcpy ((void *) temp->data, (void *) data, length);
	temp->length = length;
	temp->next = NULL;
	temp->buffer_overflow = buffer_overflow;

	// is the queue empty?
	if ((*front == NULL) && (*rear == NULL)) {
		*front = *rear = temp;
		return TRUE;
	}

	// else add to the end of the queue
	(*rear)->next = temp;
	*rear = temp;
	return TRUE;
}

// get the first value in the queue
BOOLEAN dequeue (queue_status_t queue, uint8_t * data, uint8_t * length, BOOLEAN * buffer_overflow) {

	// which queue are you using
	struct node_t ** front;
	struct node_t ** rear;
	if (queue == SEND_QUEUE) {
		front = &send_front;
		rear = &send_rear;
	} else {
		front = &receive_front;
		rear = &receive_rear;
	}

	// get the top of the queue
	struct node_t * temp = *front;

	// if the front is empty then we have nothing
	if (*front == NULL) {
		*length = 0;
		return FALSE;
	};

	// move the front pointer
	if(*front == *rear) {
		*front = *rear = NULL;
	} else {
		*front = (*front)->next;
	}

	// copy the data
	memcpy (data, temp->data, temp->length);
	*length = temp->length;
	*buffer_overflow = temp->buffer_overflow;

	// free up the data
	free (temp->data);
	free (temp);

	// fin
	return TRUE;
}
