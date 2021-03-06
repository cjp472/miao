package com.liuzhuni.lzn.core.login;

import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.google.gson.reflect.TypeToken;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;
import com.liuzhuni.lzn.R;
import com.liuzhuni.lzn.base.Base2Activity;
import com.liuzhuni.lzn.config.UrlConfig;
import com.liuzhuni.lzn.core.login.model.LoginModel;
import com.liuzhuni.lzn.core.login.ui.DoubleRightView;
import com.liuzhuni.lzn.core.model.BaseModel;
import com.liuzhuni.lzn.core.regist.RegistActivity;
import com.liuzhuni.lzn.core.select.ui.CleanableEditText;
import com.liuzhuni.lzn.sec.RsaEncode;
import com.liuzhuni.lzn.utils.PreferencesUtils;
import com.liuzhuni.lzn.utils.TextModify;
import com.liuzhuni.lzn.utils.ToastUtil;
import com.liuzhuni.lzn.utils.log.CommonLog;
import com.liuzhuni.lzn.utils.log.LogFactory;
import com.liuzhuni.lzn.volley.ApiParams;
import com.liuzhuni.lzn.volley.GsonBaseRequest;
import com.liuzhuni.lzn.volley.GsonRequest;

import java.util.HashMap;
import java.util.Map;

import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.framework.PlatformDb;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.sina.weibo.SinaWeibo;
import cn.sharesdk.tencent.qq.QQ;
import cn.sharesdk.wechat.friends.Wechat;

//Ctrl+Alt+T 代码块包裹
public class LoginActivity extends Base2Activity {

    private CommonLog log = LogFactory.createLog("login");

    private ButtonThread mThread;
//    private boolean mIsSend;//检测信号


//    private volatile boolean mQuit=false;//线程结束信号量


    @ViewInject(R.id.title_left)
    private TextView mBackTv;
    @ViewInject(R.id.title_right)
    private TextView mRegistTv;

    @ViewInject(R.id.submit_login)
    private TextView mSubmitTv;

    @ViewInject(R.id.forgot_login)
    private TextView mForgotTv;

    @ViewInject(R.id.tel_login)
    private CleanableEditText mTelEt;

    @ViewInject(R.id.passwd_login)
    private DoubleRightView mPasswdPv;

    @ViewInject(R.id.qq_login)
    private ImageView mQQIv;

    @ViewInject(R.id.weixin_login)
    private ImageView mWeixinIv;

