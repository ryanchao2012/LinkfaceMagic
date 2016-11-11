package com.page.acuratek.magic;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PointF;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.support.v7.app.ActionBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import util.net.ddns.twoman.usbinject.components.UsbDeviceCommunicatorFactory;
import util.net.ddns.twoman.usbinject.components.events.OnTouchDataReady;
import util.net.ddns.twoman.usbinject.components.touchpanel.RawTouchPadEvent;
import util.net.ddns.twoman.usbinject.components.touchpanel.TouchPadEvent;
import util.net.ddns.twoman.usbinject.components.touchpanel.TouchPanelUsbDeviceCommunicator;
import android.view.View.OnTouchListener;

public class MainActivity extends AppCompatActivity implements OnTouchListener {
    final static int TOUCHPAD_EVENT = 2486;
    final static int TOUCHPAD_DEBUG = 7788;
    final static int TOUCHPAD_DEBUG2 = 7789;
    private static final String ACTION_USB_PERMISSION = "com.page.acuratek.magic.USB_PERMISSION";
    private DeviceCostumHID hid = null;
    private UsbDeviceCommunicatorFactory usbDeviceCommunicatorFactory = null;
    TouchPanelUsbDeviceCommunicator commu = null;

    boolean prevIsDown=false;
    long prevPressTime;
    private static double MOVE_THRESHOLD = 5;
    PointF prevTouchPos = new PointF();
    HashSet<String> otgSet = new HashSet<> ();

    static MainActivity self;
    boolean hidIsAlive = false;
    boolean touchpadIsAlive = false;
    private static Toast toast;
    boolean receiverRegistered = false;
    private boolean reboot = true;

    PageHome slide2;

    PageNext slide10;  // Magic Link
    PageNext slide10b;
    PageNext slide11;
    PageEnd slide11b;

    PageNext slide12;  // Magic Massage
    PageNext slide12b;
    PageEnd slide12c;

    PageEnd slide13;  // Magic Income

    PageBonus slide3;

    PageNext slide3a;  // Frequently asked questions
    PageNext slide3b;
    PageNext slide3c;
    PageNext slide3d;
    PageNext slide3e;
    PageEnd slide3f;

    PageMode slide4;
    PageSetting slide5;
    PageNext slide6;
    PageNext slide7;
    PageProgress slide8;
    PageEnd slide9;

    PageEnd slide_debug;

//    dual mode
    PageDualAB slide4ab;

    private void iniPages() {

        slide_debug = new PageEnd("slide_debug");
        slide9 = new PageEnd("slide9");

        slide8 = new PageProgress("slide8") {
            @Override
            public void tvInfoUpdate() {  tvInfo.setText(MagicUserManager.getAllUserIV());  }
        };
        slide8.showTag = true;
        slide8.btnHomeAction = new Runnable() {
            @Override
            public void run() {
                MagicUserManager.stopUserProgress();
            }
        };

        slide7 = new PageNext("slide7", slide8) {
            @Override
            public void tvInfoUpdate() {
                Log.d("==========", "77777777777");
                tvInfo.setText(MagicUserManager.getAllUserIV());
            }
        };
        slide7.tvInfoID = R.id.tvInfoInNext;
        slide7.showTag = true;
        slide7.btnHomeAction = new Runnable() {
            @Override
            public void run() {
                MagicUserManager.stopUserProgress();
            }
        };

        slide6 = new PageNext("slide6", slide7);
        slide6.showTag = true;
        slide6.btnNextAction = new Runnable() {
            @Override
            public void run() {
                MagicUserManager.startUserProgress();
            }
        };

        slide5 = new PageSetting("slide5", slide6);
        slide5.showTag = true;
        slide4 = new PageMode("slide4", slide5);
        slide4.showTag = true;

        slide3f = new PageEnd("slide3f");
        slide3e = new PageNext("slide3e", slide3f);
        slide3d = new PageNext("slide3d", slide3e);
        slide3c = new PageNext("slide3c", slide3d);
        slide3b = new PageNext("slide3b", slide3c);
        slide3a = new PageNext("slide3a", slide3b);

        if(MagicUserManager.userNum == 2) {
            slide4ab = new PageDualAB("slide4ab", slide4);
            slide3 = new PageBonus("slide3", slide4ab, slide3a);
            slide4.backgroundImgFileName = "slide4a";
            slide4ab.setProgressPage(slide8);
            slide8.setGotoPage(slide4, slide8);
        } else {
            slide3 = new PageBonus("slide3", slide4, slide3a);
        }


        slide13 = new PageEnd("slide13");

        slide12c = new PageEnd("slide12c");
        slide12b = new PageNext("slide12b", slide12c);
        slide12 = new PageNext("slide12", slide12b);

        slide11b = new PageEnd("slide11b");
        slide11 = new PageNext("slide11", slide11b);
        slide10b = new PageNext("slide10b", slide11);
        slide10 = new PageNext("slide10", slide10b);

        slide2 = new PageHome("slide2");
        slide2.pageLinkFit = slide10;
        slide2.pageLinkMassage = slide12;
        slide2.pageLinkIncome = slide13;
        slide2.pageLinkIonCleanse = slide3;
        PageEnd.home = slide2;
        PageManager.sessionEndPage = slide9;
        PageManager.homePage = slide2;
        PageManager.inProgressPage = slide8;
        PageManager.setBitmapOptions(1);
        PageManager.updateContentView(slide2);

    }

