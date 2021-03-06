package com.liuzhuni.lzn.core.regist;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;
import com.liuzhuni.lzn.R;
import com.liuzhuni.lzn.base.Base2Activity;
import com.liuzhuni.lzn.core.login.ButtonThread;
import com.liuzhuni.lzn.core.login.Threadable;
import com.liuzhuni.lzn.core.login.ui.CleanNoEditText;
import com.liuzhuni.lzn.core.model.BaseModel;
import com.liuzhuni.lzn.core.personInfo.ui.LoginoutDialog;
import com.liuzhuni.lzn.utils.TextModify;
import com.liuzhuni.lzn.utils.ToastUtil;
import com.liuzhuni.lzn.utils.log.CommonLog;
import com.liuzhuni.lzn.utils.log.LogFactory;
import com.liuzhuni.lzn.volley.ApiParams;
import com.liuzhuni.lzn.volley.GsonRequest;

import java.util.Map;

public class RegistActivity extends Base2Activity {


    private CommonLog log= LogFactory.createLog("regist");

    @ViewInject(R.id.title_left)
    private TextView mBackTv;
    @ViewInject(R.id.title_right)
    private TextView mRightTv;
    @ViewInject(R.id.title_middle)
    private TextView mMiddleTv;
    @ViewInject(R.id.submit_regist)
    private TextView mSubmitTv;

    @ViewInject(R.id.tel_tips)
    private TextView mTelTips;
    @ViewInject(R.id.regist_et)
    private CleanNoEditText mTelEt;


    private ButtonThread mThread;

    private boolean mIsRegister;

    private LoginoutDialog mDialog;

    private String mTel;






    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_regist);
//        initData();
        if(getIntent()!=null){
            if(getIntent().getExtras()!=null){
                mIsRegister=getIntent().getExtras().getBoolean("isRegister")?true:false;

            }

        }
        findViewById();
        initUI();
//        setListener();
    }

    @Override
    protected void initData() {

        mThread=new ButtonThread(mSubmitTv,new Threadable() {
            @Override
            public boolean isSubmit() {
                return RegistActivity.this.isTel();
            }
        });

    }

    @Override
    protected void findViewById() {

        ViewUtils.inject(this);

    }

    @Override
    protected void initUI() {

        mBackTv.setText(getResources().getText(R.string.back));
        mRightTv.setVisibility(View.GONE);
        if(mIsRegister){
            mMiddleTv.setText(getResources().getString(R.string.regist));
        }else{
            mMiddleTv.setText(getResources().getString(R.string.find_passwd));
            mTelTips.setText(getResources().getString(R.string.regist_tel_tips));
        }



    }

    @Override
    protected void setListener() {
        mThread.start();

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) { // 返回键
            showDialog();
        }

        return true;
    }

    @OnClick(R.id.title_left)
    public void back(View v){

        showDialog();
    }

    @OnClick(R.id.submit_regist)
    public void submit(View v){

        String telNo=delSpace(mTelEt.getText().toString().trim());
        if (!TextModify.getInstance().isTel(telNo)){
            ToastUtil.customShow(RegistActivity.this, getResources().getText(R.string.tel_error));
            return;
        }
        mTel=telNo;


        Intent intent=new Intent(RegistActivity.this,SendCodeActivity.class);
        Bundle bundle=new Bundle();
        if(mIsRegister){
            bundle.putBoolean("isRegister",true);
        }else{
            bundle.putBoolean("isRegister",false);
        }
        bundle.putString("tel",mTel);
        intent.putExtras(bundle);
        startActivity(intent);

//        if(mIsRegister){
//            pullData(UrlConfig.SEND_CODE,telNo);
//        }else{
//            pullData(UrlConfig.SEND_FOGOT_CODE,telNo);
//        }


    }

    protected synchronized void pullData(final String url,final String tel){
        executeRequest(new GsonRequest<BaseModel>(Request.Method.POST, url,BaseModel.class,responseListener(),errorListener()){
            protected Map<String, String> getParams() {
                return new ApiParams().with("phone", tel);
            }
        });
    }

    private Response.Listener<BaseModel> responseListener() {
        return new Response.Listener<BaseModel>(){
            @Override
            public void onResponse(BaseModel baseModel) {
                if(baseModel.getRet()==0){

                    Intent intent=new Intent(RegistActivity.this,SendCodeActivity.class);
                    Bundle bundle=new Bundle();
                    if(mIsRegister){
                        bundle.putBoolean("isRegister",true);
                    }else{
                        bundle.putBoolean("isRegister",false);
                    }
                    bundle.putString("tel",mTel);
                    intent.putExtras(bundle);
                    startActivity(intent);

                }else{
                    ToastUtil.customShow(RegistActivity.this, baseModel.getMes());
                }

            }
        };

    }


    public boolean isTel(){
        String telNo=delSpace(mTelEt.getText().toString().trim());
        return TextModify.getInstance().isTel(telNo);
    }

    public String delSpace(String string){

        if(string.length()!=13){
            return "";
        }


        StringBuffer buffer = new StringBuffer();
        buffer.append(string);
        int index = 0;
        while (index < buffer.length()) {
            if (buffer.charAt(index) == ' ') {
                buffer.deleteCharAt(index);
            } else {

                index++;
            }
        }
        return buffer.toString();

    }

    public void showDialog(){

        if(mIsRegister){
            mDialog=new LoginoutDialog(this,getResources().getString(R.string.register_dialog));

            mDialog.mSubmit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mDialog.dismiss();
                    finish();
                }
            });
            mDialog.mCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mDialog.dismiss();
                }
            });

            mDialog.show();

        }else{

            finish();
        }


    }


    @Override
    protected void onResume() {
        super.onResume();

        mThread=null;
        initData();
        setListener();
    }

    @Override
    protected void onPause() {

        super.onPause();
        mThread.stopThread();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }



}
