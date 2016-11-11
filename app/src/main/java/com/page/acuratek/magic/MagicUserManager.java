package com.page.acuratek.magic;

import android.media.MediaPlayer;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Ryan on 2016/7/29.
 */
public class MagicUserManager {
    static final int RESET_COUNTER = 8643;
    static final int UPDATE_SLIDE8 = 4321;
    static final int UPDATE_SLIDE7 = 4325;
    static final int SESSION_END = 1234;
    static final int IDLE_TIMEOUT = 4488;
    static final int SHOW_TOAST = 5566;
    static final boolean DEBUG = false;
    static final int GO_HOME = 9988;
    static final int GO_PROGRESS = 9977;
    static int userNum = 1;
    static int COUNTER = 0;
    static int userIndex = 0;
    public static final int USER_A_IDX = 0;
    public static final int USER_B_IDX = 1;
    static final MediaPlayer mp = MediaPlayer.create(PageManager.main, R.raw.windows_notify);
    static private MagicUser[] users;
    private static Timer mainTimer;
    private static boolean mainTimerStatus = false;
    static int counterIdle = 0;

    static void iniManager(int num){
        if(num > 2) { num = 1; }
        userNum = num;
        users = new MagicUser[userNum];

        if(userNum == 1) {
            users[0] = new MagicUser(0, null);
        } else {
            for(int i = 0; i <userNum; i++ ) {
                users[i] = new MagicUser(i, String.valueOf((char)('A' + i)));
            }
        }
    }
    static public void startMainTimerTask() {
        if (!mainTimerStatus) {
            mainTimerStatus = true;
            mainTimer = new Timer();
            mainTimer.schedule(new MainTimerTask(), 1000, 1000);
        }
    }
    static public void stopMainTimerTask() {
        if (mainTimerStatus) {
            mainTimerStatus = false;
            mainTimer.cancel();
        }
    }
    static public void playBeep() {
        if(mp.isPlaying()) { mp.seekTo(0); }
        else {  mp.start();  }
    }
    static public void stopUserProgress(){  users[userIndex].stopProgress();  }
    static public void startUserProgress(){  users[userIndex].startProgress();  }
    static public void updateUserMode(int idx){  users[userIndex].updateMode(idx); }
    static public void setUserBypassProgress(boolean tf){
        users[userIndex].bBypassProgress = tf;
        String msg;
        if(tf) {
            msg = "Timer paused";
            MainActivity.writeHidRelayIO(userIndex, (byte)0x0);
        }
        else {
            msg = "Timer resumed";
            MainActivity.writeHidRelayIO(userIndex, (byte)0x1);
        }
        MainActivity.updateToast(PageManager.main, users[userIndex].userInfo + msg, Toast.LENGTH_SHORT);
    }
    static public int getUserRemainSeconds(){ return users[userIndex].remainSeconds; }
    static public void setUserTargetSeconds(int sec){  users[userIndex].addSeconds(sec);}
    static public int getUserTargetSeconds(){ return users[userIndex].targetSeconds; }
    static public boolean getUserProgressStatus(int idx) { return users[idx].progressStatus; }
    static public String getAllUserIV() {
        String allIVInfo = "";
        for(int i = 0; i < userNum; i++) {
            allIVInfo += users[i].ivFormatInfo + "    ";
        }
        return allIVInfo;
    }
    static public void updateAllUserIV() {
        for(int i = 0; i < userNum; i++) {
            users[i].updateCurrent();
        }
    }

    static final class MainTimerTask extends TimerTask
    {
        public void run()
        {
            updateAllUserIV();
            String name = PageManager.currentPage.backgroundImgFileName;
            if(name.equals("slide7")) {
                Message msg = new Message();
                msg.what = MagicUserManager.UPDATE_SLIDE7;
                PageManager.mainHandler.sendMessage(msg);
            }
            else if(name.equals("slide8")) {
                Message msg = new Message();
                msg.what = MagicUserManager.UPDATE_SLIDE8;
                PageManager.mainHandler.sendMessage(msg);
            }


            if (!name.equals("slide8")) {
                counterIdle += 1;
                if(counterIdle > 30) {
                    counterIdle = 0;
                    for(int i = 0; i < userNum; i++) {
                        if(getUserProgressStatus(i)) {
                            userIndex = i;
                            Message msg = new Message();
                            msg.what = IDLE_TIMEOUT;
                            msg.obj = GO_PROGRESS;
                            PageManager.mainHandler.sendMessage(msg);
                            return;
                        }
                    }
                    if(!name.equals("slide2")) {
                        Message msg = new Message();
                        msg.what = IDLE_TIMEOUT;
                        msg.obj = GO_HOME;
                        PageManager.mainHandler.sendMessage(msg);
                    }
                }
            }

            else { counterIdle = 0; }
        }
    }
}
