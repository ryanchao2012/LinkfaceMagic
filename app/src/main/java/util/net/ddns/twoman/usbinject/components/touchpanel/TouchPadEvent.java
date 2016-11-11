package util.net.ddns.twoman.usbinject.components.touchpanel;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

/**
 * Created by sunneo on 2016/7/25.
 */
public class TouchPadEvent {
    public int x;
    public int y;
    public boolean down;
    public RawTouchPadEvent raw;
    public double vWidth,vHeight,ratioX,ratioY;
    public int resX;
    public int resY;

    private TouchPadEvent(Context ctx,RawTouchPadEvent raw){
        WindowManager window = (WindowManager)ctx.getSystemService(Context.WINDOW_SERVICE);
        Display display = window.getDefaultDisplay();
        this.raw = raw;
        DisplayMetrics dm = new DisplayMetrics();
        display.getMetrics(dm);
        vWidth = (double)dm.widthPixels;
        vHeight = (double)dm.heightPixels;
        resX = dm.widthPixels;
        resY = dm.heightPixels;
        ratioX = 1.0 - raw.x / (double)raw.maxCoordination;
        ratioY = raw.y / (double)raw.maxCoordination;
        x = (int)(ratioX*vWidth);
        y = (int)(ratioY*vHeight);
        down = raw.status;
    }
    public static TouchPadEvent FromRawEvent(Context ctx,RawTouchPadEvent raw){
        return new TouchPadEvent(ctx,raw);
    }
}
