package com.intention.android.goodmask.issc.reliableburst;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.LinkedList;
import java.util.Queue;
import java.util.UUID;


import android.annotation.SuppressLint;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.util.Log;

@SuppressLint("NewApi")

public class ReliableBurstData {

    private String _version = "2.0";

    private int max_mtu = 0;
    private int credit = 0;
    private int max_credit;
    private BluetoothGattCharacteristic _airPatchCharacteristic;
    private BluetoothGattCharacteristic _transparentDataWriteChar;
    private BluetoothGatt _peripheral;
    private boolean vendorMPEnable = false;
    private boolean sendData = true;
    private boolean haveCredit = true;
    private int boardNo;

    private final static UUID CLIENT_CHR_CONFIG_DES     = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    public  static  UUID TRANS_TX         = UUID.fromString("49535343-1E4D-4BD9-BA61-23C647249616");
    // private final static UUID CLIENT_CHR_CONFIG_DES     = Util.uuidFromStr("2902");
    private final static String sPREFIX = "0000";
    private final static String sPOSTFIX = "-0000-1000-8000-00805f9b34fb";


    //private final static UUID CLIENT_CHR_CONFIG_DES     = UUID.fromString("49535343-4C8A-39B3-2F49-511CFF073B7E");
    private final static byte AIR_PATCH_SUCCESS = 0x00;
    private final static byte AIR_PATCH_ACTION_READ_MTU_BM70 = 0x14;
    private final static byte AIR_PATCH_ACTION_READ_MTU_BM78 = 0x24;
    private byte [] write_data ;

    private final static String TAG = "TransmitData";

    private Queue<Object> writeQueue = new LinkedList<Object>();

    private ReliableBurstDataListener mListener = null;

    public void setListener(ReliableBurstDataListener listener) {
        this.mListener = listener;
    }

    @SuppressLint("NewApi")
    private class tempQueueObject extends Object {
        public BluetoothGattCharacteristic chr;
        public byte[] value;

        public tempQueueObject(BluetoothGattCharacteristic characteristic, byte[] data) {
            chr = characteristic;
            value = data;
            write_data = data;

            //Log.d("Bluebit", "write_data : "+(new String(write_data)));

            write_data = data;
            //Log.d("Bluebit", "data : "+(new String(data)));
            //Log.d("Bluebit", "write_data : "+(new String(write_data)));
        }

        public void write() {
            String str3 = new String(write_data);
            //byte[] val = {(byte)0x14};
            //String str4 = new String(val);
            Log.d("Bluebit", "write char: "+str3+ "val "+str3);
            chr.setValue(str3);
            _peripheral.writeCharacteristic(chr);
        }
    }

    public interface ReliableBurstDataListener {
        public abstract void onSendDataWithCharacteristic(ReliableBurstData reliableBurstData, BluetoothGattCharacteristic transparentDataWriteChar);
    }

    public int transmitSize() {
        return max_mtu;
        //return (max_mtu*credit);
    }
    public int setTransmitSize() {

        if (boardNo == 70) {
            max_mtu = 244;

        }
        else if(boardNo == 78) {
            max_mtu = 96;
        }

        Log.d("RELIABLE BURST", "MTU FIXED  "+max_mtu);
        return max_mtu;  //if calback with MTU is missed due to delay/any other issues
        //currently fix mtu to 244 bytes
        //return (max_mtu*credit);
    }

    public void setBoardId(int board) {

        boardNo = board;
        //boardNo = 70;
    }

    public boolean canSendReliableBurstTransmit() {
        if (credit == Integer.MAX_VALUE) {
            return sendData;
        }
        synchronized (this) {
            if (credit > 0) {
                return true;
            }
            else {
                return false;
            }
        }
    }

    public boolean canDisconnect() {
        if (credit == Integer.MAX_VALUE) {
            return true;
        }
        synchronized (this) {
            if (credit >= max_credit) {
                disableAirPatchNotification();
                return true;
            }
            else {
                return false;
            }
        }
    }

