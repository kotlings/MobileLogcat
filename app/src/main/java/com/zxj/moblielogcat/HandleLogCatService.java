package com.zxj.moblielogcat;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.view.WindowManager;

import androidx.annotation.Nullable;

import com.zxj.moblielogcat.utils.SharedPreferencesUtils;
import com.zxj.moblielogcat.utils.WindowUtil;

import static com.zxj.moblielogcat.MainActivity.INIT_X;
import static com.zxj.moblielogcat.MainActivity.INIT_Y;


/**
 * 维持LogCatView状态
 * Created by zxj on 2019-10-09.
 */

public class  HandleLogCatService extends Service {
    private static final String TAG = "HandleLogCatService";
    private WindowUtil windowUtil;

    @Override
    public void onCreate() {
        super.onCreate();
        WindowUtil.initX = (int) SharedPreferencesUtils.getFromSpfs(this, INIT_X, 0);
        WindowUtil.initY = (int) SharedPreferencesUtils.getFromSpfs(this, INIT_Y, 0);
        windowUtil = new WindowUtil(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (!windowUtil.isShowing()) {
            windowUtil.showSpeedView();
        }
        SharedPreferencesUtils.putToSpfs(this, MainActivity.IS_SHOWN, true);
        //return super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (windowUtil.speedView != null) {
            WindowManager.LayoutParams params = (WindowManager.LayoutParams) windowUtil.speedView.getLayoutParams();
            SharedPreferencesUtils.putToSpfs(this, INIT_X, params.x);
            SharedPreferencesUtils.putToSpfs(this, INIT_Y, params.y);
            if (windowUtil.isShowing()) {
                windowUtil.closeSpeedView();
                SharedPreferencesUtils.putToSpfs(this, MainActivity.IS_SHOWN, false);
            }
        }
        Log.d(TAG, "service destroy");
    }
}
