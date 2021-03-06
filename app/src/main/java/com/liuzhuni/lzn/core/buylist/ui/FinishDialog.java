package com.liuzhuni.lzn.core.buylist.ui;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.liuzhuni.lzn.R;

/**
 * Created by Andrew Lee on 2015/6/12.
 * E-mail:jieooo7@163.com
 * Date: 2015-06-12
 * Time: 11:46
 */
public class FinishDialog {

    private TextView mTitle;
    public TextView mItemOne;
    public TextView mItemTwo;
    public TextView mItemThree;


    private Dialog mDialog;

    public FinishDialog(Activity context, String title) {
        View view = LayoutInflater.from(context).inflate(R.layout.finish_dialog, null);
        mDialog = new Dialog(context, R.style.MyDialog);

        mDialog.setContentView(view);

        DisplayMetrics dm = new DisplayMetrics();
        context.getWindowManager().getDefaultDisplay().getMetrics(dm);
        Window dialogWindow = mDialog.getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.width = (int) (dm.widthPixels*0.85);
        lp.height = (int) (0.8 * lp.width);
        dialogWindow.setAttributes(lp);
        mDialog.setCanceledOnTouchOutside(true);
        mDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode,
                                 KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    dismiss();
                    return true;
                }
                return false;
            }
        });

        mTitle = (TextView) view.findViewById(R.id.finish_text_title);
        mItemOne = (TextView) view.findViewById(R.id.finish_text_one);
        mItemTwo = (TextView) view.findViewById(R.id.finish_text_two);
        mItemThree = (TextView) view.findViewById(R.id.finish_text_three);

        mTitle.setText(title);

    }

    public void show() {
        mDialog.show();
    }

    public void dismiss() {

        if (mDialog.isShowing()) {
            mDialog.dismiss();
        }
    }
}