    @ViewInject(R.id.sina_login)
    private ImageView mSinaIv;

//    private boolean isSend=true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
//        initData();
        findViewById();
//        initUI();
        setListener();
    }

    @Override
    protected void initData() {


        mThread = new ButtonThread(mSubmitTv, new Threadable() {
            @Override
            public boolean isSubmit() {
                return LoginActivity.this.isSubmit();
            }
        });


        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int screenHeight, screenWidth;
        screenHeight = dm.heightPixels;
        screenWidth = dm.widthPixels;

//        ToastUtil.showLong(this,
//                dm.toString() + "\n屏幕宽度像素:" + screenWidth + "px,换算dp：" + DensityUtil.px2dip(this,
//                        screenWidth));

//        log.i(dm.toString() + "++++++" + dm.densityDpi + "\n屏幕宽度像素:" + screenWidth + "px,换算dp：" + DensityUtil.px2dip(this,
//                screenWidth));


    }

    @Override
    protected void findViewById() {
        ViewUtils.inject(this);
    }

    @Override
    protected void initUI() {

        mBackTv.setText(getText(R.string.i_want_back));
        mThread.start();

    }

    @Override
    protected void setListener() {

    }


    protected boolean isSubmit() {
        String tel = mTelEt.getText().toString().trim();
        String passwd = mPasswdPv.getText().toString();

        return passwd.length() > 1 && (TextModify.getInstance().isTel(tel) || TextModify.getInstance().isEmail(tel));
    }


    protected void submit() {

        String tel = mTelEt.getText().toString().trim();
        String passwd = mPasswdPv.getText().toString();
        if (!TextModify.getInstance().isTel(tel) && !TextModify.getInstance().isEmail(tel)) {
            //邮箱手机校验
            ToastUtil.customShow(LoginActivity.this, getResources().getText(R.string.email_error));
            return;
        }


        if (passwd == null || passwd.length() < 1) {
            //密码校验
            ToastUtil.customShow(LoginActivity.this, getResources().getText(R.string.passwd_error));
            return;

        }
        String code = "";
        try {
            code = RsaEncode.encode(passwd);
        } catch (Exception e) {
            e.printStackTrace();
        }
        loadingdialog.show();
        pullLoginData(tel + "|" + code);
//        }
    }


    @Override
    protected void onResume() {
        super.onResume();

        mThread = null;
        initData();
        initUI();
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

    protected synchronized void pullLoginData(final String code) {

        executeRequest(new GsonBaseRequest<BaseModel<LoginModel>>(Request.Method.POST, UrlConfig.LOGIN, new TypeToken<BaseModel<LoginModel>>() {
        }.getType(), responseLoginListener(), errorListener()) {



            protected Map<String, String> getParams() {
                return new ApiParams().with("code", code);
            }

        });
    }

    private Response.Listener<BaseModel<LoginModel>> responseLoginListener() {
        return new Response.Listener<BaseModel<LoginModel>>() {
            @Override
            public void onResponse(BaseModel<LoginModel> loginModel) {
                loadingdialog.dismiss();
                if (loginModel.getRet() == 0) {
                    ToastUtil.customShow(LoginActivity.this, getResources().getText(R.string.toast_login));
                    PreferencesUtils.putBooleanToSPMap(LoginActivity.this, PreferencesUtils.Keys.IS_LOGIN, true);
                    PreferencesUtils.putValueToSPMap(LoginActivity.this, PreferencesUtils.Keys.AUTH, loginModel.getData().getAuthName(), PreferencesUtils.Keys.USERINFO);
                    PreferencesUtils.putValueToSPMap(LoginActivity.this, PreferencesUtils.Keys.TOKEN, loginModel.getData().getToken(), PreferencesUtils.Keys.USERINFO);
                    PreferencesUtils.putValueToSPMap(LoginActivity.this, PreferencesUtils.Keys.HEAD_URL, loginModel.getData().getPic(), PreferencesUtils.Keys.USERINFO);
                    PreferencesUtils.putValueToSPMap(LoginActivity.this, PreferencesUtils.Keys.NICKNAME, loginModel.getData().getName(), PreferencesUtils.Keys.USERINFO);
                    PreferencesUtils.putValueToSPMap(LoginActivity.this, PreferencesUtils.Keys.TEL, loginModel.getData().getPhone(), PreferencesUtils.Keys.USERINFO);
                    PreferencesUtils.putValueToSPMap(LoginActivity.this, PreferencesUtils.Keys.LEVEL, "Lv." + loginModel.getData().getGrade(), PreferencesUtils.Keys.USERINFO);
                    PreferencesUtils.putValueToSPMap(LoginActivity.this, PreferencesUtils.Keys.CITY, loginModel.getData().getCity(), PreferencesUtils.Keys.USERINFO);
                    PreferencesUtils.putIntToSPMap(LoginActivity.this, PreferencesUtils.Keys.UN_READ, loginModel.getData().getUnreadNum(), PreferencesUtils.Keys.USERINFO);
                    if (!PreferencesUtils.getBooleanFromSPMap(LoginActivity.this, PreferencesUtils.Keys.IS_SEND_SEX, PreferencesUtils.Keys.USERINFO)) {
                        pullSexData();
                    }
                    finish();
                } else {

                    ToastUtil.customShow(LoginActivity.this, loginModel.getMes());
                }

            }
        };

    }

    protected synchronized void pullSexData() {
        executeRequest(new GsonRequest<BaseModel>(Request.Method.POST, UrlConfig.SEX_SET, BaseModel.class, responseSexListener(), errorListener(false)) {

            protected Map<String, String> getParams() {
                return new ApiParams().with("v", "" + PreferencesUtils.getIntFromSPMap(LoginActivity.this, PreferencesUtils.Keys.SEX, PreferencesUtils.Keys.USERINFO));
            }

        });
    }

    private Response.Listener<BaseModel> responseSexListener() {
        return new Response.Listener<BaseModel>() {
            @Override
            public void onResponse(BaseModel sign) {
                if (sign.getRet() == 0) {
                    PreferencesUtils.putBooleanToSPMap(LoginActivity.this, PreferencesUtils.Keys.IS_SEND_SEX, true, PreferencesUtils.Keys.USERINFO);
                }

            }
        };

    }

    protected synchronized void pullThirdData(final String token, final String openid, final String pf) {
        executeRequest(new GsonBaseRequest<BaseModel<LoginModel>>(Request.Method.POST, UrlConfig.THIRD_LOGIN, new TypeToken<BaseModel<LoginModel>>() {
        }.getType(), responseThirdListener(), errorListener()) {
            protected Map<String, String> getParams() {
                return new ApiParams().with("token", token).with("openid", openid).with("pf", pf);
            }
        });
    }

    private Response.Listener<BaseModel<LoginModel>> responseThirdListener() {
        return new Response.Listener<BaseModel<LoginModel>>() {
            @Override
            public void onResponse(BaseModel<LoginModel> baseModel) {
                if (baseModel.getRet() == 0 && baseModel.getData() != null) {
                    ToastUtil.customShow(LoginActivity.this, getResources().getText(R.string.toast_login));
                    PreferencesUtils.putBooleanToSPMap(LoginActivity.this, PreferencesUtils.Keys.IS_LOGIN, true);
                    PreferencesUtils.putBooleanToSPMap(LoginActivity.this, PreferencesUtils.Keys.IS_THIRD, true, PreferencesUtils.Keys.USERINFO);
                    PreferencesUtils.putValueToSPMap(LoginActivity.this, PreferencesUtils.Keys.AUTH, baseModel.getData().getAuthName(), PreferencesUtils.Keys.USERINFO);
                    PreferencesUtils.putValueToSPMap(LoginActivity.this, PreferencesUtils.Keys.TOKEN, baseModel.getData().getToken(), PreferencesUtils.Keys.USERINFO);
                    PreferencesUtils.putValueToSPMap(LoginActivity.this, PreferencesUtils.Keys.HEAD_URL, baseModel.getData().getPic(), PreferencesUtils.Keys.USERINFO);
                    PreferencesUtils.putValueToSPMap(LoginActivity.this, PreferencesUtils.Keys.NICKNAME, baseModel.getData().getName(), PreferencesUtils.Keys.USERINFO);
                    PreferencesUtils.putValueToSPMap(LoginActivity.this, PreferencesUtils.Keys.TEL, baseModel.getData().getPhone(), PreferencesUtils.Keys.USERINFO);
                    PreferencesUtils.putValueToSPMap(LoginActivity.this, PreferencesUtils.Keys.LEVEL, "Lv." + baseModel.getData().getGrade(), PreferencesUtils.Keys.USERINFO);
                    PreferencesUtils.putIntToSPMap(LoginActivity.this, PreferencesUtils.Keys.UN_READ, baseModel.getData().getUnreadNum(), PreferencesUtils.Keys.USERINFO);

                    finish();
                } else {
                    ToastUtil.customShow(LoginActivity.this, baseModel.getMes());
                }

            }
        };

    }

    @OnClick(R.id.title_left)
    public void back(View v) {

        finish();
    }

    @OnClick(R.id.forgot_login)
    public void forgot(View v) {

        Intent forgotIntent = new Intent(this, RegistActivity.class);
        Bundle forgotBundle = new Bundle();
        forgotBundle.putBoolean("isRegister", false);
        forgotIntent.putExtras(forgotBundle);
        startActivity(forgotIntent);
    }


    @OnClick(R.id.submit_login)
    public void login(View v) {

        submit();
    }

    @OnClick(R.id.title_right)
    public void register(View v) {

        Intent registIntent = new Intent(this, RegistActivity.class);
        Bundle bundle = new Bundle();
        bundle.putBoolean("isRegister", true);
        registIntent.putExtras(bundle);
        startActivity(registIntent);
    }

    @OnClick(R.id.qq_login)
    public void qq_login(View v) {
        showLoadingDialog();
        ShareSDK.initSDK(this);

        Platform qq = ShareSDK.getPlatform(this, QQ.NAME);
        if (qq.isAuthValid()) {
            qq.removeAccount();//先清除在登录
        }
        qq.setPlatformActionListener(new PlatformActionListener() {
            @Override
            public void onComplete(Platform platform, int action, HashMap<String, Object> res) {
                DismissDialog();
                if (action == Platform.ACTION_USER_INFOR) {

                    PlatformDb platDB = platform.getDb();//获取数平台数据DB
                    //通过DB获取各种数据
                    log.i(platDB.getToken() + "=======" + platDB.getUserGender() + "=======" + platDB.getUserIcon() + "=======" + platDB.getUserId() + "=======" + platDB.getUserName());
                    pullThirdData(platDB.getToken(), platDB.getUserId(), "QQ");

                }

            }

            @Override
            public void onError(Platform platform, int i, Throwable throwable) {
                DismissDialog();
                ToastUtil.customShow(LoginActivity.this, getResources().getText(R.string.auth_error));

            }

            @Override
            public void onCancel(Platform platform, int i) {
                DismissDialog();

            }
        });


        //关闭SSO授权
        qq.SSOSetting(false);

//                qq.authorize();
        qq.showUser(null);//执行登录，登录后在回调里面获取用户资料
        //weibo.showUser(“3189087725”);//获取账号为“3189087725”的资料 null


    }


    @OnClick(R.id.weixin_login)
    public void weixin_login(View v) {

        showLoadingDialog();
        ShareSDK.initSDK(this);

        Platform weixin = ShareSDK.getPlatform(this, Wechat.NAME);

        if (weixin.isAuthValid()) {
            weixin.removeAccount();
        }
        weixin.setPlatformActionListener(new PlatformActionListener() {
            @Override
            public void onComplete(Platform platform, int action, HashMap<String, Object> res) {
                DismissDialog();

//                        if (action == Platform.ACTION_USER_INFOR) {

                PlatformDb platDB = platform.getDb();//获取数平台数据DB
                //通过DB获取各种数据
                log.i(platDB.getToken() + "=======" + platDB.getUserGender() + "=======" + platDB.getUserIcon() + "=======" + platDB.getUserId() + "=======" + platDB.getUserName());
                pullThirdData(platDB.getToken(), platDB.getUserId(), "Wechat");
//                        }

            }

            @Override
            public void onError(Platform platform, int i, Throwable throwable) {
                DismissDialog();
                ToastUtil.customShow(LoginActivity.this, getResources().getText(R.string.auth_error));
            }

            @Override
            public void onCancel(Platform platform, int i) {
                DismissDialog();

            }
        });
//                weixin.authorize();
        //关闭SSO授权
        weixin.SSOSetting(false);
//                weixin.authorize();
        weixin.showUser(null);
    }


    @OnClick(R.id.sina_login)
    public void sina_login(View v) {
        showLoadingDialog();
        ShareSDK.initSDK(this);

        Platform sina = ShareSDK.getPlatform(this, SinaWeibo.NAME);
        if (sina.isAuthValid()) {
            sina.removeAccount();
        }
        sina.setPlatformActionListener(new PlatformActionListener() {
            @Override
            public void onComplete(Platform platform, int action, HashMap<String, Object> res) {
                DismissDialog();

                if (action == Platform.ACTION_USER_INFOR) {

                    PlatformDb platDB = platform.getDb();//获取数平台数据DB
                    //通过DB获取各种数据
                    log.i(platDB.getToken() + "=======" + platDB.getUserGender() + "=======" + platDB.getUserIcon() + "=======" + platDB.getUserId() + "=======" + platDB.getUserName());
                    pullThirdData(platDB.getToken(), platDB.getUserId(), "SinaWeibo");

                }

            }

            @Override
            public void onError(Platform platform, int i, Throwable throwable) {
                DismissDialog();
                ToastUtil.customShow(LoginActivity.this, getResources().getText(R.string.auth_error));
            }

            @Override
            public void onCancel(Platform platform, int i) {
                DismissDialog();
            }
        });

        //true关闭SSO授权
        sina.SSOSetting(false);
        sina.showUser(null);
    }


}
