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

package com.intention.android.goodmask.issc.impl;

import com.intention.android.goodmask.activity.MainActivity;
import com.intention.android.goodmask.issc.Bluebit;
import com.intention.android.goodmask.issc.gatt.Gatt;
import com.intention.android.goodmask.issc.gatt.GattAdapter;
import com.intention.android.goodmask.issc.gatt.Gatt.Listener;
import com.intention.android.goodmask.issc.gatt.GattCharacteristic;
import com.intention.android.goodmask.issc.gatt.GattDescriptor;
import com.intention.android.goodmask.issc.gatt.GattService;
import com.intention.android.goodmask.issc.impl.aosp.AospGattAdapter;
import com.intention.android.goodmask.issc.impl.test.FakeGattAdapter;
import com.intention.android.goodmask.issc.util.Log;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

import kotlin.jvm.internal.Intrinsics;

/**
 * This class is a wrapper that handles Gatt related operations.
 *
 * Upper layer just use this service so that they do not have to
 * deal with Gatt by themself.
 */
public class LeService extends Service {

    private IBinder mBinder ;

    private boolean mGattReady = false;
    private GattAdapter mGattAdapter = null;
    public Gatt mGatt = null;
    private Gatt.Listener mCallback;

    private List<Listener> mListeners;
    private Object mLock;
    private String address;
    private String status;
    public BluetoothDevice device;
    private Context ctx;
    private String msg;

    @Override
    public void onCreate() {
        super.onCreate();
        mLock = new Object();
        mCallback   = new TheCallback();
        mGattAdapter = new AospGattAdapter(this, mCallback);
        mListeners  = new ArrayList<Listener>();

        mBinder = new LocalBinder();
        Log.d("LeService", "LeService is created");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("onDestroy", "onDestroy LeService");
        releaseGatt();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        boolean fake = intent.getBooleanExtra(Bluebit.USE_FAKE, false);

        if (fake) {
            Log.d("Use FakeGattAdapter for testing");
            mGattAdapter = new FakeGattAdapter(this, mCallback);
        } else {
            Log.d("Use AospGattAdapter");
            mGattAdapter = new AospGattAdapter(this, mCallback);
        }
        Log.d("LeService", "start service");

        return Service.START_STICKY;
    }

    private final void startForeground() {
        this.channelRegister();
        PendingIntent contentIntent = PendingIntent.getActivity((Context)this, 0, new Intent((Context)this, MainActivity.class), PendingIntent.FLAG_IMMUTABLE);
        this.msg = "미세먼지 " + this.status;
        if (Intrinsics.areEqual(this.status, "나쁨") || Intrinsics.areEqual(this.status, "매우 나쁨")) {
            this.msg = "미세먼지 수치가 " + this.status + " 이므로 펜 세기를 높여보세요.";
        }

        NotificationCompat.Builder var10000;
        String var10001;
        Notification var3;
        if (Build.VERSION.SDK_INT >= 26) {
            var10000 = (new NotificationCompat.Builder((Context)this, "notification channel")).setContentTitle((CharSequence)("현재위치 " + this.address));
            var10001 = this.msg;
            if (var10001 == null) {
                Intrinsics.throwUninitializedPropertyAccessException("msg");
            }

            var3 = var10000.setContentText((CharSequence)var10001).setSmallIcon(700077).setContentIntent(contentIntent).build();
        } else {
            var10000 = (new NotificationCompat.Builder((Context)this)).setContentTitle((CharSequence)("현재위치 " + this.address));
            var10001 = this.msg;
            if (var10001 == null) {
                Intrinsics.throwUninitializedPropertyAccessException("msg");
            }

            var3 = var10000.setContentText((CharSequence)var10001).setSmallIcon(700077).setContentIntent(contentIntent).build();
        }

        Intrinsics.checkNotNullExpressionValue(var3, "if (Build.VERSION.SDK_IN…       .build()\n        }");
        Notification notification = var3;
        this.startForeground(1, notification);
    }