    public void decodeReliableBurstTransmitEvent(byte[] value) {
        //Log.d("Bluebit", "decodeReliableBurstTransmitEvent ");
        synchronized (this) {
            if (value[0] != AIR_PATCH_SUCCESS) {
                Log.d("Bluebit", "decodeReliableBurstTransmitEvent AIR_PATCH_SUCCESS");
                return;
            }
            if (boardNo == 70) {
                if (value[1] != AIR_PATCH_ACTION_READ_MTU_BM70) {
                    //return;
                }
            }
            else if (boardNo == 78) {
                if (value[1] != AIR_PATCH_ACTION_READ_MTU_BM78) {
                    //return;
                }

            }
            ByteBuffer bb = ByteBuffer.wrap(value);
            bb.order( ByteOrder.BIG_ENDIAN);
            max_mtu = bb.getShort(2)-3;
            Log.d("MTU FROM FIRMWARE", "MTU FROM FIRMWARE  "+max_mtu);
            if (credit == Integer.MAX_VALUE) {
                credit = 0;
                max_credit = bb.get(4);
                Log.d("Bluebit", "max_credit  "+max_credit);
            }
            Log.d("Bluebit", "APP credit  "+ credit);
            Log.d("Bluebit", "FW --> credit  "+ bb.get(4));
            credit = credit + bb.get(4);
            //Log.d("Bluebit", "New credit  "+credit +" haveCredit "+haveCredit);
            //Log.d("Bluebit", "max_mtu  "+max_mtu);

            if (!haveCredit) {
                Log.d("Bluebit", "  decodeReliableBurstTransmitEvent havecredit is 0 ");
                haveCredit = true;
                if (mListener != null) {
                    Log.d("Bluebit", " decodeReliableBurstTransmitEvent calling onSendDataWithCharacteristic ");
                    mListener.onSendDataWithCharacteristic(this, _transparentDataWriteChar);
                }
            }
        }
    }

    public void enableReliableBurstTransmit(BluetoothGatt gatt,BluetoothGattCharacteristic airPatchCharacteristic
    ) {

        Log.d("Bluebit", "enableReliableBurstTransmit ");

        if (airPatchCharacteristic == null) {
            return;
        }
        _airPatchCharacteristic = airPatchCharacteristic;
        _peripheral = gatt;
        //vendorMPEnable = true;
        if (!vendorMPEnable) {
            Log.d("Bluebit","vendorMPEnable true");
            sendVendorMPEnable();
            vendorMPEnable = true;
        }

        //_airPatchCharacteristic = TxCharacteristic;
        //_peripheral = gatt;

        //enableAirPatchNotification();
    }

    public void reliableBurstTransmit(byte[] data,BluetoothGattCharacteristic transparentDataWriteChar) {
        Log.d("Bluebit", "reliableBurstTransmit ,credit= " + credit);
        synchronized (this) {
            if (_transparentDataWriteChar == null || !_transparentDataWriteChar.equals(transparentDataWriteChar)) {
                _transparentDataWriteChar = transparentDataWriteChar;
            }
            if (credit != Integer.MAX_VALUE) {
                credit--;
                _transparentDataWriteChar.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
                _transparentDataWriteChar.setValue(data);
                _peripheral.writeCharacteristic(_transparentDataWriteChar);
                //Log.d("Bluebit", "reliableBurstTransmit credit " +credit);
                if (credit >0) {
                    if (mListener != null) {
                        mListener.onSendDataWithCharacteristic(this, _transparentDataWriteChar);
                    }
                }
                else {
                    Log.d("Bluebit", "reliableBurstTransmit credit is zero " +credit);
                    haveCredit = false;
                }
            }
            else {
                _transparentDataWriteChar.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
                tempQueueObject temp = new tempQueueObject(_transparentDataWriteChar, data);
                writeQueue(temp);
                sendData = false;
            }

        }
    }

    public boolean isBusy() {
        return writeQueue.size()>0?true:false;
    }

