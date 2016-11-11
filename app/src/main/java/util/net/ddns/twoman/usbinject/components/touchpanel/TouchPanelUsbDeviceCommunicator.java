package util.net.ddns.twoman.usbinject.components.touchpanel;

import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.hardware.usb.UsbRequest;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

import util.net.ddns.twoman.usbinject.components.UsbDeviceCommunicator;
import util.net.ddns.twoman.usbinject.components.events.OnTouchDataReady;

/**
 * Created by sunneo on 2016/7/24.
 */
public class TouchPanelUsbDeviceCommunicator extends UsbDeviceCommunicator{
    final static int VENDOR_ID  = 3828;

    final static int PRODUCT_ID  = 1;

    // HID Class-Specific Requests values. See section 7.2 of the HID specifications
    final static int HID_GET_REPORT     =           0x01;
    final static int HID_GET_IDLE        =          0x02;
    final static int HID_GET_PROTOCOL     =         0x03;
    final static int HID_SET_REPORT        =        0x09;
    final static int HID_SET_IDLE           =       0x0A;
    final static int HID_SET_PROTOCOL        =      0x0B;
    final static int HID_REPORT_TYPE_INPUT    =     0x01;
    final static int HID_REPORT_TYPE_OUTPUT    =    0x02;
    final static int HID_REPORT_TYPE_FEATURE    =   0x03;
    final static int LIBUSB_RECIPIENT_INTERFACE = 0x01;

    final static int CTRL_IN	=	UsbConstants.USB_DIR_IN|UsbConstants.USB_TYPE_CLASS| LIBUSB_RECIPIENT_INTERFACE;
    final static int CTRL_OUT=	UsbConstants.USB_DIR_OUT|UsbConstants.USB_TYPE_CLASS|LIBUSB_RECIPIENT_INTERFACE;
    protected OnTouchDataReady onTouchReadyHandler;
    public void setOnTouchReady(OnTouchDataReady l){
        onTouchReadyHandler = l;
    }
    public TouchPanelUsbDeviceCommunicator(UsbManager usbManager, UsbDevice dev) {
        super(usbManager, dev);
    }
    private int readBytes(IOInterface io,byte[] bytes) throws IOException{
        final UsbRequest request = new UsbRequest();

        request.initialize(io.getConnection(), this.mEndpointOut);
        final ByteBuffer buf = ByteBuffer.wrap(bytes);
        if (!request.queue(buf, bytes.length)) {
            throw new IOException("Error queueing request.");
        }

        final UsbRequest response = io.getConnection().requestWait();

        return buf.position();
    }
    protected int writeBytes(IOInterface io,int... bytes) throws IOException{
        ByteBuffer byteBuf = ByteBuffer.allocate(bytes.length);
        for(int i=0; i<bytes.length; ++i) {
            byteBuf.put((byte) bytes[i]);
        }
        return io.write(byteBuf.array());
    }
    protected int writeBytes(IOInterface io,byte... bytes) throws IOException{
        return io.write(bytes);
    }
    private void GetData(IOInterface io,byte[] pkg)throws IOException {
        io.getConnection().controlTransfer(CTRL_OUT, HID_GET_REPORT, (HID_REPORT_TYPE_FEATURE<<8)|0x00,0,pkg,pkg.length,0);
        io.getConnection().bulkTransfer(this.mEndpointOut, pkg, pkg.length, 0);
        //readBytes(io,pkg);
    }

    boolean nativeOrder = true;
    private void swapBytes(byte[] a,int i,int j){
        byte t = a[i];
        a[i] = a[j];
        a[j] = t;
    }
    public void Run(IOInterface io){
        //TODO

        try {
            long time = System.currentTimeMillis();
            byte[] pkg = new byte[6];
            byte[] pkgPrev = new byte[6];
            while(IsRunning){
                pkg[0]= 0x0a;
                pkg[1]= 1;
                pkg[2]='A';
                GetData(io, pkg);
                if(!IsRunning) break;
                if(((pkg[0])&0x80) > 0){
                }
                else{
                    swapBytes(pkg,0,1);
                    swapBytes(pkg,2,3);
                    swapBytes(pkg,4,5);
                }
                long now = System.currentTimeMillis();
                RawTouchPadEvent res = null;
                if(!Arrays.equals(pkg,pkgPrev)) {
                    res = RawTouchPadEvent.FromBytes(pkg);
                    if(onTouchReadyHandler != null){
                        onTouchReadyHandler.onTouch(res);
                    }
                }
                else{
                    if(now - time > 100){
                        res = RawTouchPadEvent.FromBytes(pkg);
                        if(onTouchReadyHandler != null){
                            onTouchReadyHandler.onTouch(res);
                        }
                    }
                }
                System.arraycopy(pkg,0,pkgPrev,0,6);
            }
        }catch(Exception ee){
            ee.printStackTrace();
        }
    }
}
