package com.liuzhuni.lzn.core.buylist.fragment;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.gson.reflect.TypeToken;
import com.liuzhuni.lzn.R;
import com.liuzhuni.lzn.base.BaseFragment;
import com.liuzhuni.lzn.config.UrlConfig;
import com.liuzhuni.lzn.core.buylist.adapter.FinishAdapter;
import com.liuzhuni.lzn.core.buylist.model.BuyListModel;
import com.liuzhuni.lzn.core.login.LoginActivity;
import com.liuzhuni.lzn.core.model.BaseListModel;
import com.liuzhuni.lzn.pulltorefresh.PullToRefreshBase;
import com.liuzhuni.lzn.pulltorefresh.PullToRefreshListView;
import com.liuzhuni.lzn.utils.PreferencesUtils;
import com.liuzhuni.lzn.volley.GsonBaseRequest;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Andrew Lee on 2015/4/23.
 * E-mail:jieooo7@163.com
 * Date: 2015-04-23
 * Time: 11:24
 */
public class FragmentFinish extends BaseFragment {


    public static List<BuyListModel> mList;
    private List<BuyListModel> mCurrentList;
    private TextView mNoContent;
    private FinishAdapter mAdapter;
    private PullToRefreshListView mPullList;

    private int mTotal=1;
    private int mIndex=0;
    private boolean isUp = false;


    private SimpleDateFormat mDateFormat = new SimpleDateFormat("HH:mm");
    private String mTime;

    private Handler handler=new Handler();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_finish,
                container, false);

        mNoContent=(TextView)view.findViewById(R.id.finish_no_content);

        initData(view);
        pullRefresh();


        return view;
    }

    protected void initData(View view){


        mList=new ArrayList<BuyListModel>();

        ListView lv;
        mPullList = (PullToRefreshListView) view.findViewById(R.id.fragment_finsh_list);

        mPullList.setPullLoadEnabled(true);
        mPullList.setScrollLoadEnabled(true);
        lv = mPullList.getRefreshableView();
        lv.setDivider(getResources().getDrawable(R.drawable.divide));
        lv.setDividerHeight(1);
        lv.setCacheColorHint(Color.TRANSPARENT);
        lv.setSelector(getResources().getDrawable(R.drawable.trans));

        mAdapter=new FinishAdapter(getActivity(),mList);
        lv.setAdapter(mAdapter);

        if(mIndex<mTotal){
            pullData(mIndex,1);
        }


    }


    @Override
    public void onResume() {
        super.onResume();
        mAdapter.notifyDataSetChanged();
        if(mList.size()<1){
            mNoContent.setVisibility(View.VISIBLE);

        }else{
            mNoContent.setVisibility(View.GONE);
        }

    }


//    @Override
//    public void onHiddenChanged(boolean hidden) {
//        super.onHiddenChanged(hidden);
//        if(hidden){
//
//            mAdapter.notifyDataSetChanged();
//            if(mList.size()<1){
//                mNoContent.setVisibility(View.VISIBLE);
//
//            }else{
//                mNoContent.setVisibility(View.GONE);
//            }
//        }
//
//    }

    protected void pullRefresh(){

        mPullList.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ListView>() {

            @Override
            public void onPullDownToRefresh(
                    PullToRefreshBase <ListView> refreshView) {
                // TODO Auto-generated method stub
                isUp = true;
                mIndex = 0;
                pullData(mIndex, 1);
                mPullList.onPullDownRefreshComplete();
					// 上拉刷新完成
                mPullList.onPullUpRefreshComplete();
            }

            @Override
            public void onPullUpToRefresh(
                    PullToRefreshBase<ListView> refreshView) {
                // TODO Auto-generated method stub

                isUp = false;
                if (mIndex < mTotal) {
                    pullData(mIndex,1);

                } else {
                    // 下拉加载完成
                    mPullList.onPullDownRefreshComplete();
//					// 上拉刷新完成
                    mPullList.onPullUpRefreshComplete();
                    // 设置是否有更多的数据
                    mPullList.setHasMoreData(false);
                }

            }

        });
    }


    protected synchronized void pullData(final int id,final int type){
        executeRequest(new GsonBaseRequest<BaseListModel<BuyListModel>>(Request.Method.GET, UrlConfig.WANT_BUY+id+"?t="+type,new TypeToken<BaseListModel<BuyListModel>>(){}.getType(),responseListener(),errorListener()){

        });
    }

    public Response.ErrorListener errorListener() {
        return new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                mPullList.onPullDownRefreshComplete();

                // 上拉刷新完成
                mPullList.onPullUpRefreshComplete();
//                Toast.makeText(activity,getResources().getText(R.string.error_retry), Toast.LENGTH_LONG).show();
                if(error.networkResponse!=null){
                    if(error.networkResponse.statusCode==402){//重新登录
                        PreferencesUtils.putBooleanToSPMap(getActivity(), PreferencesUtils.Keys.IS_LOGIN, false);
                        PreferencesUtils.clearSPMap(getActivity(),PreferencesUtils.Keys.USERINFO);
                        Intent intent=new Intent(getActivity(),LoginActivity.class);
                        startActivity(intent);
                        getActivity().finish();
                    }else{
//                        ToastUtil.customShow(getActivity(), getResources().getText(R.string.error_retry));
                    }
                }else{
//                    ToastUtil.customShow(getActivity(), getResources().getText(R.string.bad_net));
                }
//                RequestManager.cancelAll(this);
            }
        };
    }

    private Response.Listener<BaseListModel<BuyListModel>> responseListener() {
        return new Response.Listener<BaseListModel<BuyListModel>>(){
            @Override
            public void onResponse(BaseListModel<BuyListModel> indexBuyListModel) {

                if(indexBuyListModel.getRet()==0){
                    mTotal=indexBuyListModel.getTotalpage();
                    mIndex++;
                    if(indexBuyListModel.getData()!=null){
                        mCurrentList=indexBuyListModel.getData();


                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                if(isUp){
                                    mList.clear();
                                    mPullList.setLastUpdatedLabel(mTime);//设置最后刷新时间
                                    Date date=new Date();
                                    mTime=mDateFormat.format(date);
                                }

                                mList.addAll(mCurrentList);
                                mAdapter.notifyDataSetChanged();
                            }
                        });

                    }
                }

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if(mList.size()<1){
                            mNoContent.setVisibility(View.VISIBLE);

                        }else{
                            mNoContent.setVisibility(View.GONE);
                        }
                    }
                });


            }

        };

    }
}


