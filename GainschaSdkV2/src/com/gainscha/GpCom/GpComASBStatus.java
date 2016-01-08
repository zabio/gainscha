 package com.gainscha.GpCom;
 
 import java.util.Vector;
 
 public class GpComASBStatus
 {
   private Vector<Byte> m_ASBStatusData = new Vector(5);
   public Boolean DrawerKickoutConnectorPin3High;
   public Boolean Online;
   public Boolean CoverOpen;
   public Boolean PaperFedByFeedButton;
   public Boolean WaitingForOnlineRevovery;
   public Boolean PaperFeedButtonIsTurnedOn;
   public Boolean MechanicalError;
   public Boolean AutoCutterError;
   public Boolean UnrecoverableError;
   public Boolean AutomaticallyRecoverableError;
   public Boolean PaperNearEnd;
   public Boolean PaperOut;
   public Boolean PaperPresentAtTOFSensor;
   public Boolean PaperPresentAtBOFSensor;
   public Boolean SlipSelectedAsActiveSheet;
   public Boolean CanPrintOnSlip;
 
   public GpComASBStatus()
   {
     this.m_ASBStatusData.clear();
     this.DrawerKickoutConnectorPin3High = Boolean.valueOf(false);
     this.Online = Boolean.valueOf(false);
     this.CoverOpen = Boolean.valueOf(false);
     this.PaperFedByFeedButton = Boolean.valueOf(false);
     this.WaitingForOnlineRevovery = Boolean.valueOf(false);
     this.PaperFeedButtonIsTurnedOn = Boolean.valueOf(false);
     this.MechanicalError = Boolean.valueOf(false);
     this.AutoCutterError = Boolean.valueOf(false);
     this.UnrecoverableError = Boolean.valueOf(false);
     this.AutomaticallyRecoverableError = Boolean.valueOf(false);
     this.PaperNearEnd = Boolean.valueOf(false);
     this.PaperOut = Boolean.valueOf(false);
     this.PaperPresentAtTOFSensor = Boolean.valueOf(false);
     this.PaperPresentAtBOFSensor = Boolean.valueOf(false);
     this.SlipSelectedAsActiveSheet = Boolean.valueOf(false);
     this.CanPrintOnSlip = Boolean.valueOf(false);
   }
 
   GpCom.ERROR_CODE setASBStatus(Vector<Byte> ASBData)
   {
     byte byte1 = 0;
     byte byte2 = 0;
     byte byte3 = 0;
     byte byte4 = 0;
 
     if (ASBData.size() != 4)
     {
       return GpCom.ERROR_CODE.FAILED;
     }
 
     this.m_ASBStatusData = ((Vector)ASBData.clone());
 
     byte1 = ((Byte)ASBData.elementAt(0)).byteValue();
     byte2 = ((Byte)ASBData.elementAt(1)).byteValue();
     byte3 = ((Byte)ASBData.elementAt(2)).byteValue();
     byte4 = ((Byte)ASBData.elementAt(3)).byteValue();
 
     this.DrawerKickoutConnectorPin3High = Boolean.valueOf((byte1 & 0x4) == 4);
     this.Online = Boolean.valueOf((byte1 & 0x8) == 0);
     this.CoverOpen = Boolean.valueOf((byte1 & 0x20) == 32);
     this.PaperFedByFeedButton = Boolean.valueOf((byte1 & 0x40) == 64);
     this.WaitingForOnlineRevovery = Boolean.valueOf((byte2 & 0x1) == 1);
     this.PaperFeedButtonIsTurnedOn = Boolean.valueOf((byte2 & 0x2) == 2);
     this.MechanicalError = Boolean.valueOf((byte2 & 0x4) == 4);
     this.AutoCutterError = Boolean.valueOf((byte2 & 0x8) == 8);
     this.UnrecoverableError = Boolean.valueOf((byte2 & 0x20) == 32);
     this.AutomaticallyRecoverableError = Boolean.valueOf((byte2 & 0x40) == 64);
     this.PaperNearEnd = Boolean.valueOf((byte3 & 0x3) == 3);
     this.PaperOut = Boolean.valueOf((byte3 & 0xC) == 12);
     this.PaperPresentAtTOFSensor = Boolean.valueOf((byte3 & 0x20) == 0);
     this.PaperPresentAtBOFSensor = Boolean.valueOf((byte3 & 0x40) == 0);
     this.SlipSelectedAsActiveSheet = Boolean.valueOf((byte4 & 0x1) == 0);
     this.CanPrintOnSlip = Boolean.valueOf((byte4 & 0x2) == 0);
 
     return GpCom.ERROR_CODE.SUCCESS;
   }
 
   Vector<Byte> getASBStatus()
   {
     return this.m_ASBStatusData;
   }
 
   public String toString()
   {
     String retval = String.format("%02X %02X %02X %02X", new Object[] { this.m_ASBStatusData.elementAt(0), this.m_ASBStatusData.elementAt(1), this.m_ASBStatusData.elementAt(2), this.m_ASBStatusData.elementAt(3) });
     return retval;
   }
 }
