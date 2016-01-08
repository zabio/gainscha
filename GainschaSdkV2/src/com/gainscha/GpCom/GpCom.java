 package com.gainscha.GpCom;
 
 import android.app.Activity;
 import android.os.Bundle;
 
 public class GpCom extends Activity
 {
   static final String PROG_VERSION = "0.71";
   static final int GP_VENDOR_ID = 1208;
 
   public void onCreate(Bundle savedInstanceState)
   {
     super.onCreate(savedInstanceState);
   }
 
   public static String getErrorText(ERROR_CODE errorcode)
   {
     return GpTools.getErrorText(errorcode);
   }
 
   public static String getVersion()
   {
     return "0.71";
   }
 
   public static enum ALIGNMENT
   {
     LEFT, 
     CENTER, 
     RIGHT;
   }
 
   public static enum ASCII_CONTROL_CODE
   {
     NUL(0), 
     SOH(1), 
     STX(2), 
     ETX(3), 
     EOT(4), 
     ENQ(5), 
     ACK(6), 
     BEL(7), 
     BS(8), 
     HT(9), 
     LF(10), 
     VT(11), 
     FF(12), 
     CR(13), 
     SO(14), 
     SI(15), 
     DLE(16), 
     DC1(17), 
     DC2(18), 
     DC3(19), 
     DC4(20), 
     NAK(21), 
     SYN(22), 
     ETB(23), 
     CAN(24), 
     EM(25), 
     SUB(26), 
     ESC(27), 
     FS(28), 
     GS(29), 
     RS(30), 
     US(31);
 
     private final int value;
 
     private ASCII_CONTROL_CODE(int value)
     {
       this.value = value;
     }
 
     public byte getASCIIValue()
     {
       return (byte)this.value;
     }
   }
 
   public static enum BITDEPTH
   {
     BW, 
     GRAYSCALE;
   }
 
   public static enum DATA_TYPE
   {
     GENERAL, 
     RESERVED1, //MICR
     RESERVED2, //IMAGE, 
     DEVICESTATUS, 
     ASB, 
     INKSTATUS, 
     EJ_DATA, 
     NOTHING;
   }
 
   public static enum ERROR_CODE
   {
     SUCCESS, 
     FAILED, 
     UNDETERMINED, 
     TIMEOUT, 
     NO_DEVICE_PARAMETERS, 
     DEVICE_ALREADY_OPEN, 
     INVALID_PORT_TYPE, 
     INVALID_PORT_NAME, 
     INVALID_PORT_NUMBER, 
     INVALID_IP_ADDRESS, 
     INVALID_IMAGE_FORMAT, 
     INVALID_BIT_DEPTH, 
     INVALID_IMAGE_PROCESSING, 
     INVALID_THRESHOLD, 
     INVALID_DEVICE_STATUS_TYPE, 
     INVALID_SCAN_AREA, 
     INVALID_CROP_AREA, 
     INVALID_CROP_AREA_INDEX, 
     INVALID_PAPER_SIDE, 
     INVALID_FONT, 
     INVALID_JUSTIFICATION, 
     INVALID_PRINT_DIRECTION, 
     INVALID_PRINT_PAGE_MODE, 
     INVALID_CALLBACK_OBJECT, 
     INVALID_PARAMETER_COMBINATION, 
     INVALID_PARAMETER_FOR_CARDSCAN, 
     INVALID_APPLICATION_CONTEXT, 
     NO_USB_DEVICE_FOUND, 
     NO_ACCESS_GRANTED_BY_USER, 
     ERROR_OR_NO_ACCESS_PERMISSION;
   }
 
   public static enum FONT
   {
     FONT_A, 
     FONT_B;
   }
 
//   public static enum IMAGEFORMAT
//   {
//     RAW, 
//     BMP, 
//     TIFF, 
//     TIFF_COMP, 
//     JPEG_HIGH, 
//     JPEG_MED, 
//     JPEG_LOW;
//   }
 
//   public static enum IMAGEPROCESSING
//   {
//     NONE, 
//     SHARPENING;
//   }
 
//   public static enum MICR_FONT
//   {
//     E13B, 
//     CMC7;
//   }
 
   public static enum PAPERSIDE
   {
     FRONT, 
     BACK;
   }
 
   public static enum PORT_TYPE
   {
     SERIAL, 
     PARALLEL, 
     USB, 
     ETHERNET, 
     BLUETOOTH;
   }
 
   public static enum PRINTDIRECTION
   {
     LEFTTORIGHT, 
     BOTTOMTOTOP, 
     RIGHTTOLEFT, 
     TOPTOBOTTOM;
   }
 
   static enum RECEIVESTATE
   {
     RSTATE_SINGLE, 
     RSTATE_INKSTATUS, 
     RSTATE_EXTSTATUS, 
     RSTATE_ASB, 
     RSTATE_BLOCK_HEXADECIMAL, 
     RSTATE_BLOCK_BINARY, 
     RSTATE_RESERVED, //RSTATE_MICR, 
     RSTATE_WAVEFORM, 
     RSTATE_EJDATA;
   }
 
   static enum RECEIVESUBSTATE
   {
     RSUBSTATE_INITSTATE, 
     RSUBSTATE_EJDATA, 
     RSUBSTATE_FILEINFO, 
     RSUBSTATE_SIZEINFO, 
     RSUBSTATE_IMAGEDATA, 
     RSUBSTATE_J9100IMAGEDATA;
   }
 }
