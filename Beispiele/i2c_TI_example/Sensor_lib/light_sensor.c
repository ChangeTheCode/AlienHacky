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
	I2C_Transaction i2cTransaction;
	uint8_t txBuffer[2];

    // Set config of the light sensor
    i2cTransaction.slaveAddress = 0x44;// Lightsensor read slave adress ; 136 = schreiben 137 lesen
	i2cTransaction.writeBuf = txBuffer;
	i2cTransaction.writeCount = 2;
	i2cTransaction.readBuf = NULL;//rxBuffer;
	i2cTransaction.readCount = 0;//4;
	txBuffer[0] = 0x00; // Command register 1
	txBuffer[1] = 0xA1; //  NUMBER OF INTEGRATION CYCLES is 4 and  The IC measures ALS continuously is set

	// init command register 1
	if (I2C_transfer(i2c, &i2cTransaction)) {
		System_printf("Light sensor is configured! \n" );
		return TRUE;
	}else{
		System_printf("Light sensor configured is failed! \n" );
		return FALSE;
	}
}

BOOL config_light_sensor_reg2(I2C_Handle i2c){
	I2C_Transaction i2cTransaction;
	uint8_t txBuffer[2];

    // Set config of the light sensor
    i2cTransaction.slaveAddress = 0x44;// Lightsensor read slave adress ; 136 = schreiben 137 lesen
	i2cTransaction.writeBuf = txBuffer;
	i2cTransaction.writeCount = 2;
	i2cTransaction.readBuf = NULL;//rxBuffer;
	i2cTransaction.readCount = 0;//4;
	txBuffer[0] = 0x01; // Command register 2
	txBuffer[1] = 0x00; //  FULL SCALE RANGE  = Range4


	// init command register 2
	if (I2C_transfer(i2c, &i2cTransaction)) {
		System_printf("Light sensor is configured op reg 2 ! \n" );
		return TRUE;
	}else{
		System_printf("Light sensor configured op reg 2 is failed! \n" );
		return FALSE;
	}

}

I2C_Transaction single_read_light_sensor_value(I2C_Handle i2c, uint8_t reg_address){
	I2C_Transaction i2c_receive;
	return i2c_receive;
}

I2C_Transaction single_write_light_sensor_value(I2C_Handle i2c, uint8_t reg_address, uint8_t write_value ){
	I2C_Transaction i2c_receive;
	return i2c_receive;
}

