package util.net.ddns.twoman.usbinject.starter;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.IBinder;
import android.util.Log;

import java.util.HashMap;
import java.util.Iterator;

import util.net.ddns.twoman.usbinject.components.TouchPanelPermissionReceiver;
import util.net.ddns.twoman.usbinject.components.touchpanel.RawTouchPadEvent;

public class TouchPanelStarter {
    Activity context;
    TouchPanelPermissionReceiver mUsbReceiverPermission;
    PendingIntent mPermissionIntent;
    UsbManager mUsbManager;
    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    public static TouchPanelStarter New(Activity ctx){
        TouchPanelStarter instance = new TouchPanelStarter(ctx);
        return instance;
    }
    private TouchPanelStarter(Activity context) {
        this.context = context;
    }
    private void RequestTouchPanelPermission(){
        Log.d("TouchPanelStarter","RequestTouchPanelPermission");
        HashMap<String , UsbDevice> deviceList = mUsbManager.getDeviceList();
        Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
        String i = "";
        Log.d("TouchPanelStarter","deviceList.Count="+Integer.toString(deviceList.size()));
        while (deviceIterator.hasNext()) {
            UsbDevice device = deviceIterator.next();
            Log.d("TouchPanelStarter","device vendorId="+Integer.toString(device.getVendorId()));
            if(device.getVendorId()==3823) {
                mUsbManager.requestPermission(device, mPermissionIntent);
                break;
            }
        }
        Log.d("TouchPanelStarter","RequestTouchPanelPermission done");
    }

    private final BroadcastReceiver mUsbReceiverDetach = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {

            }
        }
    };

    private final BroadcastReceiver mUsbReceiverAttach = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if(UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)){
                UsbDevice dev = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                if(dev != null && dev.getVendorId()==3823){
                    mUsbManager.requestPermission(dev, mPermissionIntent);
                }
            }
        }
    };

    TouchPanelPermissionReceiver.ManuallyHandler pendingManuallyListener;
    public void setOnManuallyHandle(TouchPanelPermissionReceiver.ManuallyHandler listener){
        pendingManuallyListener=listener;
    }

    public IBinder onBind(Intent intent) {
        return  null;
    }
    private void handleStart(){
        Log.d("TouchPanelStarter","handleStart Service");
        mUsbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        mUsbReceiverPermission = new TouchPanelPermissionReceiver(context);
        if(pendingManuallyListener!=null){
            mUsbReceiverPermission.setOnManuallyHandle(pendingManuallyListener);
        }
        mPermissionIntent = PendingIntent.getBroadcast(context, 0, new Intent(ACTION_USB_PERMISSION), 0);
        // SEE ME
        context.registerReceiver(mUsbReceiverPermission, new IntentFilter(ACTION_USB_PERMISSION));
        context.registerReceiver(mUsbReceiverDetach, new IntentFilter(UsbManager.ACTION_USB_DEVICE_DETACHED));
        context.registerReceiver(mUsbReceiverAttach, new IntentFilter(UsbManager.ACTION_USB_DEVICE_ATTACHED));
        // Trigger a Permission Request
        // This Would indirectly invoke TouchPanelPermissionReceiver.OnReceive
        RequestTouchPanelPermission();
    }

    public void Start() {
        handleStart();
    }

    public void onDestroy() {
        mUsbReceiverPermission.clearUsbDev();
        context.unregisterReceiver(mUsbReceiverPermission);
        context.unregisterReceiver(mUsbReceiverDetach);
        context.unregisterReceiver(mUsbReceiverAttach);
    }


}
