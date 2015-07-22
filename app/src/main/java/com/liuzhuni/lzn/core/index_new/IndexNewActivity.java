package com.liuzhuni.lzn.core.index_new;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.google.gson.reflect.TypeToken;
import com.igexin.sdk.PushManager;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;
import com.liuzhuni.lzn.R;
import com.liuzhuni.lzn.base.Base2Activity;
import com.liuzhuni.lzn.config.AppManager;
import com.liuzhuni.lzn.config.Check;
import com.liuzhuni.lzn.config.MessageWhat;
import com.liuzhuni.lzn.config.UrlConfig;
import com.liuzhuni.lzn.core.buylist.BuyActivity;
import com.liuzhuni.lzn.core.index.model.CountModel;
import com.liuzhuni.lzn.core.index_new.adapter.PickAdapter;
import com.liuzhuni.lzn.core.index_new.model.PickModel;
import com.liuzhuni.lzn.core.login.LoginActivity;
import com.liuzhuni.lzn.core.login.Loginable;
import com.liuzhuni.lzn.core.login.TheIntent;
import com.liuzhuni.lzn.core.model.BaseListModel;
import com.liuzhuni.lzn.core.model.BaseModel;
import com.liuzhuni.lzn.core.personInfo.InfoActivity;
import com.liuzhuni.lzn.core.select.SelectActivity;
import com.liuzhuni.lzn.core.siri.TextSiriActivity;
import com.liuzhuni.lzn.example.qr_codescan.MipcaActivityCapture;
import com.liuzhuni.lzn.pinHeader.PinnedSectionListView;
import com.liuzhuni.lzn.pinHeader.StickyLayout;
import com.liuzhuni.lzn.utils.PreferencesUtils;
import com.liuzhuni.lzn.utils.ToastUtil;
import com.liuzhuni.lzn.utils.fileHelper.CommonUtil;
import com.liuzhuni.lzn.utils.log.CommonLog;
import com.liuzhuni.lzn.utils.log.LogFactory;
import com.liuzhuni.lzn.volley.ApiParams;
import com.liuzhuni.lzn.volley.GsonBaseRequest;
import com.liuzhuni.lzn.volley.GsonRequest;
import com.liuzhuni.lzn.volley.RequestManager;
import com.liuzhuni.lzn.xList.XListViewNew;
import com.melnykov.fab.FloatingActionButton;
import com.umeng.update.UmengUpdateAgent;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class IndexNewActivity extends Base2Activity implements
        StickyLayout.OnGiveUpTouchEventListener,XListViewNew.IXListViewListener {

    private CommonLog log = LogFactory.createLog("index");

    @ViewInject(R.id.title_right_iv)
    private ImageView mRightIv;
    @ViewInject(R.id.index_person_info)
    private TextView mInfoIv;
    @ViewInject(R.id.index_people_say)
    private TextView mPeopleIv;
    @ViewInject(R.id.index_want_buy)
    private ImageView mWantBuyIv;
    @ViewInject(R.id.title_middle)
    private TextView mMiddleTv;
    @ViewInject(R.id.index_new_message)
    private TextView mMessageTv;
    @ViewInject(R.id.index_buy_list)
    private TextView mBuyTv;
    @ViewInject(R.id.want_buy_ll)
    private LinearLayout mBuyLl;
    @ViewInject(R.id.title)
    private View mTitleView;
    @ViewInject(R.id.index_rl)
    private RelativeLayout mRel;

    @ViewInject(R.id.expandablelist)
    private PinnedSectionListView pinListView;
    @ViewInject(R.id.sticky_layout)
    private StickyLayout stickyLayout;
    @ViewInject(R.id.fab)
    private FloatingActionButton fab;


    private int lastY;

    private boolean give=false;
    private boolean dayFlag=true;

    private PopupWindow window = null;
    private int screenWidth;

    private volatile boolean mIsExit = false;
    private volatile boolean mIsNetExit = false;//退出网络请求线程

    private volatile int mCurrentId = 0;
    private volatile int mOldId = -1;

    public LocationClient mLocationClient;
    public MyLocationListener mMyLocationListener;

    private SimpleDateFormat mDateFormat = new SimpleDateFormat("HH:mm");
    private String mTime;

    private final int TEXI_SIZE = 30;
    private Boolean isExit = false;// 双击退出


    private ImageLoader mImageLoader;
    private List<PickModel> mList = null;
    private List<PickModel> mOldList = null;
    private List<PickModel> mCurrentList = null;
    private PickAdapter mAdapter;
    private boolean isRefresh = true;

    private String mTag="";

    private int mTouchSlop;

    private final int REQUEST_CODE = 2;


    public Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case MessageWhat.SHOW_POP:

                    showPopup();
                    break;
            }

        }

    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UmengUpdateAgent.setUpdateOnlyWifi(false);
        UmengUpdateAgent.update(this);

        setContentView(R.layout.activity_index_new);
        PushManager.getInstance().initialize(this.getApplicationContext());
        mLocationClient = new LocationClient(this.getApplicationContext());
        mMyLocationListener = new MyLocationListener();
        mLocationClient.registerLocationListener(mMyLocationListener);

        initLoc();
        initData();
        findViewById();
        initUI();
        setListener();
        if (!Check.isNotFirst(this)) {
            startThread();
        }
    }


    protected void initLoc() {
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        option.setCoorType("bd09");
        option.setIsNeedAddress(true);
        option.setScanSpan(50000);
        mLocationClient.setLocOption(option);
        mLocationClient.start();
    }

    @Override
    protected void initData() {
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        screenWidth = dm.widthPixels;
        mImageLoader = RequestManager.getImageLoader();
        mList = new ArrayList<PickModel>();
        mList.add(0,new PickModel(-1));
//        mList.add(new IndexModel("http://i0.sinaimg.cn/blog/http/blog.sina.com.cn/s/U11486P346T8D333331F3034DT20150422094733.jpg", "小猫", "耳机", "2015-04-22 15:33:55"));
//        mList.add(new IndexModel("http://i0.sinaimg.cn/blog/http/blog.sina.com.cn/s/U11486P346T8D333331F3034DT20150422094733.jpg", "大雄", "5-13元耳机", "2015-04-22 15:48:41"));
        mAdapter = new PickAdapter(this,mList,mImageLoader);
        mTouchSlop = ViewConfiguration.get(this).getScaledTouchSlop();



    }

    @Override
    protected void findViewById() {
        ViewUtils.inject(this);

//        pinListView = (PinnedHeaderListView) findViewById(R.id.expandablelist);
//        stickyLayout = (StickyLayout)findViewById(R.id.sticky_layout);
//        fab = (FloatingActionButton) findViewById(R.id.fab);

    }

    @Override
    protected void initUI() {

//        mMiddleTv.setText(getResources().getText(R.string.the_name));
//        mPullList.setPullLoadEnabled(true);
//        mPullList.setScrollLoadEnabled(true);
//        mListView = mPullList.getRefreshableView();
//        mListView.setDivider(getResources().getDrawable(R.drawable.divide));
//        mListView.setDividerHeight(1);
//        mListView.setCacheColorHint(Color.TRANSPARENT);
//        mListView.setSelector(getResources().getDrawable(R.drawable.trans));
//        mListView.setVerticalScrollBarEnabled(true);
        fab.hide();
        mMiddleTv.setText(getText(R.string.the_name));
        pinListView.setPullLoadEnable(true);
        pinListView.setPullRefreshEnable(true);
        pinListView.setXListViewListener(this);
        pinListView.setAdapter(mAdapter);
        pullData(0,"","back");

        Date date = new Date();
        mTime = mDateFormat.format(date);

        sendClientId();

        stickyLayout.setOnGiveUpTouchEventListener(this);
        pinListView.setAdapter(mAdapter);
        fab.attachToListView(pinListView);


    }


    @Override
    protected void onResume() {
        super.onResume();
        mIsNetExit = false;

//        startNet();
        if (!CommonUtil.checkNetState(this)) {

            ToastUtil.customShow(this, getResources().getText(R.string.bad_net));
        }

        if (Check.isLogin(this)) {
            //登陆成功
            pullMessageData();

        } else {
            mMessageTv.setText(getResources().getText(R.string.new_message));
            mBuyTv.setText(getResources().getText(R.string.buy_list));

        }

    }

    /**
     * 设置添加屏幕的背景透明度
     * @param bgAlpha
     */
    public void backgroundAlpha(float bgAlpha)
    {
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.alpha = bgAlpha; //0.0-1.0
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        getWindow().setAttributes(lp);
    }

    protected void showPopup() {

        View contentView = LayoutInflater.from(this).inflate(
                R.layout.popup_window, null);

        ImageView boyIv = (ImageView) contentView.findViewById(R.id.pop_boy);
        ImageView girlIv = (ImageView) contentView.findViewById(R.id.pop_girl);
        window = new PopupWindow(contentView, screenWidth - getResources().getDimensionPixelOffset(R.dimen.pop_offset), LinearLayout.LayoutParams.WRAP_CONTENT, true);
        window.setTouchable(true);
        window.setOutsideTouchable(false);
//            window.showAsDropDown(mTitleView, 0, 20);
        window.showAtLocation(mRel, Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 260);
        backgroundAlpha(0.45f);
        boyIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backgroundAlpha(1.0f);
                window.dismiss();
                PreferencesUtils.putBooleanToSPMap(IndexNewActivity.this, PreferencesUtils.Keys.IS_FIRST, true);
                PreferencesUtils.putIntToSPMap(IndexNewActivity.this, PreferencesUtils.Keys.SEX, 0, PreferencesUtils.Keys.USERINFO);
            }
        });

