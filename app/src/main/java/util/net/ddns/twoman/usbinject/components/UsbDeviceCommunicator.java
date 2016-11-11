package util.net.ddns.twoman.usbinject.components;

import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.util.Log;
import android.util.Pair;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by sunneo on 2016/7/23.
 */
public class UsbDeviceCommunicator {
    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    private static String TAG="UsbDeviceCommunicator";
    UsbManager usbManager;
    UsbDevice mDev;
    protected UsbDeviceConnection USBIO;
    protected UsbEndpoint mEndpointOut;
    protected UsbEndpoint mEndpointIn;
    UsbDeviceCommunicator self;
    Thread thread;
    public volatile boolean IsRunning = false;
    public static interface IOInterface{
        int write(byte[] src, int timeoutMillis) throws IOException;
        int write(byte[] src)throws IOException;
        int read(byte[] dest)throws IOException;
        int read(byte[] dest, int timeoutMillis) throws IOException;
        UsbDeviceConnection getConnection();
    }
    public static class UsbBulkIOInterface implements IOInterface{
        byte[] mWriteBuffer = new byte[4096];
        byte[] mReadBuffer = new byte[4096];
        Object mWriteBufferLock = new Object();
        Object mReadBufferLock = new Object();
        private UsbDeviceConnection mConnection;
        private UsbEndpoint mEndpointOut;
        private UsbEndpoint mEndpointIn;
        UsbBulkIOInterface(UsbDeviceCommunicator parent){
            mConnection=parent.USBIO;
            mEndpointOut = parent.mEndpointOut;
            mEndpointIn = parent.mEndpointIn;
        }
        public UsbDeviceConnection getConnection(){
            return mConnection;
        }
        public int write(byte[] src, int timeoutMillis) throws IOException {
            int offset = 0;

            while (offset < src.length) {
                final int writeLength;
                final int amtWritten;

                synchronized (mWriteBufferLock) {
                    final byte[] writeBuffer;

                    writeLength = Math.min(src.length - offset, mWriteBuffer.length);
                    if (offset == 0) {
                        writeBuffer = src;
                    } else {
                        // bulkTransfer does not support offsets, make a copy.
                        System.arraycopy(src, offset, mWriteBuffer, 0, writeLength);
                        writeBuffer = mWriteBuffer;
                    }

                    amtWritten = mConnection.bulkTransfer(mEndpointOut, writeBuffer, writeLength,
                            timeoutMillis);
                }
                if (amtWritten <= 0) {
                    throw new IOException("Error writing " + writeLength
                            + " bytes at offset " + offset + " length=" + src.length);
                }

                Log.d(TAG, "Wrote amt=" + amtWritten + " attempted=" + writeLength);
                offset += amtWritten;
            }
            return offset;
        }
        public int write(byte[] src)throws IOException{
            return write(src,Integer.MAX_VALUE);
        }
        public int read(byte[] dest)throws IOException{
            return read(dest,Integer.MAX_VALUE);
        }
        public int read(byte[] dest, int timeoutMillis) throws IOException {
            final int numBytesRead;
            synchronized (mReadBufferLock) {
                int readAmt = Math.min(dest.length, mReadBuffer.length);
                numBytesRead = mConnection.bulkTransfer(mEndpointIn, mReadBuffer, readAmt,
                        timeoutMillis);
                if (numBytesRead < 0) {
                    // This sucks: we get -1 on timeout, not 0 as preferred.
                    // We *should* use UsbRequest, except it has a bug/api oversight
                    // where there is no way to determine the number of bytes read
                    // in response :\ -- http://b.android.com/28023
                    if (timeoutMillis == Integer.MAX_VALUE) {
                        // Hack: Special case "~infinite timeout" as an error.
                        return -1;
                    }
                    return 0;
                }
                System.arraycopy(mReadBuffer, 0, dest, 0, numBytesRead);
            }
            return numBytesRead;
        }
    }
    public static class UsbControlIOInterface implements IOInterface{
        byte[] mWriteBuffer = new byte[4096];
        byte[] mReadBuffer = new byte[4096];
        Object mWriteBufferLock = new Object();
        Object mReadBufferLock = new Object();
        private UsbDeviceConnection mConnection;
        UsbControlIOInterface(UsbDeviceCommunicator parent,UsbEndpoint epint){
            mConnection=parent.USBIO;
            parent.mEndpointIn = epint;
            parent.mEndpointOut = epint;
        }
        public UsbDeviceConnection getConnection(){
            return mConnection;
        }
        public int write(byte[] src, int timeoutMillis) throws IOException {
            return mConnection.controlTransfer(0x21, 0x01, 0x01, 0x00, src, src.length, 0);
            //return mConnection.bulkTransfer(mEndpointOut,src,src.length,timeoutMillis);
        }
        public int write(byte[] src)throws IOException{
            return write(src,Integer.MAX_VALUE);
        }
        public int read(byte[] dest)throws IOException{
            return read(dest,Integer.MAX_VALUE);
        }
        public int read(byte[] dest, int timeoutMillis) throws IOException {
            final int numBytesRead;
            synchronized (mReadBufferLock) {
                int readAmt = Math.min(dest.length, mReadBuffer.length);
                numBytesRead = mConnection.controlTransfer(0xA1, 0x01, 0x00, 0x00, dest, dest.length, 0);
                if (numBytesRead < 0) {
                    if (timeoutMillis == Integer.MAX_VALUE) {
                        // Hack: Special case "~infinite timeout" as an error.
                        return -1;
                    }
                    return 0;
                }
            }
            return numBytesRead;
        }
    }
    public UsbDeviceCommunicator(UsbManager usbManager, UsbDevice dev){
        this.usbManager = usbManager;
        this.mDev =dev;
        self = this;

    }
    private void close(){

            try {
                for (int i = 0; i < mDev.getInterfaceCount(); i++) {
                    UsbInterface usbIface = mDev.getInterface(i);
                    if (USBIO.releaseInterface(usbIface)) {
                        Log.d(TAG, "releaseInterface " + i + " SUCCESS");
                    } else {
                        Log.d(TAG, "releaseInterface " + i + " FAIL");
                    }
                }
                USBIO.close();
                USBIO = null;

            }catch(Exception e){
                e.printStackTrace();
            }

        isOpened=false;
    }
    private void threadInterruptedExceptionHandler(Thread thread, Throwable ex){
        if(ex instanceof InterruptedException){

            close();
        }
    }
    boolean isOpened=false;
    protected ArrayList<Pair<Integer, UsbInterface>> IOInterfaces = new ArrayList<>();
    private void open() {
        if(isOpened) {
            return;
        }
        Log.d(TAG, "openAccessory: " + mDev);

        try {

            USBIO = usbManager.openDevice(mDev);
            if (USBIO != null) {
                for (int i = 0; i < mDev.getInterfaceCount(); i++) {
                    UsbInterface usbIface = mDev.getInterface(i);
                    if (USBIO.claimInterface(usbIface, true)) {
                        Log.d(TAG, "claimInterface " + i + " SUCCESS");
                    } else {
                        Log.d(TAG, "claimInterface " + i + " FAIL");
                    }
                }
                boolean hasIn = false;
                boolean hasOut = false;
                int usbIOType = 0;
                UsbEndpoint epint=null;
                for (int interfaceIdx = mDev.getInterfaceCount() - 1; interfaceIdx >= 0; --interfaceIdx) {
                    UsbInterface mUsbIntf = null;
                    hasIn = false;
                    hasOut = false;
                    mUsbIntf = mDev.getInterface(interfaceIdx);
                    Log.d(TAG, "Usb Interface " + interfaceIdx+" Has "+ mUsbIntf.getEndpointCount()+" EndPoints");
                    for (int i = 0; i < mUsbIntf.getEndpointCount(); i++) {
                        UsbEndpoint ep = mUsbIntf.getEndpoint(i);
                        usbIOType = ep.getType();
                        if (usbIOType == UsbConstants.USB_ENDPOINT_XFER_BULK) {
                            if (ep.getDirection() == UsbConstants.USB_DIR_IN) {
                                mEndpointIn = ep;
                                hasIn = true;
                            } else if(ep.getDirection() == UsbConstants.USB_DIR_OUT){
                                mEndpointOut = ep;
                                hasOut = true;
                            }
                        }
                        else{
                            Log.d(TAG, String.format("Usb Interface[%d] EndPoint[%d/%d] tp is %d",
                                    interfaceIdx,i,mUsbIntf.getEndpointCount(),ep.getType()));
                            if(usbIOType == UsbConstants.USB_ENDPOINT_XFER_INT){
                                epint = ep;
                            }
                        }
                    }
                    if (hasIn && hasOut) {
                        Log.d(TAG, "Found Usb Interface with InOut at index " + interfaceIdx);
                        //break;
                        //IOInterfaces.add(new Pair<Integer, UsbInterface>(interfaceIdx,mUsbIntf));
                        break;
                    }
                }
                if (hasIn && hasOut && usbIOType == UsbConstants.USB_ENDPOINT_XFER_BULK ) {
                    thread = new Thread(null, new Runnable() {
                        @Override
                        public void run() {
                            UsbBulkIOInterface ioInterface = new UsbBulkIOInterface(self);
                            Run(ioInterface);
                        }
                    }, "UsbDeviceThread");
                    thread.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
                        @Override
                        public void uncaughtException(Thread thread, Throwable ex) {
                            threadInterruptedExceptionHandler(thread, ex);
                        }
                    });
                    thread.start();
                    isOpened=true;
                }
                else if(usbIOType == UsbConstants.USB_ENDPOINT_XFER_INT){
                    final UsbEndpoint _epint = epint;
                    thread = new Thread(null, new Runnable() {
                        @Override
                        public void run() {
                            UsbControlIOInterface ioInterface = new UsbControlIOInterface(self,_epint);
                            IsRunning = true;
                            Run(ioInterface);
                            IsRunning = false;
                        }
                    }, "UsbDeviceThread");

                    thread.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
                        @Override
                        public void uncaughtException(Thread thread, Throwable ex) {
                            threadInterruptedExceptionHandler(thread, ex);
                        }
                    });
                    thread.start();
                    isOpened=true;
                }
            }
        }finally {
            if (!isOpened) {
               close();
            }
        }
    }
    public void Stop(){
        IsRunning = false;
        if (thread != null && thread.isAlive()) {
            thread.interrupt();
            thread = null;
        }
    }
    protected void Run(IOInterface io){

    }
    public void Start(){
        open();
    }
}