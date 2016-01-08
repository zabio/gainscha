package com.gainscha.GpCom;

import android.content.Context;
import java.net.InetAddress;

public class GpComDeviceParameters
{
  public GpCom.PORT_TYPE PortType;
  public String PortName;
  public int PortNumber;
  public String IPAddress;
  public char DeviceID;
  public String DeviceName;
  public Context ApplicationContext;

  public GpComDeviceParameters()
  {
    this.PortType = GpCom.PORT_TYPE.ETHERNET;
    this.PortName = "";
    this.PortNumber = 9100;
    this.IPAddress = "192.168.192.168";
    this.DeviceID = '\000';
    this.DeviceName = "";
    this.ApplicationContext = null;
  }

  public GpCom.ERROR_CODE validateParameters()
  {
    GpCom.ERROR_CODE retval = GpCom.ERROR_CODE.SUCCESS;

    switch (this.PortType)
    {
    case SERIAL:
      break;
    case PARALLEL:
      break;
    case USB:
      if (this.ApplicationContext == null)
      {
        retval = GpCom.ERROR_CODE.INVALID_APPLICATION_CONTEXT;
      }
      break;
    case ETHERNET:
      if (this.PortNumber <= 0)
      {
        retval = GpCom.ERROR_CODE.INVALID_PORT_NUMBER;
      }
      else if (this.IPAddress.length() != 0)
      {
        try
        {
          InetAddress.getByName(this.IPAddress);
        }
        catch (Exception e)
        {
          retval = GpCom.ERROR_CODE.INVALID_IP_ADDRESS;
        }

      }
      else
      {
        retval = GpCom.ERROR_CODE.INVALID_IP_ADDRESS;
      }
      break;
    default:
      retval = GpCom.ERROR_CODE.INVALID_PORT_TYPE;
    }

    return retval;
  }

  public GpComDeviceParameters copy()
  {
    GpComDeviceParameters dp = new GpComDeviceParameters();
    dp.PortType = this.PortType;
    dp.PortName = this.PortName;
    dp.PortNumber = this.PortNumber;
    dp.IPAddress = this.IPAddress;
    dp.DeviceID = this.DeviceID;
    dp.DeviceName = this.DeviceName;
    dp.ApplicationContext = this.ApplicationContext;

    return dp;
  }
}
