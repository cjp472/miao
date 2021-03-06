package com.liuzhuni.lzn.core.index_new.adapter;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Handler;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.ArrowKeyMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.text.style.URLSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.liuzhuni.lzn.R;
import com.liuzhuni.lzn.core.index_new.model.DetailContentModel;
import com.liuzhuni.lzn.core.index_new.utils.LinkMovementMethodExt;

import net.nightwhistler.htmlspanner.HtmlSpanner;

import java.util.List;

/**
 * Created by Andrew Lee on 2015/7/15.
 * E-mail:jieooo7@163.com
 * Date: 2015-07-15
 * Time: 14:27
 */
public class DetailAdapter extends BaseAdapter {
    private List<DetailContentModel> mList;
    private Context mContext;
    private ImageLoader mImageLoader;

    private boolean isPick;
    private Handler handler;

    private final int TEXT = 0;
    private final int IMAGE = 1;


    public DetailAdapter(List<DetailContentModel> mList, Context mContext, ImageLoader mImageLoader, Handler _handler, boolean pick) {
        this.mList = mList;
        this.mContext = mContext;
        this.mImageLoader = mImageLoader;
        this.handler = _handler;
        this.isPick = pick;
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
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        if (mList.get(position).getType().equals("text")) {
            return this.TEXT;

        } else {
            return this.IMAGE;
        }

    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {


        ViewHolderText textHolder = null;
        ViewHolderImage imageHolder = null;
        int type = getItemViewType(position);
        if (convertView == null) {
            switch (type) {

                case TEXT:

                    convertView = LayoutInflater.from(mContext).inflate(
                            R.layout.detail_text_item, null);
                    textHolder = new ViewHolderText();
                    textHolder.textTv = (TextView) convertView.findViewById(R.id.detail_item_tv);
                    textHolder.headTv = (TextView) convertView.findViewById(R.id.detail_head);
                    convertView.setTag(textHolder);

                    break;
                case IMAGE:

                    convertView = LayoutInflater.from(mContext).inflate(
                            R.layout.detail_image_item, null);
                    imageHolder = new ViewHolderImage();
                    imageHolder.imageIv = (NetworkImageView) convertView.findViewById(R.id.detail_item_img);

                    convertView.setTag(imageHolder);
                    break;
                default:
                    break;

            }


        } else {

            switch (type) {

                case TEXT:

                    textHolder = (ViewHolderText) convertView.getTag();

                    break;
                case IMAGE:

                    imageHolder = (ViewHolderImage) convertView.getTag();
                    break;

            }

        }

        switch (type) {

            case TEXT:
//                SpannableString image=new SpannableString(mContext.getText(R.string.good_good));
//
                Typeface iconfont = Typeface.createFromAsset(mContext.getAssets(), "iconfont.ttf");
                textHolder.headTv.setTypeface(iconfont);
////                Drawable drawable = mContext.getResources().getDrawable(R.drawable.detail_ic_praise);
////                drawable.setBounds(0, -8, textHolder.textTv.getLineHeight()+3, textHolder.textTv.getLineHeight()+3);
//                image.setSpan(new BackgroundColorSpan(Color.argb(0xff, 0xcc, 0xcc, 0xcc)),0, image.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);  //设置背景色为青色
////                image.setSpan(new ImageSpan(drawable), 1, 3, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//
//                textHolder.textTv.setText(image);


                String msp;

                if (isPick) {

                    msp = "" + mContext.getText(R.string.good_good);
                } else {
                    msp = "" + mContext.getText(R.string.good_news);

                }
//                msp.setSpan(new AbsoluteSizeSpan(18,true), 0, msp.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//                msp.setSpan(new BackgroundColorSpan(Color.argb(0xff, 0xcc, 0xcc, 0xcc)), 0, msp.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//                msp.setSpan(new ForegroundColorSpan(Color.argb(0xff, 0xff, 0xff, 0xff)), 0, msp.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                if (position == 0) {
//                    textHolder.textTv.setText("\n");
                    textHolder.headTv.setText(msp);
                } else {
                    textHolder.headTv.setText("");
                }

//                SpannableString html=new SpannableString(new HtmlSpanner().fromHtml(mList.get(position).getData()));
////                html.setSpan(new BackgroundColorSpan(Color.argb(0xff, 0xcc, 0xcc, 0xcc)), 0, msp.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//                html.setSpan(new BackgroundColorSpan(Color.argb(0xff, 0xff, 0xff, 0xff)), 0, html.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                String tran = "我 " + msp;
                SpannableString trans = new SpannableString(tran);
                trans.setSpan(new ForegroundColorSpan(Color.argb(0x00, 0x00, 0x00, 0x00)), 0, tran.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                int sdk = Build.VERSION.SDK_INT;

                if (sdk < Build.VERSION_CODES.HONEYCOMB) {

                    textHolder.textTv.setFocusableInTouchMode(true);
                    textHolder.textTv.setFocusable(true);
                    textHolder.textTv.setClickable(true);
                    textHolder.textTv.setLongClickable(true);
                    textHolder.textTv.setMovementMethod(ArrowKeyMovementMethod.getInstance());
                } else {
                    textHolder.textTv.setTextIsSelectable(true);
                }
                if (position == 0) {
                    textHolder.textTv.setText(trans);
                } else {
                    textHolder.textTv.setText("");
                }
                textHolder.textTv.append(new HtmlSpanner().fromHtml(mList.get(position).getData()));
//                textHolder.textTv.append(mList.get(position).getData());
                textHolder.textTv.setMovementMethod(LinkMovementMethodExt.getInstance(handler, URLSpan.class));
                break;
            case IMAGE:
                imageHolder.imageIv.setImageUrl(mList.get(position).getData(), mImageLoader);
                break;

        }


        return convertView;


    }


    static class ViewHolderText {
        TextView textTv;
        TextView headTv;

    }

    static class ViewHolderImage {
        NetworkImageView imageIv;

    }


}
