package com.gainscha.sample;

import android.content.Context;

import com.gainscha.GpCom.CallbackInterface;
import com.gainscha.GpCom.GpCom;
import com.gainscha.GpCom.GpComASBStatus;
import com.gainscha.GpCom.GpComCallbackInfo;
import com.gainscha.GpCom.GpComDevice;
import com.gainscha.GpCom.GpComDeviceParameters;
import com.gainscha.GpCom.USBPort;

import java.util.Vector;

/**
 * 仅适用于 USB口连接打印机
 * android 主板必须底层内置驱动
 * Created by henry  16/1/7.
 */
public class PrintUtils implements CallbackInterface {

    private Context mContext;
    private GpComDevice mDevice;
    //Status Thread
    private Thread mThread;
    private boolean isThreadStop;
    private Boolean isOpened = false;

    //-------style-------
    private GpCom.FONT mTextFont;
    private boolean mTextIsBold = false;
    private boolean mTextIsUnderline = false;
    private GpCom.ALIGNMENT mTextAlignment;
    private boolean mTextIsWidth_x2 = false;
    private boolean mTextIsHeight_x2 = false;
    private EncodeType mTextEncodeType;
    //-------style-------

    private PrintListener mListener;

    public enum Status {
        Online, CoverOpen, PaperOut, PaperNearEnd, SlipSelectedAsActiveSheet
    }

    public enum EncodeType {
        GBK, GB2312
    }

    /**
     * construct
     *
     * @param context context
     */
    public PrintUtils(Context context) {
        mContext = context;
    }


    /**
     * initPrint
     *
     * @param listener callback
     */
    public void initPrint(PrintListener listener) {
        this.mListener = listener;
        init();
    }

    /**
     * initDevice
     */
    private void init() {

        if (!isOpened) {
            USBPort.requestPermission(mContext);
            //initialize GpCom
            mDevice = new GpComDevice();
            //设置回调
            mDevice.registerCallback(this);
            //监听开启状态
            listenDeviceStatus();
            //开启设备
            openDevice();
        }
    }


    /**
     * OpenDevices
     * step1: init parameters
     * step2: set parameters
     * step3: open device
     * step3: activate ASB sending
     */
    public void openDevice() {

        GpCom.ERROR_CODE code = GpCom.ERROR_CODE.SUCCESS;
        //fill in some parameters

        GpComDeviceParameters parameters = new GpComDeviceParameters();

        parameters.PortType = GpCom.PORT_TYPE.USB;
        parameters.PortName = "";
        parameters.ApplicationContext = mContext;

        if (code == GpCom.ERROR_CODE.SUCCESS) {

            //set the parameters to the device
            code = mDevice.setDeviceParameters(parameters);
            if (code != GpCom.ERROR_CODE.SUCCESS) {
                String errorString = GpCom.getErrorText(code);
                showErrorMsg("setDeviceParameters Error\n" + errorString);
            }

            if (code == GpCom.ERROR_CODE.SUCCESS) {
                //open the device
                code = mDevice.openDevice();

                if (code != GpCom.ERROR_CODE.SUCCESS) {
                    String errorString = GpCom.getErrorText(code);
                    showErrorMsg("openDevice Error\n" + errorString);
                }
            }

            if (code == GpCom.ERROR_CODE.SUCCESS) {
                //activate ASB sending
                code = mDevice.activateASB(true, true, true, true, true, true);

                if (code != GpCom.ERROR_CODE.SUCCESS) {
                    String errorString = GpCom.getErrorText(code);
                    showErrorMsg("openDevice Error from activateASB:\n" + errorString);
                }
            }
        }
    }

    @Override
    public GpCom.ERROR_CODE CallbackMethod(GpComCallbackInfo gpComCallbackInfo) {
        GpCom.ERROR_CODE code = GpCom.ERROR_CODE.SUCCESS;

        try {
            switch (gpComCallbackInfo.ReceivedDataType) {
                case GENERAL:
                    //do nothing, ignore any general incoming data
                    break;
                case ASB:    //new ASB data came in
                    showASBStatus(mDevice.getASB());
                    break;
            }
        } catch (Exception e) {
            showErrorMsg("callback method threw exception: Callback Error");
        }

        return code;
    }


