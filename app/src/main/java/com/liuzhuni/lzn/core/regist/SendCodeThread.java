package com.liuzhuni.lzn.core.regist;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.widget.TextView;

import com.liuzhuni.lzn.R;
import com.liuzhuni.lzn.config.MessageWhat;
import com.liuzhuni.lzn.utils.log.CommonLog;
import com.liuzhuni.lzn.utils.log.LogFactory;

/**
 * Created by Andrew Lee on 2015/4/16.
 * E-mail:jieooo7@163.com
 * Date: 2015-04-16
 * Time: 17:21
 */
public class SendCodeThread extends Thread {


    private CommonLog log = LogFactory.createLog("sendcode");

    private TextView mSubmitTv;

    private Context mContext;

    private volatile boolean mQuit = false;
    private volatile boolean mSend = true;


    public Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MessageWhat.CODE_COUNT:
                    int count = (int) msg.obj;

                    if (count < 0) {
                        SendCodeThread.this.mSend = true;
                        mSubmitTv.setText(mContext.getResources().getString(R.string.send_again));
                        mSubmitTv.setBackgroundResource(R.drawable.button_select_back);
                        mSubmitTv.setTextColor(mContext.getResources().getColor(R.color.white));
                    } else {

                        SendCodeThread.this.mSend = false;
                        mSubmitTv.setText("" + count + mContext.getResources().getString(R.string.count));
                        mSubmitTv.setBackgroundResource(R.drawable.send_code_white);
                        mSubmitTv.setTextColor(mContext.getResources().getColor(R.color.regist_text));
                    }


                    break;
            }

        }

    };

    public SendCodeThread(Context context, TextView submitTv) {
        this.mContext = context;
        this.mSubmitTv = submitTv;
    }


    public void stopThread() {
        this.mQuit = true;

    }


    public boolean isSendAgain() {

        return this.mSend;
    }

    public void run() {

        int count = 90;
        while (!mQuit && count >= 0) {

                try {
                    this.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                count--;

                Message message = Message.obtain();
                message.obj = count;
                message.what = MessageWhat.CODE_COUNT;
                mHandler.sendMessage(message);

        }


    }

}