    private void connectOTG() {
            if(hid == null || commu == null) {
//                appendDebugMsg("/OTG called");
                simpleDelay(50);
                UsbManager mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
                HashMap<String, UsbDevice> deviceList = mUsbManager.getDeviceList();
                Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
                while (deviceIterator.hasNext()) {
                    UsbDevice device = deviceIterator.next();
                    int pid = device.getProductId();
                    int vid = device.getVendorId();
                    String key = Integer.toString(vid) + "/" + Integer.toString(pid);
                    try {
//                        appendDebugMsg("/" + otgSet.size() + ":" + key);
                        if (key.equals("1240/63")) {
                            if (!otgSet.contains(key)) {
                                if (mUsbManager.hasPermission(device)) {
                                    initHID(device);
                                    otgSet.add(key);
                                }
                            }
                        }
//                        else if (key.equals("3823/1")) {
//                            if (!otgSet.contains(key)) {
//                                if (mUsbManager.hasPermission(device)) {
//                                    initTouchPad(device);
//                                    otgSet.add(key);
//                                }
//
//                            }
//                        }
//                        appendDebugMsg("*");
                    } catch (Exception e) {
                        appendDebugMsg("/" + e.toString());
                    }
                }
            }
    }
    private void disconnectOTG() {
        if(hid != null || commu != null) {
            UsbManager mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
            HashMap<String, UsbDevice> deviceList = mUsbManager.getDeviceList();
            Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
            HashSet<String> tempSet = new HashSet<>();
            HashSet<String> removeSet = new HashSet<>();
            while(deviceIterator.hasNext()) {
                UsbDevice device = deviceIterator.next();
                int pid = device.getProductId();
                int vid = device.getVendorId();
                String key = Integer.toString(vid) + "/" + Integer.toString(pid);
                tempSet.add(key);
            }
            for(String key : otgSet) {
                if(!tempSet.contains(key)) {
                    try {
                        if (key.equals("1240/63")) {
                            killHID();
                            removeSet.add(key);

                        } else if (key.equals("3823/1")) {
                            killTouchPad();
                            removeSet.add(key);
                        }
                    } catch (Exception e) {
                        appendDebugMsg(e.toString());
                    }
                }
            }
            for(String key : removeSet) {
                if(otgSet.contains(key)) {
                    otgSet.remove(key);
                }
            }
        }
    }
    private void initHID(UsbDevice device) {
        if (hid == null) {
            hid = new DeviceCostumHID(MainActivity.self, device, PageManager.mainHandler);
//            updateToast(self, "HID attached", Toast.LENGTH_SHORT);
        }
    }
    private void initTouchPad(UsbDevice device) {
        UsbManager mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        if(!touchpadIsAlive) {
            touchpadIsAlive = true;

            commu = usbDeviceCommunicatorFactory.GetTouchPanelUsbDeviceCommunicator(mUsbManager, device);
            if(commu != null ) {
                commu.setOnTouchReady(new OnTouchDataReady() {
                    @Override
                    public void onTouch(RawTouchPadEvent evt) {
                        final TouchPadEvent touchEvent = TouchPadEvent.FromRawEvent(self, evt);
                        // raw Touch Event is here
                        // touchEvent object is translated for screen resolution.
                        PointF touchPos = new PointF(touchEvent.x, touchEvent.y);
                        long time = System.currentTimeMillis();

                        MotionEvent me;

                        if (touchEvent.down) {
                            if (prevIsDown) {
                                if (PointFDistance(touchPos, prevTouchPos) >= MOVE_THRESHOLD) {
                                    int action = MotionEvent.ACTION_MOVE;
                                    me = MotionEvent.obtain(prevPressTime, time, action, touchPos.x, touchPos.y, 0);
                                } else {
                                    int action = MotionEvent.ACTION_DOWN;
                                    me = MotionEvent.obtain(prevPressTime, time, action, touchPos.x, touchPos.y, 0);
                                }
                            } else {
                                int action = MotionEvent.ACTION_DOWN;
                                me = MotionEvent.obtain(time, time, action, touchPos.x, touchPos.y, 0);
                                prevPressTime = time;
                            }
                        } else {
                            int action = MotionEvent.ACTION_UP;
                            me = MotionEvent.obtain(prevPressTime, time, action, touchPos.x, touchPos.y, 0);
                            prevPressTime = -1;
                        }
                        sendMsgToHandler(me, MainActivity.TOUCHPAD_EVENT);
                        prevIsDown = touchEvent.down;
                        prevTouchPos = touchPos;

                    }
                });
//                commu.Start();
            }
//            appendDebugMsg("/TouchPad attached");
        }
    }

