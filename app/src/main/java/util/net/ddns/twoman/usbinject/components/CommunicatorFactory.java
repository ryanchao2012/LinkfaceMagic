package util.net.ddns.twoman.usbinject.components;

import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;

public interface CommunicatorFactory {

    UsbDeviceCommunicator GetUsbDeviceCommunicator(UsbManager mManager, UsbDevice dev);


}

