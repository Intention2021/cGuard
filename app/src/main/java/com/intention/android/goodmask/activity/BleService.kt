package com.intention.android.goodmask.activity
import android.app.*
import android.bluetooth.*
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.graphics.Color
import android.os.*
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.PRIORITY_MIN
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.intention.android.goodmask.*
import com.intention.android.goodmask.util.BluetoothUtils
import com.intention.android.goodmask.viewmodel.BleViewModel
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*


class BleService : Service() {
    private val mBinder: IBinder = LocalBinder()

    private lateinit var msg: String
    public var address : String = ""
    public var status : String = ""
    public var device : BluetoothDevice? = null
    public var connectivity = false

    companion object {
        const val NOTIFICATION_ID = 1
        const val CHANNEL_ID = "notification channel"
    }

    private val bleRepository: BleRepository by inject()

    // ble Gatt
    private var bleGatt: BluetoothGatt? = null


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Action Received = ${intent?.action}")

        if (intent?.getStringExtra("address") != null && intent.getStringExtra("dustStatus") != null){
            address = intent.getStringExtra("address")!!
            status = intent.getStringExtra("dustStatus")!!
        }


        when (intent?.action) {
            Actions.START_FOREGROUND -> {
                startForegroundService()
            }
            Actions.STOP_FOREGROUND -> {
                stopForegroundService()

            }
            Actions.DISCONNECT_DEVICE->{
                disconnectGattServer("Disconnected")
            }
            Actions.START_NOTIFICATION->{
                startNotification()
            }
            Actions.STOP_NOTIFICATION->{
                stopNotification()
            }
            Actions.WRITE_DATA->{
                bleRepository.cmdByteArray?.let { writeData(it) }
            }
        }
        return START_STICKY
    }

    private fun startForegroundService() {
        startForeground()
    }

    private fun stopForegroundService() {
        stopForeground(true)
        stopSelf()
    }

    /**
     * Class used for the client Binder. The Binder object is responsible for returning an instance
     * of "MyService" to the client.
     */
    inner class LocalBinder : Binder() {
        // Return this instance of MyService so clients can call public methods
        val service: BleService
            get() = this@BleService
    }

    /**
     * This is how the client gets the IBinder object from the service. It's retrieve by the "ServiceConnection"
     * which you'll see later.
     */
    override fun onBind(intent: Intent): IBinder {
        return mBinder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.d(TAG, "onUnbind called")
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy called")
        super.onDestroy()
    }

    private fun broadcastUpdate(action: String, msg: String) {
        val intent = Intent(action)
        intent.putExtra(Actions.MSG_DATA, msg)
        sendBroadcast(intent)
    }
    private fun broadcastUpdate(action: String, readBytes: ByteArray) {
        val intent = Intent(action)
        intent.putExtra(Actions.READ_BYTES, readBytes)
        sendBroadcast(intent)
    }


    /**
     * BLE gattClientCallback
     */
    private val gattClientCallback: BluetoothGattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)

            if (status == BluetoothGatt.GATT_FAILURE) {
                disconnectGattServer("Bluetooth Gatt Failure")
                return
            } else if (status != BluetoothGatt.GATT_SUCCESS) {
                disconnectGattServer("Disconnected")
                return
            }
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                // update the connection status message
                broadcastUpdate(Actions.GATT_CONNECTED, "Connected")
                Log.d("connect", "Connected to the GATT server, ${bleRepository.deviceToConnect?.name}")
                val handler = Handler(Looper.getMainLooper())
                handler.postDelayed(Runnable {
                    Toast.makeText(applicationContext, "${bleRepository.deviceToConnect?.name}에 연결되었습니다.", Toast.LENGTH_LONG).show()
                }, 0)
                gatt.discoverServices()
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.d("disconnect", "Disconnect to the device")
                broadcastUpdate(Actions.GATT_DISCONNECTED, "Disconnected")
            }
            else {
                val handler = Handler(Looper.getMainLooper())
                handler.postDelayed(Runnable {
                    Toast.makeText(applicationContext, "${bleRepository.deviceToConnect?.name}에 연결할 수 없습니다.", Toast.LENGTH_SHORT).show()
                }, 0)
                ActivityCompat.finishAffinity(Activity())
                val intent = Intent(baseContext, DeviceActivity::class.java)
                startActivity(intent.addFlags(FLAG_ACTIVITY_NEW_TASK))
                System.exit(0)
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            super.onServicesDiscovered(gatt, status)

            Log.d("blegatt", "${bleGatt}")
            // check if the discovery failed
            if (status != BluetoothGatt.GATT_SUCCESS) {
                disconnectGattServer("Device service discovery failed, status: $status")
                return
            }
            // log for successful discovery
            bleGatt = gatt
            Log.d(TAG, "Services discovery is successful")



        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic
        ) {
            super.onCharacteristicChanged(gatt, characteristic)
            Log.d("change", "characteristic changed: " + characteristic.uuid.toString())
            readCharacteristic(characteristic)
        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            super.onCharacteristicWrite(gatt, characteristic, status)
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "Characteristic written successfully")
            } else {
                disconnectGattServer("Characteristic write unsuccessful, status: $status")
            }
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            super.onCharacteristicRead(gatt, characteristic, status)
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "Characteristic read successfully")
                readCharacteristic(characteristic)
            } else {
                Log.e(TAG, "Characteristic read unsuccessful, status: $status")
                // Trying to read from the Time Characteristic? It doesnt have the property or permissions
                // set to allow this. Normally this would be an error and you would want to:
                // disconnectGattServer()
            }
        }

        /**
         * Log the value of the characteristic
         * @param characteristic
         */
        private fun readCharacteristic(characteristic: BluetoothGattCharacteristic) {
            val msg = characteristic.value
            broadcastUpdate(Actions.READ_CHARACTERISTIC ,msg)
            Log.d("read/write", "read: ${String(msg)}")
            sendMSG(String(msg))
        }


    }

    private fun sendMSG(readMSG: String) {
        val intent = Intent("sendMSG")
        intent.putExtra("msg", readMSG)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    /**
     * Connect to the ble device
     */
    private fun connectDevice(device: BluetoothDevice?) {
        // update the status
        broadcastUpdate(Actions.STATUS_MSG, "Connecting to ${device?.address}")
        bleGatt = device?.connectGatt(BleApplication.applicationContext(), false, gattClientCallback)
        Log.d("blegatt", "${bleGatt}")
    }


    /**
     * Disconnect Gatt Server
     */
    fun disconnectGattServer(msg: String) {
        Log.d("hereigo", "Closing Gatt connection")
        // disconnect and close the gatt

        Log.d("hereigo", "${bleGatt}")
        if(bleGatt != null){
            bleGatt!!.disconnect()
            bleGatt!!.close()
        }else {
            val handler = Handler(Looper.getMainLooper())
            handler.postDelayed(Runnable {
                Toast.makeText(applicationContext, "${bleRepository.deviceToConnect?.name}에 연결할 수 없습니다.", Toast.LENGTH_SHORT).show()
            }, 0)
            ActivityCompat.finishAffinity(Activity())
            val intent = Intent(baseContext, DeviceActivity::class.java)
            startActivity(intent.addFlags(FLAG_ACTIVITY_NEW_TASK))
            System.exit(0)
        }
        Log.d("hereigo", "${bleGatt}")
        stopForegroundService()
        broadcastUpdate(Actions.GATT_DISCONNECTED, msg)
    }

    private fun writeData(cmdByteArray: ByteArray) {
        var cmdCharacteristic : BluetoothGattCharacteristic?
        Log.d("bleservice", "blegatt : ${bleGatt.toString()}")
        if(bleGatt == null) {
            cmdCharacteristic = null
            val handler = Handler(Looper.getMainLooper())
            handler.postDelayed(Runnable {
                Toast.makeText(applicationContext, "${bleRepository.deviceToConnect?.name}에 연결할 수 없습니다.", Toast.LENGTH_SHORT).show()
            }, 0)
            return
        }
        else cmdCharacteristic  = BluetoothUtils.findCommandCharacteristic(bleGatt!!)
        Log.d("bleservice", "blegatt : ${bleGatt}, cmdCharacteristic : ${cmdCharacteristic}, cmdByteArray : ${String(cmdByteArray)}")
        // disconnect if the characteristic is not found
        if (cmdCharacteristic == null) {
            bleRepository.cmdByteArray = null
            disconnectGattServer("Unable to find cmd characteristic")
            return
        }

        cmdCharacteristic.value = cmdByteArray
        val success: Boolean = bleGatt!!.writeCharacteristic(cmdCharacteristic)
        // check the result
        if (!success) {
            Log.e(TAG, "Failed to write command")
        }
        else Log.d("read/write", "write : ${String(cmdCharacteristic.value)}")
        bleRepository.cmdByteArray = null

    }
    private fun startNotification(){
        // find command characteristics from the GATT server
        Log.d("readcmd", "Start Notification in bleservice")
        val respCharacteristic = bleGatt?.let { BluetoothUtils.findResponseCharacteristic(it) }
        Log.d("readcmd", "repCharacteristic : ${respCharacteristic}")

        // disconnect if the characteristic is not found
        if (respCharacteristic == null) {
            Log.d("readcmd", "disconnee")
            disconnectGattServer("Unable to find characteristic")
            return
        }
        // READ
        bleGatt?.setCharacteristicNotification(respCharacteristic, true)
    }

    private fun stopNotification(){
        // find command characteristics from the GATT server
        val respCharacteristic = bleGatt?.let { BluetoothUtils.findResponseCharacteristic(it) }
        // disconnect if the characteristic is not found
        if (respCharacteristic == null) {
            disconnectGattServer("Unable to find characteristic")
            return
        }
        bleGatt?.setCharacteristicNotification(respCharacteristic, false)
    }

    private fun startForeground(){
        channelRegister()
        val contentIntent = PendingIntent.getActivity(this, 0, Intent(this, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE)

        msg = "미세먼지 $status"
        if(status == "나쁨" || status == "매우 나쁨") {
            msg = "미세먼지 수치가 $status 이므로 펜 세기를 높여보세요."
        }
        // 오레오 버전 이상 (요즘 안드로이드는 다 오레오 버전 이상임)
        val notification = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("현재위치 $address")
                .setContentText(msg)
                .setSmallIcon(R.drawable.goodmask_icon)
                .setContentIntent(contentIntent)
                .build()
        }
        // 오레오 버전 미만
        else {
            NotificationCompat.Builder(this)
                .setContentTitle("현재위치 $address")
                .setContentText(msg)
                .setSmallIcon(R.drawable.goodmask_icon)
                .setContentIntent(contentIntent)
                .build()
        }
        startForeground(1, notification)

        //connect
        connectDevice(bleRepository.deviceToConnect)
    }

    // 오레오 버전 이상이면 체널 등록
    private fun channelRegister() {
        val channelName = "Service channel name"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // 체널
            val channel = NotificationChannel(CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_DEFAULT)
            // 알림 매니저
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            // 알림 매니저에 체널 등록
            manager.createNotificationChannel(channel)
        }
    }
}