    private final void channelRegister() {
        String channelName = "Service channel name";
        if (Build.VERSION.SDK_INT >= 26) {
            @SuppressLint("WrongConstant") NotificationChannel channel = new NotificationChannel("notification channel", (CharSequence)channelName, 0);
            @SuppressLint("WrongConstant") Object var10000 = this.getSystemService("notification");
            if (var10000 == null) {
                throw new NullPointerException("null cannot be cast to non-null type android.app.NotificationManager");
            }

            NotificationManager manager = (NotificationManager)var10000;
            manager.createNotificationChannel(channel);
        }

    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public void addListener(Listener l) {
        synchronized(mLock) {
            mListeners.add(l);
        }
    }

    public boolean rmListener(Listener l) {
        synchronized(mLock) {
            return mListeners.remove(l);
        }
    }

    private void releaseGatt() {
        synchronized(mLock) {
            mGattReady = false;
            if (mGatt != null) {
                mGatt.disconnect();
                mGatt.close();
            }
            mGatt.disconnect();
            mGatt.close();
            mGatt = null;
        }
    }

    /**
     * Invoke this method to initialize Gatt before using Gatt.
     *
     * FIXME: right now we support connect to just 1 device.
     */
    public Gatt connectGatt(Context ctx, boolean auto, BluetoothDevice dev) {
        if (mGatt != null) {
            closeGatt(dev);
        }
        device = dev;
        mGatt = mGattAdapter.connectGatt(ctx, auto, mCallback, dev);
        Log.d("mGatt", "mGatt : "+mGatt);

        return mGatt;
    }

    public void closeGatt(BluetoothDevice device) {
        /* In general, there should be an activity that close an Gatt when
         * onDestroy. However, if user press-back-key too fast, this Service will
         * release Gatt before destroying the activity, therefore Gatt might be null
         * when activity do closing Gatt.*/
        if (mGatt != null) {
            mGatt.close();
            mGatt = null;
        }
    }

    public boolean startScan(GattAdapter.LeScanCallback clbk) {
        return mGattAdapter.startLeScan(clbk);
    }

    public void stopScan(GattAdapter.LeScanCallback clbk) {
        mGattAdapter.stopLeScan(clbk);
    }

    public boolean connect(BluetoothDevice device, boolean auto) {
        return mGatt.connect();
    }
    public boolean requestMtu(BluetoothDevice device, int mtu) {
        return mGatt.requestMtu(mtu);
    }

    public void disconnect(BluetoothDevice device) {
        /* In general, there should be an activity that disconnect from an Gatt when
         * onDestroy. However, if user press-back-key too fast, this Service will
         * release Gatt before destroying the activity, therefore Gatt might be null
         * when activity do disconnecting.*/
        if (mGatt != null) {
            mGatt.disconnect();
        }
    }

    public Gatt getGatt() {
		return mGatt;
	}



	public List<BluetoothDevice> getConnectedDevices() {
        return mGattAdapter.getConnectedDevices();
    }

    public boolean discoverServices(BluetoothDevice device) {
        return mGatt.discoverServices();
    }

    public int getConnectionState(BluetoothDevice device) {
        Log.d("LeService", mGattAdapter+"is null");
        this.device = device;
        return mGattAdapter.getConnectionState(device);
    }

    public GattService getService(BluetoothDevice device, UUID uuid) {
        return mGatt.getService(uuid);
    }

    public List<GattService> getServices(BluetoothDevice device) {
        return mGatt.getServices();
    }

    public boolean readCharacteristic(GattCharacteristic chr) {
        return mGatt.readCharacteristic(chr);
    }

    public boolean writeCharacteristic(GattCharacteristic chr) {
        return mGatt.writeCharacteristic(chr);
    }

    public boolean readDescriptor(GattDescriptor dsc) {
        return mGatt.readDescriptor(dsc);
    }

    public boolean writeDescriptor(GattDescriptor dsc) {
        Log.d("mGatt.writeDescriptor : LeService");
        return mGatt.writeDescriptor(dsc);
    }

    public boolean setCharacteristicNotification(GattCharacteristic chr, boolean enable) {
    	if (mGatt != null && chr != null) {
            Log.d("mGatt.setCharacteristicNotification : LeService");
            return mGatt.setCharacteristicNotification(chr, enable);
		}
    	return false;
    }

    /* This is the only one callback that register to GATT. It dispatch each
     * of returen value to listeners. */
    class TheCallback implements Gatt.Listener {

        public void onMtuChanged (Gatt Gatt, int mtu, int status) {
            Log.d("onMtuChanged", "MTU: " + mtu + "status: " + status);
            synchronized(mListeners) {
                Iterator<Listener> it = mListeners.iterator();
                while(it.hasNext()) {
                    it.next().onMtuChanged(Gatt,mtu,status);                }
            }
        }
        @Override
        public void onCharacteristicChanged(Gatt gatt, GattCharacteristic chrc) {
            synchronized(mListeners) {
                Iterator<Listener> it = mListeners.iterator();
                while(it.hasNext()) {
                    it.next().onCharacteristicChanged(gatt, chrc);
                }
            }
        }

        @Override
        public void onCharacteristicRead(Gatt gatt, GattCharacteristic chrc, int status) {
            synchronized(mListeners) {
                Iterator<Listener> it = mListeners.iterator();
                while(it.hasNext()) {
                    it.next().onCharacteristicRead(gatt, chrc, status);
                }
            }
        }

        @Override
        public void onCharacteristicWrite(Gatt gatt, GattCharacteristic chrc, int status) {
            synchronized(mListeners) {
                Iterator<Listener> it = mListeners.iterator();
                while(it.hasNext()) {
                    it.next().onCharacteristicWrite(gatt, chrc, status);
                }
            }
        }

        @Override
        public void onConnectionStateChange(Gatt gatt, int status, int newState) {
            synchronized(mListeners) {
                Iterator<Listener> it = mListeners.iterator();
                while(it.hasNext()) {
                    it.next().onConnectionStateChange(gatt, status, newState);
                }
            }
        }

        @Override
        public void onDescriptorRead(Gatt gatt, GattDescriptor descriptor, int status) {
            synchronized(mListeners) {
                Iterator<Listener> it = mListeners.iterator();
                while(it.hasNext()) {
                    it.next().onDescriptorRead(gatt, descriptor, status);
                }
            }
        }

        @Override
        public void onDescriptorWrite(Gatt gatt, GattDescriptor descriptor, int status) {
            synchronized(mListeners) {
                Iterator<Listener> it = mListeners.iterator();
                while(it.hasNext()) {
                    it.next().onDescriptorWrite(gatt, descriptor, status);
                }
            }
        }

        @Override
        public void onReadRemoteRssi(Gatt gatt,  int rssi, int status) {
            synchronized(mListeners) {
                Iterator<Listener> it = mListeners.iterator();
                while(it.hasNext()) {
                    it.next().onReadRemoteRssi(gatt, rssi, status);
                }
            }
        }

        @Override
        public void onServicesDiscovered(Gatt gatt,  int status) {
            synchronized(mListeners) {
                Iterator<Listener> it = mListeners.iterator();
                while(it.hasNext()) {
                    it.next().onServicesDiscovered(gatt, status);
                }
            }
        }
    }

    public class LocalBinder extends Binder {
        public LeService getService() {
            return LeService.this;
        }
    }
}
