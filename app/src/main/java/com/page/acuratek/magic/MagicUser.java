package com.page.acuratek.magic;

import android.os.Message;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Ryan on 2016/7/29.
 */
public class MagicUser {
    private static final int MODE_A = 1;
    private static final int MODE_B = 2;
    private static final int MODE_C = 3;
    private static final int MODE_D = 4;
    private static final int MODE_E = 5;
    static final int secondStep = 30;
    boolean bBypassProgress = false;
    int mode = 0;
    int targetSeconds = 30;
    int remainSeconds = 30;
    boolean progressStatus = false;
    double[] volCurr = new double[2];
    int userIndex = 0;
    String userName = null;
    String userInfo = "";
    String ivFormatInfo = "";
    Timer progressTimer;
    MagicUser(int idx, String name) {
        userIndex = idx;
        userName = name;
        if(userName != null) {  userInfo = "[" + userName + "]";  }
    }
    final class ProgressTimerTask extends TimerTask
    {
        public void run()
        {
            if(!bBypassProgress) { remainSeconds--; }
            if(remainSeconds  == 0) {
                MagicUserManager.playBeep();
                stopProgress();
                Message msg = new Message();
                msg.what = MagicUserManager.SESSION_END;
                PageManager.mainHandler.sendMessage(msg);
            }
        }
    }

    public void updateCurrent() {  // TODO
        double[] temp = new double[2];
        MainActivity.readHidAnalogIO(userIndex, temp);
        volCurr[0] = 53.4 * temp[0] / 1023.0; // Voltage
        volCurr[1] = 5.0 * temp[1] / 1023.0;  // Current
        if (volCurr[1] > 4.0) {
            Message msg = new Message();
            try {
                bBypassProgress = true;
                MainActivity.writeHidRelayIO(userIndex, (byte) 0x0);
                msg.what = MagicUserManager.SHOW_TOAST;
                msg.obj = String.valueOf("WARNNING: OVER CURRENT!!!");
                PageManager.mainHandler.sendMessage(msg);
            } catch (Exception e) {
                msg.what = MainActivity.TOUCHPAD_DEBUG;
                msg.obj = e.toString();
                PageManager.mainHandler.sendMessage(msg);
            }
        }

        ivFormatInfo = String.format(userInfo + "V: %.1f(V), I: %.1f(A)", volCurr[0], volCurr[1]);
    }

    public void addSeconds(int sec) {
        if(sec < 0) {
            if(targetSeconds < -sec) { targetSeconds = 0; }
            else { targetSeconds += sec; }
        }
        else {
            if(targetSeconds + sec > 3600) {  targetSeconds = 3600;  }
            else {  targetSeconds += sec;  }
        }
    }


    // TODO:
    public void startProgress() {
        if (!progressStatus && targetSeconds > 0) {
            remainSeconds = targetSeconds;
            progressStatus = true;
            progressTimer = new Timer();
            progressTimer.schedule(new ProgressTimerTask(), 1000, 1000);
            MainActivity.writeHidRelayIO(userIndex, (byte)0x1);
            MainActivity.updateToast(PageManager.main, userInfo + "Timer Start", Toast.LENGTH_SHORT);
        }
    }

    public void stopProgress() {
        if (progressStatus) {
            progressTimer.cancel();
            MainActivity.writeHidRelayIO(userIndex, (byte) 0x0);
            MainActivity.updateToast(PageManager.main, userInfo + "Timer stopped", Toast.LENGTH_SHORT);
            progressStatus = false;
        }
    }

    public void updateMode(int m) {
        mode = m;
        switch (m) {
            case MODE_A:
                targetSeconds = 90;
                break;
            case MODE_B:
                targetSeconds = 150;
                break;
            case MODE_C:
                targetSeconds = 240;
                break;
            case MODE_D:
                targetSeconds = 300;
                break;
            case MODE_E:
                targetSeconds = 450;
                break;
            default:
                mode = -1;
                break;
        }
    }

}
