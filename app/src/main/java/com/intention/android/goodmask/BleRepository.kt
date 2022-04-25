package com.intention.android.goodmask

import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.MutableLiveData
import com.intention.android.goodmask.activity.BleService
import com.intention.android.goodmask.util.Event
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class BleRepository {

    private val TAG = "BleRepository"

    var statusTxt: String = ""

    var isConnected = MutableLiveData<Event<Boolean>>()

    var isRead = false
    var isStatusChange: Boolean = false


    var deviceToConnect: BluetoothDevice? = null
    var cmdByteArray: ByteArray? = null

    val readDataFlow = MutableLiveData<String>()
    val fetchStatusText = flow{
        while(true) {
            if(isStatusChange) {
                emit(statusTxt)
                isStatusChange = false
            }
        }
    }.flowOn(IO)







    /**
     * Handles various events fired by the Service.
     */
    private var mGattUpdateReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        @RequiresApi(Build.VERSION_CODES.O)
        override fun onReceive(context: Context, intent: Intent) {
            Log.d(TAG,"action ${intent.action}")
            when(intent.action){
                Actions.GATT_CONNECTED-> {
                    isConnected.postValue(Event(true))
                    intent.getStringExtra(Actions.MSG_DATA)?.let {
                        statusTxt = it
                        isStatusChange = true
                    }
                }
                Actions.GATT_DISCONNECTED->{
                    stopForegroundService()
                    isConnected.postValue(Event(false))
                    intent.getStringExtra(Actions.MSG_DATA)?.let{
                        statusTxt = it
                        isStatusChange = true
                    }
                }
                Actions.STATUS_MSG->{
                    intent.getStringExtra(Actions.MSG_DATA)?.let{
                        statusTxt = it
                        isStatusChange = true
                    }
                }
                Actions.READ_CHARACTERISTIC->{
                    intent.getByteArrayExtra(Actions.READ_BYTES)?.let{ bytes->
                        val hexString: String = bytes.joinToString(separator = " ") {
                            String.format("%02X", it)
                        }
                        readDataFlow.postValue(hexString)
                    }
                }

            }

        }
    }



    fun registerGattReceiver(){
        BleApplication.applicationContext().registerReceiver(mGattUpdateReceiver,
            makeGattUpdateIntentFilter())
    }
    fun unregisterReceiver(){
        BleApplication.applicationContext().unregisterReceiver(mGattUpdateReceiver)
    }
    private fun makeGattUpdateIntentFilter(): IntentFilter {
        val intentFilter = IntentFilter()
        intentFilter.addAction(Actions.GATT_CONNECTED)
        intentFilter.addAction(Actions.GATT_DISCONNECTED)
        intentFilter.addAction(Actions.STATUS_MSG)
        intentFilter.addAction(Actions.READ_CHARACTERISTIC)
        return intentFilter
    }




    /**
     * Connect to the ble device
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun connectDevice(device: BluetoothDevice?) {
        deviceToConnect = device
        startForegroundService()
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun startForegroundService(){
        Intent(BleApplication.applicationContext(), BleService::class.java).also { intent ->
            intent.action = Actions.START_FOREGROUND
            BleApplication.applicationContext().startForegroundService(intent)
        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    fun stopForegroundService(){
        Intent(BleApplication.applicationContext(), BleService::class.java).also { intent ->
            intent.action = Actions.STOP_FOREGROUND
            BleApplication.applicationContext().startForegroundService(intent)
        }
    }

    /**
     * Disconnect Gatt Server
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun disconnectGattServer() {
        Log.d(TAG, "disconnect device : ${deviceToConnect?.name}")
        deviceToConnect = null
        Intent(BleApplication.applicationContext(), BleService::class.java).also { intent ->
            intent.action = Actions.DISCONNECT_DEVICE
            BleApplication.applicationContext().startForegroundService(intent)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun writeData(byteArray: ByteArray){
        cmdByteArray = byteArray
        Intent(BleApplication.applicationContext(), BleService::class.java).also { intent ->
            //MyApplication.applicationContext().bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE)
            intent.action = Actions.WRITE_DATA
            BleApplication.applicationContext().startForegroundService(intent)
        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    fun readToggle(){
        if(isRead){
            Log.d("readcmd", "Stop Notification")
            isRead = false
            stopNotification()
        }else{
            Log.d("readcmd", "Start Notification")
            isRead = true
            startNotification()
        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun startNotification(){
        Intent(BleApplication.applicationContext(), BleService::class.java).also { intent ->
            //MyApplication.applicationContext().bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE)
            intent.action = Actions.START_NOTIFICATION
            BleApplication.applicationContext().startForegroundService(intent)
        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun stopNotification(){
        Intent(BleApplication.applicationContext(), BleService::class.java).also { intent ->
            //MyApplication.applicationContext().bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE)
            intent.action = Actions.STOP_NOTIFICATION
            BleApplication.applicationContext().startForegroundService(intent)
        }
    }

}