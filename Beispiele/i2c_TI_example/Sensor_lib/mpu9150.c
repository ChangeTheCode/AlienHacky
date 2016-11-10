/*
 * mpu9150.c
 *
 *  Created on: 09.11.2016
 *      Author: Jim
 */

#include "../AlienHacky_datatypes.h"
#include "mpu9150.h"

#include <xdc/runtime/System.h>


BOOL mpu9150_write(I2C_Handle i2c, uint8_t reg_address, uint8_t value){
	I2C_Transaction i2cTransaction;
	uint8_t txBuffer[2];

	// Set config of the light sensor
	i2cTransaction.slaveAddress = MPU9150_I2C_ADDRESS;//
	i2cTransaction.writeBuf = txBuffer;
	i2cTransaction.writeCount = 2;
	i2cTransaction.readBuf = NULL;
	i2cTransaction.readCount = 0;
	txBuffer[0] = reg_address; //
	txBuffer[1] = value;

	// init command register 1
	if (I2C_transfer(i2c, &i2cTransaction)) {
		System_printf("Write successful! \n" );
		return TRUE;
	}else{
		System_printf("Write failed! \n" );
		return FALSE;
	}
}

BOOL mpu9150_read(I2C_Handle i2c , uint8_t reg_address, uint8_t count_of_bytes, uint8_t* read_buffer){

	I2C_Transaction i2cTransaction;
	uint8_t txBuffer[2];

    // Set config of the light sensor
    i2cTransaction.slaveAddress = MPU9150_I2C_ADDRESS;//
	i2cTransaction.writeBuf = txBuffer;
	i2cTransaction.writeCount = 1;
	i2cTransaction.readBuf = *read_buffer;
	i2cTransaction.readCount = count_of_bytes;//4;
	txBuffer[0] = reg_address; // Command register 1

	// init command register 1
	if (I2C_transfer(i2c, &i2cTransaction)) {
		System_printf("Read successful! \n" );
		return TRUE;
	}else{
		System_printf("read failed! \n" );
		return FALSE;
	}

}



