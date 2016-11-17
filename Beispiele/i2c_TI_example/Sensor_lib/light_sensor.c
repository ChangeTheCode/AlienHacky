/*
 * light_sensor.c
 *
 *  Created on: 05.11.2016
 *      Author: Jim
 */

#include <ti/drivers/I2C.h>
#include <xdc/runtime/System.h>
#include <xdc/std.h>

#include "light_sensor.h"

// Configure the light sensor to get number of Integration cycles 4 and IC measures ALS continuously
BOOL config_light_sensor(I2C_Handle i2c){
	I2C_Transaction I2C_transaction;
	uint8_t tx_buffer[2];

    // Set config of the light sensor
    I2C_transaction.slaveAddress = 0x44;// Lightsensor read slave adress ; 136 = schreiben 137 lesen
	I2C_transaction.writeBuf = tx_buffer;
	I2C_transaction.writeCount = 2;
	I2C_transaction.readBuf = NULL;//rxBuffer;
	I2C_transaction.readCount = 0;//4;
	tx_buffer[0] = 0x00; // Command register 1
	tx_buffer[1] = 0xA1; //  NUMBER OF INTEGRATION CYCLES is 4 and  The IC measures ALS continuously is set



	I2C_transaction.slaveAddress = 0x0c;
	tx_buffer[0] = 0x6b;// MPU9150_O_PWR_MGMT_1;
	tx_buffer[1] = 0x80;//MPU9150_PWR_MGMT_1_DEVICE_RESET;

	I2C_transaction.writeBuf = tx_buffer;
	I2C_transaction.writeCount = 2;
	I2C_transaction.readBuf = NULL;
	I2C_transaction.readCount = 0;



	// init command register 1
	if (I2C_transfer(i2c, &I2C_transaction)) {
		System_printf("Light sensor is configured! \n" );
		return TRUE;
	}else{
		System_printf("Light sensor configured is failed! \n" );
		return FALSE;
	}
}

BOOL config_light_sensor_reg2(I2C_Handle i2c){
	I2C_Transaction I2C_transaction;
	uint8_t tx_buffer[2];

    // Set config of the light sensor
    I2C_transaction.slaveAddress = 0x44;// Lightsensor read slave adress ; 136 = schreiben 137 lesen
	I2C_transaction.writeBuf = tx_buffer;
	I2C_transaction.writeCount = 2;
	I2C_transaction.readBuf = NULL;//rxBuffer;
	I2C_transaction.readCount = 0;//4;
	tx_buffer[0] = 0x01; // Command register 2
	tx_buffer[1] = 0x00; //  FULL SCALE RANGE  = Range4


	// init command register 2
	if (I2C_transfer(i2c, &I2C_transaction)) {
		System_printf("Light sensor is configured op reg 2 ! \n" );
		return TRUE;
	}else{
		System_printf("Light sensor configured op reg 2 is failed! \n" );
		return FALSE;
	}

}


