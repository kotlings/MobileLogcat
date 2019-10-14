package com.zxj.moblielogcat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.zxj.moblielogcat.utils.SharedPreferencesUtils;

/**
 * Created by zxj on 2019-10-09.
 */

//开机自启动
public class MyBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "MyBroadcastReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG,"receiver receive boot broadcast");
        boolean isShown= (boolean) SharedPreferencesUtils.getFromSpfs(context,MainActivity.IS_SHOWN,false);
        if(isShown){
            context.startService(new Intent(context, HandleLogCatService.class));
        }
    }
}
