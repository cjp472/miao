package com.liuzhuni.lzn.core.siri.adapter;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.liuzhuni.lzn.R;
import com.liuzhuni.lzn.base.Base2Activity;
import com.liuzhuni.lzn.core.goods.GoodsActivity;
import com.liuzhuni.lzn.core.goods.ToBuyActivity;
import com.liuzhuni.lzn.core.select.model.SelectGoodsModel;
import com.liuzhuni.lzn.core.siri.TextSiriActivity;
import com.liuzhuni.lzn.core.siri.model.GoodsListModel;
import com.liuzhuni.lzn.core.siri.model.SiriModel;
import com.liuzhuni.lzn.db.DatabaseOperate;
import com.liuzhuni.lzn.db.DbModel;
import com.liuzhuni.lzn.utils.TextModify;

import java.util.List;

/**
 * Created by Andrew Lee on 2015/4/28.
 * E-mail:jieooo7@163.com
 * Date: 2015-04-28
 * Time: 14:13
 */
public class SimpleGoodAdapter extends BaseAdapter {


    private List<SelectGoodsModel> mList;
    private Base2Activity mActivity;
    private ImageLoader mImageLoader;
    private int id;
    private int pos = 0;
    private boolean once = true;

    private List<SiriModel> mBackList;
    private SQLiteDatabase db;


    public SimpleGoodAdapter(GoodsListModel goodsList, Base2Activity activity, ImageLoader imageLoader) {
        this.mList = goodsList.getList();
        this.mActivity = activity;
        this.mImageLoader = imageLoader;
    }

    public SimpleGoodAdapter(GoodsListModel goodsList, Base2Activity activity, ImageLoader imageLoader, int id, SQLiteDatabase db, List<SiriModel> mBackList, int position) {
        this.mList = goodsList.getList();
        this.mActivity = activity;
        this.mImageLoader = imageLoader;
        this.id = id;
        this.db = db;
        this.mBackList = mBackList;
        this.pos = position;
    }

    @Override
    public int getCount() {
        return this.mList.size();
    }

    @Override
    public Object getItem(int position) {
        return this.mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if (convertView == null) {
            convertView = LayoutInflater.from(mActivity).inflate(
                    R.layout.siri_goods_item, null);
            viewHolder = new ViewHolder();

            viewHolder.imageIv = (NetworkImageView) convertView.findViewById(R.id.siri_good_item_iv);
            viewHolder.rl = (RelativeLayout) convertView.findViewById(R.id.goods_item_rl);
            viewHolder.ll = (LinearLayout) convertView.findViewById(R.id.siri_goods_ll);
            viewHolder.nameTv = (TextView) convertView.findViewById(R.id.siri_goods_item_name);
            viewHolder.shopTv = (TextView) convertView.findViewById(R.id.siri_goods_item_shop);
            viewHolder.priceTv = (TextView) convertView.findViewById(R.id.siri_goods_item_price);

            convertView.setTag(viewHolder);
        } else {

            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.imageIv.setImageUrl(mList.get(position).getImg(), mImageLoader);
        viewHolder.nameTv.setText(TextModify.ToDBC(mList.get(position).getTitle()));
        viewHolder.shopTv.setText(mList.get(position).getMall());
        viewHolder.priceTv.setText(mList.get(position).getPrice());


        viewHolder.rl.setOnClickListener(new CustomListener(position));

        return convertView;

    }

    public class CustomListener implements View.OnClickListener {

        private int position;

        public CustomListener(int position) {
            this.position = position;
        }

        @Override
        public void onClick(View v) {

            if (pos > 0) {

                if (id!=-1&&mBackList.get(pos).getFlag() != 101) {
                    mBackList.get(pos).setFlag(101);
                    mBackList.add(new SiriModel<Boolean>(SiriAdapter.RIGHT_DIALOG, id, true, 0));
                    DatabaseOperate.insert(db, new DbModel(SiriAdapter.RIGHT_DIALOG, id, 0, "true"));
                    ++TextSiriActivity.addNew;
                    DatabaseOperate.updateFlag(db, id);
                }
            }

//            mList.get(position).getMallprice();
            //相关跳转
            Intent intent = new Intent();
            if (TextUtils.isEmpty(mList.get(position).getUrl())) {

                intent.setClass(mActivity, GoodsActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("code", mList.get(position).getCode());
                bundle.putString("mall", mList.get(position).getMall());
                bundle.putBoolean("isCode", false);
                intent.putExtras(bundle);
            } else {
                intent.setClass(mActivity, ToBuyActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("url", mList.get(position).getUrl());
                bundle.putString("title", "");
                intent.putExtras(bundle);
            }
            mActivity.startActivity(intent);

        }
    }


    static class ViewHolder {
        NetworkImageView imageIv;
        LinearLayout ll;
        TextView shopTv;
        TextView nameTv;
        TextView priceTv;
        RelativeLayout rl;
    }

}
