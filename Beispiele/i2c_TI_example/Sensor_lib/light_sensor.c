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
	tx_buffer[1] = 0xA0; //  NUMBER OF INTEGRATION CYCLES is 4 and  The IC measures ALS continuously is set


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

BOOL config_light_int_threshold(I2C_Handle i2c, int threshold_top, int threshold_down){
	I2C_Transaction I2C_transaction;
	uint8_t tx_buffer[2];
	int length_to_write = 0;

	if( threshold_down == 0){ // if the lower threshold is 0 so don't write the register
		length_to_write = 3;
	}else{
		length_to_write = 6;
	}

	// Set config of the light sensor
	I2C_transaction.slaveAddress = 0x44;// Lightsensor read slave adress
	I2C_transaction.writeBuf = tx_buffer;
	I2C_transaction.writeCount = length_to_write;
	I2C_transaction.readBuf = NULL;//rxBuffer;
	I2C_transaction.readCount = 0;//4;
	tx_buffer[0] = INT_HT_LSB; // interrupt Threshold register low byte
	tx_buffer[1] = threshold_top & 0x00FF ; // clear high byte part
	tx_buffer[2] = threshold_top >> 8  ; // clear low byte part


	// init command register 2
	if (I2C_transfer(i2c, &I2C_transaction)) {
		System_printf("Light sensor is configured interrupt ! \n" );
		return TRUE;
	}else{
		System_printf("Light sensor configured interrupt is failed! \n" );
		return FALSE;
	}
}


void read_light_sensor_values(I2C_Handle i2c, int* read_value){
	I2C_Transaction I2C_transaction;
	uint8_t tx_buffer[1];
	uint8_t rx_buffer[4];

	/* Point to the T ambient register and read its 2 bytes */
	I2C_transaction.slaveAddress = 0x44;
	I2C_transaction.writeBuf = tx_buffer;
	I2C_transaction.writeCount = 1;
	I2C_transaction.readBuf = read_value;// rx_buffer;
	I2C_transaction.readCount = 4;

	tx_buffer[0] = DATA_LSB;
	if (I2C_transfer(i2c, &I2C_transaction)) {
		// pin toggle
		//System_printf("Sample: %d , %d (RAW)\n", *read_value , *++read_value);
	}
	else {
		/*System_printf("I2C Bus fault \n" );
		System_printf("I2C Werte Slave Adresse %d ",I2C_transaction.slaveAddress );
		System_printf("I2C Werte TX %d, %d \n", tx_buffer[0], tx_buffer[1] );
		System_printf("I2C Werte RX %d, %d \n", rx_buffer[0], rx_buffer[1] );*/
		//Task_sleep(20000);
	}

	//System_flush();
}


