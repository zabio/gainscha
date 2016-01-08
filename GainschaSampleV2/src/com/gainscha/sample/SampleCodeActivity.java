package com.gainscha.sample;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Vector;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.res.Configuration;
import android.hardware.usb.UsbManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.gainscha.sample.R;
import com.gainscha.GpCom.CallbackInterface;
import com.gainscha.GpCom.GpCom;
import com.gainscha.GpCom.GpCom.ALIGNMENT;
import com.gainscha.GpCom.GpCom.ERROR_CODE;
import com.gainscha.GpCom.GpCom.FONT;
import com.gainscha.GpCom.GpCom.PORT_TYPE;
import com.gainscha.GpCom.GpComASBStatus;
import com.gainscha.GpCom.GpComCallbackInfo;
import com.gainscha.GpCom.GpComDevice;
import com.gainscha.GpCom.GpComDeviceParameters;
import com.gainscha.GpCom.USBPort;
import com.posin.filebrowser.FileBrowser;

public class SampleCodeActivity extends Activity implements CallbackInterface {

    protected static final String TAG = "SampleCodeActivity";
	//GpCom objects
    GpComDevice m_Device;
    GpComDeviceParameters m_DeviceParameters;
    GpComASBStatus m_ASBData;
    
    //Status Thread
 	Thread m_statusThread;
 	boolean m_bStatusThreadStop;

    //progress dialog
 	ProgressDialog m_progressDialog;

	@Override
	protected void onDestroy() {
		   if(m_Device.isDeviceOpen())
			   m_Device.closeDevice();
		   super.onDestroy();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sample_code);
		
        try
        {
        	m_progressDialog=null;
        	
        	//fill spinner with data
	        Spinner spinner = (Spinner) findViewById(R.id.spinnerPortType);
	        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.porttypes_array, android.R.layout.simple_spinner_item);
	        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	        spinner.setAdapter(adapter);
	        
	        spinner.setOnItemSelectedListener(new OnItemSelectedListener(){
				@Override
				public void onItemSelected(AdapterView<?> arg0, View arg1,
						int position, long id) {
					Log.d(TAG, "select "+position);
					if(position == 1) {
						((EditText)findViewById(R.id.editTextIPAddress)).setText("");
				    	USBPort.requestPermission(SampleCodeActivity.this);
					}
				}

				@Override
				public void onNothingSelected(AdapterView<?> arg0) {
				}
	        	
	        });
	        
        	//initialize GpCom
        	m_Device = new GpComDevice();
        	m_DeviceParameters = new GpComDeviceParameters();
        	
	        //register myself as callback
	        m_Device.registerCallback(this);
        	