//            if (window != null && window.isShowing()) {
//                window.dismiss();
//            }
        girlIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backgroundAlpha(1.0f);
                window.dismiss();
                PreferencesUtils.putBooleanToSPMap(IndexNewActivity.this, PreferencesUtils.Keys.IS_FIRST, true);
                PreferencesUtils.putIntToSPMap(IndexNewActivity.this, PreferencesUtils.Keys.SEX, 1, PreferencesUtils.Keys.USERINFO);
            }
        });


    }

    protected void startThread() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (!mIsExit) {
                    if (mRel.getVisibility() == View.VISIBLE && mRel.isShown()) {
                        mHandler.sendEmptyMessage(MessageWhat.SHOW_POP);
                        break;
                    } else {

                        try {
                            Thread.currentThread().sleep(200);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                }
            }
        }).start();

    }

    protected void startNet() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (!mIsNetExit) {
                    if (mCurrentId > mOldId) {
//                        pullData(mCurrentId);
                    }
                    try {
                        Thread.currentThread().sleep(10000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
            }
        }).start();


    }

    protected  void pullData(final int id,final String tags,final String way) {
        executeRequest(new GsonBaseRequest<BaseListModel<PickModel>>(Request.Method.POST, UrlConfig.GET_PICK, new TypeToken<BaseListModel<PickModel>>() {
        }.getType(), responseListener(), errorListener()) {

            protected Map<String, String> getParams() {
                return new ApiParams().with("id", ""+id).with("tags", mTag).with("way",way);
            }

        });
    }

    private Response.Listener<BaseListModel<PickModel>> responseListener() {
        return new Response.Listener<BaseListModel<PickModel>>() {
            @Override
            public void onResponse(BaseListModel<PickModel> indexBaseListModel) {
                if(indexBaseListModel.getRet()==0){

                    if (indexBaseListModel.getData() != null) {

                        mCurrentList = indexBaseListModel.getData();
                        if(isRefresh){
                            mList.addAll(1, mCurrentList);
                            pinListView.setRefreshTime(mTime);
                            Date date = new Date();
                            mTime = mDateFormat.format(date);
                        }else{

                            mList.addAll(mCurrentList);
                        }
                        mAdapter.notifyDataSetChanged();

                    }else{

                        if(!isRefresh){
                            ToastUtil.show(IndexNewActivity.this, getResources().getText(R.string.no_more_error));
                        }
                    }

                    //mlist  为空时 显示错误页面
                }

            }
        };

    }


    protected  void pullMessageData() {
        executeRequest(new GsonBaseRequest<BaseModel<CountModel>>(Request.Method.GET, UrlConfig.GET_MESSAGE_COUNT, new TypeToken<BaseModel<CountModel>>() {
        }.getType(), responseMessageListener(), errorMesListener()) {

        });
    }

    public Response.ErrorListener errorMesListener() {
        return new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                mMessageTv.setText(getResources().getText(R.string.new_message));
                mBuyTv.setText(getResources().getText(R.string.buy_list));

            }
        };
    }

    private Response.Listener<BaseModel<CountModel>> responseMessageListener() {
        return new Response.Listener<BaseModel<CountModel>>() {
            @Override
            public void onResponse(BaseModel<CountModel> indexcountModel) {
                if (indexcountModel.getRet() == 0) {
                    SpannableString spanMessage = new SpannableString("" + indexcountModel.getData().getPushs());
                    spanMessage.setSpan(new AbsoluteSizeSpan(TEXI_SIZE, true), 0, spanMessage.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);//第二个参数boolean dip，如果为true，表示前面的字体大小单位为dip，否则为像素，同上。
                    mMessageTv.setText(spanMessage);
                    mMessageTv.append(getResources().getText(R.string.new_message_enter));

                    SpannableString spanBuy = new SpannableString("" + indexcountModel.getData().getShoplists());
                    spanBuy.setSpan(new AbsoluteSizeSpan(TEXI_SIZE, true), 0, spanBuy.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    mBuyTv.setText(spanBuy);
                    mBuyTv.append(getResources().getText(R.string.buy_list_enter));


                } else {
                    mMessageTv.setText(getResources().getText(R.string.new_message));
                    mBuyTv.setText(getResources().getText(R.string.buy_list));
                }

            }
        };

    }

    protected  void pullDayData() {  //签到

        executeRequest(new GsonRequest<BaseModel>(Request.Method.POST, UrlConfig.USER_SIGN, BaseModel.class, responseDayListener(), errorListener()) {

        });

    }

    public Response.ErrorListener errorDayListener() {
        return new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
//                Toast.makeText(activity,getResources().getText(R.string.error_retry), Toast.LENGTH_LONG).show();
                loadingdialog.dismiss();
                dayFlag=true;
                if (error.networkResponse != null) {
                    if (error.networkResponse.statusCode == 401) {//重新登录
                        PreferencesUtils.putBooleanToSPMap(IndexNewActivity.this, PreferencesUtils.Keys.IS_LOGIN, false);
                        PreferencesUtils.clearSPMap(IndexNewActivity.this, PreferencesUtils.Keys.USERINFO);
                        Intent intent = new Intent(IndexNewActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }
            }
        };
    }

    private Response.Listener<BaseModel> responseDayListener() {
        return new Response.Listener<BaseModel>() {
            @Override
            public void onResponse(BaseModel sign) {
                dayFlag=true;
                if (sign.getRet() == 0) {

                    ToastUtil.customShow(IndexNewActivity.this, sign.getMes());

                } else {
                    ToastUtil.customShow(IndexNewActivity.this, sign.getMes());
                }

            }
        };

    }


    @Override
    protected void onPause() {
        super.onPause();
        mIsExit = true;
        mIsNetExit = true;//停止网络线程

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mLocationClient.isStarted()) {

            mLocationClient.stop();

        }
    }

    @Override
    protected void setListener() {

        pinListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

//                Toast.makeText(activity, ""+position, Toast.LENGTH_LONG).show();

                if (position != 1) {
                    Intent intent = new Intent();
                    intent.setClass(IndexNewActivity.this, DetailActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("id", "" + mList.get(position-1).getId());
                    bundle.putBoolean("isSelect", true);
                    intent.putExtras(bundle);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(IndexNewActivity.this, FilterActivity.class);
                    startActivityForResult(intent, REQUEST_CODE);
                }

            }


        });

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        switch (requestCode) {

            case REQUEST_CODE:
                if(resultCode==RESULT_OK){
                    mList.clear();
                    mList.add(0,new PickModel(-1));
                    mTag=data.getExtras().getString("tag");
//                    ToastUtil.customShow(this,data.getExtras().getString("tag"));
                    pullData(0,data.getExtras().getString("tag"),"back");
                }
                break;
        }
    }

    @OnClick(R.id.title_left)
    public void check(View v) {

        //签到
        if (!Check.isLogin(this)) {//没有登录
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);

        } else {

            if(dayFlag){
                dayFlag=false;
                pullDayData();
            }


        }


    }

    @OnClick(R.id.title_right_iv)
    public void qrCode(View v) {

        //签到
        //扫描条码
        Intent intent = new Intent();
        intent.setClass(IndexNewActivity.this, MipcaActivityCapture.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    @OnClick(R.id.index_person_info)
    public void info(View v) {

        //个人信息
        Intent intent = new Intent(IndexNewActivity.this, InfoActivity.class);
        startActivity(intent);
        finish();

//        showPopup();
    }

    @OnClick(R.id.index_people_say)
    public void allSay(View v) {

        //个人信息
        Intent intent = new Intent(IndexNewActivity.this, AllNewsActivity.class);
        startActivity(intent);
        finish();

//        showPopup();
    }

    @OnClick(R.id.fab)
    public void fab(View v) {
        pinListView.setSelection(0);
        fab.hide();

    }

    @OnClick(R.id.index_new_message)
    public void message(View v) {

        //消息
        TheIntent theIntent = new TheIntent(this, new Loginable() {
            @Override
            public void intent() {
                Intent intent = new Intent(IndexNewActivity.this, TextSiriActivity.class);
                startActivity(intent);
            }
        });
        theIntent.go();
    }

    @OnClick(R.id.index_buy_list)
    public void buyList(View v) {

        //购买清单
        TheIntent theIntent = new TheIntent(this, new Loginable() {
            @Override
            public void intent() {
                Intent intent = new Intent(IndexNewActivity.this, BuyActivity.class);
                startActivity(intent);
            }
        });
        theIntent.go();

    }

    @OnClick(R.id.want_buy_ll)
    public void wantBuy(View v) {
        //想买
        TheIntent theIntent = new TheIntent(this, new Loginable() {
            @Override
            public void intent() {
                Intent intent = new Intent(IndexNewActivity.this, SelectActivity.class);
                Bundle bundle = new Bundle();
                bundle.putBoolean("fromIndex", true);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
        theIntent.go();
    }

    @Override
    public boolean giveUpTouchEvent(MotionEvent event) {//StickyLayout中content何时放弃事件处理

//        int y = (int) event.getRawY();
//
//
//        switch (event.getAction()) {
//            case MotionEvent.ACTION_DOWN: {
//                lastY = y;
//                break;
//            }
//            case MotionEvent.ACTION_MOVE: {
//                int deltaY = y - lastY;
//                 if (deltaY<0&&Math.abs(deltaY)  >= mTouchSlop&&deltaY<0) {
//
//                    give=true;
//                 }
//                break;
//            }
//            case MotionEvent.ACTION_UP: {
//                int deltaY = y - lastY;
//                if (deltaY<0&&Math.abs(deltaY)  >= mTouchSlop) {
//
//                    give=true;
//                }
//                break;
//            }
//            default:
//                break;
//        }


//        event.getY()>stickyLayout.getOriginHeight()

        if(pinListView.getFirstVisiblePosition() == 2){
            fab.hide();
        }
        if (pinListView.getFirstVisiblePosition() == 0){
            View view0=pinListView.getChildAt(0);
            int height=view0.getMeasuredHeight();
            View view = pinListView.getChildAt(1);
            if (view != null && view.getTop() >= height&&stickyLayout.getHeaderHeight()==0) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onRefresh() {

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                isRefresh=true;

                if(mList.size()>=1){
                    pullData(mList.get(0).getId(),null,"back");
                }else{
                    pullData(0,"","back");
                }

                pinListView.stopRefresh();
            }
        },200);

    }

    @Override
    public void onLoadMore() {

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {

                isRefresh=false;
                if(mList.size()>=1) {
                    pullData(mList.get(mList.size() - 1).getId(), "", "forward");
                }else{
                    pullData(0,"","back");
                }
                pinListView.stopLoadMore();
            }
        },200);

    }


    public class MyLocationListener implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation bdLocation) {

            if (Check.isLogin(IndexNewActivity.this) && bdLocation.getAddrStr() != null) {
                pullAddresData(bdLocation.getProvince(), bdLocation.getCity(), bdLocation.getDistrict());
            }
            mLocationClient.stop();
        }
    }

    /**
     * 发送地址
     *
     * @param pro  省
     * @param city 市
     * @param area 区
     */
    protected  void pullAddresData(final String pro, final String city, final String area) {
        executeRequest(new GsonRequest<BaseModel>(Request.Method.POST, UrlConfig.POST_ADDRES, BaseModel.class, responseAddresListener(), errorListener(false)) {

            protected Map<String, String> getParams() {
                return new ApiParams().with("Province", pro).with("City", city).with("Area", area);
            }

        });
    }

    private Response.Listener<BaseModel> responseAddresListener() {
        return new Response.Listener<BaseModel>() {
            @Override
            public void onResponse(BaseModel addresModel) {

                //备用
            }

        };

    }


    protected void sendClientId() {

        if (Check.isLogin(this) && Check.isSendClientId(this) && !"".equals(PreferencesUtils.getValueFromSPMap(this, PreferencesUtils.Keys.CLIENTID, "", PreferencesUtils.Keys.USERINFO))) {//发送clientid,成功后 PreferencesUtils.putBooleanToSPMap(context, PreferencesUtils.Keys.IS_SEND_CLIENTID, true, PreferencesUtils.Keys.USERINFO);
            pullClientData(PreferencesUtils.getValueFromSPMap(this, PreferencesUtils.Keys.CLIENTID, "", PreferencesUtils.Keys.USERINFO));

        }

    }

    protected  void pullClientData(final String clientId) {
        executeRequest(new GsonRequest<BaseModel>(Request.Method.POST, UrlConfig.PUSH_CLIENT_ID, BaseModel.class, responseClientListener(), errorListener(false)) {

            protected Map<String, String> getParams() {
                return new ApiParams().with("id", clientId);
            }

        });
    }

    private Response.Listener<BaseModel> responseClientListener() {
        return new Response.Listener<BaseModel>() {
            @Override
            public void onResponse(BaseModel addresModel) {

                if (addresModel.getRet() == 0) {
                    PreferencesUtils.putBooleanToSPMap(IndexNewActivity.this, PreferencesUtils.Keys.IS_SEND_CLIENTID, true, PreferencesUtils.Keys.USERINFO);
                }

                //备用
            }

        };

    }



    /**
     * 自定义返回键的效果
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK) { // 返回键
            exitBy2Click();
        }
        return true;
    }

    private void exitBy2Click() {
        Timer tExit = null;
        if (isExit == false) {
            isExit = true; // 准备退出
            // Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
            ToastUtil.show(this, getResources()
                    .getString(R.string.back_to_exit));
            tExit = new Timer();
            tExit.schedule(new TimerTask() {
                @Override
                public void run() {
                    isExit = false; // 取消退出
                }
            }, 1000); // 如果2秒钟内没有按下返回键，则启动定时器取消掉刚才执行的任务

        } else {
//            MobclickAgent.onKillProcess(this);
            // finish();
            AppManager.getAppManager().finishAllActivity();
            AppManager.getAppManager().AppExit(this);
        }
    }




}