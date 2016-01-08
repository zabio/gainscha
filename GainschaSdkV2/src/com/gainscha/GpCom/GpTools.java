package com.gainscha.GpCom;

import java.io.IOException;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.Vector;

public class GpTools
{
  public static Vector<Byte> convertHexadecimalToBinary(Vector<Byte> HexadecimalData)
  {
    int hexLength = HexadecimalData.size();
    int index1 = 0;
    int index2 = 0;
    byte binaryValue = 0;
    
    Vector<Byte> binaryData = new Vector<Byte>(hexLength / 2, 1024);
    
    for (index2 = 1; index2 < hexLength; index2 += 2)
    {
      index1 = index2 - 1;
      binaryValue = (byte)(xtod(((Byte)HexadecimalData.elementAt(index1)).byteValue()) * 16 + xtod(((Byte)HexadecimalData.elementAt(index2)).byteValue()));
      binaryData.add(new Byte(binaryValue));
    }
    
    return binaryData;
  }
  
  public static byte xtod(byte c)
  {
    byte retval = 0;
    
    if ((c >= 48) && (c <= 57)) { retval = (byte)(c - 48);
    } else if ((c >= 65) && (c <= 70)) { retval = (byte)(c - 65 + 10);
    } else if ((c >= 97) && (c <= 102)) { retval = (byte)(c - 97 + 10);
    }
    return retval;
  }
  
  public static Vector<Byte> convertEscposToBinary(String escpos)
  {
    int value = -1;
    
    StringReader r = new StringReader(escpos);
    StreamTokenizer st = new StreamTokenizer(r);
    st.resetSyntax();
    st.slashSlashComments(false);
    st.slashStarComments(false);
    st.whitespaceChars(0, 32);
    st.wordChars(33, 255);
    st.quoteChar(34);
    st.quoteChar(39);
    st.eolIsSignificant(true);
    
    Vector<Byte> binaryData = new Vector<Byte>(100, 50);
    
    try
    {
      while (st.nextToken() != -1)
      {
        switch (st.ttype)
        {
        case -3: 
          String s = st.sval;
          value = -1;
          
          if ((s.length() == 1) && (!Character.isDigit(s.charAt(0))))
          {
            byte[] bytes = s.getBytes();
            value = bytes[0];
          }
          else if ((s.length() > 2) && (s.substring(0, 2) == "0x"))
          {
            value = Integer.parseInt(s.substring(2), 16);
          }
          else if (isInteger(s))
          {
            value = Integer.parseInt(s);
          }
          else if (s.contentEquals("NUL")) { value = GpCom.ASCII_CONTROL_CODE.NUL.getASCIIValue();
          } else if (s.contentEquals("SOH")) { value = GpCom.ASCII_CONTROL_CODE.SOH.getASCIIValue();
          } else if (s.contentEquals("STX")) { value = GpCom.ASCII_CONTROL_CODE.STX.getASCIIValue();
          } else if (s.contentEquals("ETX")) { value = GpCom.ASCII_CONTROL_CODE.ETX.getASCIIValue();
          } else if (s.contentEquals("EOT")) { value = GpCom.ASCII_CONTROL_CODE.EOT.getASCIIValue();
          } else if (s.contentEquals("ENQ")) { value = GpCom.ASCII_CONTROL_CODE.ENQ.getASCIIValue();
          } else if (s.contentEquals("ACK")) { value = GpCom.ASCII_CONTROL_CODE.ACK.getASCIIValue();
          } else if (s.contentEquals("BEL")) { value = GpCom.ASCII_CONTROL_CODE.BEL.getASCIIValue();
          } else if (s.contentEquals("BS")) { value = GpCom.ASCII_CONTROL_CODE.BS.getASCIIValue();
          } else if (s.contentEquals("HT")) { value = GpCom.ASCII_CONTROL_CODE.HT.getASCIIValue();
          } else if (s.contentEquals("LF")) { value = GpCom.ASCII_CONTROL_CODE.LF.getASCIIValue();
          } else if (s.contentEquals("VT")) { value = GpCom.ASCII_CONTROL_CODE.VT.getASCIIValue();
          } else if (s.contentEquals("FF")) { value = GpCom.ASCII_CONTROL_CODE.FF.getASCIIValue();
          } else if (s.contentEquals("CR")) { value = GpCom.ASCII_CONTROL_CODE.CR.getASCIIValue();
          } else if (s.contentEquals("SO")) { value = GpCom.ASCII_CONTROL_CODE.SO.getASCIIValue();
          } else if (s.contentEquals("SI")) { value = GpCom.ASCII_CONTROL_CODE.SI.getASCIIValue();
          } else if (s.contentEquals("DLE")) { value = GpCom.ASCII_CONTROL_CODE.DLE.getASCIIValue();
          } else if (s.contentEquals("DC1")) { value = GpCom.ASCII_CONTROL_CODE.DC1.getASCIIValue();
          } else if (s.contentEquals("DC2")) { value = GpCom.ASCII_CONTROL_CODE.DC2.getASCIIValue();
          } else if (s.contentEquals("DC3")) { value = GpCom.ASCII_CONTROL_CODE.DC3.getASCIIValue();
          } else if (s.contentEquals("DC4")) { value = GpCom.ASCII_CONTROL_CODE.DC4.getASCIIValue();
          } else if (s.contentEquals("NAK")) { value = GpCom.ASCII_CONTROL_CODE.NAK.getASCIIValue();
          } else if (s.contentEquals("SYN")) { value = GpCom.ASCII_CONTROL_CODE.SYN.getASCIIValue();
          } else if (s.contentEquals("ETB")) { value = GpCom.ASCII_CONTROL_CODE.ETB.getASCIIValue();
          } else if (s.contentEquals("CAN")) { value = GpCom.ASCII_CONTROL_CODE.CAN.getASCIIValue();
          } else if (s.contentEquals("EM")) { value = GpCom.ASCII_CONTROL_CODE.EM.getASCIIValue();
          } else if (s.contentEquals("SUB")) { value = GpCom.ASCII_CONTROL_CODE.SUB.getASCIIValue();
          } else if (s.contentEquals("ESC")) { value = GpCom.ASCII_CONTROL_CODE.ESC.getASCIIValue();
          } else if (s.contentEquals("FS")) { value = GpCom.ASCII_CONTROL_CODE.FS.getASCIIValue();
          } else if (s.contentEquals("GS")) { value = GpCom.ASCII_CONTROL_CODE.GS.getASCIIValue();
          } else if (s.contentEquals("RS")) { value = GpCom.ASCII_CONTROL_CODE.RS.getASCIIValue();
          } else if (s.contentEquals("US")) { value = GpCom.ASCII_CONTROL_CODE.US.getASCIIValue();
          }
          
          if (value != -1)
          {
            Byte b = new Byte((byte)value);
            binaryData.add(b);
          }
          break;
        case 34: 
        case 39: 
          String str = st.sval;
          
          for (int i = 0; i < str.length(); i++)
          {
            byte b = str.getBytes()[i];
            binaryData.add(new Byte(b));
          }
        }
        
      }
    }
    catch (NumberFormatException localNumberFormatException) {}catch (IOException localIOException) {}
    
    return binaryData;
  }
  
