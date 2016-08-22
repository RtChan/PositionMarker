package com.gzcz.rtchen.positionmarker;

import android.app.Application;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import dji.sdk.SDKManager.DJISDKManager;
import dji.sdk.base.DJIBaseComponent;
import dji.sdk.base.DJIBaseProduct;
import dji.sdk.base.DJIError;
import dji.sdk.base.DJISDKError;

/**
 * Created by RtChen on 2016/7/12.
 */
public class DjiSdkApplication extends Application{

    private static final String TAG = DjiSdkApplication.class.getName();
    public static final String FLAG_CONNECTION_CHANGE = "DJI SDK Connection Change";

    private Handler mHandler;
    private static DJIBaseProduct mProduct;

    /*
     * 初始化飞控的时候调用
     */
    public static synchronized DJIBaseProduct getProductInstance() {
        if (null == mProduct) {
            mProduct = DJISDKManager.getInstance().getDJIProduct();
        }
        return mProduct;
    }

    /*
     * DJISDKManager被实例化时，执行初始化
     */
    @Override
    public void onCreate() {
        super.onCreate();

        Toast toast = Toast.makeText(this, "In DJIManager [Rt]",Toast.LENGTH_SHORT);
        toast.show();

        mHandler = new Handler(Looper.getMainLooper());
        DJISDKManager.getInstance().initSDKManager(this, mDJISDKManagerCallback);
    }

    /*
     * 实例化一个DJISDKManagerCallback对象，处理激活状态
     * 重写该对象中的onGetRegisteredResult和onProductChanged接口方法
     */
    private DJISDKManager.DJISDKManagerCallback mDJISDKManagerCallback = new DJISDKManager.DJISDKManagerCallback() {
        /* 方法onGetRegisteredResult判断SDK是否激活成功 */
        @Override
        public void onGetRegisteredResult(DJIError error) {
            Log.d(TAG, error == null ? "Success" : error.getDescription()); //输出日志

            if (error == DJISDKError.REGISTRATION_SUCCESS) {
                DJISDKManager.getInstance().startConnectionToProduct();
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "DJI SDK Success", Toast.LENGTH_LONG).show();
                    }
                });
            } else {
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "register sdk fails, check network is available", Toast.LENGTH_LONG).show();
                    }
                });
            }
            Log.e("TAG", error.toString());
        }

        /* 方法onProductChanged判断连接的产品是否更改 */
        @Override
        public void onProductChanged(DJIBaseProduct oldProduct, DJIBaseProduct newProduct) {
            mProduct = newProduct;
            if (mProduct != null) {
                mProduct.setDJIBaseProductListener(mDJIBaseProductListener);
            }
            notifyStatusChange();   //弹出提示：连接的产品发生改变
        }

        /*
         * 实例化一个DJIBaseProductListener对象
         */
        private DJIBaseProduct.DJIBaseProductListener mDJIBaseProductListener = new DJIBaseProduct.DJIBaseProductListener() {
            @Override
            public void onComponentChange(DJIBaseProduct.DJIComponentKey key, DJIBaseComponent oldComponent, DJIBaseComponent newComponent) {
                if(newComponent != null) {
                    newComponent.setDJIComponentListener(mDJIComponentListener);
                }
                notifyStatusChange();
            }

            /* 当产品连接性改变时弹出提示 */
            @Override
            public void onProductConnectivityChanged(boolean isConnected) {
                notifyStatusChange();
            }
        };

        /*
         * 实例化一个DJIComponentListener，被DJIBaseProductListener对象调用
         */
        private DJIBaseComponent.DJIComponentListener mDJIComponentListener = new DJIBaseComponent.DJIComponentListener() {
            @Override
            public void onComponentConnectivityChanged(boolean isConnected) {
                notifyStatusChange();
            }
        };

        /*
         * 显示提示
         */
        private void notifyStatusChange() {
            mHandler.removeCallbacks(updateRunnable);
            mHandler.postDelayed(updateRunnable, 500);
        }
        private Runnable updateRunnable = new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(FLAG_CONNECTION_CHANGE);
                sendBroadcast(intent);
            }
        };

    };
}

