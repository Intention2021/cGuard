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

import com.intention.android.goodmask.issc.gatt.Gatt;
import com.intention.android.goodmask.issc.gatt.GattAdapter;
import com.intention.android.goodmask.issc.gatt.Gatt.Listener;
import com.intention.android.goodmask.issc.gatt.GattCharacteristic;
import com.intention.android.goodmask.issc.gatt.GattDescriptor;
import com.intention.android.goodmask.issc.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;

public class AospGattAdapter implements GattAdapter {

    private Context mContext;

    private AospGatt mGattInterface;
    private BluetoothGatt mGatt;
    private BluetoothGattCallback mCallback;
    private BluetoothDevice realDevice;
    private Listener mListener;
    private ScanCallback mScanCallback;
    private Handler mTimeoutHandler;
    private Runnable mTimeoutRunnable;
    
    private Object mLock;

	public AospGattAdapter(Context ctx, Listener listener) {
        mContext = ctx;
        mLock = new Object();
        mCallback = new AospCallback();
        mScanCallback = new ScanCallback();
        mListener = listener;
    }

    @Override
    public Gatt connectGatt(Context ctx, boolean autoConnect, Listener listener, BluetoothDevice dev) {
        mListener = listener;
        realDevice = dev;
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            // only for gingerbread and newer versions
            mGatt = dev.connectGatt(ctx, autoConnect, mCallback,2);
        }
        else {
            mGatt = dev.connectGatt(ctx, autoConnect, mCallback);
        }
        mGattInterface = new AospGatt(mGatt);
        /*Method connectGattMethod;
        try {
            connectGattMethod = dev.getClass().getMethod("connectGatt", Context.class, boolean.class, BluetoothGattCallback.class, int.class);
        } catch (NoSuchMethodException e) {
            //NoSuchMethod
        }

        try {
            mGatt = (BluetoothGatt) connectGattMethod.invoke(dev, this, false, mCallback, 2); // (2 == LE, 1 == BR/EDR)
        } catch (IllegalAccessException e) {
            //IllegalAccessException
        } catch (IllegalArgumentException e) {
            //IllegalArgumentException
        } catch (InvocationTargetException e) {
            //InvocationTargetException
        }*/
        /*mTimeoutHandler = new Handler(Looper.getMainLooper());
        mTimeoutRunnable = new Runnable() {
			
			@Override
			public void run() {
				mGatt.disconnect();
				mGatt.connect();
				
			}
		};
        mTimeoutHandler.postDelayed(mTimeoutRunnable, 5*1000);*/
        return mGattInterface;
    }


    @Override
    public boolean startLeScan(GattAdapter.LeScanCallback callback) {
        mScanCallback.setListener(callback);
        BluetoothManager mgr = (BluetoothManager)mContext.getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter adapter = mgr.getAdapter();
        return adapter.startLeScan(mScanCallback);
    }

    @Override
    public void stopLeScan(GattAdapter.LeScanCallback callback) {
        mScanCallback.setListener(null);
        BluetoothManager mgr = (BluetoothManager)mContext.getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter adapter = mgr.getAdapter();
        adapter.stopLeScan(mScanCallback);
    }

    @Override
    public int getConnectionState(BluetoothDevice device) {
        BluetoothManager mgr = (BluetoothManager)mContext.getSystemService(Context.BLUETOOTH_SERVICE);
        Log.d("bluetoothconnection", ""+mgr);
        return mgr.getConnectionState(device, BluetoothProfile.GATT);
    }

    @Override
    public List<BluetoothDevice> getConnectedDevices() {
        BluetoothManager mgr = (BluetoothManager)mContext.getSystemService(Context.BLUETOOTH_SERVICE);
        return mgr.getConnectedDevices(BluetoothProfile.GATT);
    }

    class ScanCallback implements BluetoothAdapter.LeScanCallback {
        private GattAdapter.LeScanCallback mScanCallback;

        public void setListener(GattAdapter.LeScanCallback clbk) {
            mScanCallback = clbk;
        }

        public void onLeScan(BluetoothDevice dev, int rssi, byte[] records) {
            if (mScanCallback != null) {
                mScanCallback.onLeScan(dev, rssi, records);
            }
        }
    }

    /* This is the only one callback that register to GATT Profile. It dispatch each
     * of returen value to listeners. */
    class AospCallback extends BluetoothGattCallback {

        @Override
        public void onCharacteristicChanged(BluetoothGatt Gatt, BluetoothGattCharacteristic chrc) {
            //Log.i("onCharacteristicChanged: AospGattadapter ");
            GattCharacteristic c = new AospGattCharacteristic(chrc);
            mListener.onCharacteristicChanged(mGattInterface, c);
        }

        public void onMtuChanged (BluetoothGatt Gatt, int mtu, int status) {
            Log.d("onMtuChanged", "MTU: " + mtu + "status: " + status);
            mListener.onMtuChanged(mGattInterface,mtu,status);

            /*Context context = getActivity().getApplicationContext();
            CharSequence text = "Applied the UUID as per input";
            int duration = Toast.LENGTH_SHORT;

            Toast toast = Toast.makeText(context, text, duration);
            toast.show();*/

        }

        @Override
        public void onCharacteristicRead(BluetoothGatt Gatt, BluetoothGattCharacteristic chrc, int status) {
            GattCharacteristic c = new AospGattCharacteristic(chrc);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                mListener.onCharacteristicRead(mGattInterface, c, Gatt.GATT_SUCCESS);
            } else {
                mListener.onCharacteristicRead(mGattInterface, c, status);
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt Gatt, BluetoothGattCharacteristic chrc, int status) {
            GattCharacteristic c = new AospGattCharacteristic(chrc);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                mListener.onCharacteristicWrite(mGattInterface, c, Gatt.GATT_SUCCESS);
            } else {
                mListener.onCharacteristicWrite(mGattInterface, c, status);
            }
        }

        @Override
        public void onConnectionStateChange(BluetoothGatt Gatt, int status, int newState) {
        	Log.i("status = "+ status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                mListener.onConnectionStateChange(mGattInterface, Gatt.GATT_SUCCESS, newState);
                if (newState == Gatt.STATE_DISCONNECTED) {
                	if (mTimeoutHandler != null && mTimeoutRunnable != null) {
                        mTimeoutHandler.removeCallbacks(mTimeoutRunnable);
                        mTimeoutRunnable = null;
                        mTimeoutHandler = null;
					}
				}
            } else {
                mListener.onConnectionStateChange(mGattInterface, status, newState);
                mGatt.connect();
            }
        }

        @Override
        public void onDescriptorRead(BluetoothGatt Gatt, BluetoothGattDescriptor descriptor, int status) {
            GattDescriptor dsc = new AospGattDescriptor(descriptor);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                mListener.onDescriptorRead(mGattInterface, dsc, Gatt.GATT_SUCCESS);
            } else {
                mListener.onDescriptorRead(mGattInterface, dsc, status);
            }
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt Gatt, BluetoothGattDescriptor descriptor, int status) {
            GattDescriptor dsc = new AospGattDescriptor(descriptor);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                mListener.onDescriptorWrite(mGattInterface, dsc, Gatt.GATT_SUCCESS);
            } else {
                mListener.onDescriptorWrite(mGattInterface, dsc, status);
                if (status == 133) {
                	mGatt.disconnect();
				}
             }
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt Gatt, int rssi, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                mListener.onReadRemoteRssi(mGattInterface, rssi, Gatt.GATT_SUCCESS);
            } else {
                mListener.onReadRemoteRssi(mGattInterface, rssi, status);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                mListener.onServicesDiscovered(mGattInterface, Gatt.GATT_SUCCESS);
                Log.i("onServicesDiscovered: AospGattadapter ");
                if (mTimeoutHandler != null && mTimeoutRunnable != null) {
                    Log.i("mTimeoutRunnable and mTimeoutHandler not null  ");
                    mTimeoutHandler.removeCallbacks(mTimeoutRunnable);
                    mTimeoutRunnable = null;
                    mTimeoutHandler = null;
                }
            } else {
                mListener.onServicesDiscovered(mGattInterface, status);
            }
        }
    }
}

