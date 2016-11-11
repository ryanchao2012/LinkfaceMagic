package util.net.ddns.twoman.usbinject.components;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.PointF;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;

import java.util.Hashtable;

import util.net.ddns.twoman.usbinject.components.events.OnTouchDataReady;
import util.net.ddns.twoman.usbinject.components.touchpanel.RawTouchPadEvent;
import util.net.ddns.twoman.usbinject.components.touchpanel.TouchPadEvent;
import util.net.ddns.twoman.usbinject.components.touchpanel.TouchPanelUsbDeviceCommunicator;

/**
 * Created by sunneo on 2016/8/5.
 */
public class TouchPanelPermissionReceiver extends BroadcastReceiver {
    private static final String TAG="TouchPanel~Recv";
    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    Hashtable<UsbDevice,UsbDeviceCommunicator> communicatorHashtable = new Hashtable<>();
    UsbManager mUsbManager;
    private static double MOVE_THRESHOLD=5;
    UsbDeviceCommunicatorFactory usbDeviceCommunicatorFactory;
    Activity self;
    PointF prevTouchPos = new PointF();
    boolean prevIsDown=false;
    long prevPressTime;
    WindowManager windowManager;
    public void clearUsbDev(){
        synchronized (communicatorHashtable) {
            for (UsbDeviceCommunicator commu : communicatorHashtable.values()) {
                try {
                    commu.Stop();
                } catch (Exception ee) {
                    ee.printStackTrace();
                }
            }
            communicatorHashtable.clear();
        }

    }
    public TouchPanelPermissionReceiver(Activity ctx){
        this.self =ctx;
        mUsbManager = (UsbManager) ctx.getSystemService(Context.USB_SERVICE);
        windowManager = (WindowManager)ctx.getSystemService(Context.WINDOW_SERVICE);
        usbDeviceCommunicatorFactory = new UsbDeviceCommunicatorFactory();
    }
    public static interface ManuallyHandler{
        public boolean OnTouch(RawTouchPadEvent raw);
    }
    ManuallyHandler manuallyHandler;
    public void setOnManuallyHandle(ManuallyHandler listener){
        this.manuallyHandler=listener;
    }

    public static double PointFDistance(PointF a,PointF b){
        double dx = a.x-b.x;
        double dy = a.y-b.y;
        return Math.sqrt(dx*dx+dy*dy);
    }
    // upon permission request occurred, create a thread for communication
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        if (ACTION_USB_PERMISSION.equals(action)) {
            synchronized (this) {
                UsbDevice dev = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {

                    if(dev != null){
                        if (communicatorHashtable.containsKey(dev)) {
                            communicatorHashtable.get(dev).Stop();
                        }
                        final TouchPanelUsbDeviceCommunicator commu = usbDeviceCommunicatorFactory.GetTouchPanelUsbDeviceCommunicator(mUsbManager,dev);
                        if(commu != null ){
                            commu.setOnTouchReady(new OnTouchDataReady() {
                                @Override
                                public void onTouch(RawTouchPadEvent raw) {
                                    if(manuallyHandler!=null){
                                        if(manuallyHandler.OnTouch(raw)){
                                            // it has been handled.
                                            return;
                                        }
                                    }
                                    // raw Touch Event is here
                                    final TouchPadEvent touchEvent=TouchPadEvent.FromRawEvent(self,raw);
                                    // touchEvent object is translated for screen resolution.
                                    PointF touchPos = new PointF(touchEvent.x,touchEvent.y);
                                    Window window = self.getWindow();
                                    long time = System.currentTimeMillis();

                                    if(touchEvent.down){
                                        if(prevIsDown) {
                                            if (PointFDistance(touchPos, prevTouchPos) >= MOVE_THRESHOLD) {
                                                int action = MotionEvent.ACTION_MOVE;
                                                MotionEvent me = MotionEvent.obtain(prevPressTime, time, action, touchPos.x, touchPos.y, 0);
                                                window.superDispatchTouchEvent(me);
                                            } else {
                                                int action = MotionEvent.ACTION_DOWN;
                                                MotionEvent me = MotionEvent.obtain(prevPressTime, time, action, touchPos.x, touchPos.y, 0);
                                                window.superDispatchTouchEvent(me);
                                            }
                                        }
                                        else{
                                            int action = MotionEvent.ACTION_DOWN;
                                            MotionEvent me = MotionEvent.obtain(time, time, action, touchPos.x, touchPos.y, 0);
                                            window.superDispatchTouchEvent(me);
                                            prevPressTime=time;
                                        }
                                    }
                                    else{
                                        int action = MotionEvent.ACTION_UP;
                                        MotionEvent me = MotionEvent.obtain(prevPressTime, time, action, touchPos.x, touchPos.y, 0);
                                        window.superDispatchTouchEvent(me);
                                        prevPressTime=-1;
                                    }



                                    prevIsDown = touchEvent.down;
                                    prevTouchPos = touchPos;
                                }
                            });
                            commu.Start();
                            synchronized (communicatorHashtable) {
                                communicatorHashtable.put(dev, commu);
                            }
                        }

                    }
                }
                else {
                    Log.d(TAG, "permission denied for device " + dev);
                }
            }
        }
    }
}
