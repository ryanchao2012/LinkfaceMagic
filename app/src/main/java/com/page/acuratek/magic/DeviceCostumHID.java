package com.page.acuratek.magic;

import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbEndpoint;
import android.os.Handler;
import android.os.Message;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Random;

/**
 * Created by Ryan on 2016/9/9.
 */
public class DeviceCostumHID extends AbstractOTG implements Runnable {
    private final static int OTG_RW_RETRY_TIMES = 2;
    private final static int OTG_ANALOG_BYTES = 2;
    Thread thread;
    /* Create the packets that we are going to send to the attached USB
         * device.
	 */
    final byte commendReport = (byte) 0x10;
    final byte targetAioNum = (byte) 0x14;
    final byte commandRoutin = (byte) 0x50;
    final byte commandRelay = (byte) 0x30;
    final byte targetDioNum = (byte) 0x02;
    final byte targetGenBitNum = (byte) 0x12;
    final byte targetOverall = (byte) 0x10;

    final byte[] dummyData = {0x0, 0x0};
    int analogIONum = -1;
    int generalBitNum = -1;

    int hardwareMode = -1;
    boolean resetCounter = false;
    boolean displayReady = false;
    boolean bypassRoutin = false;
    int[] analogIOValue;
    int[] genBitState;
    int debugCounter = 0;
    UsbEndpoint endpointOUT = null;
    UsbEndpoint endpointIN = null;

    DeviceCostumHID(Context context, UsbDevice device, Handler handler) {

        /*
		 * Get the required interface from the USB device.  In this case
		 * we are hard coding the interface number to 0.  In a dynamic example
		 * the code could scan through the interfaces to find the right
		 * interface.  In this case since we know the exact device we are connecting
		 * to, we can hard code it.
		 */

//        super.setIntfNum(0);
        if (super.configOTG(context, device, handler)) {
            thread = new Thread(this);
            thread.start();
            setConnected(true);
        } else {
        }
    }

    public void run() {
        /* Get the OUT endpoint.  It is the second endpoint in the interface */
        endpointOUT = getHardIntf().getEndpoint(1);
		/* Get the IN endpoint.  It is the first endpoint in the interface */
        endpointIN = getHardIntf().getEndpoint(0);

        byte[] requestPacket = new byte[4];
        byte[] readBuffer = new byte[64];

        PIC18xxObj[] pic18 = new PIC18xxObj[2];
        simpleDelay(3000);

        sendMsgToHandler("",MainActivity.TOUCHPAD_DEBUG);

        buildRequestPacket(commendReport, targetAioNum, dummyData, requestPacket);
        readSequence(requestPacket, readBuffer);
        if (validateInputData(requestPacket, readBuffer)) analogIONum = (int)readBuffer[2];
        if(analogIONum > 0) {  analogIOValue = new int[analogIONum];  }
        else {  analogIOValue = new int[2];  }

        buildRequestPacket(commendReport, targetGenBitNum, dummyData, requestPacket);
        readSequence(requestPacket, readBuffer);
        if (validateInputData(requestPacket, readBuffer)) generalBitNum = readBuffer[2];
        if(generalBitNum > 0) {  genBitState = new int[generalBitNum];  }
        else {  genBitState = new int[2];  }

        while (!wasCloseRequested()) {
            /* If the connection was closed, destroy the connections and variables
			 * and exit this thread. */
            /* Sleep the thread for a while */
            try {
                if(!bypassRoutin) {
                    routinRequest(requestPacket, readBuffer);
                }
            } catch (Exception e) {
                sendMsgToHandler(e.toString(), MainActivity.TOUCHPAD_DEBUG);
            }
            setResetCounter();
            simpleDelay(500);
            if (debugCounter++ > 1000) {
                debugCounter = 0;
            }

        }
        super.destroy();

//        sendMsgToHandler("otg finished", MainActivity.TOUCHPAD_DEBUG);

    }

