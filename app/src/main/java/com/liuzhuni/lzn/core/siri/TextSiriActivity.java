package com.liuzhuni.lzn.core.siri;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;
import com.liuzhuni.lzn.R;
import com.liuzhuni.lzn.base.Base2Activity;
import com.liuzhuni.lzn.config.MessageWhat;
import com.liuzhuni.lzn.config.UrlConfig;
import com.liuzhuni.lzn.core.buylist.BuyActivity;
import com.liuzhuni.lzn.core.goods.GoodsActivity;
import com.liuzhuni.lzn.core.model.BaseListModel;
import com.liuzhuni.lzn.core.select.SelectActivity;
import com.liuzhuni.lzn.core.select.model.SelectGoodsModel;
import com.liuzhuni.lzn.core.siri.adapter.SiriAdapter;
import com.liuzhuni.lzn.core.siri.model.GoodsListModel;
import com.liuzhuni.lzn.core.siri.model.SiriModel;
import com.liuzhuni.lzn.db.DataBaseHelper;
import com.liuzhuni.lzn.db.DatabaseOperate;
import com.liuzhuni.lzn.db.DbModel;
import com.liuzhuni.lzn.example.qr_codescan.MipcaActivityCapture;
import com.liuzhuni.lzn.utils.PreferencesUtils;
import com.liuzhuni.lzn.utils.TimeUtil;
import com.liuzhuni.lzn.utils.ToastUtil;
import com.liuzhuni.lzn.volley.ApiParams;
import com.liuzhuni.lzn.volley.GsonBaseRequest;
import com.liuzhuni.lzn.volley.RequestManager;
import com.liuzhuni.lzn.xList.XListView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class TextSiriActivity extends Base2Activity implements XListView.IXListViewListener {

    @ViewInject(R.id.title_left)
    private TextView mBackTv;
    @ViewInject(R.id.title_right_iv)
    private ImageView mRightIv;
    @ViewInject(R.id.title_middle)
    private TextView mMiddleTv;
    @ViewInject(R.id.siri_buy_list)
    private TextView mWantBuyTv;


    @ViewInject(R.id.siri_i_want_buy)
    private TextView mBuyTv;
    @ViewInject(R.id.siri_list)
    private XListView mListView;


    private SQLiteDatabase db;
    private DataBaseHelper helper;

    private SimpleDateFormat mDateFormat = new SimpleDateFormat("HH:mm");
    private String mTime;


    private int mTotal = 1;
    private int mIndex = 0;

    private int back = 0;
    private int dbBack = 0;
    private int forward = 0;
    //    private int mUnRead = 0;

    private List<DbModel> mDbList = null;
    /**
     * 退出线程状态量
     */
    private boolean mIsExit = false;
    private boolean mIsPullDown=false;


    private boolean mIsFirst = true;

    private boolean fromSelect=false;

    private boolean isHisScroll=true;  // 设置滚动,历史记录 无动作 第一次 进入 到最后

    private static int position=0;
    public static int addNew=0;


    private String mKey;
    private String mBrand;
    private String mPrice;
    private String mIds;

    private boolean isSmoth=false;


//    private Thread mThread=null;
    private SiriAdapter mAdapter;
    private ImageLoader mImageLoader;
    private List<SiriModel> mList;
    private List<SiriModel> mCurrentList = new ArrayList<SiriModel>();
    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case MessageWhat.SIRI_DELAY:
                    mAdapter.notifyDataSetChanged();
//                    mListView.setSelection(mCurrentList.size() - 1);
                    mListView.smoothScrollToPosition(mCurrentList.size());
                    break;
            }

        }

    };


    public static final int REQUEST_CODE = 1;
    private final static int SCANNIN_GREQUEST_CODE = 2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_siri);
        findViewById();
        initData();
        initUI();
        setListener();
        mListView.setSelection(mListView.getAdapter().getCount()-1); //滑动到最后
    }

    @Override
    protected void initData() {
        mImageLoader = RequestManager.getImageLoader();
        addNew=0;
        mIsExit=false;
        position=0;
        isHisScroll=true;


        Date date = new Date();
        mTime = mDateFormat.format(date);

        helper = new DataBaseHelper(this, "goods_" + PreferencesUtils.getValueFromSPMap(this, PreferencesUtils.Keys.AUTH, "", PreferencesUtils.Keys.USERINFO).hashCode() + ".db");
        db = helper.getWritableDatabase();

        mList = new ArrayList<SiriModel>();


        Thread mThread = new Thread(new Runnable() {//不操作mlist则 不会使用线程  添加控制即可
            @Override
            public void run() {  //延时处理


                while (!mIsExit) {

                    try {
                        if (position < mList.size() && mList.get(position) != null) {
                            Thread.currentThread().sleep(mList.get(position).getDelayTime());
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (position < mList.size() && mList.get(position) != null) {
//                    mCurrentList.add(mList.get(position));

                        addList(mList.get(position));
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {

                                mAdapter.notifyDataSetChanged();

                                if(isSmoth){
                                    mListView.setSelection(mCurrentList.size() - 1);
                                    isSmoth=false;
                                }else{
                                    mListView.smoothScrollToPosition(mCurrentList.size());
                                }
                            }
                        });

//                        mHandler.sendEmptyMessage(MessageWhat.SIRI_DELAY);


                    }
                }
            }
        });

        mThread.start();

        mAdapter = new SiriAdapter(mCurrentList, mList, this, mImageLoader,db);//mlist 实际掌控 在adapter中有操作  mCurrentlist用于模拟
        mListView.setCacheColorHint(Color.TRANSPARENT);
        mListView.setDivider(getResources().getDrawable(R.drawable.trans));
        mListView.setDividerHeight(12);
        mListView.setSelector(getResources().getDrawable(R.drawable.trans));
        mListView.setPullLoadEnable(false);
        mListView.setXListViewListener(this);

        mListView.setAdapter(mAdapter);

        mDbList = DatabaseOperate.getDb(db,dbBack++,addNew);
//        ++back;
        Collections.reverse(mDbList);//反转
        fromDb(mDbList, false, true);// 读数据库

        if(getIntent()!=null){//搜索页面跳转
            if(getIntent().getExtras()!=null){
                fromSelect=true;// from select start 标志从select界面过来的
                mBrand=getIntent().getExtras().getString("brand");
                mKey=getIntent().getExtras().getString("key");
                mPrice=getIntent().getExtras().getString("price");
                mIds=getIntent().getExtras().getString("id");

                if(!PreferencesUtils.getBooleanFromSPMap(this, PreferencesUtils.Keys.IS_DIALOG)) {
                    mList.add(new SiriModel<String>(SiriAdapter.LEFT_TEXT, getResources().getString(R.string.dialog_first)));
                    mList.add(new SiriModel<String>(SiriAdapter.RIGHT_TEXT_CLICK, getResources().getString(R.string.dialog_second)));
                    insertDb(db,SiriAdapter.LEFT_TEXT,0,0,TextSiriActivity.this.getResources().getString(R.string.dialog_first));
                    insertDb(db,SiriAdapter.RIGHT_TEXT_CLICK,0,0,TextSiriActivity.this.getResources().getString(R.string.dialog_second));
                    PreferencesUtils.putBooleanToSPMap(this, PreferencesUtils.Keys.IS_DIALOG, true);
                }


                mList.add(new SiriModel<String>(SiriAdapter.RIGHT_TEXT,0, getResources().getString(R.string.dialog_third)+" ["+mKey+"]",0));
                insertDb(db, SiriAdapter.RIGHT_TEXT, 0, 0, getResources().getString(R.string.dialog_third)+" ["+mKey+"]");

                mList.add(new SiriModel<String>(SiriAdapter.LEFT_TEXT, getResources().getString(R.string.dialog_fou)));
                insertDb(db,SiriAdapter.LEFT_TEXT,0,0,getResources().getString(R.string.dialog_fou));

                pullSubmitData(mKey, mBrand, mPrice,mIds);

                //同选择返回

            }
        }

        if(!fromSelect){//刷新
            if(back>=0){
                pullHistoryData(back, "back");
            }else{
                ToastUtil.show(TextSiriActivity.this, getResources().getText(R.string.no_more_error));
            }
        }



    }

    @Override
    protected void findViewById() {

        ViewUtils.inject(this);

    }

    @Override
    protected void initUI() {
        mMiddleTv.setText(getResources().getText(R.string.secreter));
        mRightIv.setVisibility(View.GONE);
        mRightIv.setImageDrawable(getResources().getDrawable(R.drawable.ic_saoyisao));
    }

    @Override
    protected void setListener() {

    }

    protected synchronized void addList(SiriModel model) {

        mCurrentList.add(model);
        ++position;

    }

    protected synchronized void addList(List<SiriModel> model) {

        mCurrentList.addAll(0, model);


    }
    protected synchronized void addListLast(List<SiriModel> model) {

        mCurrentList.addAll(model);


    }

    protected synchronized void clear() {
        mCurrentList.clear();
    }


    protected synchronized void pullHistoryData(final int id, final String way) {//推送
        executeRequest(new GsonBaseRequest<BaseListModel<DbModel>>(Request.Method.GET, UrlConfig.GET_DIALOG + id + "&way=" + way, new TypeToken<BaseListModel<DbModel>>() {
        }.getType(), responseHisListener(), errorHisListener()) {
        });
    }


    public Response.ErrorListener errorHisListener() {
        return new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                mListView.stopRefresh();
                mListView.stopLoadMore();
            }
        };
    }

    private Response.Listener<BaseListModel<DbModel>> responseHisListener() {
        return new Response.Listener<BaseListModel<DbModel>>() {
            @Override
            public void onResponse(BaseListModel<DbModel> historyModel) {

                mListView.stopRefresh();
                mListView.stopLoadMore();

                back = historyModel.getBack();
                forward = historyModel.getForward();
                if (historyModel.getRet() == 0) {

                    if (historyModel.getData() != null) {
                        Collections.reverse(historyModel.getData());
                        fromDbNoDelay(historyModel.getData(), true, false);


                    }

                }
            }
        };

    }

    protected void fromDb(List<DbModel> dbList, boolean isDialog, boolean isHistory) {//无延时处理

        final List<SiriModel> tempList = new ArrayList<SiriModel>();
        if(!PreferencesUtils.getBooleanFromSPMap(this, PreferencesUtils.Keys.IS_DIALOG)) {
            mList.add(new SiriModel<String>(SiriAdapter.LEFT_TEXT, getResources().getString(R.string.dialog_first)));
            mList.add(new SiriModel<String>(SiriAdapter.RIGHT_TEXT_CLICK, getResources().getString(R.string.dialog_second)));
            insertDb(db,SiriAdapter.LEFT_TEXT,0,0,TextSiriActivity.this.getResources().getString(R.string.dialog_first));
            insertDb(db,SiriAdapter.RIGHT_TEXT_CLICK,0,0,TextSiriActivity.this.getResources().getString(R.string.dialog_second));
            PreferencesUtils.putBooleanToSPMap(this, PreferencesUtils.Keys.IS_DIALOG, true);
        }
        if (dbList != null) {
            for (DbModel dbModel : dbList) {
//                tempList.add(new SiriModel<String>(SiriAdapter.TIME, dbModel.getDate()));

                switch(dbModel.getType()){
                    case SiriAdapter.RIGHT_TEXT:
                    case SiriAdapter.LEFT_TEXT:
                        if(!TextUtils.isEmpty(dbModel.getBody())){
                            tempList.add(new SiriModel<String>(dbModel.getType(), dbModel.getBody()));
                        }
                        break;
                    case SiriAdapter.RIGHT_TEXT_CLICK:
                        if(!TextUtils.isEmpty(dbModel.getBody())){
                            tempList.add(new SiriModel<String>(SiriAdapter.RIGHT_TEXT_CLICK, dbModel.getBody()));
                        }
                        break;

                    case SiriAdapter.TIME:

                        if(dbModel.getDate()!=0){
                            if(!TextUtils.isEmpty(TimeUtil.getInstance().timeFormat(dbModel.getDate()*1000))){
                                tempList.add(new SiriModel<String>(SiriAdapter.TIME,TimeUtil.getInstance().timeFormat(dbModel.getDate()*1000)));
                            }
                        }

                        break;
                    case SiriAdapter.RIGHT_GOODS:
                        ArrayList<SelectGoodsModel> goodsList = new Gson().fromJson(dbModel.getBody(), new TypeToken<ArrayList<SelectGoodsModel>>() {
                        }.getType());

                        tempList.add(new SiriModel<GoodsListModel>(SiriAdapter.RIGHT_GOODS,dbModel.getBody_id(), new GoodsListModel(goodsList),0,(int)dbModel.getDate()));

                        break;

                    case SiriAdapter.RIGHT_DIALOG:

                        if(dbModel.getBody().equals("false")){
                            tempList.add(new SiriModel<Boolean>(SiriAdapter.RIGHT_DIALOG, dbModel.getBody_id(), false));
                        }else{
                            tempList.add(new SiriModel<Boolean>(SiriAdapter.RIGHT_DIALOG, dbModel.getBody_id(), true));
                        }


                        break;

                    default:
                        break;

                }


            }

        }
        if (isHistory) {

            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    addList(tempList);
                    mAdapter.notifyDataSetChanged();
                    if(mIsFirst){
                        if(isHisScroll){
                            mListView.setSelection(mCurrentList.size() - 1);
                            isHisScroll=false;
                        }else{
                            mListView.setSelection(tempList.size());
                        }
//                mListView.smoothScrollToPosition(mCurrentList.size() - 1);
                    }
                }
            });

        } else {
            if (mIsFirst) {
//                clear();
                mIsFirst = false;
            }

            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    addList(tempList);
                    mAdapter.notifyDataSetChanged();

                    if(!mIsPullDown){
                        if(isHisScroll){
                            mListView.setSelection(mCurrentList.size() - 1);
                            isHisScroll=false;
                        }else{
                            mListView.setSelection(tempList.size());
                        }
                    }
                }
            });

        }

    }

    protected void fromDbDelay(List<DbModel> dbList, boolean isDialog, boolean isHistory) {

        List<SiriModel> tempList = new ArrayList<SiriModel>();
        if(!PreferencesUtils.getBooleanFromSPMap(this, PreferencesUtils.Keys.IS_DIALOG)) {
            mList.add(new SiriModel<String>(SiriAdapter.LEFT_TEXT, getResources().getString(R.string.dialog_first)));
            mList.add(new SiriModel<String>(SiriAdapter.RIGHT_TEXT_CLICK, getResources().getString(R.string.dialog_second)));
            insertDb(db,SiriAdapter.LEFT_TEXT, 0, 0,getResources().getString(R.string.dialog_first));
            insertDb(db,SiriAdapter.RIGHT_TEXT_CLICK,0,0,getResources().getString(R.string.dialog_second));
            PreferencesUtils.putBooleanToSPMap(this, PreferencesUtils.Keys.IS_DIALOG, true);
        }
        if (dbList != null) {
            for (DbModel dbModel : dbList) {

                if(!TextUtils.isEmpty(dbModel.getBody())){
                    insertDb(db, SiriAdapter.TIME, 0, dbModel.getDate(), dbModel.getBody());
                    insertDb(db, SiriAdapter.RIGHT_GOODS, dbModel.getBody_id(), 0, dbModel.getBody());
            }
                if(!TextUtils.isEmpty(TimeUtil.getInstance().timeFormat(dbModel.getDate()*1000))){
                    tempList.add(new SiriModel<String>(SiriAdapter.TIME,TimeUtil.getInstance().timeFormat(dbModel.getDate()*1000)));
                }
                ArrayList<SelectGoodsModel> goodsList = new Gson().fromJson(dbModel.getBody(), new TypeToken<ArrayList<SelectGoodsModel>>() {
                }.getType());
                tempList.add(new SiriModel<GoodsListModel>(SiriAdapter.RIGHT_GOODS,dbModel.getBody_id(), new GoodsListModel(goodsList),0,(int)dbModel.getDate()));
            }

        }
      mList.addAll(tempList);

    }
    protected void fromDbNoDelay(List<DbModel> dbList, boolean isDialog, boolean isHistory) {//推送专用

       final List<SiriModel> tempList = new ArrayList<SiriModel>();
        if(!PreferencesUtils.getBooleanFromSPMap(this, PreferencesUtils.Keys.IS_DIALOG)) {
            mList.add(new SiriModel<String>(SiriAdapter.LEFT_TEXT, getResources().getString(R.string.dialog_first)));
            mList.add(new SiriModel<String>(SiriAdapter.RIGHT_TEXT_CLICK, getResources().getString(R.string.dialog_second)));
            insertDb(db,SiriAdapter.LEFT_TEXT, 0, 0, TextSiriActivity.this.getResources().getString(R.string.dialog_first));
            insertDb(db,SiriAdapter.RIGHT_TEXT_CLICK,0,0,TextSiriActivity.this.getResources().getString(R.string.dialog_second));
            PreferencesUtils.putBooleanToSPMap(this, PreferencesUtils.Keys.IS_DIALOG, true);
        }
        if (dbList != null) {
            for (DbModel dbModel : dbList) {

                switch(dbModel.getType()){
                    case SiriAdapter.RIGHT_TEXT:
                    case SiriAdapter.LEFT_TEXT:
                        if(!TextUtils.isEmpty(dbModel.getBody())){
                            tempList.add(new SiriModel<String>(dbModel.getType(), dbModel.getBody()));
                            insertDb(db,dbModel.getType(), 0, 0, dbModel.getBody());
                        }
                        break;
                    case SiriAdapter.RIGHT_TEXT_CLICK:
                        if(!TextUtils.isEmpty(dbModel.getBody())){
                            String temp=dbModel.getBody();

                            if(!fromSelect&&temp.contains("神不神")){
                                if(!TextUtils.isEmpty(TimeUtil.getInstance().timeFormat(dbModel.getDate()*1000))){
                                    tempList.add(new SiriModel<String>(SiriAdapter.TIME, 0,TimeUtil.getInstance().timeFormat(dbModel.getDate()*1000),0));
                                }
                                insertDb(db,SiriAdapter.TIME, 0, dbModel.getDate(), dbModel.getBody());

                            }
                            tempList.add(new SiriModel<String>(SiriAdapter.RIGHT_TEXT_CLICK, dbModel.getBody()));
                            insertDb(db,SiriAdapter.RIGHT_TEXT_CLICK,0,0,TextSiriActivity.this.getResources().getString(R.string.dialog_second));
                        }
                        break;

                    case SiriAdapter.TIME:

                        if(dbModel.getDate()!=0){
                            if(!TextUtils.isEmpty(TimeUtil.getInstance().timeFormat(dbModel.getDate()*1000))){
                                tempList.add(new SiriModel<String>(SiriAdapter.TIME, 0,TimeUtil.getInstance().timeFormat(dbModel.getDate()*1000),0));
                            }
                            insertDb(db,SiriAdapter.TIME, 0, dbModel.getDate(), dbModel.getBody());
                        }

                        break;
                    case SiriAdapter.RIGHT_GOODS:
                        ArrayList<SelectGoodsModel> goodsList = new Gson().fromJson(dbModel.getBody(), new TypeToken<ArrayList<SelectGoodsModel>>() {
                        }.getType());
                        tempList.add(new SiriModel<GoodsListModel>(SiriAdapter.RIGHT_GOODS,dbModel.getBody_id(), new GoodsListModel(goodsList),0,(int)dbModel.getDate()));
                        insertDb(db, SiriAdapter.RIGHT_GOODS, dbModel.getBody_id(), 0, dbModel.getBody());

                        break;


                    default:
                        break;

                }

            }

        }
//            mCurrentList.addAll(0, tempList);

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                addListLast(tempList);
                mAdapter.notifyDataSetChanged();
                mListView.setSelection(mCurrentList.size() - 1);
            }
        });



    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mIsExit = true;
        db.close();
    }

    @OnClick(R.id.title_left)
    public void back(View v) {

        finish();
    }

    @OnClick(R.id.title_right_iv)
    public void qrCode(View v) {
        //扫描条码

        Intent intent = new Intent();
        intent.setClass(TextSiriActivity.this, MipcaActivityCapture.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        startActivityForResult(intent, SCANNIN_GREQUEST_CODE);
        startActivity(intent);


    }

    @OnClick(R.id.siri_buy_list)
    public void wantBuyList(View v) {
        //悦购清单
        Intent intent = new Intent(TextSiriActivity.this, BuyActivity.class);
        startActivity(intent);


    }

    @OnClick(R.id.siri_i_want_buy)
    public void wantBuy(View v) {
        //我想买
        Intent intent = new Intent(TextSiriActivity.this, SelectActivity.class);
        startActivityForResult(intent, REQUEST_CODE);

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //此处操作

        switch (requestCode) {

            case REQUEST_CODE:

                if (resultCode == RESULT_OK) {


                    String brand=data.getExtras().getString("brand");
                    String key=data.getExtras().getString("key");
                    String price=data.getExtras().getString("price");
                    String ids=data.getExtras().getString("id");
                    mKey=key;

                    isSmoth=true;
                    mList.add(new SiriModel<String>(SiriAdapter.RIGHT_TEXT,0, getResources().getString(R.string.dialog_third)+" ["+mKey+"]",0));
                    insertDb(db, SiriAdapter.RIGHT_TEXT, 0, 0, getResources().getString(R.string.dialog_third)+" ["+mKey+"]");
                    mList.add(new SiriModel<String>(SiriAdapter.LEFT_TEXT, TextSiriActivity.this.getResources().getString(R.string.dialog_fou)));
                    insertDb(db, SiriAdapter.LEFT_TEXT, 0, 0, getResources().getString(R.string.dialog_fou));
                    pullSubmitData(key, brand, price,ids);


                }


                break;


            case SCANNIN_GREQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    Bundle bundle = data.getExtras();
                    Intent intent = new Intent(this, GoodsActivity.class);
                    Bundle goodsBundle = new Bundle();
                    goodsBundle.putString("code", bundle.getString("result"));
                    goodsBundle.putString("mall", "");
                    goodsBundle.putBoolean("isCode", true);
                    intent.putExtras(goodsBundle);
                    startActivity(intent);
                }


                break;
        }


    }



    protected synchronized void pullSubmitData(final String key, final String brand, final String price,final String id) {
        executeRequest(new GsonBaseRequest<BaseListModel<DbModel>>(Request.Method.POST, UrlConfig.SELECT_SUBMIT, new TypeToken<BaseListModel<DbModel>>() {
        }.getType(), responseSubmitListener(), errorSiriListener()) {

            protected Map<String, String> getParams() {
                return new ApiParams().with("key", key).with("brands", brand).with("brandids", id).with("price", price);
            }

        });
    }

    private Response.Listener<BaseListModel<DbModel>> responseSubmitListener() {
        return new Response.Listener<BaseListModel<DbModel>>() {
            @Override
            public void onResponse(BaseListModel<DbModel> goodsModel) {
                if (goodsModel.getRet() == 0 && goodsModel.getData() != null) {

                    List<DbModel> model=goodsModel.getData();
                    Collections.reverse(model);
                    fromDbDelay(model,true,false);

                } else {
                    mList.add(new SiriModel<String>(SiriAdapter.LEFT_TEXT, 0,TextSiriActivity.this.getResources().getString(R.string.no_dialog_six),3000));
                    insertDb(db, SiriAdapter.LEFT_TEXT,0,0,TextSiriActivity.this.getResources().getString(R.string.no_dialog_six));
                    mList.add(new SiriModel<String>(SiriAdapter.LEFT_TEXT,0, TextSiriActivity.this.getResources().getString(R.string.no_help_dialog_six),3000));
                    insertDb(db,SiriAdapter.LEFT_TEXT,0,0,TextSiriActivity.this.getResources().getString(R.string.no_help_dialog_six));
                }

            }
        };

    }


    public Response.ErrorListener errorSiriListener() {
        return new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                loadingdialog.dismiss();
                mList.add(new SiriModel<String>(SiriAdapter.LEFT_TEXT, 0,TextSiriActivity.this.getResources().getString(R.string.no_dialog_six),3000));
                insertDb(db, SiriAdapter.LEFT_TEXT,0,0,TextSiriActivity.this.getResources().getString(R.string.no_dialog_six));
                mList.add(new SiriModel<String>(SiriAdapter.LEFT_TEXT,0, TextSiriActivity.this.getResources().getString(R.string.no_help_dialog_six),3000));
                insertDb(db,SiriAdapter.LEFT_TEXT,0,0,TextSiriActivity.this.getResources().getString(R.string.no_help_dialog_six));
            }
        };
    }



    @Override
    public void onRefresh() {

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mIsPullDown=true;
                if(mDbList.size()>=45){

                    mDbList = DatabaseOperate.getDb(db,dbBack++,addNew);
                    Collections.reverse(mDbList);//反转
                    fromDb(mDbList, false, true);


                }else{
                    ToastUtil.show(TextSiriActivity.this, getResources().getText(R.string.no_more_error));
                    mListView.stopRefresh();
                    mListView.stopLoadMore();
                }
            }
        },200);

    }

    @Override
    public void onLoadMore() {

    }


    public void insertDb(SQLiteDatabase db,int type,int id,long date,String body){
        DatabaseOperate.insert(db, new DbModel(type, id, date, body));
        ++addNew;
    }
}
