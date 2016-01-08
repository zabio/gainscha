 package com.gainscha.GpCom;
 
 import java.util.Vector;
 
 public class BluetoothPort extends Port
 {
   BluetoothPort(GpComDeviceParameters parameters)
   {
     super(parameters);
   }
 
   GpCom.ERROR_CODE openPort()
   {
     return null;
   }
 
   GpCom.ERROR_CODE closePort()
   {
     return null;
   }
 
   boolean isPortOpen()
   {
     return false;
   }
 
   GpCom.ERROR_CODE writeData(Vector<Byte> data)
   {
     return null;
   }
 
   protected GpCom.ERROR_CODE writeDataImmediately(Vector<Byte> data)
   {
     return null;
   }
 }