    /**
     * showASBStatus
     */
    private void showASBStatus(GpComASBStatus m_ASBData) {
        try {

            if (m_ASBData != null) {
                //light up indicators

                if (m_ASBData.Online) {
                    showStatus(Status.Online);
                }

                if (m_ASBData.CoverOpen) {
                    showStatus(Status.CoverOpen);
                }
                if (m_ASBData.PaperOut) {
                    showStatus(Status.PaperOut);
                } else {
                    if (m_ASBData.PaperNearEnd) {
                        showStatus(Status.PaperNearEnd);
                    }
                    if (m_ASBData.SlipSelectedAsActiveSheet) {
                        showStatus(Status.SlipSelectedAsActiveSheet);
                    }
                }
            }

        } catch (Exception e) {
            showErrorMsg("receiveAndShowASBData threw exception:ReceiveAndShowASBData Error");
        }


    }

    /**
     * 监听打印机开启与否
     */
    private void listenDeviceStatus() {
        isThreadStop = false;
        mThread = new Thread(new Runnable() {
            public void run() {
                while (!isThreadStop) try {
                    isOpened = mDevice.isDeviceOpen();

                    if (isListenerNotNull()) {
                        mListener.onDeviceOpened(isOpened);
                    }

                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    isThreadStop = true;
                    showErrorMsg("Exception in status thread: createAndRunStatusThread Error");
                }
            }
        });
        mThread.start();
    }

    /**
     * 打印
     */
    public void print(String text) {

        try {

            if (mDevice.isDeviceOpen()) {
                //设置默认样式
                GpCom.ERROR_CODE code = setDefaultTextStyle(GpCom.FONT.FONT_A, GpCom.ALIGNMENT.LEFT, EncodeType.GB2312, false, false, false, false);

                if (!isCodeSuccess(code)) {
                    String errorString = GpCom.getErrorText(code);
                    showErrorMsg("Error setTextStyle: " + errorString);
                }

                //print string
                if (isCodeSuccess(code)) {


                    byte[] bs = (text).getBytes(mTextEncodeType.name());


                    Vector<Byte> data = new Vector<>(bs.length);
                    for (int i = 0; i < bs.length; i++) {
                        data.add(bs[i]);
                    }


                    //打印输出必须满行
                    //换行符
                    byte lineFeed = (byte) (0x0A);

                    data.add(lineFeed);


                    code = mDevice.sendData(data);


                    if (isCodeSuccess(code)) {
                        //cut pager
                        cutPaper();

                        if (isListenerNotNull()) {
                            mListener.onPrintFinished();
                        }


                    } else {
                        String errorString = GpCom.getErrorText(code);
                        showErrorMsg("printString Error :" + errorString);
                    }

                }
            } else {
                showErrorMsg("Error printString Device is not open  ");
            }
        } catch (Exception e) {
            showErrorMsg("Error printString  Exception: " + e.toString() + " - " + e.getMessage());
        }


    }


    /**
     * 设置打印字体风格
     *
     * @param font 字体 A  B
     */
    public void setTextFont(GpCom.FONT font) {
        mTextFont = font;
    }


    /**
     * 设置字体是否加粗
     *
     * @param isBold true false
     */
    public void setTextBold(boolean isBold) {
        this.mTextIsBold = isBold;
    }


    /**
     * 设置字体下划线
     *
     * @param isUnderline true false
     */
    public void setTextUnderline(boolean isUnderline) {
        this.mTextIsUnderline = isUnderline;
    }


    /**
     * 设置2x大小字体
     *
     * @param width_x2  2倍宽
     * @param height_x2 2倍高
     */
    public void setText_x2(boolean width_x2, boolean height_x2) {
        this.mTextIsWidth_x2 = width_x2;
        this.mTextIsHeight_x2 = height_x2;
    }


