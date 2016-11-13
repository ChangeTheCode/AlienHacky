/*
 * RF.h
 *
 *  Created on: 13. Nov. 2016
 *      Author: ursus
 */

#ifndef RF_H_
#define RF_H_

#define DATA_ENTRY_HEADER_SIZE 8  /* Constant header size of a Generic Data Entry */
#define MAX_LENGTH             30 /* Max length byte the radio will accept */
#define NUM_APPENDED_BYTES     2  /* The Data Entries data field will contain:
                                   * 1 Header byte (RF_cmdPropRx.rxConf.bIncludeHdr = 0x1)
                                   * Max 30 payload bytes
                                   * 1 status byte (RF_cmdPropRx.rxConf.bAppendStatus = 0x1) */
#define MAX_PACKET_LENGTH (MAX_LENGTH + NUM_APPENDED_BYTES - 1)

#endif /* RF_H_ */
