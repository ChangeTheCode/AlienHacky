/*
 * timer.c
 *
 *  Created on: 24. Nov. 2016
 *      Author: Tobias
 */

#include "timer.h"


/***** Variable declarations *****/
static Task_Params timer_task_params;
Task_Struct timer_task;    /* not static so you can see in ROV */
static uint8_t timer_task_stack[TIMER_TASK_STACK_SIZE];

GPTimerCC26XX_Handle timer_handle;

BOOLEAN heartbeat = FALSE;

void timer_task_init(void);
static void timer_task_function(UArg arg0, UArg arg1);

void timer_init(void)
{
	timer_task_init();
}

void timer_callback(GPTimerCC26XX_Handle handle, GPTimerCC26XX_IntMask interruptMask) {
    // interrupt callback code goes here. Minimize processing in interrupt.
	PIN_setOutputValue(LED_pin_handle, Board_LED2, !PIN_getOutputValue(Board_LED2));	// Red LED
	heartbeat = TRUE;
	Semaphore_post(sem_tx_handle);
}

void timer_task_init(void)
{
    Task_Params_init(&timer_task_params);
    timer_task_params.stackSize = TIMER_TASK_STACK_SIZE;
    timer_task_params.priority = TIMER_TASK_PRIORITY;
    timer_task_params.stack = &timer_task_stack;
    timer_task_params.arg0 = (UInt)1000000;

    Task_construct(&timer_task, timer_task_function, &timer_task_params, NULL);
}

static void timer_task_function(UArg arg0, UArg arg1)
{
	GPTimerCC26XX_Params params;
	GPTimerCC26XX_Params_init(&params);
	params.width          = GPT_CONFIG_32BIT;
	params.mode           = GPT_MODE_PERIODIC_UP;
	params.debugStallMode = GPTimerCC26XX_DEBUG_STALL_OFF;
	timer_handle = GPTimerCC26XX_open(Board_GPTIMER0A, &params);
	if(timer_handle == NULL) {
		System_abort("Failed to open GPTimer");
	}

	Types_FreqHz  freq;
	BIOS_getCpuFreq(&freq);
	GPTimerCC26XX_Value loadVal = freq.lo * 30 - 1; // 30 seconds (1.439.999.999)
	GPTimerCC26XX_setLoadValue(timer_handle, loadVal);
	GPTimerCC26XX_registerInterrupt(timer_handle, timer_callback, GPT_INT_TIMEOUT);

	GPTimerCC26XX_start(timer_handle);

	while(1) {
	  Task_sleep(BIOS_WAIT_FOREVER);
	}
}
