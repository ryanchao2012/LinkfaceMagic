package util.net.ddns.twoman.usbinject.components.touchpanel;

/**
 * Created by sunneo on 2016/7/25.
 */
public class RawTouchPadEvent {
    public byte[] bytes;
    public byte header;
    public boolean Z;
    public boolean M;
    public boolean AD1;
    public boolean AD0;
    public boolean status;
    public int x,y,z;
    public int coordinationResolution=11;
    public int maxCoordination=2048;
    private void parse(){
        // TODO:
        header = bytes[0];
        Z = (0x40 & header) > 0;
        M = (0x20 & header) > 0;
        AD1 = (0x04 & header) > 0;
        AD0 = (0x02 & header) > 0;
        status = (0x01 & header) > 0; // up or down
        coordinationResolution = 11;
        if (AD1) coordinationResolution += 2;
        if (AD0) coordinationResolution += 1;
        maxCoordination =(1 << coordinationResolution) - 1;
        // Log.d("TouchPanel~Commu", String.format("Z:[%s];M:[%s];coordinationResolution:[%d]; IsDown:[%s]", Z, M, coordinationResolution, status));
        // in some case the
        // x = maxCoordination - ((int) bytes[2]) * 128 + (int) bytes[3];
        x = (bytes[2] & 127) * 128 + (bytes[3] & 127);
        y = (bytes[4] & 127) * 128 + (bytes[5] & 127);
        x = x & maxCoordination;
        y = y & maxCoordination;
        z = bytes[5];

        //Log.d("TouchPanel~Commu",String.format("(x,y,z)=(%02x%02x,%02x%02x,%d)(%d,%d)",bytes[1],bytes[2],bytes[3],bytes[4],z,x,y));
    }
    public static RawTouchPadEvent FromBytes(byte[] bytes){
        RawTouchPadEvent ret = new RawTouchPadEvent(bytes);
        ret.parse();
        return ret;
    }
    private RawTouchPadEvent(byte[] bytes){
        this.bytes = new byte[bytes.length];
        // copy content
        for(int i=0; i<this.bytes.length; ++i){
            this.bytes[i] = bytes[i];
        }
    }

    public String toString(){
        return String.format("header=%02x;(x,y,z)=(%02x%02x,%02x%02x,%d)(%d,%d)",header,bytes[1],bytes[2],bytes[3],bytes[4],z,x,y);
    }
}
