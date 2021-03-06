package com.liuzhuni.lzn.core.index_new;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;
import com.liuzhuni.lzn.R;
import com.liuzhuni.lzn.base.Base2Activity;
import com.liuzhuni.lzn.config.UrlConfig;
import com.liuzhuni.lzn.core.index_new.adapter.FilterAdapter;
import com.liuzhuni.lzn.core.index_new.model.FilterModel;
import com.liuzhuni.lzn.core.model.BaseListModel;
import com.liuzhuni.lzn.utils.PreferencesUtils;
import com.liuzhuni.lzn.utils.ToastUtil;
import com.liuzhuni.lzn.volley.GsonBaseRequest;

import java.util.ArrayList;
import java.util.List;

public class FilterActivity extends Base2Activity {

    @ViewInject(R.id.title_left)
    private TextView mBackTv;
    @ViewInject(R.id.title_right)
    private TextView mRegistTv;
    @ViewInject(R.id.title_middle)
    private TextView mTitleTv;

    @ViewInject(R.id.filter_grid_view)
    private GridView mGridView;


    private List<FilterModel> mList;
    private FilterAdapter mAdapter;

    private String mTag="";

    private Handler handler=new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);
        initData();
        findViewById();
        initUI();
        setListener();
    }

    @Override
    protected void initData() {

        mList=new ArrayList<FilterModel>();



        mAdapter=new FilterAdapter(mList,this);
        if(PreferencesUtils.getValueFromSPMap(this,PreferencesUtils.Keys.FILTER,"").equals("")){
            pullData();
        }else{

            ArrayList<FilterModel> tempList = new Gson().fromJson(PreferencesUtils.getValueFromSPMap(this,PreferencesUtils.Keys.FILTER,""), new TypeToken<ArrayList<FilterModel>>() {
            }.getType());
            mList.addAll(tempList);
            mAdapter.notifyDataSetChanged();
        }


    }

    @Override
    protected void findViewById() {

        ViewUtils.inject(this);

    }

    @Override
    protected void initUI() {

        mBackTv.setText(getText(R.string.back));
        mRegistTv.setText(getText(R.string.save));
        mTitleTv.setText(getText(R.string.filter_bro));
        mGridView.setAdapter(mAdapter);


    }

    @Override
    protected void setListener() {

        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                Object obj=mGridView.getItemAtPosition(position);
//                if(obj instanceof FilterModel){
                    if(mList.get(position).isSelected()){
                        mList.get(position).setSelected(false);

                    }else{
                        mList.get(position).setSelected(true);
                    }
                mAdapter.notifyDataSetChanged();

            }
        });

    }


    @OnClick(R.id.title_left)
    public void back(View v) {

        finish();
    }


    @OnClick(R.id.title_right)
    public void save(View v) {


        StringBuffer sb = new StringBuffer();
        if (mList != null && !mList.isEmpty()) {
            for (FilterModel brand : mList) {
                if (brand.isSelected()) {
                    sb.append(brand.getId());
                    sb.append(",");

                }

            }

        }
        if (mList != null && !mList.isEmpty() && sb.length() > 0) {//至于点击自选品牌时 才进行操作 防止崩溃
            sb.deleteCharAt(sb.lastIndexOf(","));
        }

        if(sb.length()<1){
            ToastUtil.customShow(this,getText(R.string.save_tips));
            return;
        }

        PreferencesUtils.putValueToSPMap(this,PreferencesUtils.Keys.FILTER,new Gson().toJson(mList));

        mTag=sb.toString();
        PreferencesUtils.putValueToSPMap(this,PreferencesUtils.Keys.TAG,mTag);
        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putString("tag", mTag);
        intent.putExtras(bundle);
        this.setResult(RESULT_OK, intent);
        finish();
    }


    protected  void pullData() {
        executeRequest(new GsonBaseRequest<BaseListModel<FilterModel>>(Request.Method.GET, UrlConfig.GET_FILTER, new TypeToken<BaseListModel<FilterModel>>() {
        }.getType(), responseFilterListener(), errorListener()) {

        });
    }

    protected Response.Listener<BaseListModel<FilterModel>> responseFilterListener() {
        return new Response.Listener<BaseListModel<FilterModel>>() {
            @Override
            public void onResponse(BaseListModel<FilterModel> indexBaseListModel) {
                if (indexBaseListModel.getData() != null) {

                    final List<FilterModel> listModel=indexBaseListModel.getData();

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            mList.addAll(listModel);
                            mAdapter.notifyDataSetChanged();
                        }
                    });


                }
            }
        };

    }


}
