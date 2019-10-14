package com.zxj.moblielogcat.utils;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.WindowManager;

import androidx.annotation.NonNull;

import com.zxj.moblielogcat.weight.LogcatView;

import java.lang.ref.WeakReference;

/**
 * Created by zxj on 2019-10-09.
 */

public class WindowUtil implements Handler.Callback {

    public static int statusBarHeight = 0;
    //记录悬浮窗的位置
    public static int initX, initY;
    private WindowManager windowManager;

    public LogcatView speedView;
    private WindowManager.LayoutParams params;

    public boolean isShowing() {
        return isShowing;
    }

    public static boolean isShowing = false;

    private static final int INTERVAL = 1600;


    private Handler handler;

    //TODO Loop
    public static class MyHandler extends Handler {

        WeakReference<WindowUtil> weakReference;

        private MyHandler(WindowUtil adapter) {
            weakReference = new WeakReference<>(adapter);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (null == weakReference || null == weakReference.get()) {
                return;
            }
            weakReference.get().handleMessage(msg);
        }
    }


    @Override
    public boolean handleMessage(@NonNull Message message) {

        calculateNetSpeed();
        handler.sendEmptyMessageDelayed(0, INTERVAL);

        return false;
    }


    //TODO  logcat刷新
    private void calculateNetSpeed() {


    }




    public WindowUtil(Context context) {
        handler = new MyHandler(this);
        windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        speedView = new LogcatView(context);
        params = new WindowManager.LayoutParams();
        params = new WindowManager.LayoutParams();
        params.x = initX;
        params.y = initY;
        params.width = params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        params.gravity = Gravity.LEFT | Gravity.TOP;
        params.format = PixelFormat.RGBA_8888;
        params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
//                | WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //Android 8.0
            params.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            params.type = WindowManager.LayoutParams.TYPE_PHONE;
        }
    }

    public void showSpeedView() {
        windowManager.addView(speedView, params);
        isShowing = true;
        handler.sendEmptyMessage(0);
    }

    public void closeSpeedView() {
        speedView.closeTask();
        windowManager.removeView(speedView);
        isShowing = false;
    }


}