    private void simpleDelay(int d) {
        // Sleep the thread for a while
        try {
            Thread.sleep(d);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // TODO
        public void buildRequestPacket(byte command, byte target, byte[] data, byte[] output){
            output[0] = command;
            output[1] = target;
            output[2] = data[1];
            output[3] = data[0];
        }

        private void readFromOTG(byte[] inbuffer) {
            if(endpointIN != null) {
                int retry_times = 0;
                int result = -1;
                while (!wasCloseRequested() && (result < 0) && (retry_times++ < DeviceCostumHID.OTG_RW_RETRY_TIMES)) {
                    result = getConnection().bulkTransfer(endpointIN, inbuffer, inbuffer.length, 500);
                    if(result >= 0) break;
                    else simpleDelay(100);
                }
            }
        }

        private void requestToOTG(byte[] requestPacket) {
            if(endpointOUT != null) {
                int retry_times = 0;
                int result = -1;
                while (!wasCloseRequested() && (result < 0) && (retry_times++ < DeviceCostumHID.OTG_RW_RETRY_TIMES)) {
                    result = getConnection().bulkTransfer(endpointOUT, requestPacket, requestPacket.length, 500);
                    if(result >= 0) return;
                    else simpleDelay(100);
                }
            }
        }

        synchronized public void readSequence(byte[] request, byte[] inbuffer) {
            requestToOTG(request);
            readFromOTG(inbuffer);
        }
        synchronized public void writeSequence(byte[] request) {
            requestToOTG(request);
        }
        public boolean validateInputData(byte[] request, byte[] readBuffer) {
            boolean ret = false;
            if(request[0] == readBuffer[0] && request[1] == readBuffer[1]) {
                ret = true;
            }
            return ret;
        }

        private void routinRequest(byte[] request, byte[] buffer){
            Random r = new Random();
            buildRequestPacket(commandRoutin, (byte)r.nextInt(255), dummyData, request);
            readSequence(request, buffer);
            String msg = String.format("0x%02x, 0x%02x:: 0x%02x, 0x%02x,", request[0], request[1], buffer[0], buffer[1]);
            int datalen = buffer[2];
            for (int i = 0; i < datalen; i++) {
                msg += String.format("0x%02x, ", buffer[3 + i]);
            }
            if (validateInputData(request, buffer)) {
                msg += "\n";
                int length = (int)buffer[2];
                if(length == (generalBitNum + analogIONum * DeviceCostumHID.OTG_ANALOG_BYTES)) {
                    int offset = 3;
                    for(int i = 0;  i < generalBitNum; i++) {
                        genBitState[i] = buffer[offset++];
                        msg += String.format("0x%02x", genBitState[i]) + ",";
                    }

                    byte[] temp = new byte[DeviceCostumHID.OTG_ANALOG_BYTES  - 1];
                    int j = 0;
                    for(int i = 0; i < analogIONum * DeviceCostumHID.OTG_ANALOG_BYTES; i++) {

                        int ir = i % DeviceCostumHID.OTG_ANALOG_BYTES;
                        int ia = i / DeviceCostumHID.OTG_ANALOG_BYTES;
                        if(ir == DeviceCostumHID.OTG_ANALOG_BYTES - 1) {
                            analogIOValue[ia] = 0;
                            for(int k = 0; k < j; k++) {
                                analogIOValue[ia] = 256 * analogIOValue[ia] + (temp[k] & 0xff);
                            }
                            analogIOValue[ia] = 256 * analogIOValue[ia] + (buffer[offset++] & 0xff);
                            msg += String.format("%d, ", analogIOValue[ia]);
                            j = 0;
                        }
                        else {
                            temp[j] = buffer[offset++];
                            j += 1;
                        }
                    }
                    sendMsgToHandler("data received: " + msg, MainActivity.TOUCHPAD_DEBUG);

                } else {
                    sendMsgToHandler("otg request and read data dismatch", MainActivity.TOUCHPAD_DEBUG);
                }
            } else {
                msg += String.format("0x%02x, 0x%02x, 0x%02x", buffer[0], buffer[1], buffer[2]);
                sendMsgToHandler("data validation failed: " + msg, MainActivity.TOUCHPAD_DEBUG);
            }

        }

    private void setResetCounter() {
        try {
            if (genBitState[1] > 0x0) {
                MagicUserManager.COUNTER = 0;
                genBitState[1] = 0x0;
                Message msg = new Message();
                msg.what = MagicUserManager.RESET_COUNTER;
                PageManager.mainHandler.sendMessage(msg);
            }
        } catch (Exception e) {
            sendMsgToHandler("[setResetCounter exception]: " + e.toString(), MainActivity.TOUCHPAD_DEBUG);
        }
    }

    private void debugRequest() {
        int result = -1;
        byte[] getPotentiometerRequest = new byte[]{(byte) 0x37};
        byte[] getPotentiometerResults = new byte[64];
        byte[] getPushButtonStatusPacket = new byte[]{(byte) 0x81};
        byte[] getPushButtonStatusResults = new byte[64];
        // Send the request to read the Potentiometer
        do {
            result = getConnection().bulkTransfer(endpointOUT, getPotentiometerRequest, getPotentiometerRequest.length, 1000);
        } while ((result < 0) && (wasCloseRequested() == false));

        // Read the results of that request
        do {
            result = getConnection().bulkTransfer(endpointIN, getPotentiometerResults, getPotentiometerResults.length, 1000);
        } while ((result < 0) && (wasCloseRequested() == false));

        // Convert the resulting data to an int
        byte[] potentiometerBuffer = new byte[]{0, 0, 0, 0};
        potentiometerBuffer[0] = getPotentiometerResults[1];
        potentiometerBuffer[1] = getPotentiometerResults[2];

        ByteBuffer buf = ByteBuffer.wrap(potentiometerBuffer);
        buf.order(ByteOrder.LITTLE_ENDIAN);
        int potentiometerResults = buf.getInt();

        // Send the request to get the push button status
        do {
            result = getConnection().bulkTransfer(endpointOUT, getPushButtonStatusPacket, getPushButtonStatusPacket.length, 1000);
        } while ((result < 0) && (wasCloseRequested() == false));

        // Read the push button status
        do {
            result = getConnection().bulkTransfer(endpointIN, getPushButtonStatusResults, getPushButtonStatusResults.length, 1000);
        } while ((result < 0) && (wasCloseRequested() == false));

        // If there was data successfully read,...
        String currentButtonState = "";
        if (result > 0) {
            // convert the byte data to a boolean representing the current button state
            currentButtonState = (getPushButtonStatusResults[1] == 0x0) ? "true" : "false";
        }
        sendMsgToHandler(Integer.toString(potentiometerResults) + ", " + currentButtonState, MainActivity.TOUCHPAD_DEBUG);
    }

    private void sendMsgToHandler(final String msg_body, int msg_type) {
        Message msg = new Message();
        msg.what = msg_type;
        msg.obj = msg_body + "[" + Integer.toString(debugCounter) + "]";
//        PageManager.mainHandler.sendMessage(msg);
    }
}
