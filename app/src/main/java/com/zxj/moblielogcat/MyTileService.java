package com.zxj.moblielogcat;

import android.content.Intent;
import android.os.Build;
import android.service.quicksettings.TileService;

import androidx.annotation.RequiresApi;

import com.zxj.moblielogcat.utils.WindowUtil;

/**
 * Created by zxj on 2019-10-09.
 */

@RequiresApi(api = Build.VERSION_CODES.N)
public class MyTileService extends TileService {
    @Override
    public void onClick() {
        super.onClick();
        if(WindowUtil.isShowing){
            stopService(new Intent(this, HandleLogCatService.class));
        }else{
            startService(new Intent(this, HandleLogCatService.class));
        }

    }

    
}