  public static boolean isInteger(String input)
  {
    try
    {
      Integer.parseInt(input);
      return true;
    }
    catch (Exception e) {}
    
    return false; }
  
  public static String getErrorText(GpCom.ERROR_CODE errorcode) {
    String s;
    switch (errorcode)
    {
    case SUCCESS: 
      s = "Operation succeeded";
      break;
    case INVALID_APPLICATION_CONTEXT: 
        s = "Invalid application context specified";
        break;
    case TIMEOUT: 
        s = "Timeout occured";
        break;
    case DEVICE_ALREADY_OPEN: 
        s = "Device is already open";
        break;
    case INVALID_PORT_NAME: 
        s = "Invalid port name specified";
        break;
    case INVALID_PORT_NUMBER: 
        s = "Invalid port number specified";
        break;
    case INVALID_CROP_AREA: 
        s = "Invalid crop area index specified";
        break;
    case INVALID_JUSTIFICATION: 
        s = "Invalid justification specified";
        break;
    case INVALID_THRESHOLD: 
        s = "Invalid threshold specified";
        break;
    case INVALID_PARAMETER_FOR_CARDSCAN: 
        s = "Invalid parameter setting for card scan";
    case NO_USB_DEVICE_FOUND: 
        s = "No compatible USB device found";
        break;
    case ERROR_OR_NO_ACCESS_PERMISSION: 
        s = "Error or no permission to access the port";
        break;
    case NO_DEVICE_PARAMETERS: 
        s = "No device parameters set";
        break;
    case INVALID_PARAMETER_COMBINATION: 
        s = "Invalid parameter combination";
        break;
    case NO_ACCESS_GRANTED_BY_USER: 
        s = "No port access granted by the user";
        break;
    case INVALID_IMAGE_FORMAT: 
        s = "Invalid image format specified";
        break;
    case INVALID_BIT_DEPTH: 
        s = "Invalid bit depth specified";
        break;
    case INVALID_CALLBACK_OBJECT: 
        s = "Invalid callback object specified";
        break;
    case INVALID_IP_ADDRESS: 
        s = "Invalid IP address specified";
        break;
    case INVALID_PRINT_DIRECTION: 
        s = "Invalid print direction specified";
        break;
    case INVALID_PRINT_PAGE_MODE: 
        s = "Invalid print page mode specified";
        break;
    case INVALID_SCAN_AREA: 
        s = "Invalid scan area specified";
        break;
    case INVALID_PAPER_SIDE: 
        s = "Invalid paper side specified";
        break;
    case FAILED: 
        s = "Operation failed";
        break;
    case INVALID_IMAGE_PROCESSING: 
        s = "Invalid image processing specified";
        break;
    case INVALID_DEVICE_STATUS_TYPE: 
        s = "Invalid device status type specified";
        break;
    case INVALID_PORT_TYPE: 
        s = "Invalid port type specified";
        break;
    case INVALID_CROP_AREA_INDEX: 
        s = "Invalid crop area specified";
        break;
    case INVALID_FONT: 
        s = "Invalid font specified";
      break;
    case UNDETERMINED: 
    default: 
      s = "Unknown error code";
    }
    
    return s;
  }
  
  public static Boolean isEven(int number)
  {
    if (number % 2 == 0)
    {
      return Boolean.valueOf(true);
    }
    
    return Boolean.valueOf(false);
  }
}
