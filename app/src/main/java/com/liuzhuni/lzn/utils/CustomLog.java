/*
 * D.java
 * classes : com.cloud.app.debug.D
 * author Andrew Lee
 * V 1.0.0
 * Create at 2014年7月7日 下午2:24:10
 * Copyright: 2014 Interstellar Cloud Inc. All rights reserved.
 */
package com.liuzhuni.lzn.utils;


import android.content.Context;
import android.util.Log;
import android.widget.Toast;

/**
 * cn.icnt.dinners.debug.DebugUtil
 * @author Andrew Lee <br/>
 * create at 2014年7月7日 下午2:24:10
 */
public class CustomLog {
    public static final boolean DEBUG = false;
     
    public static void toast(Context context,String content){
        Toast.makeText(context, content, Toast.LENGTH_SHORT).show();
    }
    
    public static void v(String tag,String msg){
        if(DEBUG){
            Log.v(tag, msg);
        }
    }
     
    public static void d(String tag,String msg){
        if(DEBUG){
            Log.d(tag, msg);
        }
    }
    
    public static void i(String tag,String msg){
        if(DEBUG){
            Log.i(tag, msg);
        }
    }
    
    public static void w(String tag,String msg){
        if(DEBUG){
            Log.w(tag, msg);
        }
    }
    
    public static void e(String tag,String msg){
        if(DEBUG){
            Log.e(tag, msg);
        }
    }    
}