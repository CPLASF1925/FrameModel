package com.example.niko.framemodel.activity;


import android.Manifest;
import android.app.Activity;
import android.app.Application;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.example.framelib.activtiy.BaseActivity;
import com.example.framelib.pop.PopDownLoadProgress;
import com.example.framelib.utils.Tools.SDCardUtils;
import com.example.framelib.utils.Tools.StatusBarUtils;
import com.example.framelib.utils.permission.PermissionFail;
import com.example.framelib.utils.permission.PermissionHelper;
import com.example.framelib.utils.permission.PermissionSucceed;
import com.example.niko.framemodel.R;
import com.example.niko.framemodel.config.Config;
import com.example.niko.framemodel.net.ProgressListener;
import com.example.niko.framemodel.net.RetrofitClient;
import com.facebook.drawee.view.SimpleDraweeView;
import com.jakewharton.rxbinding2.view.RxView;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.functions.Consumer;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends BaseActivity {
    public static final int REQUECT_CODE_SDCARD = 2;
    private static final int REQUECT_CODE_CALL_PHONE = 3;
    private String mApkUrl = "http://7xk9dj.com1.z0.glb.clouddn.com/BGAUpdateDemo_v1.0.1_debug.apk";
    private String image = "http://sw.bos.baidu.com/sw-search-sp/software/28e3e9a56da44/BaiduNetdisk_mac_2.2.0.dmg";
    @BindView(R.id.imageview)
    SimpleDraweeView mImageView;
    @BindView(R.id.btn_net)
    Button mBtnNet;
    @BindView(R.id.btn_1)
    Button btn1;

    @Override
    protected void setLayout() {
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
    }



    @Override
    protected void setupViews() {

        /**
         * fresco 网络图片加载
         */
        Log.e("mImageView", (mImageView == null) + "");
        mImageView.setImageURI("http://f.hiphotos.baidu.com/image/pic/item/00e93901213fb80e0ee553d034d12f2eb9389484.jpg");

        RxView.clicks(mImageView)
                .throttleFirst(1, TimeUnit.SECONDS)
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) throws Exception {
                        /**
                         * 运行时权限
                         */
                        PermissionHelper.requestPermission(MainActivity.this, REQUECT_CODE_SDCARD,
                                new String[]{Manifest.permission.CALL_PHONE,Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.READ_EXTERNAL_STORAGE}, "需要申请电话权限");
                    }
                });

        RxView.clicks(mBtnNet)
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) throws Exception {
                        skip(mContext, RxjavaAndRetrofitActivity.class);
                    }
                });


    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        PermissionHelper.requestPermissionsResult(this, requestCode, permissions);
    }

    /**
     * 运行时权限成功时调用
     */
    @PermissionSucceed(requestCode = REQUECT_CODE_SDCARD)
    private void callPhone() {
        Toast.makeText(mContext, "申请权限成功", Toast.LENGTH_SHORT).show();
    }


    /**
     * 运行时权限失败时调用
     */
    @PermissionFail(requestCode = REQUECT_CODE_SDCARD)
    private void failPermission() {

        Toast.makeText(mContext, "申请权限失败", Toast.LENGTH_SHORT).show();

    }


    @OnClick(R.id.btn_1)
    public void onViewClicked() {

        final PopDownLoadProgress mPopDownLoadProgress=  new PopDownLoadProgress(((Activity) mContext)).setTitle("文件下载",R.mipmap.ic_launcher);
        mPopDownLoadProgress.showPopupWindow();

        RetrofitClient.getUploadOrDownloadService(new ProgressListener(mContext) {
            @Override
            public void onProgress(long progress, long total, boolean done) {
                mPopDownLoadProgress.setProgress(progress, total);

                if(done){
                    mPopDownLoadProgress.dismiss();
                }
            }
        }).downloadFile(image)
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, final Response<ResponseBody> response) {
                        Log.e("TAG","长度："+ response.body().contentLength());
                        final InputStream inputStream = response.body().byteStream();

                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                File docFile = new File(Config.DOC_PATH + File.separator + "下载文件.apk");
                                SDCardUtils.writeFile(docFile, inputStream);
                            }
                        }).start();

                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        t.printStackTrace();
                        mPopDownLoadProgress.dismiss();
                    }
                });


    }

}
