package com.gainscha.sample;

import android.os.Bundle;
import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


public class TestPrintActivity extends Activity implements Handler.Callback, PrintUtils.PrintListener {


    private static final int MSG_ERROR = 0;
    private static final int MSG_OPENED = 1;
    private static final int MSG_STATUS = 2;
    private static final int MSG_PRINT = 3;
    private static final int MSG_FINISHED = 4;


    private Button mBtn;
    private TextView mTv;
    private Handler mHandler;

    private PrintUtils mPrintUtils;
    private boolean isDeviceOpened = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_print);
        mHandler = new Handler(this);
        mPrintUtils = new PrintUtils(this);
        mPrintUtils.initPrint(this);
        initView();
    }

    private void initView() {
        mBtn = (Button) findViewById(R.id.btn_print);
        mTv = (TextView) findViewById(R.id.tv_status);
        mBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                print();
            }
        });
        findViewById(R.id.btn_esc).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void print() {
        EditText et = (EditText) findViewById(R.id.et_text);
        final String str = et.getText().toString();
        if (TextUtils.isEmpty(str)) {
            Toast.makeText(this, "请输入文字", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isDeviceOpened) {
            mPrintUtils.print(str);
            //mHandler.obtainMessage(MSG_PRINT, str).sendToTarget();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPrintUtils.closeDevice();
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case MSG_ERROR:
                Toast.makeText(this, msg.obj.toString(), Toast.LENGTH_SHORT).show();
                break;
            case MSG_OPENED:
                isDeviceOpened = (boolean) msg.obj;
                mTv.setText(isDeviceOpened ? "已开启" : "未开启");
                break;
            case MSG_PRINT:
                mPrintUtils.print(msg.obj.toString());
                break;
            case MSG_STATUS:
                PrintUtils.Status s = (PrintUtils.Status) msg.obj;
                mTv.setText(s.name());
                break;
            case MSG_FINISHED:
                mTv.setText("打印完成");
                break;
        }
        return false;
    }

    @Override
    public void onError(String errorMessage) {
        mHandler.obtainMessage(MSG_ERROR, errorMessage).sendToTarget();
    }

    @Override
    public void onDeviceOpened(boolean isOpened) {
        mHandler.obtainMessage(MSG_OPENED, isOpened).sendToTarget();
    }

    @Override
    public void onStatus(PrintUtils.Status status) {
        mHandler.obtainMessage(MSG_STATUS, status).sendToTarget();

    }

    @Override
    public void onPrintFinished() {
        mHandler.obtainMessage(MSG_FINISHED).sendToTarget();

    }
}
