package util.net.ddns.twoman.usbinject.components;

import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbManager;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;

/**
 * Created by sunneo on 2016/7/23.
 */
public class UsbAccessoryCommunicator{
    private static String TAG="UsbAccessoryCommunicator";
    UsbManager usbManager;
    UsbAccessory mAccessory;
    FileInputStream mInputStream;
    FileOutputStream mOutputStream;
    Thread thread;
    public UsbAccessoryCommunicator(UsbManager usbManager, UsbAccessory accessory){
        this.usbManager = usbManager;
        this.mAccessory=accessory;

    }
    private void threadInterruptedExceptionHandler(Thread thread, Throwable ex){
        if(ex instanceof InterruptedException){
            if(mInputStream!=null){
                try {
                    mInputStream.close();
                    mInputStream = null;
                }catch(Exception ee){
                    ee.printStackTrace();
                }
            }
            if(mOutputStream!=null){
                try {
                    mOutputStream.close();
                    mOutputStream = null;
                }catch(Exception ee){
                    ee.printStackTrace();
                }
            }
        }
    }
    private void openAccessory() {

        if(thread!=null && thread.isAlive()){
            thread.interrupt();

            thread = null;
        }
        Log.d(TAG, "openAccessory: " + mAccessory);
        ParcelFileDescriptor mFileDescriptor = usbManager.openAccessory(mAccessory);
        if (mFileDescriptor != null) {
            FileDescriptor fd = mFileDescriptor.getFileDescriptor();
            mInputStream = new FileInputStream(fd);
            mOutputStream = new FileOutputStream(fd);
            thread = new Thread(null, new Runnable() {
                @Override
                public void run() {
                    Run();
                }
            }, "AccessoryThread");
            thread.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
                @Override
                public void uncaughtException(Thread thread, Throwable ex) {
                    threadInterruptedExceptionHandler(thread,ex);
                }
            });
            thread.start();
        }
    }
    public void Stop(){
        if (thread != null && thread.isAlive()) {
            thread.interrupt();
            thread = null;
        }
    }
    private void Run(){

    }
    public void Start(){
        openAccessory();
    }
}