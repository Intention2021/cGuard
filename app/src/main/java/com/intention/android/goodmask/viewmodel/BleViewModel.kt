package com.intention.android.goodmask.viewmodel

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.Build
import android.os.ParcelUuid
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.*
import com.intention.android.goodmask.BleApplication
import com.intention.android.goodmask.BleRepository
import com.intention.android.goodmask.SERVICE_STRING
import com.intention.android.goodmask.di.viewModelModule
import com.intention.android.goodmask.util.Event
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.schedule

class BleViewModel(private val bleRepository: BleRepository) : ViewModel() {

    val TAG = "BleViewModel"

    val statusTxt: LiveData<String> = bleRepository.fetchStatusText.asLiveData(viewModelScope.coroutineContext)

    val readTxt: LiveData<String> = bleRepository.readDataFlow
    val isRead: Boolean
        get() = bleRepository.isRead

    val _isConnect : LiveData<Event<Boolean>>
        get() = bleRepository.isConnected

    // ble manager
    val bleManager: BluetoothManager =
        BleApplication.applicationContext().getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    // ble adapter
    val bleAdapter: BluetoothAdapter?
        get() = bleManager.adapter


    private val _requestEnableBLE = MutableLiveData<Event<Boolean>>()
    val requestEnableBLE : LiveData<Event<Boolean>>
        get() = _requestEnableBLE

    private val _listUpdate = MutableLiveData<Event<ArrayList<BluetoothDevice>?>>()
    val listUpdate : LiveData<Event<ArrayList<BluetoothDevice>?>>
        get() = _listUpdate

    // scan results
    var scanResults: ArrayList<BluetoothDevice>? = ArrayList()

    /**
     *  Start BLE Scan
     */
    @RequiresApi(Build.VERSION_CODES.M)
    fun onClickScan(){
        startScan()
    }

    fun startScan() {
        // check ble adapter and ble enabled
        if (bleAdapter == null || !bleAdapter?.isEnabled!!) {
            _requestEnableBLE.postValue(Event(true))
            bleRepository.statusTxt="Scanning Failed: ble not enabled"
            bleRepository.isStatusChange = true
            return
        }
        //scan filter
        val filters: MutableList<ScanFilter> = ArrayList()
        val scanFilter: ScanFilter = ScanFilter.Builder()
            .setServiceUuid(ParcelUuid(UUID.fromString(SERVICE_STRING)))
            .build()
        filters.add(scanFilter)
        // scan settings
        // set low power scan mode
        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
            .build()
        // start scan
        bleAdapter?.bluetoothLeScanner?.startScan(filters, settings, BLEScanCallback)
        //bleAdapter?.bluetoothLeScanner?.startScan(BLEScanCallback)

        bleRepository.statusTxt = "Scanning...."
        bleRepository.isStatusChange = true

        Timer("SettingUp", false).schedule(3000) { stopScan() }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun stopScan(){
        bleAdapter?.bluetoothLeScanner?.stopScan(BLEScanCallback)
        bleRepository.statusTxt = "Scan finished. Click on the name to connect to the device."
        bleRepository.isStatusChange = true

        scanResults = ArrayList() //list 초기화
        Log.d(TAG, "BLE Stop!")
    }

    /**
     * BLE Scan Callback
     */
    private val BLEScanCallback: ScanCallback = @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            Log.i(TAG, "Remote device name: " + result.device.name)
            addScanResult(result)
        }

        override fun onBatchScanResults(results: List<ScanResult>) {
            for (result in results) {
                addScanResult(result)
            }
        }

        override fun onScanFailed(_error: Int) {
            Log.e(TAG, "BLE scan failed with code $_error")
            bleRepository.statusTxt = "BLE scan failed with code $_error"
            bleRepository.isStatusChange = true
        }

        /**
         * Add scan result
         */
        private fun addScanResult(result: ScanResult) {
            // get scanned device
            val device = result.device
            // get scanned device MAC address
            val deviceAddress = device.address
            val deviceName = device.name
            // add the device to the result list
            for (dev in scanResults!!) {
                if (dev.address == deviceAddress) return
            }
            scanResults?.add(result.device)
            // log
            bleRepository.statusTxt = "add scanned device: $deviceAddress"
            bleRepository.isStatusChange = true
            _listUpdate.postValue(Event(scanResults))
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun onClickDisconnect(){
        bleRepository.disconnectGattServer()
    }
    @RequiresApi(Build.VERSION_CODES.O)
    fun connectDevice(bluetoothDevice: BluetoothDevice){
        bleRepository.connectDevice(bluetoothDevice)
    }

    fun registBroadCastReceiver(){
        bleRepository.registerGattReceiver()
    }
    fun unregisterReceiver(){
        bleRepository.unregisterReceiver()
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun onClickRead(){
        bleRepository.readToggle()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun onClickWrite(){
        val cmdBytes = ByteArray(2)
        cmdBytes[0] = 1
        cmdBytes[1] = 2
        bleRepository.writeData(cmdBytes)
    }
}
