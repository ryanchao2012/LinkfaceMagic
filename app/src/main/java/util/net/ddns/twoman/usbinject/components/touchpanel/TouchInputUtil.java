package util.net.ddns.twoman.usbinject.components.touchpanel;

import android.os.Handler;
import android.os.HandlerThread;

/**
 * Created by sunneo on 2016/8/6.
 */
public class TouchInputUtil {
    Handler handler;
    HandlerThread handlerThread;
    Handler GetHandler(){
        if(handler == null){
            handlerThread = new HandlerThread("TouchInputUtilThread");
            handlerThread.start();
            handler = new Handler(handlerThread.getLooper());
        }
        return handler;
    }

    public String do_exec(final String cmd) {

        Runnable r = new Runnable() {
            @Override
            public void run() {
                try {
                    Process p = Runtime.getRuntime().exec(cmd);
                    p.waitFor();
                }catch(Exception ee){
                    ee.printStackTrace();
                }
            }
        };
        GetHandler().post(r);
        return cmd;
    }

    public void EmitLongTouch(int x,int y){
        do_exec(String.format("/system/bin/input swipe %d %d %d %d 1000",x,y,x,y));
    }
    public void EmitTouch(int x,int y){
        do_exec(String.format("/system/bin/input tap %d %d",x,y));
    }
    public void EmitSwipe(int x1,int y1,int x2,int y2,long ms){
        do_exec(String.format("/system/bin/input swipe %d %d %d %d %d",x1,y1,x2,y2,ms));
    }
    public void EmitRoll(int x,int y){
        do_exec(String.format("/system/bin/input roll %d %d",x,y));
    }

}
