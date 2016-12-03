/*
 * AlienTypes.h
 *
 *  Created on: 13. Nov. 2016
 *      Author: ursus
 */

#ifndef ALIENTYPES_H_
#define ALIENTYPES_H_

#include <xdc/std.h>

#define BOOLEAN int

#ifndef xdc_std__include

	#define TRUE (1==1)
	#define FALSE !TRUE

#endif

typedef struct{
	uint8_t _kick_int_high_x;	 // means:  1.
	uint8_t _kick_float_high_x;	// 			 . 00
	uint8_t _kick_float_low_x;  // 			 .    00

	uint8_t _kick_int_high_y;	 // means:  1.
	uint8_t _kick_float_high_y;	// 			 . 00
	uint8_t _kick_float_low_y;  // 			 .    00

	uint8_t _kick_int_high_z;	 // means:  1.
	uint8_t _kick_float_high_z;	// 			 . 00
	uint8_t _kick_float_low_z;  // 			 .    00

}kick_vectors_t;

#endif /* ALIENTYPES_H_ */