    public static double PointFDistance(PointF a,PointF b){
        double dx = a.x-b.x;
        double dy = a.y-b.y;
        return Math.sqrt(dx*dx+dy*dy);
    }

    private static void sendMsgToHandler(final Object msg_body, int msg_type){
        Message msg = new Message();
        msg.what = msg_type;
        msg.obj = msg_body;
        PageManager.mainHandler.sendMessage(msg);
    }
    @Override
    protected void onPause() {
        super.onPause();
        /* If there is a hid running, close it */
        killHID();
        killTouchPad();
        otgSet.clear();
		/* unregister any receivers that we have */
//        if(receiverRegistered) {
//            unregisterReceiver(mUsbReceiver);
//            receiverRegistered = false;
//        }
       clearDebugMsg();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        self = this;
//        usbDeviceCommunicatorFactory = new UsbDeviceCommunicatorFactory();
        PageManager.iniManager(this);
        MagicUserManager.iniManager(2);
        setContentView(R.layout.layout_home);
        MyPage.background404ResId = R.drawable.background404;
        iniPages();
        MagicUserManager.startMainTimerTask();
        this.findViewById(android.R.id.content).setOnTouchListener(this);

        hideTopBar();
    }

//    ManuallyHandler manuallyHandler;
//    public void onStart() {
//        super.onStart();

//        if(true) {
//            // handle onTouch manully...
//            TouchPanelStarter panelStarter = TouchPanelStarter.New(this);
//            manuallyHandler=new ManuallyHandler() {
//                @Override
//                public boolean OnTouch(RawTouchPadEvent raw) {
//                    // return true if you've handled it.
//                    // if you want to track the data source, go into the TouchPanelPermissionReceiver.java
//                    final TouchPadEvent touchEvent=TouchPadEvent.FromRawEvent(PageManager.main, raw);
//                    Message msg = new Message();
//                    msg.what = MainActivity.TOUCHPAD_DEBUG;
//                    String d = "rawX: " + Integer.toString(raw.x) + ", rawY: " + Integer.toString(raw.y)
//                            + "\nx: " + Integer.toString(touchEvent.x) + ", y: " + Integer.toString(touchEvent.y)
//                            + "\nresX: " + Integer.toString(touchEvent.resX) + ", resY: " + Integer.toString(touchEvent.resY)
//                            + "\nratioX: " + Double.toString(touchEvent.ratioX) + ", ratioY: " + Double.toString(touchEvent.ratioY);
//                    msg.obj = d;
//                    PageManager.mainHandler.sendMessage(msg);
//                    return false;
//                }
//            };
//            panelStarter.setOnManuallyHandle(manuallyHandler);
//            panelStarter.Start(); // TODO: don't forget to stop
//        } else {
//            // Start Touch Panel Traker
//            TouchPanelStarter.New(this).Start();
//        }
//    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    void hideBottomBar() {
//        Process proc = null;
//
//        String ProcID = "79"; //HONEYCOMB AND OLDER
//
//        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH){
//            ProcID = "42"; //ICS AND NEWER
//        }
//
//        try {
//            proc = Runtime.getRuntime().exec(new String[] { "su", "-c", "service call activity "+ProcID+" s16 com.android.systemui" });
//        } catch (Exception e) {
//            Log.w("Main","Failed to kill task bar (1).");
//            e.printStackTrace();
//        }
//        try {
//            proc.waitFor();
//        } catch (Exception e) {
//            Log.w("Main", "Failed to kill task bar (2).");
//            e.printStackTrace();
//        }
    }

    void hideTopBar() {
        View decorView = getWindow().getDecorView();
        // Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
        // Remember that you should never show the action bar if the
        // status bar is hidden, so hide that too if necessary.
        ActionBar ab = getSupportActionBar();
        if (ab != null) ab.hide();
    }

    public static void updateToast(final Context context, final String text, final int duration) {
        if (toast == null) {
            toast = android.widget.Toast.makeText(context, text, duration);
        } else {
            toast.setText(text);
            toast.setDuration(duration);
        }
        toast.show();
    }

    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
