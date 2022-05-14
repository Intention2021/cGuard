// vim: et sw=4 sts=4 tabstop=4
/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.intention.android.goodmask.issc.impl.aosp;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;

import com.intention.android.goodmask.issc.gatt.Gatt;
import com.intention.android.goodmask.issc.gatt.GattCharacteristic;
import com.intention.android.goodmask.issc.gatt.GattDescriptor;
import com.intention.android.goodmask.issc.gatt.GattService;
import com.intention.android.goodmask.issc.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AospGatt implements Gatt {

    private BluetoothGatt mGatt;
    private BluetoothDevice mDevice;
    private Listener mListener;
    public Object getImpl() {
    	return mGatt;
    }

    public AospGatt(BluetoothGatt gatt) {
        mGatt = gatt;
    }

    public boolean requestMtu(int mtu) {
        // MBD

        //if (sdk version < 21) return
        if (android.os.Build.VERSION.SDK_INT >= 21) {
            // Use APIs supported by API level 11 (Android 3.0) and up
            return mGatt.requestMtu( mtu);
        } else {
            // Do something different to support older versions
            return false;
        }


    }
    @Override
    public void close() {
        mGatt.close();
    }

    @Override
    public boolean connect() {
        return mGatt.connect();
    }

    @Override
    public void disconnect() {
        mGatt.disconnect();
    }

    @Override
    public boolean discoverServices() {
        return mGatt.discoverServices();
    }

    @Override
    public BluetoothDevice getDevice() {
        return mGatt.getDevice();
    }

    @Override
    public GattService getService(UUID uuid) {
        return new AospGattService(mGatt.getService(uuid));
    }

    @Override
    public List<GattService> getServices() {
        List<BluetoothGattService> srvs = mGatt.getServices();
        ArrayList<GattService> list = new ArrayList<GattService>();
        for (BluetoothGattService srv: srvs) {
            list.add(new AospGattService(srv));
        }

        return list;
    }

    @Override
    public boolean readCharacteristic(GattCharacteristic chr) {
        return mGatt.readCharacteristic(
                (BluetoothGattCharacteristic)chr.getImpl());
    }

    @Override
    public boolean readDescriptor(GattDescriptor dsc) {
        return mGatt.readDescriptor((BluetoothGattDescriptor)dsc.getImpl());
    }

    @Override
    public boolean setCharacteristicNotification(GattCharacteristic chr, boolean enable) {
        Log.d("mGatt.setCharacteristicNotification : AospGatt");
        boolean iret;
        BluetoothGattCharacteristic characteristic;

                iret = mGatt.setCharacteristicNotification(
                (BluetoothGattCharacteristic)chr.getImpl(), enable);

        Log.d("iret = " + iret);
        Log.d("UUID = "+chr.getUuid());

        if (chr.getImpl() == null)
            Log.d("chr.getImpl() is null");

       /* characteristic = (BluetoothGattCharacteristic)chr.getImpl();
        //Check the UUID is proper, charactrestic or service ??????
        //BluetoothGattDescriptor descriptor=characteristic.getDescriptor((chr.getUuid()));
        AospGattCharacteristic   descriptor = (AospGattCharacteristic)chr.getDescriptor((chr.getUuid()));
        if (descriptor != null) {
            Log.d("ITS NOT NULL!!!!!!!");
        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE); }
        else {
            Log.d("ITS NULL!!!!!!!");

            mGatt.writeDescriptor( (BluetoothGattDescriptor)(descriptor));
            //mGatt.writeDescriptor(descriptor);
        }*/
        return iret;
    }

    @Override
    public boolean writeCharacteristic(GattCharacteristic chr) {

        Log.d("mGatt.writeCharacteristic : AospGatt");
        return mGatt.writeCharacteristic(
                (BluetoothGattCharacteristic)chr.getImpl());
    }

    @Override
    public boolean writeDescriptor(GattDescriptor dsc) {
        Log.d("mGatt.writeDescriptor : AospGatt");
        return mGatt.writeDescriptor((BluetoothGattDescriptor)dsc.getImpl());
    }
}