    public boolean isReliableBurstTransmit(BluetoothGattCharacteristic characteristic) {
        if (characteristic.equals(_airPatchCharacteristic)) {
            if (writeQueue.size()>0) {
                writeQueue.remove();
                if (writeQueue.size()>0) {
                    writeQueue();
                }
                return true;
            }
            else {
                return false;
            }
        }
        else {
            if (_transparentDataWriteChar == null) {
                return false;
            }
            if (!sendData && _transparentDataWriteChar.equals(characteristic)) {
                sendData = true;
                if (mListener != null) {
                    mListener.onSendDataWithCharacteristic(this, _transparentDataWriteChar);
                }
                return true;
            }
            else {
                return false;
            }
        }
    }

    @SuppressLint("NewApi")
    private void sendVendorMPEnable()
    {
        if (_airPatchCharacteristic == null)
        {
            return;
        }
        enableAirPatchNotification();
        _airPatchCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
        Log.d("Bluebit", "BOARDNO:" + boardNo);

        if (boardNo == 70) {

            //      byte[] enable = {(byte)0x03};

            //      tempQueueObject temp = new tempQueueObject(_airPatchCharacteristic, enable);
            //     writeQueue(temp);

            if (_airPatchCharacteristic.getUuid().equals(TRANS_TX)) {
                return;
            }

            byte[] value = {(byte) AIR_PATCH_ACTION_READ_MTU_BM70};
            String str1 = new String(value);
            Log.d("Bluebit", "AIR_PATCH_ACTION_READ_MTU_BM70" + str1);
            tempQueueObject temp = new tempQueueObject(_airPatchCharacteristic, value);
            //Log.d("RELIABLEBURST","AIR_PATCH_ACTION_READ_MTU_BM70");
            writeQueue(temp);
        }

        else if (boardNo == 78){
            byte[] enable = {(byte)0x03};

            tempQueueObject temp = new tempQueueObject(_airPatchCharacteristic, enable);
            writeQueue(temp);

            byte[] value = {(byte)AIR_PATCH_ACTION_READ_MTU_BM78};
            temp = new tempQueueObject(_airPatchCharacteristic, value);
            writeQueue(temp);
        }

    }

    public static UUID uuidFromStr(String str) {
        if (!str.matches(".{4}")) {
            return null;
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append(sPREFIX);
            sb.append(str);
            sb.append(sPOSTFIX);
            return UUID.fromString(sb.toString());
        }
    }

    private void disableAirPatchNotification() {
        boolean set = _peripheral.setCharacteristicNotification(_airPatchCharacteristic, false);
        BluetoothGattDescriptor dsc = _airPatchCharacteristic.getDescriptor(CLIENT_CHR_CONFIG_DES);
        dsc.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
        writeQueue(dsc);
    }

    @SuppressLint("NewApi")
    private void enableAirPatchNotification() {
        Log.d("Bluebit" , "Transparent Control point:SetNotification");
        boolean set = _peripheral.setCharacteristicNotification(_airPatchCharacteristic, true);
        BluetoothGattDescriptor dsc = _airPatchCharacteristic.getDescriptor(CLIENT_CHR_CONFIG_DES);
        dsc.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);

        writeQueue(dsc);
    }

    private void writeQueue(Object d)
    {
        //put the descriptor into the write queue
        writeQueue.add(d);
        writeQueue();
        //if there is only 1 item in the queue, then write it.  If more than 1, we handle asynchronously in the callback above
	    /*if(writeQueue.size() == 1){
	    	writeQueue();
	    }*/
    }

    private void writeQueue () {
        Object d = writeQueue.element();
        if (d instanceof BluetoothGattDescriptor) {
            BluetoothGattDescriptor w = (BluetoothGattDescriptor)d;
            Log.d("Bluebit","Writing Descriptor");
            _peripheral.writeDescriptor(w);
        }
        else {
            tempQueueObject w = (tempQueueObject)d;
            w.write();
        }
    }

    public String version() {
        return _version;
    }
}
