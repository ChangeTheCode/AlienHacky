/***********************************************************************************
  Filename: hello.c

  Description:  This example reads Chip Id and Chip Version from CC2520.

  Press S1 to output Chip Id and Chip version on the serial port.
  Configure the PC HyperTerminal for baudrate 38400 bps, 8 data bits
  no parity, and 1 stop bit.

***********************************************************************************/

/*************************************7**********************************************
* INCLUDES
*/
#include <hal_lcd.h>
#include <hal_led.h>
#include <hal_board.h>
#include <hal_uart.h>
#include <hal_rf.h>
#include <hal_assert.h>
#include "util.h"
#include "util_lcd.h"
#include "util_uart.h"


/***********************************************************************************
* LOCAL VARIABLES
*/

/***********************************************************************************
* LOCAL FUNCTIONS
*/

static void appPrintMenu(void);


/******************************************************************************
* @fn          appPrintMenu
*
* @brief       Prints menu on UART
*
* @param       none
*
* @return      none
*/
static void appPrintMenu(void)
{
    static char *pLine= "------------------------------\n";

    printStr("\033[H\033[2J");             // Cursor home, erase screen (ANSI)
    printStr(pLine);
    printStr("CC2520 Application Example\n");
    printStr("Hello\n");
    printStr(pLine);
    printStr("\n");
}


/***********************************************************************************
* @fn          main
*
* @brief       This is the main entry of the "hello" application
*
* @param       none
*
* @return      none
*/
void main (void)
{
    uint8 chipId;
    uint8 chipVer;

    // Initalise board peripherals
    halBoardInit();

    // Initalise hal_rf
    if(halRfInit()==FAILED) {
      HAL_ASSERT(FALSE);
    }

    // Indicate that device is powered
    halLedSet(1);

    // Initalise UART
    halUartInit(HAL_UART_BAUDRATE_38400, 0);

    // Print Logo and splash screen on LCD
    utilPrintLogo("Hello");
    appPrintMenu();


    // Puts MCU in endless loop
    while(TRUE) {
        // Wait for user to press S1
        while (!HAL_BUTTON_1_PUSHED());
        halMcuWaitMs(350);

        // Read Chip id and version
        chipId = halRfGetChipId();
        chipVer = halRfGetChipVer();

        // Output to serial port
        printKvpHex("Chip ID: ", chipId);
        printKvpHex("Chip Version: ", chipVer);
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
