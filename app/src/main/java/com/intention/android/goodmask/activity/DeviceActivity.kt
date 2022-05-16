package com.intention.android.goodmask.activity

import DeviceController
import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.intention.android.goodmask.R
import com.intention.android.goodmask.adapter.BleListAdapter
import com.intention.android.goodmask.databinding.ActivityDeviceBinding
import com.intention.android.goodmask.databinding.ActivityMainBinding
import com.intention.android.goodmask.db.DeviceDB
import com.intention.android.goodmask.db.MaskDB
import com.intention.android.goodmask.model.DeviceData
import com.intention.android.goodmask.model.MaskData
import retrofit2.Retrofit
import java.sql.Timestamp
import java.util.*


class DeviceActivity : AppCompatActivity() {

    private var maskDB : MaskDB? = null
    private var maskList = mutableListOf<MaskData>()
    private var ddb: DeviceDB ?= null
    private var deviceConnectivity: Boolean = false
    private var handler = Handler()
    private var deviceID: String = ""
    private lateinit var load : ConstraintLayout
    private lateinit var binding: ActivityDeviceBinding
    private fun PackageManager.missingSystemFeature(name: String): Boolean = !hasSystemFeature(name)
    private val bluetoothAdapter: BluetoothAdapter? by lazy(LazyThreadSafetyMode.NONE) {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    private val mReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {

            val action = intent.action
            if(BluetoothDevice.ACTION_FOUND == action) {
                val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                var existence : Boolean = false
                for (dev in leDeviceListAdapter.items!!){
                    if(device?.address == dev.address || device?.name == null){
                        existence = true
                        break
                    }
                }
                if(!existence && device?.name != "BM64_DSPK v2.1.2_GATT")leDeviceListAdapter.addDevice(device)
                leDeviceListAdapter.notifyDataSetChanged()
                Log.d("hihi","${device?.name}")
            }
        }
    }

    lateinit var leDeviceListAdapter: BleListAdapter
    private val BluetoothAdapter.isDisabled: Boolean
        get() = !isEnabled

