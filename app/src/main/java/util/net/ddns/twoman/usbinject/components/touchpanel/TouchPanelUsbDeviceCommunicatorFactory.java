package util.net.ddns.twoman.usbinject.components.touchpanel;

import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;

import util.net.ddns.twoman.usbinject.components.CommunicatorFactory;
import util.net.ddns.twoman.usbinject.components.UsbDeviceCommunicator;

/**
 * Created by sunneo on 2016/7/24.
 */
public class TouchPanelUsbDeviceCommunicatorFactory  implements CommunicatorFactory {
    public TouchPanelUsbDeviceCommunicatorFactory(){
    }
    public UsbDeviceCommunicator GetUsbDeviceCommunicator(UsbManager usbManager, UsbDevice dev){
        return new TouchPanelUsbDeviceCommunicator(usbManager,dev);
    }
}
