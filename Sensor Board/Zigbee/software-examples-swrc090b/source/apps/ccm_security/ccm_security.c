/***********************************************************************************
  Filename: ccm_security.c

  Description:  This example illustrates how to use CCM security for
  a packet transmission. The example uses the test vectors
  from section 2.3 in the IEEE Std. 802.15.4-2006 specification

  Press joystick up to send the authenticated and encrypted packet
  The authenticated and encrypted packet can be verified with the
  packet sniffer on channel 25.

***********************************************************************************/

/***********************************************************************************
* INCLUDES
*/
#include <hal_lcd.h>
#include <hal_led.h>
#include <hal_joystick.h>
#include <hal_board.h>
#include <hal_assert.h>
#include "util.h"
#include "util_lcd.h"
#include "hal_cc2520.h"
#include "hal_rf.h"


/***********************************************************************************
* CONSTANTS
*/
// Application parameters
#define RF_CHANNEL              25                  // 2.4 GHz RF channel

#define ENCR_LENGTH              1
#define AUTH_LENGTH             29
#define MIC_LENGTH               8
#define NONCE_FLAG_BYTE       0x09
#define LENGTH_BYTE           0x28

#define KEY_LENGTH              16
#define NONCE_LENGTH            16

// CC2520 Memory addresses
// Security
#define ADDR_RX               0x200
#define ADDR_TX               ADDR_RX + 127
#define ADDR_NONCE		      0x320                 // Nonce address
#define ADDR_K                0x340                 // Key address
#define TXBUF_START           0x100
#define RXBUF_START           0x180

#define HIGH_PRIORITY                 1


/***********************************************************************************
* LOCAL VARIABLES
*/
// Security key
static uint8 pKey[]= {
    0xc0, 0xc1, 0xc2, 0xc3, 0xc4, 0xc5, 0xc6, 0xc7,
    0xc8, 0xc9, 0xca, 0xcb, 0xcc, 0xcd, 0xce, 0xcf,
};

// Security Nonce
static uint8 pNonce[]= {
    NONCE_FLAG_BYTE, 0xAC, 0xDE, 0x48, 0x00, 0x00, 0x00, 0x00,
    0x01, 0x00, 0x00, 0x00, 0x05, 0x06, 0x00, 0x00
};

// Packet
static uint8 pPacket[]= {
    LENGTH_BYTE, 0x2B, 0xDC, 0x84, 0x21, 0x43, 0x02, 0x00, 0x00,
    0x00, 0x00, 0x48, 0xDE, 0xAC, 0xFF, 0xFF, 0x01,
    0x00, 0x00, 0x00, 0x00, 0x48, 0xDE, 0xAC, 0x06,
    0x05, 0x00, 0x00, 0x00, 0x01, 0xCE
};

/***********************************************************************************
* LOCAL FUNCTIONS
*/


/***********************************************************************************
* @fn          securityInit
*
* @brief       Write key and nonce to CC2520 Memory
*
* @param       uint8* pKey - Pointer to security key
*              uint8* pNonce - Pointer to security nonce
*
* @return      none
*/
static void securityInit(uint8* pKey, uint8* pNonce)
{
    // Write key
    CC2520_MEMWR(ADDR_K,KEY_LENGTH,pKey);

    // Write nonce
    CC2520_MEMWR(ADDR_NONCE,NONCE_LENGTH,pNonce);

    // Reverse key
    CC2520_MEMCPR(HIGH_PRIORITY,KEY_LENGTH,ADDR_K,ADDR_K);

    // Reverse nonce
    CC2520_MEMCPR(HIGH_PRIORITY,NONCE_LENGTH,ADDR_NONCE,ADDR_NONCE);
}


