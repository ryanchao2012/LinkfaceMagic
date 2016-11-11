package util.net.ddns.twoman.usbinject.components;

import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.util.Log;

import java.util.Hashtable;

import util.net.ddns.twoman.usbinject.components.touchpanel.TouchPanelUsbDeviceCommunicator;
import util.net.ddns.twoman.usbinject.components.touchpanel.TouchPanelUsbDeviceCommunicatorFactory;

/**
 * Created by sunneo on 2016/7/24.
 */
public class UsbDeviceCommunicatorFactory implements CommunicatorFactory {
    protected static Hashtable<String,CommunicatorFactory> factoryGetter = new Hashtable<>();
    public UsbDeviceCommunicator GetUsbDeviceCommunicator(UsbManager mManager, UsbDevice dev){
        String key = String.format("%d,%d",dev.getVendorId(),dev.getProductId());
        if(factoryGetter.containsKey(key)){
            Log.d("UsbDevComm~Factory","FactoryGetter Contains Key "+key);
            return factoryGetter.get(key).GetUsbDeviceCommunicator(mManager,dev);
        }
        return null;
    }
    public UsbDeviceCommunicatorFactory(){
        // touch panel
        synchronized (factoryGetter) {
            if(!factoryGetter.containsKey("3823,1")) {
                factoryGetter.put("3823,1", new TouchPanelUsbDeviceCommunicatorFactory());
            }
        }
    }
    public TouchPanelUsbDeviceCommunicator GetTouchPanelUsbDeviceCommunicator(UsbManager mManager, UsbDevice dev){
        if(dev.getVendorId() == 3823 && dev.getProductId() == 1){
            String key = String.format("%d,%d",dev.getVendorId(),dev.getProductId());
            if(factoryGetter.containsKey(key)){
                Log.d("UsbDevComm~Factory","FactoryGetter Contains Key "+key);
                return (TouchPanelUsbDeviceCommunicator) factoryGetter.get(key).GetUsbDeviceCommunicator(mManager,dev);
            }
        }
        return null;
    }

}
