package com.page.acuratek.magic;

import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.Handler;
import android.os.Message;

/**
 * Created by Ryan on 2016/9/9.
 */
public abstract class AbstractOTG {
    private UsbDevice device = null;
    private UsbManager manager = null;
    private Handler handler = null;
    private Boolean closeRequested = (Boolean)false;
    private UsbDeviceConnection connection;
//    private int intfNum;
    private UsbInterface hardIntf;
    private boolean connected = false;

    public boolean configOTG(Context context, UsbDevice device, Handler handler) {
        boolean ret = false;
        /* Save the device and handler information for later use. */
        this.device = device;
        this.handler = handler;

		/* Get the USB manager from the requesting context */
        this.manager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
//        try {
//            hardIntf = device.getInterface(intfNum);
//        } catch (Exception e) {
//            Message msg = new Message();
//            msg.what = MainActivity.TOUCHPAD_DEBUG;
//            msg.obj = e.toString() + "QQ";
//            PageManager.mainHandler.sendMessage(msg);
//        }

		/* Open a connection to the USB device */
        try {
            connection = manager.openDevice(device);
            if(connection != null) {
                /* Claim the required interface to gain access to it */
                String deb = "C";
                UsbInterface usbIface = null;
                for (int i = 0; i < device.getInterfaceCount(); i++) {
                    usbIface = device.getInterface(i);
                    deb += "B";
                    if(connection.claimInterface(usbIface, true)) {
                        deb += "A";
                    }
                }
                Message msg = new Message();
                if(usbIface != null) {
                    msg.what = MainActivity.TOUCHPAD_DEBUG2;
                    msg.obj = deb;
//                    PageManager.mainHandler.sendMessage(msg);
                    hardIntf = usbIface;
                    ret = true;
                } else {
//                 if the interface claim failed, we should close the
//                 connection and exit.
                    connection.close();
                    msg = new Message();
                    msg.what = MainActivity.TOUCHPAD_DEBUG2;
                    msg.obj = deb + "~.~";
//                    PageManager.mainHandler.sendMessage(msg);
                }
            }
        } catch (Exception e) {
            Message msg = new Message();
            msg.what = MainActivity.TOUCHPAD_DEBUG;
            msg.obj = "QQ";
            PageManager.mainHandler.sendMessage(msg);
        }

        return ret;
    }
    /**
     * @return boolean Indicates if the connection to the USB device
     * was successfully made.
     */
    public boolean isConnected() {
        return connected;
    }

    /**
     * Request that the demo close itself.
     */
    public void close(){
        connected = false;

		/* We should synchronize to the closeRequested object here to insure that the
		 * running thread isn't in the middle of checking this object when we change it.
		 */
        synchronized(closeRequested) {
            closeRequested = true;
        }
    }

    /***********************************************************************
     * Private methods
     ***********************************************************************/

    /**
     * @return boolean Indicates if someone has requested to close the demo
     */
    public boolean wasCloseRequested()
    {
        synchronized(closeRequested){
            return closeRequested;
        }
    }

    /**
     * Closes connections, releases resources, cleans up variables
     */
    public void destroy(){
		/* Release the interface that was previously claimed and close
		 * the connection.
		 */
        if(hardIntf != null) {
            connection.releaseInterface(hardIntf);
        }
        connection.close();

		/* Clear up all of the locals */
        device = null;
        manager = null;
        handler = null;

        closeRequested = false;
        connection = null;
        hardIntf = null;
    }

//    abstract public void toggleLED();

    public void setDevice(UsbDevice dev) {
        this.device = dev;
    }
    public UsbDevice getDevice() {
        return this.device;
    }

    public void setManager(UsbManager man) {
        this.manager = man;
    }
    public UsbManager getManager() {
        return this.manager;
    }

    public void setHandler(Handler h) {
        this.handler = h;
    }
    public Handler getHandler() {
        return this.handler;
    }

    public void setHardIntf(UsbInterface intf) {
        this.hardIntf = intf;
    }
    public UsbInterface getHardIntf() {
        return this.hardIntf;
    }

    public void setConnection(UsbDeviceConnection con) {
        this.connection = con;
    }

    public UsbDeviceConnection getConnection() {
        return this.connection;
    }

//    public void setIntfNum(int num) {
//        this.intfNum = num;
//    }

//    public int getIntfNum() {
//        return this.intfNum;
//    }

    public void setConnected(boolean tf) {
        this.connected = tf;
    }


}
