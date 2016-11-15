/*
 * queue.c
 *
 *  Created on: 13. Nov. 2016
 *      Author: ursus
 */

#include <stdio.h>
#include <stdlib.h>
#include <stdint.h>
#include "RF.h"

struct node_t {

	uint8_t length;
	uint8_t data [MAX_PACKET_LENGTH];
	struct node_t * next;

} node_t ;

// Two global variables to store address of front and rear nodes.
struct node_t * front = NULL;
struct node_t * rear = NULL;

// add to the end of the queue
BOOLEAN enqueue (uint8_t * data, uint8_t length) {

	// add to the top of the queue
	struct node_t * temp = (struct node_t *) malloc (sizeof(struct node_t));
	memcpy ((void *) temp->data, (void *) data, length);
	temp->length = length;
	temp->next = NULL;

	// is the queue empty?
	if (front == NULL && rear == NULL){
		front = rear = temp;
		return TRUE;
	}

	// else add to the end of the queue
	rear->next = temp;
	rear = temp;
	return TRUE;
}

// get the first value in the queue
BOOLEAN dequeue (uint8_t * data, uint8_t * length) {

	// get the top of the queue
	struct node_t * temp = front;

	// if the front is empty then we have nothing
	if (front == NULL) {
		*length = 0;
		return FALSE;
	};

	// move the front pointer
	if(front == rear) {
		front = rear = NULL;
	} else {
		front = front->next;
	}

	// copy the data
	memcpy (data, temp->data, temp->length);
	*length = temp->length;

	// free up the data
	free (temp);

	// fin
	return TRUE;
}