    public fun checkLocPermission() {
        if (ActivityCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION,
            )
            == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
            == PackageManager.PERMISSION_GRANTED
        ){
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            requestPermissions(
                arrayOf(
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_ADVERTISE,
                    Manifest.permission.BLUETOOTH_CONNECT
                ),
                1
            )
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(
                arrayOf(
                    Manifest.permission.BLUETOOTH
                ),
                1
            )
        }
    }

    override fun onResume() {
        super.onResume()
        packageManager.takeIf { it.missingSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE) }?.also {
            Toast.makeText(this,"기기가 블루투스를 지원하지 않아 디바이스 등록이 불가합니다.", Toast.LENGTH_SHORT).show()
            deviceConnectivity = false
            checkDeviceConnectivity("None")
        }
        // Ensures Bluetooth is available on the device and it is enabled. If not,
        // displays a dialog requesting user permission to enable Bluetooth.
        bluetoothAdapter?.takeIf { it.isDisabled }?.apply {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, 1)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(mReceiver)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkLocPermission()

        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        registerReceiver(mReceiver, filter)
        ddb = DeviceDB.getInstance(this)
        binding = ActivityDeviceBinding.inflate(layoutInflater)
        setContentView(binding.root)
        var linear = binding.parennnt
        load = binding.door
        leDeviceListAdapter = BleListAdapter(linear.context)
        scanDevice()
        var deviceAdderBtn = binding.deviceAdder
        deviceAdderBtn.setOnClickListener {
            scanDevice()
        }
        checkDeviceConnectivity(deviceID)

        var deviceBLE = binding.deviceList
        leDeviceListAdapter.setItemClickListener(object : BleListAdapter.ItemClickListener{
            override fun onClick(view: View, device: BluetoothDevice?, position:Int) {
                val intent = Intent(applicationContext, MainActivity::class.java)
                intent.putExtra("device", device)
                Toast.makeText(applicationContext, "${device?.name}에 연결합니다.", Toast.LENGTH_LONG).show()
                // 연결한 기기를 DB에 추가
                val r = Runnable {
                    Log.e("New Device", device.toString())
                    val newDevice = DeviceData(device.toString())
                    ddb?.DeviceDao()?.insert(newDevice)
                }
                val thread = Thread(r)
                thread.start()
                startActivity(intent)
                finish()
            }

        })
        deviceBLE.adapter = leDeviceListAdapter
    }


    // device scan
    private fun scanDevice() {
        leDeviceListAdapter.items?.clear()
        val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter?.bondedDevices
        pairedDevices?.forEach { device ->
            leDeviceListAdapter.addDevice(device)
        }
        bluetoothAdapter?.startDiscovery()
        scanLeDevice(true)
        // 기존에 사용했던 기기 탐색, 있으면 그 기기를 mainActivity로 인텐트
        val r = Runnable {
            val data = ddb?.DeviceDao()?.getAll()
            Log.e("저장된 기기들", "$data")
            Log.e("leDevice", leDeviceListAdapter.items.toString())
            val cc = ddb?.DeviceDao()?.checkDevice("CC:88:26:B8:8D:B0")
            val dd = ddb?.DeviceDao()?.findDevice("CC:88:26:B8:8D:B0")
            Log.e("EE", "$cc / $dd")
            // 블루투스 연결 가능한 기기들 중 연결한 적 있는 기기들이 있는지 확인
            for (i in 0 until leDeviceListAdapter.items!!.size){
                var check = ddb?.DeviceDao()?.findDevice(leDeviceListAdapter.items!![i].toString())
                Log.e("Check", check.toString())
                if (leDeviceListAdapter.items!![i].toString() == check){
                    val intent = Intent(applicationContext, MainActivity::class.java)
                    intent.putExtra("device", leDeviceListAdapter.items!![i])
                    runOnUiThread {Toast.makeText(applicationContext, "${leDeviceListAdapter.items!![i].name}에 연결합니다.", Toast.LENGTH_LONG).show()}
                    Log.e("Existed Device", "Free Pass!")
                    startActivity(intent)
                    finish()
                }
            }
        }
        val thread = Thread(r)
        thread.start()

        leDeviceListAdapter.items
        load.visibility = View.VISIBLE
        Handler().postDelayed({
            load.visibility = View.GONE
        }, 5000)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    // device가 연결되어 있는지 확인
    public fun checkDeviceConnectivity(deviceID: String) {
        if (deviceConnectivity) {
            handler.postDelayed({
                var intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }, 0)
        }
    }

    private val SCAN_PERIOD: Long = 10000
    private var mScanning: Boolean = false
    private fun scanLeDevice(enable: Boolean) {
        val leScanCallback = BluetoothAdapter.LeScanCallback { device, rssi, scanRecord ->
                var existence : Boolean = true
                if (device.name != null){
                    for(dev in leDeviceListAdapter.items!!){
                        if(device.address == dev.address){
                            existence = false
                            break
                        }
                    }
                }else existence = false



                if(existence && device.name != "BM64_DSPK v2.1.2_GATT") {
                    leDeviceListAdapter.addDevice(device)
                    leDeviceListAdapter.notifyDataSetChanged()
                }

        }

        bluetoothAdapter?.startDiscovery()
        when (enable) {
            true -> {
                // Stops scanning after a pre-defined scan period.
                handler.postDelayed({
                    mScanning = false
                    bluetoothAdapter?.stopLeScan(leScanCallback)
                }, SCAN_PERIOD)
                mScanning = true
                bluetoothAdapter?.startLeScan(leScanCallback)
            }
            else -> {
                mScanning = false
                bluetoothAdapter?.stopLeScan(leScanCallback)
            }
        }
    }
}