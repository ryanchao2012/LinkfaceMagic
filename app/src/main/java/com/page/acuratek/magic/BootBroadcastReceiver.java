package com.page.acuratek.magic;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by Ryan on 2016/8/5.
 */
public class BootBroadcastReceiver extends BroadcastReceiver {
    static final String ACTION = "android.intent.action.BOOT_COMPLETED";
    @Override
    public void onReceive(Context context, Intent intent) {
        // Sleep the thread for a while
        if (intent.getAction().equals(ACTION)){
            Intent autoBootIntent=new Intent(context, MainActivity.class);
            autoBootIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(autoBootIntent);
        }
    }
}