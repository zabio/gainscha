package com.gprinter.sample;

import java.util.Vector;

import org.apache.commons.lang.ArrayUtils;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.gprinter.aidl.GpService;
import com.gprinter.command.EscCommand;
import com.gprinter.command.EscCommand.ENABLE;
import com.gprinter.command.EscCommand.FONT;
import com.gprinter.command.EscCommand.HRI_POSITION;
import com.gprinter.command.EscCommand.JUSTIFICATION;
import com.gprinter.command.GpCom;
import com.gprinter.command.TscCommand;
import com.gprinter.command.TscCommand.BARCODETYPE;
import com.gprinter.command.TscCommand.BITMAP_MODE;
import com.gprinter.command.TscCommand.DIRECTION;
import com.gprinter.command.TscCommand.EEC;
import com.gprinter.command.TscCommand.FONTMUL;
import com.gprinter.command.TscCommand.FONTTYPE;
import com.gprinter.command.TscCommand.MIRROR;
import com.gprinter.command.TscCommand.READABEL;
import com.gprinter.command.TscCommand.ROTATION;
import com.gprinter.io.GpDevice;
import com.gprinter.service.GpPrintService;
import com.sample.R;

public class MainActivity extends Activity {
	private GpService mGpService= null;
	public static final String CONNECT_STATUS = "connect.status";
	private static final String DEBUG_TAG = "MainActivity";
	private PrinterServiceConnection conn = null;
    private  int mPrinterIndex = 0;
	class PrinterServiceConnection implements ServiceConnection {
		@Override
		public void onServiceDisconnected(ComponentName name) {
			Log.i("ServiceConnection", "onServiceDisconnected() called");
			mGpService = null;
		}
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mGpService =GpService.Stub.asInterface(service);
		} 
	};
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		Log.e(DEBUG_TAG, "onResume");
	}
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Log.e(DEBUG_TAG, "onCreate");
		startService();
		connection();
	}
	private void startService() {
		Intent i= new Intent(this, GpPrintService.class);
		startService(i);
		 try {
             Thread.sleep(1000);
         } catch (InterruptedException e) {
             e.printStackTrace();
         }
	}
	private void connection() {
		conn = new PrinterServiceConnection();
		Intent intent = new Intent("com.gprinter.aidl.GpPrintService");
		bindService(intent, conn, Context.BIND_AUTO_CREATE); // bindService
	}
	public boolean[] getConnectState() {
		boolean[] state = new boolean[GpPrintService.MAX_PRINTER_CNT];
		for (int i = 0; i < GpPrintService.MAX_PRINTER_CNT; i++) {
			state[i] = false;
		}
		for (int i = 0; i < GpPrintService.MAX_PRINTER_CNT; i++) {
				try {
					if (mGpService .getPrinterConnectStatus(i) == GpDevice.STATE_CONNECTED) {
						state[i] = true;
					}
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		return state;
	}
	public void openPortDialogueClicked(View view) {
				Log.d(DEBUG_TAG, "openPortConfigurationDialog ");
				Intent intent = new Intent(this,
						PrinterConnectDialog.class);
				boolean[] state = getConnectState();
				intent.putExtra(CONNECT_STATUS, state);	
				this.startActivity(intent);
	}
	public void printTestPageClicked(View view) {
		try {
			int rel = mGpService.printeTestPage(mPrinterIndex); //
			Log.i("ServiceConnection", "rel " + rel);
			GpCom.ERROR_CODE r=GpCom.ERROR_CODE.values()[rel];
			if(r != GpCom.ERROR_CODE.SUCCESS){
				Toast.makeText(getApplicationContext(),GpCom.getErrorText(r),
						Toast.LENGTH_SHORT).show();	
		}
		} catch (RemoteException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	public void getPrinterStatusClicked(View view) {
		try {
			int status = mGpService.queryPrinterStatus(mPrinterIndex,500);
			String str = new String();
			if (status == GpCom.STATE_NO_ERR) {
				str = "打印机正常";
			} else if ((byte) (status & GpCom.STATE_OFFLINE) > 0) {
				str = "打印机脱机";
			} else if ((byte) (status & GpCom.STATE_PAPER_ERR) > 0) {
				str = "打印机缺纸";
			} else if ((byte) (status & GpCom.STATE_COVER_OPEN) > 0) {
				str = "打印机开盖";
			} else if ((byte) (status & GpCom.STATE_ERR_OCCURS) > 0) {
				str = "打印机出错";
			}
			Toast.makeText(getApplicationContext(),
					"打印机：" + '0' + " 状态：" + str, Toast.LENGTH_SHORT).show();
		} catch (RemoteException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	public void getPrinterCommandTypeClicked(View view) {
		try {
			int type = mGpService.getPrinterCommandType(mPrinterIndex);
			if (type == GpCom.ESC_COMMAND) {
				Toast.makeText(getApplicationContext(), "打印机使用ESC命令",
						Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(getApplicationContext(), "打印机使用TSC命令",
						Toast.LENGTH_SHORT).show();
			}
		} catch (RemoteException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	void sendReceipt(){
		EscCommand esc = new EscCommand();
		esc.addPrintAndFeedLines((byte)3);
		esc.addSelectJustification(JUSTIFICATION.CENTER);//设置打印居中
		esc.addSelectPrintModes(FONT.FONTA, ENABLE.OFF,ENABLE.ON, ENABLE.ON, ENABLE.OFF);//设置为倍高倍宽
		esc.addText("Sample\n");   //  打印文字
		esc.addPrintAndLineFeed();

		/*打印文字*/
		esc.addSelectPrintModes(FONT.FONTA, ENABLE.OFF,ENABLE.OFF, ENABLE.OFF, ENABLE.OFF);//取消倍高倍宽
		esc.addSelectJustification(JUSTIFICATION.LEFT);//设置打印左对齐
		esc.addText("Print text\n");   //  打印文字
		esc.addText("Welcome to use Gprinter!\n");   //  打印文字
		esc.addPrintAndLineFeed();
		/*打印图片*/
		esc.addText("Print bitmap!\n");   //  打印文字
		Bitmap b = BitmapFactory.decodeResource(getResources(),
				R.drawable.gprinter);
		esc.addRastBitImage(b,b.getWidth(),0);   //打印图片
	
		/*打印一维条码*/
		esc.addText("Print code128\n");   //  打印文字
		esc.addSelectPrintingPositionForHRICharacters(HRI_POSITION.BELOW);//设置条码可识别字符位置在条码下方
		esc.addSetBarcodeHeight((byte)60); //设置条码高度为60点
		esc.addCODE128("Gprinter");  //打印Code128码
		esc.addPrintAndLineFeed();
	
		/*QRCode命令打印
			此命令只在支持QRCode命令打印的机型才能使用。
			在不支持二维码指令打印的机型上，则需要发送二维条码图片
		*/
		esc.addText("Print QRcode\n");   //  打印文字	
		esc.addSelectErrorCorrectionLevelForQRCode((byte)0x31); //设置纠错等级
		esc.addSelectSizeOfModuleForQRCode((byte)3);//设置qrcode模块大小
		esc.addStoreQRCodeData("www.gprinter.com.cn");//设置qrcode内容
		esc.addPrintQRCode();//打印QRCode
		esc.addPrintAndLineFeed();
		
		/*打印文字*/
		esc.addSelectJustification(JUSTIFICATION.CENTER);//设置打印左对齐
		esc.addText("Completed!\r\n");   //  打印结束
		esc.addPrintAndFeedLines((byte)8);
		
		Vector<Byte> datas = esc.getCommand(); //发送数据
		Byte[] Bytes = datas.toArray(new Byte[datas.size()]);
		byte[] bytes = ArrayUtils.toPrimitive(Bytes);
		String str = Base64.encodeToString(bytes, Base64.DEFAULT);
		int rel;
		try {
			rel = mGpService.sendEscCommand(mPrinterIndex, str);
			GpCom.ERROR_CODE r=GpCom.ERROR_CODE.values()[rel];
			if(r != GpCom.ERROR_CODE.SUCCESS){
				Toast.makeText(getApplicationContext(),GpCom.getErrorText(r),
						Toast.LENGTH_SHORT).show();	
		          }			
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	void sendLabel(){
		TscCommand tsc = new TscCommand();
		tsc.addSize(60, 60); //设置标签尺寸，按照实际尺寸设置
		tsc.addGap(0);           //设置标签间隙，按照实际尺寸设置，如果为无间隙纸则设置为0
    	tsc.addDirection(DIRECTION.BACKWARD,MIRROR.NORMAL);//设置打印方向
    	tsc.addReference(0, 0);//设置原点坐标
     	tsc.addTear(ENABLE.ON); //撕纸模式开启
    	tsc.addCls();// 清除打印缓冲区
    	//绘制简体中文
     	tsc.addText(20,20,FONTTYPE.SIMPLIFIED_CHINESE,ROTATION.ROTATION_0,FONTMUL.MUL_1,FONTMUL.MUL_1,"Welcome to use Gprinter!");
     	//绘制图片
		Bitmap b = BitmapFactory.decodeResource(getResources(),
				R.drawable.gprinter);
		tsc.addBitmap(20,50, BITMAP_MODE.OVERWRITE, b.getWidth(),b);
		
		tsc.addQRCode(250, 80, EEC.LEVEL_L,5,ROTATION.ROTATION_0, " www.gprinter.com.cn");	
     	//绘制一维条码
     	tsc.add1DBarcode(20,250, BARCODETYPE.CODE128, 100, READABEL.EANBEL, ROTATION.ROTATION_0, "Gprinter");
    	tsc.addPrint(1,1); // 打印标签
    	tsc.addSound(2, 100); //打印标签后 蜂鸣器响
		Vector<Byte> datas = tsc.getCommand(); //发送数据
		Byte[] Bytes = datas.toArray(new Byte[datas.size()]);
		byte[] bytes = ArrayUtils.toPrimitive(Bytes);
		String str = Base64.encodeToString(bytes, Base64.DEFAULT);
		int rel;
		try {
			rel = mGpService.sendTscCommand(mPrinterIndex, str);
			GpCom.ERROR_CODE r=GpCom.ERROR_CODE.values()[rel];
			if(r != GpCom.ERROR_CODE.SUCCESS){
				Toast.makeText(getApplicationContext(),GpCom.getErrorText(r),
						Toast.LENGTH_SHORT).show();	
		          }			
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}	
	public void printReceiptClicked(View view) {
		try {
			int type = mGpService.getPrinterCommandType(mPrinterIndex);
			if (type == GpCom.ESC_COMMAND) {
				int status = mGpService.queryPrinterStatus(mPrinterIndex,500);  
				if (status == GpCom.STATE_NO_ERR) {
					sendReceipt();
				}
				else{
					Toast.makeText(getApplicationContext(),
							"打印机错误！", Toast.LENGTH_SHORT).show();		
				}
			}	
		} 
		catch (RemoteException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	public void printLabelClicked(View view) {
		try {
			int type = mGpService.getPrinterCommandType(mPrinterIndex);
			if (type == GpCom.TSC_COMMAND) {
				int status = mGpService.queryPrinterStatus(mPrinterIndex,500);  
				if (status ==GpCom.STATE_NO_ERR) {
					sendLabel();
				}
				else{
					Toast.makeText(getApplicationContext(),
							"打印机错误！", Toast.LENGTH_SHORT).show();		
				}
			}	
		} catch (RemoteException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	@Override
	public void onDestroy() {
		Log.e(DEBUG_TAG, "onDestroy");
		super.onDestroy();
		if (conn != null) {
			unbindService(conn); // unBindService
		}
	}

}