    /**
     * 设置排列方式
     *
     * @param alignment 排列
     */
    public void setTextAlignment(GpCom.ALIGNMENT alignment) {
        mTextAlignment = alignment;
    }

    /**
     * 设置字体编码
     *
     * @param type exp: GBK
     */
    public void setTextEncodeType(EncodeType type) {
        this.mTextEncodeType = type;
    }

    /**
     * 设置默认风格
     *
     * @param font        字体
     * @param alignment   对齐
     * @param type        编码
     * @param isBold      加粗
     * @param isUnderline 下划线
     * @param isWidth_x2  2倍宽
     * @param isHeight_x2 2倍高
     * @return code
     */
    private GpCom.ERROR_CODE setDefaultTextStyle(GpCom.FONT font, GpCom.ALIGNMENT alignment, EncodeType type, boolean isBold, boolean isUnderline, boolean isWidth_x2, boolean isHeight_x2) {

        setTextFont(font);
        setTextBold(isBold);
        setTextUnderline(isUnderline);
        setTextAlignment(alignment);
        setTextEncodeType(type);
        setText_x2(isWidth_x2, isHeight_x2);

        GpCom.ERROR_CODE code;

        //set print alignment
        code = mDevice.selectAlignment(mTextAlignment);

        String command;

        //set font
        switch (mTextFont.ordinal()) {
            default:
            case 1:
                command = "ESC M 0";
                break;
            case 2:
                command = "ESC M 1";
                break;
        }

        if (isCodeSuccess(code)) {
            code = mDevice.sendCommand(command);
        }

        //set bold
        command = this.mTextIsBold ? "ESC E 1" : "ESC E 0";
        if (isCodeSuccess(code)) {
            code = mDevice.sendCommand(command);
        }
        command = this.mTextIsUnderline ? "ESC - 49" : "ESC - 48";
        if (isCodeSuccess(code)) {
            code = mDevice.sendCommand(command);
        }

        //set width height
        if (isCodeSuccess(code)) {
            int options1 = 0;
            if (this.mTextIsHeight_x2) {
                options1 |= 1;
            }

            if (this.mTextIsWidth_x2) {
                options1 |= 16;
            }
            command = String.format("GS ! %d", options1);
            code = mDevice.sendCommand(command);
        }

        return code;
    }

    /**
     * 切纸命令
     */
    private void cutPaper() {

        try {
            if (mDevice.isDeviceOpen()) {
                GpCom.ERROR_CODE code = mDevice.cutPaper();
                if (code != GpCom.ERROR_CODE.SUCCESS) {
                    String errorString = GpCom.getErrorText(code);
                    showErrorMsg("cutPaper Error\n" + errorString);
                }
            } else {
                showErrorMsg(" cutPaper Error :Device is not open ");
            }
        } catch (Exception e) {
            showErrorMsg(" cutPaper Error : Exception:" + e.getMessage());
        }
    }

    /**
     * closeDevice
     */
    public void closeDevice() {
        GpCom.ERROR_CODE err = mDevice.closeDevice();
        if (err != GpCom.ERROR_CODE.SUCCESS) {
            String errorString = GpCom.getErrorText(err);
            showErrorMsg("closeDevice Error" + errorString);
        }
    }

    /**
     * interface callback
     */
    public interface PrintListener {

        void onError(String errorMessage);

        void onDeviceOpened(boolean isOpened);

        void onStatus(Status status);

        void onPrintFinished();

    }

    private void showErrorMsg(String msg) {
        if (isListenerNotNull()) {
            mListener.onError(msg);
        }
    }

    private void showStatus(Status status) {
        if (isListenerNotNull()) {
            mListener.onStatus(status);
        }
    }

    private boolean isListenerNotNull() {
        return null != mListener;
    }

    private boolean isCodeSuccess(GpCom.ERROR_CODE code) {
        return code == GpCom.ERROR_CODE.SUCCESS;
    }


}
