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

GPTimerCC26XX_Handle timer_heartbeat_handle;
GPTimerCC26XX_Handle timer_login_handle;
GPTimerCC26XX_Handle timer_kick_handle;

BOOLEAN heartbeat = FALSE;

void timer_task_init(void);
static void timer_task_function(UArg arg0, UArg arg1);

void timer_init(void)
{
	timer_task_init();
}

void timer_login_callback(GPTimerCC26XX_Handle handle, GPTimerCC26XX_IntMask interruptMask) {
	//PIN_setOutputValue(LED_pin_handle, Board_LED2, !PIN_getOutputValue(Board_LED2));	// Red LED
	heartbeat = TRUE;
	Semaphore_post(sem_tx_handle);
}

void timer_login_callback(GPTimerCC26XX_Handle handle, GPTimerCC26XX_IntMask interruptMask) {
	login_sent = FALSE;
	Semaphore_post(sem_tx_handle);
}

void timer_kick_callback(GPTimerCC26XX_Handle handle, GPTimerCC26XX_IntMask interruptMask) {
	PIN_setOutputValue(LED_pin_handle, Board_LED2, 0);	// Red LED off
}

void heartbeat_timer_init(void)
{
	GPTimerCC26XX_Params heartbeat_params;
	GPTimerCC26XX_Params_init(&heartbeat_params);
	heartbeat_params.width          = GPT_CONFIG_32BIT;
	heartbeat_params.mode           = GPT_MODE_PERIODIC_UP;
	heartbeat_params.debugStallMode = GPTimerCC26XX_DEBUG_STALL_OFF;
	timer_heartbeat_handle = GPTimerCC26XX_open(Board_GPTIMER0A, &heartbeat_params);
	if(timer_heartbeat_handle == NULL) {
		System_abort("Failed to open GPTimer");
	}

	Types_FreqHz  freq;
	BIOS_getCpuFreq(&freq);
	GPTimerCC26XX_Value heartbeat_loadVal = freq.lo * 30 - 1; // 30 seconds (1.439.999.999)
	GPTimerCC26XX_setLoadValue(timer_heartbeat_handle, heartbeat_loadVal);
	GPTimerCC26XX_registerInterrupt(timer_heartbeat_handle, timer_login_callback, GPT_INT_TIMEOUT);

	GPTimerCC26XX_start(timer_heartbeat_handle);
}

void login_timer_init(void)
{
	GPTimerCC26XX_Params login_params;
	GPTimerCC26XX_Params_init(&login_params);
	login_params.width          = GPT_CONFIG_32BIT;
	login_params.mode           = GPT_MODE_ONESHOT_UP;
	login_params.debugStallMode = GPTimerCC26XX_DEBUG_STALL_OFF;
	timer_login_handle = GPTimerCC26XX_open(Board_GPTIMER0B, &login_params);
	if(timer_login_handle == NULL) {
		System_abort("Failed to open GPTimer");
	}

	Types_FreqHz  freq;
	BIOS_getCpuFreq(&freq);
	GPTimerCC26XX_Value login_loadVal = freq.lo * 30 - 1; // 30 seconds (1.439.999.999)
	GPTimerCC26XX_setLoadValue(timer_login_handle, login_loadVal);
	GPTimerCC26XX_registerInterrupt(timer_login_handle, timer_login_callback, GPT_INT_TIMEOUT);
}

void kick_timer_init(void)
{
	GPTimerCC26XX_Params kick_params;
	GPTimerCC26XX_Params_init(&kick_params);
	kick_params.width          = GPT_CONFIG_32BIT;
	kick_params.mode           = GPT_MODE_ONESHOT_UP;
	kick_params.debugStallMode = GPTimerCC26XX_DEBUG_STALL_OFF;
	timer_kick_handle = GPTimerCC26XX_open(Board_GPTIMER1A, &kick_params);
	if(timer_kick_handle == NULL) {
		System_abort("Failed to open GPTimer");
	}

	Types_FreqHz  freq;
	BIOS_getCpuFreq(&freq);
	GPTimerCC26XX_Value kick_loadVal = freq.lo * 30 - 1; // 30 seconds (1.439.999.999)
	GPTimerCC26XX_setLoadValue(timer_kick_handle, kick_loadVal);
	GPTimerCC26XX_registerInterrupt(timer_kick_handle, timer_kick_callback, GPT_INT_TIMEOUT);
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
	heartbeat_timer_init();
	login_timer_init();
	kick_timer_init();

	while(1) {
	  Task_sleep(BIOS_WAIT_FOREVER);
	}
}