/***********************************************************************************
* @fn          main
*
* @brief       This is the main entry of the "CCM Security" application
*
* @param       none
*
* @return      none
*/
void main (void)
{
    uint16 nPackets = 0;

    // Initalise board peripherals
    halBoardInit();

    // Power up and initialise radio
    if(halRfInit()==FAILED) {
      HAL_ASSERT(FALSE);
    }
    halRfSetChannel(RF_CHANNEL);

    // Indicate that device is powered
    halLedSet(1);

    // Print Logo and splash screen on LCD
    utilPrintLogo("CCM Security");

    // Write key and nonce
    securityInit(pKey, pNonce);

    // Write packet to work buffer
    CC2520_MEMWR(ADDR_TX, sizeof(pPacket), pPacket);

    // skip the length byte and start from the next byte in work buffer
    CC2520_CCM(HIGH_PRIORITY,ADDR_K/16, ENCR_LENGTH, ADDR_NONCE/16, ADDR_TX+1, 0, AUTH_LENGTH, 2);
    WAIT_DPU_DONE_H();

    // copy from work buffer to TX FIFO
    CC2520_TXBUFCP(HIGH_PRIORITY, ADDR_TX, sizeof(pPacket)+MIC_LENGTH, NULL);
    WAIT_DPU_DONE_H();

    // Puts MCU in endless loop
    while(TRUE) {
        // Send the packet when joystick is pushed up
        while(!HAL_JOYSTICK_UP());
        CC2520_INS_STROBE(CC2520_INS_STXON);

        // Update display
        halLcdClear();
        halLcdWriteLine(HAL_LCD_LINE_1, "CCM Security");
        halLcdWriteLine(HAL_LCD_LINE_2, "Sent:");
        utilLcdDisplayUint16(HAL_LCD_LINE_3, HAL_LCD_RADIX_DEC, ++nPackets);
        halMcuWaitMs(350);
    }
}



/***********************************************************************************
  Copyright 2007 Texas Instruments Incorporated. All rights reserved.

  IMPORTANT: Your use of this Software is limited to those specific rights
  granted under the terms of a software license agreement between the user
  who downloaded the software, his/her employer (which must be your employer)
  and Texas Instruments Incorporated (the "License").  You may not use this
  Software unless you agree to abide by the terms of the License. The License
  limits your use, and you acknowledge, that the Software may not be modified,
  copied or distributed unless embedded on a Texas Instruments microcontroller
  or used solely and exclusively in conjunction with a Texas Instruments radio
  frequency transceiver, which is integrated into your product.  Other than for
  the foregoing purpose, you may not use, reproduce, copy, prepare derivative
  works of, modify, distribute, perform, display or sell this Software and/or
  its documentation for any purpose.

  YOU FURTHER ACKNOWLEDGE AND AGREE THAT THE SOFTWARE AND DOCUMENTATION ARE
  PROVIDED “AS IS” WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESS OR IMPLIED,
  INCLUDING WITHOUT LIMITATION, ANY WARRANTY OF MERCHANTABILITY, TITLE,
  NON-INFRINGEMENT AND FITNESS FOR A PARTICULAR PURPOSE. IN NO EVENT SHALL
  TEXAS INSTRUMENTS OR ITS LICENSORS BE LIABLE OR OBLIGATED UNDER CONTRACT,
  NEGLIGENCE, STRICT LIABILITY, CONTRIBUTION, BREACH OF WARRANTY, OR OTHER
  LEGAL EQUITABLE THEORY ANY DIRECT OR INDIRECT DAMAGES OR EXPENSES
  INCLUDING BUT NOT LIMITED TO ANY INCIDENTAL, SPECIAL, INDIRECT, PUNITIVE
  OR CONSEQUENTIAL DAMAGES, LOST PROFITS OR LOST DATA, COST OF PROCUREMENT
  OF SUBSTITUTE GOODS, TECHNOLOGY, SERVICES, OR ANY CLAIMS BY THIRD PARTIES
  (INCLUDING BUT NOT LIMITED TO ANY DEFENSE THEREOF), OR OTHER SIMILAR COSTS.

  Should you have any questions regarding your right to use this Software,
  contact Texas Instruments Incorporated at www.TI.com.
***********************************************************************************/

