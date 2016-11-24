/*
 * timer.h
 *
 *  Created on: 24. Nov. 2016
 *      Author: Tobias
 */

#ifndef TIMER_H_
#define TIMER_H_

#include "RF.h"

#include <ti/drivers/timer/GPTimerCC26XX.h>
#include <xdc/runtime/Types.h>

#define TIMER_TASK_STACK_SIZE 1024
#define TIMER_TASK_PRIORITY   1

extern PIN_Handle LED_pin_handle;
extern BOOLEAN heartbeat;

void timer_init(void);


#endif /* TIMER_H_ */