        	//create and run status thread
        	createAndRunStatusThread(this);

/*        		if(usbManager.hasPermission(usbDevice)){
        		    //Other code
        		}else{
        		    //没有权限询问用户是否授予权限
        		    usbManager.requestPermission(usbDevice, pendingIntent); //该代码执行后，系统弹出一个对话框，
        		   //询问用户是否授予程序操作USB设
        		}
*/       		
        }
        catch(Exception e)
        {
			messageBox(this, "Exception: " + e.toString() + " - " + e.getMessage(), "onCreate Error");
        }
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.sample_code, menu);
		return true;
	}

    /**************************************************************************************
     * onConfigurationChanged
     * This is called when something changes, e.g. the orientation of the tablet 
     * @param newConfig
     * @see android.app.Activity#onConfigurationChanged(android.content.res.Configuration)
     **************************************************************************************/
    @Override
    public void onConfigurationChanged(Configuration newConfig) 
    {
      super.onConfigurationChanged(newConfig);
      //setContentView(R.layout.main);
      setContentView(R.layout.activity_sample_code);
      showASBStatus();
    }
    
    
    /**************************************************************************************
     * createAndRunStatusThread
     * 
     * @param act main activity
     **************************************************************************************/
    public void createAndRunStatusThread(final Activity act)
    {
        m_bStatusThreadStop=false;
		m_statusThread = new Thread(new Runnable() 
	   	{
		   	public void run() 
		   	{
		   		while(m_bStatusThreadStop==false)
		   		{
		   			try
		   			{
		   				//anything touching the GUI has to run on the Ui thread
		   		    	act.runOnUiThread(new Runnable()
	   			    	{
	   			    		public void run()
	   			    		{
	   			    	    	//set indicator background color
	   			    	        TextView tvOpen =(TextView)findViewById(R.id.textViewOpen);
	   			    	        TextView tvClosed =(TextView)findViewById(R.id.textViewClosed);
	   			    	        if(m_Device.isDeviceOpen())
	   			    	        {
	   			    	        	tvOpen.setBackgroundColor(0xFF00E000);		//green
	   			    	        	tvClosed.setBackgroundColor(0xFF707070);	//gray
	   			    	        }
	   			    	        else
	   			    	        {
	   			    	        	tvOpen.setBackgroundColor(0xFF707070);		//gray
	   			    	        	tvClosed.setBackgroundColor(0xFFE00000);	//red
	   			    	        }
	   			    		}
	   			    	});

		   				Thread.sleep(200);
		   			}
		   			catch(InterruptedException e)
		   			{
		   				m_bStatusThreadStop = true;
		   				messageBox(act, "Exception in status thread: " + e.toString() + " - " + e.getMessage(), "createAndRunStatusThread Error");
		   			}
		   		}
		   	}
	   	});
		m_statusThread.start();
    }
    
    
    /**************************************************************************************
     * openButtonClicked
     * 
     * @param view
     **************************************************************************************/
    public void openButtonClicked(View view)
    {
    	ERROR_CODE err = ERROR_CODE.SUCCESS;
    	
    	EditText editTextIPAddress = (EditText)findViewById(R.id.editTextIPAddress);
    	Spinner spinnerPortType = (Spinner)findViewById(R.id.spinnerPortType);
    	String selectedItem = (String)spinnerPortType.getSelectedItem();
    	
    	if(selectedItem.equals("Ethernet"))
    	{
	    	//fill in some parameters
	    	m_DeviceParameters.PortType = PORT_TYPE.ETHERNET;
	    	m_DeviceParameters.IPAddress = editTextIPAddress.getText().toString();
   	    	m_DeviceParameters.PortNumber = 9100;
    	}
    	else if(selectedItem.equals("USB"))
    	{
	    	//fill in some parameters
	    	m_DeviceParameters.PortType = PORT_TYPE.USB;
	    	m_DeviceParameters.PortName = editTextIPAddress.getText().toString(); 
	    	m_DeviceParameters.ApplicationContext = this;
    	}
    	else
    	{
			err = ERROR_CODE.INVALID_PORT_TYPE;
    	}
    	
    	if(err==ERROR_CODE.SUCCESS)
    	{
	    	//set the parameters to the device 
	    	err = m_Device.setDeviceParameters(m_DeviceParameters);
	    	if(err!=ERROR_CODE.SUCCESS)
	    	{
				String errorString = GpCom.getErrorText(err);
	    		messageBox(this, errorString, "SampleCode: setDeviceParameters Error");
	    	}
	    	
	    	if(err==ERROR_CODE.SUCCESS)
	    	{
	    		//open the device
	    		err = m_Device.openDevice();
	        	if(err!=ERROR_CODE.SUCCESS)
	        	{
	    			String errorString = GpCom.getErrorText(err);
			   		Log.d("SampleCode", "Error from openDevice: " + errorString);
	        		messageBox(this, errorString, "SampleCode 0: openDevice Error");
	        	}
	    	}
	    	
	    	if(err==ERROR_CODE.SUCCESS)
	    	{
	    		//activate ASB sending
	    		err = m_Device.activateASB(true, true, true, true, true, true);
	        	if(err!=ERROR_CODE.SUCCESS)
	        	{
	    			String errorString = GpCom.getErrorText(err);
			   		Log.d("SampleCode", "Error from activateASB: " + errorString);
	        		messageBox(this, errorString, "SampleCode 1: openDevice Error");
	        	}
	    	}
    	}    	
    }

    
    /**************************************************************************************
     * closeButtonClicked
     * 
     * @param view
     **************************************************************************************/
    public void closeButtonClicked(View view)
    {
    	ERROR_CODE err = m_Device.closeDevice();
    	if(err!=ERROR_CODE.SUCCESS)
    	{
			String errorString = GpCom.getErrorText(err);
    		messageBox(this, errorString, "closeDevice Error");
    	}
    	
    	//reset the ASB indicators
        TextView textViewASBOnline =(TextView)findViewById(R.id.textViewASBOnline);
        TextView textViewASBCover =(TextView)findViewById(R.id.textViewASBCover);
        TextView textViewASBPaper =(TextView)findViewById(R.id.textViewASBPaper);
        TextView textViewASBSlip =(TextView)findViewById(R.id.textViewASBSlip);
        textViewASBOnline.setBackgroundColor(0xFF707070);	//gray
        textViewASBCover.setBackgroundColor(0xFF707070);	//gray
        textViewASBPaper.setBackgroundColor(0xFF707070);	//gray
		textViewASBSlip.setBackgroundColor(0xFF707070);		//gray
    	
    }
    
    /**************************************************************************************
     * printStringButtonClicked
     * 
     * @param view
     **************************************************************************************/
    public void printStringButtonClicked(View view)
    {
    	ERROR_CODE err = ERROR_CODE.SUCCESS;
    	
    	FONT font = FONT.FONT_A;
    	Boolean bold = false;
    	Boolean underlined = false;
    	Boolean doubleHeight = false;
    	Boolean doubleWidth = false;
    	
    	try
    	{
    		//get UI elements
    		EditText editTextPrintString = (EditText)findViewById(R.id.editTextPrintString);
            RadioGroup radioGroupFont = (RadioGroup)findViewById(R.id.radioGroupFont);
            RadioGroup radioGroupDoublePrint = (RadioGroup)findViewById(R.id.radioGroupDoublePrint);
            RadioGroup radioGroupAlignment = (RadioGroup)findViewById(R.id.radioGroupAlignment);
            CheckBox checkBoxBold  = (CheckBox)findViewById(R.id.checkBoxBold);
            CheckBox checkBoxUnderlined  = (CheckBox)findViewById(R.id.checkBoxUnderlined);
    		
			//set font
	    	switch(radioGroupFont.getCheckedRadioButtonId())
	    	{
	    		case R.id.radioFontA:
	    	    	font = FONT.FONT_A;
	    	    	break;
	    		case R.id.radioFontB:
	    	    	font = FONT.FONT_B;
	    	    	break;
	    	}
	    	
	    	//set boldness
	    	if(checkBoxBold.isChecked())
	    	{
	            bold = true;
	    	}
	    	else
	    	{
	            bold = false;
	    	}
	    		
	    	//set underlined
	    	if(checkBoxUnderlined.isChecked())
	    	{
	            underlined = true;
	    	}
	    	else
	    	{
	            underlined = false;
	    	}
	    	
			//set double print
	    	switch(radioGroupDoublePrint.getCheckedRadioButtonId())
	    	{
	    		case R.id.radioX1Y1:
	    	        doubleHeight = false;
	    	        doubleWidth = false;
	    	    	break;
	    		case R.id.radioX2Y1:
	    	        doubleHeight = false;
	    	        doubleWidth = true;
	    	    	break;
	    		case R.id.radioX1Y2:
	    	        doubleHeight = true;
	    	        doubleWidth = false;
	    	    	break;
	    		case R.id.radioX2Y2:
	    	        doubleHeight = true;
	    	        doubleWidth = true;
	    	    	break;
	    	}
	    	
	    	if(m_Device.isDeviceOpen()==true)
	    	{
	    		//set print alignment
	        	switch(radioGroupAlignment.getCheckedRadioButtonId())
	        	{
	        		case R.id.radioLeft:
	        			err = m_Device.selectAlignment(ALIGNMENT.LEFT);
	    	    		if(err!=ERROR_CODE.SUCCESS)
	    	    		{
	    	    			String errorString = GpCom.getErrorText(err);
	    	        		messageBox(this, errorString, "printString Error");
	    	    		}
	        			break;
	        		case R.id.radioCenter:
	        			err = m_Device.selectAlignment(ALIGNMENT.CENTER);
	    	    		if(err!=ERROR_CODE.SUCCESS)
	    	    		{
	    	    			String errorString = GpCom.getErrorText(err);
	    	        		messageBox(this, errorString, "printString Error");
	    	    		}
	        			break;
	        		case R.id.radioRight:
	        			err = m_Device.selectAlignment(ALIGNMENT.RIGHT);
	    	    		if(err!=ERROR_CODE.SUCCESS)
	    	    		{
	    	    			String errorString = GpCom.getErrorText(err);
	    	        		messageBox(this, errorString, "printString Error");
	    	    		}
	        			break;
	        	}	        	
	        	
	        	if(err==ERROR_CODE.SUCCESS)
	        	{
		        	//print string
		    		String sendString = editTextPrintString.getText().toString();
		    		err = m_Device.printString(sendString, font, bold, underlined, doubleHeight, doubleWidth);
		    		if(err!=GpCom.ERROR_CODE.SUCCESS)
		    		{
		    			String errorString = GpCom.getErrorText(err);
		        		messageBox(this, errorString, "printString Error");
		    		}
	        	}
	    	}
	    	else
	    	{
	    		messageBox(this, "Device is not open", "printString Error");
	    	}
	    }
	    catch(Exception e)
	    {
			messageBox(this, "Exception: " + e.toString() + " - " + e.getMessage(), "printString Error");
	    }
    	
    }    

    /**************************************************************************************
     * curPaperButtonClicked
     * 
     * @param view
     **************************************************************************************/
    public void cutPaperButtonClicked(View view)
    {
    	ERROR_CODE err = ERROR_CODE.SUCCESS;

    	try
        {
	    	if(m_Device.isDeviceOpen()==true)
	    	{
	    		err = m_Device.cutPaper();
	    		if(err!=GpCom.ERROR_CODE.SUCCESS)
	    		{
	    			String errorString = GpCom.getErrorText(err);
	        		messageBox(this, errorString, "cutPaper Error");
	    		}	    		
	    	}
	    	else
	    	{
	    		messageBox(this, "Device is not open", "cutPaper Error");
	    	}
        }
        catch(Exception e)
        {
    		messageBox(this, "Exception:" + e.getMessage(), "cutPaper Error");
        }
    }    
    
    /**************************************************************************************
     * printTextFileButtonClicked
     * 
     * @param view
     **************************************************************************************/
    public void printTextFileButtonClicked(View view) 
    {
    	FileBrowser.chooseFile(this, ".txt", true, false);
    }
    
    String readTextFile(String filename) throws IOException 
    {
    	final StringBuilder sb = new StringBuilder();
    	final FileInputStream fs = new FileInputStream(new File(filename));
    	final BufferedReader br = new BufferedReader(new InputStreamReader(fs));
    	String s;
    	try {
	    	while((s = br.readLine()) != null)
	    	{
	    		sb.append(s);
	    		sb.append("\n");
	    	}
    	} finally {
    		if(br != null)
    			br.close();
    		if(fs != null)
    			fs.close();
    	}
    	return sb.toString();
    }
     
    void printTextFile(String filename) 
    {
    	ERROR_CODE err = ERROR_CODE.SUCCESS;

    	try
        {
    		String text = readTextFile(filename);
    		byte[] bs = text.getBytes("GB2312");

			if(m_Device.isDeviceOpen()==true)
	    	{
	    		Vector<Byte> data = new Vector<Byte>(bs.length);
	    		for(int i=0; i<bs.length; i++) {
	    			data.add(bs[i]);
	    		}
	    		
	    		err = m_Device.sendData(data);
	    		if(err!=GpCom.ERROR_CODE.SUCCESS)
	    		{
	    			String errorString = GpCom.getErrorText(err);
	        		messageBox(this, errorString, "cutPaper Error");
	    		}	    
	    		else {
		    		err = m_Device.cutPaper();
		    		if(err!=GpCom.ERROR_CODE.SUCCESS)
		    		{
		    			String errorString = GpCom.getErrorText(err);
		        		messageBox(this, errorString, "cutPaper Error");
		    		}
	    		}
	    	}
	    	else
	    	{
	    		messageBox(this, "Device is not open", "cutPaper Error");
	    	}
        }
        catch(Exception e)
        {
    		messageBox(this, "Exception:" + e.getMessage(), "cutPaper Error");
        }
    }
    
    @Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) 
    {
    	if(requestCode == FileBrowser.ACTIVITY_CODE && resultCode == Activity.RESULT_OK) {
			Bundle bundle = null;
			if (data != null && (bundle = data.getExtras()) != null) {
				if (!bundle.containsKey("file")) {
					return;
				}

				String fpath = bundle.getString("file");

				File f = new File(fpath);

				if (!f.isDirectory()) {
					printTextFile(f.getAbsolutePath());
				}
			}
    	}
		super.onActivityResult(requestCode, resultCode, data);
	}

	/**************************************************************************************
	 * CallbackMethod
	 * This, method will be called by the GpCom library when new data arrives from the device
	 * @param cbInfo object of type CallbackInfo to convey information into the callback
	 * @return GpCom.ERROR_CODE
     **************************************************************************************/
	public ERROR_CODE CallbackMethod(GpComCallbackInfo cbInfo)
	{
    	GpCom.ERROR_CODE retval = GpCom.ERROR_CODE.SUCCESS;
		
    	try
    	{
	    	switch(cbInfo.ReceivedDataType)
	    	{
	    		case GENERAL:
	    			//do nothing, ignore any general incoming data 
	    			break;
	    		case ASB:	//new ASB data came in
	    			Log.d("Sample", "new ASB data came in");
	    			receiveAndShowASBData();
	    			break;
	    	}
    	}
    	catch(Exception e)
    	{
    		messageBox(this, "callback method threw exception: " + e.toString() + " - " + e.getMessage(), "Callback Error");
    	}
    	
    	return retval;
	}
    
    
    /**************************************************************************************
     * receiveAndShowASBData
     * 
     **************************************************************************************/
    private void receiveAndShowASBData()
    {
		//retrieve current ASB data and show it
		m_ASBData = m_Device.getASB();
		showASBStatus();
    }

    
    /**************************************************************************************
     * showASBStatus
     * 
     **************************************************************************************/
    private void showASBStatus()
    {
		try
		{
	    	this.runOnUiThread(
				new Runnable()
				{
					public void run()
					{
						//get the status indicators from the GUI
		    	        TextView textViewASBOnline =(TextView)findViewById(R.id.textViewASBOnline);
		    	        TextView textViewASBCover =(TextView)findViewById(R.id.textViewASBCover);
		    	        TextView textViewASBPaper =(TextView)findViewById(R.id.textViewASBPaper);
		    	        TextView textViewASBSlip =(TextView)findViewById(R.id.textViewASBSlip);

						if(m_ASBData!=null)
						{
							//light up indicators
							if(m_ASBData.Online)
							{
			    	        	textViewASBOnline.setBackgroundColor(0xFF00E000);	//green
							}
							else
							{
			    	        	textViewASBOnline.setBackgroundColor(0xFFE00000);	//red
							}
							if(m_ASBData.CoverOpen)
							{
								textViewASBCover.setBackgroundColor(0xFFE00000);	//red
							}
							else
							{
			    	        	textViewASBCover.setBackgroundColor(0xFF00E000);	//green
							}
							if(m_ASBData.PaperOut==true)
							{
								textViewASBPaper.setBackgroundColor(0xFFE00000);	//red
							}
							else
							{
								if(m_ASBData.PaperNearEnd==true)
								{
									textViewASBPaper.setBackgroundColor(0xFFE0E000);	//yellow
								}
								else
								{
									textViewASBPaper.setBackgroundColor(0xFF00E000);	//green
								}
							}
							if(m_ASBData.SlipSelectedAsActiveSheet)
							{
								textViewASBSlip.setBackgroundColor(0xFF00E000);	//green
							}
							else
							{
								textViewASBSlip.setBackgroundColor(0xFF707070);	//gray
							}
						}
					} //public void run()
				}); //this.runOnUiThread(
    	}
		catch(Exception e)
		{
    		messageBox(this, "receiveAndShowASBData threw exception: " + e.toString() + " - " + e.getMessage(), "ReceiveAndShowASBData Error");
		}
    }
    
    /**************************************************************************************
     * messageBox
     * Shows a standard message box with OK button. 
     * Note: the program execution does not stop!
     * @param context
     * @param message
     * @param title
     **************************************************************************************/
    public void messageBox(final Context context, final String message, final String title)
    {
    	this.runOnUiThread(
			new Runnable()
			{
				public void run()
				{
					final AlertDialog alertDialog = new AlertDialog.Builder(context).create();
					alertDialog.setTitle(title);
					alertDialog.setMessage(message);
					alertDialog.setButton("OK", new OnClickListener()
					{
						@Override
						public void onClick(DialogInterface dialog, int which) 
						{               
							alertDialog.cancel();    	
						}
					});    	
					alertDialog.show();    	
				}
			}
    	);
    }

}
