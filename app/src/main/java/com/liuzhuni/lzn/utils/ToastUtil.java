package com.liuzhuni.lzn.utils;


import android.app.ProgressDialog;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.liuzhuni.lzn.R;

public class ToastUtil {
    private static Toast toast = null;
    public static int LENGTH_LONG = Toast.LENGTH_LONG;
    private static int LENGTH_SHORT = Toast.LENGTH_SHORT;

    /**
     * 普通文本消息提示
     * 
     * @param context
     * @param text
     */
    public static void show(Context context, CharSequence text) {
	// 创建一个Toast提示消息
	toast = Toast.makeText(context, text, LENGTH_SHORT);
	// 设置Toast提示消息在屏幕上的位置
	// toast.setGravity(Gravity.CENTER, 0, 0);
	// 显示消息
	toast.show();
    }
    public static void showLong(Context context, CharSequence text) {
    	// 创建一个Toast提示消息
    	toast = Toast.makeText(context, text, LENGTH_LONG);
    	// 设置Toast提示消息在屏幕上的位置
    	// toast.setGravity(Gravity.CENTER, 0, 0);
    	// 显示消息
    	toast.show();
    }


    public static void customShow(Context context, CharSequence text){

        toast=new Toast(context);

        View layout= LayoutInflater.from(context).inflate(R.layout.custom_toast,null);
        TextView textTv=(TextView)layout.findViewById(R.id.toast_tv);
        textTv.setText(text);

        toast.setGravity(Gravity.CENTER,0,0);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.show();



    }

    /**
     * 带图片消息提示
     * 
     * @param context
     * @param ImageResourceId
     * @param text
     */
    public static void showImg(Context context, int ImageResourceId, CharSequence text) {
	// 创建一个Toast提示消息
	toast = Toast.makeText(context, text, LENGTH_LONG);
	// 设置Toast提示消息在屏幕上的位置
	toast.setGravity(Gravity.CENTER, 0, 0);
	// 获取Toast提示消息里原有的View
	View toastView = toast.getView();
	// 创建一个ImageView
	ImageView img = new ImageView(context);
	img.setImageResource(ImageResourceId);
	// 创建一个LineLayout容器
	LinearLayout ll = new LinearLayout(context);
	// 向LinearLayout中添加ImageView和Toast原有的View
	ll.addView(img);
	ll.addView(toastView);
	// 将LineLayout容器设置为toast的View
	toast.setView(ll);
	// 显示消息
	toast.show();
    }

    private static ProgressDialog dialog;

    public static void showProgressDialog(Context context) {
	dialog = new ProgressDialog(context);
	dialog.setIcon(R.drawable.ic_launcher);
	dialog.setTitle(R.string.app_name);
	dialog.setMessage("请等候，数据加载中……");
	dialog.show();
    }

    public static void showProgressDialog(Context context, String str) {
	dialog = new ProgressDialog(context);
	dialog.setIcon(R.drawable.ic_launcher);
	dialog.setTitle(R.string.app_name);
	dialog.setMessage(str);
	dialog.show();
    }



    public static void closeProgressDialog() {
	if (dialog != null && dialog.isShowing()) {
	    dialog.dismiss();
	}
    }
}