//            if(reboot) {
//                simpleDelay(5000);
//                reboot = !reboot;
//            }
            String action = intent.getAction();
            if(UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
                synchronized (self) {
                    connectOTG();
                }
            }else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                synchronized (self) {
                    disconnectOTG();
                }
            }
        }
    };

    private void killHID() {
        if (hid != null) {
            synchronized (hid) {
                hid.close();
//                if(hid.thread != null && hid.thread.isAlive()) {
//                    hid.thread.interrupt();
//                    hid.thread = null;
//                }
            }

            hid = null;
//            updateToast(self, "HID closed", Toast.LENGTH_SHORT);
        }
        hidIsAlive = false;
    }

    private void killTouchPad() {
        if(commu != null) {
            synchronized (commu) {
                commu.Stop();
            }

            commu = null;
//            appendDebugMsg("/TouchPad closed");
        }
        touchpadIsAlive = false;
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
//        mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
        if(!receiverRegistered) {
            IntentFilter filter = new IntentFilter();
            filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
            filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
            registerReceiver(mUsbReceiver, filter);
            receiverRegistered = true;
        }
        if(hid == null || commu == null) {
            connectOTG();
        }
//        appendDebugMsg("/onPostResume Called");
    }

    private void clearDebugMsg() {
        TextView tv = PageManager.currentPage.tvName;
        if (tv != null) {
            tv.setText("");
        }
    }

    private void appendDebugMsg(String msg) {
        TextView tv = PageManager.currentPage.tvName;
        if(tv != null) {
            String txt = tv.getText() + msg;
            tv.setText(txt);
        }
    }

    private void simpleDelay(int d) {
        // Sleep the thread for a while
        try {
            Thread.sleep(d);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    static public void readHidAnalogIO(int idx, double[] out) {
        if(self.hid != null) {
//            synchronized (self.hid) {
                try {
                    if(self.hid.analogIOValue != null) {
                        out[0] = (double) self.hid.analogIOValue[idx];
                        out[1] = (double) self.hid.analogIOValue[idx + 2];
                    }
                }
                catch (Exception e) {
                    Message msg = new Message();
                    msg.what = MainActivity.TOUCHPAD_DEBUG;
                    msg.obj = e.toString();
                    PageManager.mainHandler.sendMessage(msg);
                }
            }
//        }
    }
    static public boolean writeHidRelayIO(int idx, byte value) {
        if(self.hid != null) {
//            synchronized (self.hid) {
                try {
//                    boolean ret = false;
                    byte[] requestPackage = new byte[4];
                    byte[] data = {value, value};
//                    byte[] buffer = new byte[64];
                    self.hid.bypassRoutin = true;
                    self.hid.buildRequestPacket(self.hid.commandRelay, (byte) idx, data, requestPackage);
                    self.hid.writeSequence(requestPackage);
//                    ret = self.hid.validateInputData(requestPackage, buffer);
                    self.hid.buildRequestPacket(self.hid.commandRelay, (byte) (idx + 2), data, requestPackage);
                    self.hid.writeSequence(requestPackage);
//                    ret = ret & self.hid.validateInputData(requestPackage, buffer);
                    self.hid.bypassRoutin = false;
                    return true;
                }
                catch (Exception e) {
                    Message msg = new Message();
                    msg.what = MainActivity.TOUCHPAD_DEBUG;
                    msg.obj = e.toString();
                    PageManager.mainHandler.sendMessage(msg);
                }
            }
//        }
        return false;
    }

    @Override
    public boolean onTouch(View v, MotionEvent e)
    {
        sendMsgToHandler(e, MainActivity.TOUCHPAD_EVENT);
        return false;
    }
}
