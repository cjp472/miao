package com.liuzhuni.lzn.core.personInfo;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;
import com.liuzhuni.lzn.R;
import com.liuzhuni.lzn.base.Base2Activity;
import com.liuzhuni.lzn.config.Check;
import com.liuzhuni.lzn.config.MessageWhat;
import com.liuzhuni.lzn.config.UrlConfig;
import com.liuzhuni.lzn.core.city.CityActivity;
import com.liuzhuni.lzn.core.model.BaseModel;
import com.liuzhuni.lzn.core.personInfo.ui.PicDialog;
import com.liuzhuni.lzn.utils.Md5Utils;
import com.liuzhuni.lzn.utils.PostSimulation;
import com.liuzhuni.lzn.utils.PreferencesUtils;
import com.liuzhuni.lzn.utils.ToastUtil;
import com.liuzhuni.lzn.utils.fileHelper.FileManager;
import com.liuzhuni.lzn.volley.ApiParams;
import com.liuzhuni.lzn.volley.GsonRequest;
import com.liuzhuni.lzn.volley.RequestManager;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class DetailInfoActivity extends Base2Activity {


    @ViewInject(R.id.title_left)
    private TextView mBackTv;
    @ViewInject(R.id.title_right)
    private TextView mRightTv;
    @ViewInject(R.id.title_middle)
    private TextView mMiddleTv;
    @ViewInject(R.id.nickname_detail)
    private TextView mNicknameTv;
    @ViewInject(R.id.tel_detail)
    private TextView mTelTv;
    @ViewInject(R.id.email_detail)
    private TextView mEmailTv;
    @ViewInject(R.id.adress_detail)
    private TextView mAdressTv;
    @ViewInject(R.id.head_detail_iv)
    private NetworkImageView mHeadIv;
    @ViewInject(R.id.detail_head_rl)
    private RelativeLayout mHeadRl;
    @ViewInject(R.id.detail_nickname_rl)
    private RelativeLayout mNickNameRl;
    @ViewInject(R.id.detail_tel_rl)
    private RelativeLayout mTelRl;

    @ViewInject(R.id.detail_tel_right)
    private ImageView mTelIv;

    private ImageLoader mImageLoader;

    private PicDialog mDialog;

    private String mCity="";

    private int mWidth;

    private static final int PHOTO_REQUEST_CAMERA = 1;// 拍照
    private static final int PHOTO_REQUEST_GALLERY = 2;// 从相册中选择
    private static final int PHOTO_REQUEST_CUT = 3;// 结果
    private static final int CITY = 4;// 结果

    private static final String PHOTO_FILE_NAME = "temp_photo.jpg";

    private Bitmap bitmap=null;
    private File tempFile;

    private Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case MessageWhat.IMG_UPLOAD:
                    String url=(String)msg.obj;
                    if(!TextUtils.isEmpty(url)){
                        PreferencesUtils.putValueToSPMap(DetailInfoActivity.this, PreferencesUtils.Keys.HEAD_URL, url, PreferencesUtils.Keys.USERINFO);
                        //提示信息
                        loadingdialog.dismiss();
                        mHeadIv.setImageBitmap(bitmap);
                        ToastUtil.customShow(DetailInfoActivity.this, getResources().getString(R.string.head_image_ok));
                    }
                    break;
                case MessageWhat.IMG_UPLOAD_ERROR:
                    loadingdialog.dismiss();
                    break;
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_info);
        initData();
        findViewById();
        initUI();
        setListener();
    }

    @Override
    protected void initData() {

        mImageLoader = RequestManager.getImageLoader();

                DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        mWidth = dm.widthPixels-50;

    }

    @Override
    protected void findViewById() {
        ViewUtils.inject(this);

    }


    @Override
    protected void onResume() {
        super.onResume();


        if (Check.hashead(this)) {

            mHeadIv.setImageUrl(PreferencesUtils.getValueFromSPMap(this, PreferencesUtils.Keys.HEAD_URL,"",PreferencesUtils.Keys.USERINFO), mImageLoader);
            mHeadIv.setErrorImageResId(R.drawable.my_touxiang);
            mHeadIv.setDefaultImageResId(R.drawable.my_touxiang);
        }else{
            mHeadIv.setDefaultImageResId(R.drawable.my_touxiang);
        }

        if (Check.hasNickname(this)) {
            mNicknameTv.setText(PreferencesUtils.getValueFromSPMap(this, PreferencesUtils.Keys.NICKNAME,"",PreferencesUtils.Keys.USERINFO));
        } else {
            mNicknameTv.setText(getResources().getText(R.string.no_nickname));
        }

        if (Check.hasTel(this)) {
//            mTelTv.setText(TextModify.getInstance().mobileAdjust(PreferencesUtils.getValueFromSPMap(this, PreferencesUtils.Keys.TEL,"",PreferencesUtils.Keys.USERINFO)));
            mTelTv.setText(PreferencesUtils.getValueFromSPMap(this, PreferencesUtils.Keys.TEL,"",PreferencesUtils.Keys.USERINFO));
            mTelIv.setVisibility(View.INVISIBLE);
        } else {
            mTelTv.setText(getResources().getText(R.string.no_tel));
            mTelIv.setVisibility(View.VISIBLE);
        }

        mCity=PreferencesUtils.getValueFromSPMap(this, PreferencesUtils.Keys.CITY, "", PreferencesUtils.Keys.USERINFO);
        mAdressTv.setText(mCity);
    }

    @Override
    protected void initUI() {

        mMiddleTv.setText(getResources().getString(R.string.me_info));
        mRightTv.setVisibility(View.GONE);



    }

    @Override
    protected void setListener() {

    }


    @OnClick(R.id.title_left)
    public void back(View v) {

        finish();
    }

    @OnClick(R.id.city_rl)
    public void city(View v) {
        Intent intent=new Intent(this, CityActivity.class);

        startActivityForResult(intent, CITY);

    }

    @OnClick(R.id.detail_head_rl)
    public void head(View v) {
        //传头像
        showDialog();
    }

    protected void showDialog(){

        mDialog=new PicDialog(this,null,null,null);

        mDialog.mItemOne.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gallery();
                mDialog.dismiss();
            }
        });

        mDialog.mItemTwo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                camera();
                mDialog.dismiss();
            }
        });

        mDialog.show();



    }

    @OnClick(R.id.detail_nickname_rl)
    public void nickName(View v) {
    //昵称


            Intent intent=new Intent(this, NicknameActivity.class);
            startActivity(intent);
    }

    @OnClick(R.id.detail_tel_rl)
    public void tel(View v) {

        //手机号

        if(!Check.hasTel(this)){
            if(!PreferencesUtils.getBooleanFromSPMap(DetailInfoActivity.this, PreferencesUtils.Keys.IS_THIRD, PreferencesUtils.Keys.USERINFO)){

                Intent intent=new Intent(this, BindTelActivity.class);
                startActivity(intent);
            }else{
                Intent registIntent = new Intent(this, BindTelActivity.class);
                Bundle bundle = new Bundle();
                bundle.putBoolean("isPassWd", true);
                registIntent.putExtras(bundle);
                startActivity(registIntent);
            }
        }
    }


    /*
	 * 从相册获取
	 */
    public void gallery() {
        // 激活系统图库，选择一张图片
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PHOTO_REQUEST_GALLERY);
    }

    /*
     * 从相机获取
     */
    public void camera() {
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        // 判断存储卡是否可以用，可用进行存储,将拍照后的照片存入文件
            intent.putExtra(MediaStore.EXTRA_OUTPUT,
                    Uri.fromFile(new File(FileManager.getSaveFilePath(), PHOTO_FILE_NAME)));
        startActivityForResult(intent, PHOTO_REQUEST_CAMERA);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PHOTO_REQUEST_GALLERY) {
            if (data != null) {
                // intent pick返回选中的图片,uri得到图片的全路径
                Uri uri = data.getData();
                crop(uri);
            }

        }else if(requestCode ==CITY ){

            //
            if(resultCode==RESULT_OK){

                String city=data.getExtras().getString("city");
                if(!TextUtils.isEmpty(city)){

                    if(!city.equals(mCity)){
                        //不同时   请求网络,否则不做
                        PreferencesUtils.putValueToSPMap(DetailInfoActivity.this, PreferencesUtils.Keys.CITY, city, PreferencesUtils.Keys.USERINFO);
                        pullAddresData("",mCity,"");


                    }
                }
            }


        } else if (requestCode == PHOTO_REQUEST_CAMERA) {
                tempFile = new File(FileManager.getSaveFilePath(),
                        PHOTO_FILE_NAME);
                crop(Uri.fromFile(tempFile));

        } else if (requestCode == PHOTO_REQUEST_CUT) {
            try {
                bitmap = data.getParcelableExtra("data");
                loadingdialog.show();

                new Thread() {
                    public void run() {
                        Message msg = Message.obtain();
                        msg.what = MessageWhat.IMG_UPLOAD;
                        String res= null;

                        try {
                            res = uploadImg(bitmap);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        BaseModel<String> model= null;
                        if(res!=null){
                            try {
                                model = new Gson().fromJson(res,new TypeToken<BaseModel<String>>(){}.getType());
                            } catch (JsonSyntaxException e) {
                                e.printStackTrace();
                            }
                            msg.obj =model.getData();
                            handler.sendMessage(msg);
                        }else{
                            msg.what = MessageWhat.IMG_UPLOAD_ERROR;
                            handler.sendMessage(msg);
                            ToastUtil.customShow(DetailInfoActivity.this, getResources().getString(R.string.head_image_error));
                        }


                    }
                }.start();

                this.mHeadIv.setImageBitmap(bitmap);
                boolean delete = tempFile.delete();

            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * 裁剪图片
     *
     * @param uri
     */
    private void crop(Uri uri) {

//        DisplayMetrics dm = new DisplayMetrics();
//        getWindowManager().getDefaultDisplay().getMetrics(dm);
//        int screenWidth;
//        screenWidth = dm.widthPixels;
        // 裁剪图片意图
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        intent.putExtra("crop", "true");
        // 裁剪框的比例，1：1
        intent.putExtra("aspectX", 2);
        intent.putExtra("aspectY", 2);
        // 裁剪后输出图片的尺寸大小
        intent.putExtra("outputX", 350);
        intent.putExtra("outputY", 350);
        // 图片格式
        intent.putExtra("outputFormat", "JPEG");
        intent.putExtra("noFaceDetection", true);// 取消人脸识别
        intent.putExtra("return-data", true);// true:不返回uri，false：返回uri
        startActivityForResult(intent, PHOTO_REQUEST_CUT);
    }

    private boolean hasSdcard() {
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            return true;
        } else {
            return false;
        }
    }

    protected String uploadImg(Bitmap bitmap){
        String fileName="";
        Date now=new Date();
        long currentTime=now.getTime();
        String id=PreferencesUtils.getValueFromSPMap(this, PreferencesUtils.Keys.AUTH, "", PreferencesUtils.Keys.USERINFO);
        fileName= Md5Utils.md5(id+currentTime);
        List<String> list=new ArrayList<String>();
        list.add(fileName);

        return PostSimulation.getInstance().post(UrlConfig.IMG_UPLOAD,"pic",list,bitmap,null,null);
    }



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


}
