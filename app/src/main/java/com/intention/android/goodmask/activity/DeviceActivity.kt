package com.intention.android.goodmask.activity

import DeviceController
import android.Manifest
import android.app.ListActivity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothClass
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContentProviderCompat.requireContext
import com.intention.android.goodmask.adapter.BleListAdapter
import com.intention.android.goodmask.databinding.ActivityDeviceBinding
import com.intention.android.goodmask.model.Constant.Companion.REQUEST_ENABLE_BT
import java.util.*
import android.content.IntentFilter
import android.content.BroadcastReceiver
import android.opengl.Visibility
import androidx.constraintlayout.widget.ConstraintLayout
import com.intention.android.goodmask.db.MaskDB
import com.intention.android.goodmask.model.MaskData
import android.os.Build
import android.os.Parcelable
import com.intention.android.goodmask.util.LoadingDialog


class DeviceActivity : AppCompatActivity() {

    private var maskDB : MaskDB? = null

    private var deviceConnectivity: Boolean = true
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
                Log.d("블루투스", "Device : ${device?.name}")
                for (dev in leDeviceListAdapter.items!!){
                    if(device?.address == dev.address || device?.name == null){
                        existence = true
                        break
                    }
                }
                if(!existence)leDeviceListAdapter.addDevice(device)
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
        bluetoothAdapter?.startDiscovery()
        packageManager.takeIf { it.missingSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE) }?.also {
            Toast.makeText(this,"기기가 블루투스를 지원하지 않아 디바이스 등록이 불가합니다.", Toast.LENGTH_SHORT).show()
            deviceConnectivity = false
            checkDeviceConnectivity("None")
        }
        // Ensures Bluetooth is available on the device and it is enabled. If not,
        // displays a dialog requesting user permission to enable Bluetooth.
        bluetoothAdapter?.takeIf { it.isDisabled }?.apply {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(mReceiver)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkLocPermission()

        binding = ActivityDeviceBinding.inflate(layoutInflater)
        val linear = binding.parennnt
        leDeviceListAdapter = BleListAdapter(linear.context)
        setContentView(binding.root)
        load = binding.door

        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        registerReceiver(mReceiver, filter)

        scanDevice()
        //checkMask()
        val deviceAdderBtn = binding.deviceAdder
        deviceAdderBtn.setOnClickListener {
            scanDevice()
        }
        checkDeviceConnectivity(deviceID)

        val deviceBLE = binding.deviceList
        leDeviceListAdapter.setItemClickListener(object : BleListAdapter.ItemClickListener{
            override fun onClick(view: View, device: BluetoothDevice?, position:Int) {
                val connectingDialog = LoadingDialog(this@DeviceActivity)
                connectingDialog.show()
                connectingDialog.dismiss()
                val deContoller : DeviceController = DeviceController(Handler(), device!!)
                if(deContoller.btSocket!!.isConnected){
                    val inten = Intent(applicationContext, MainActivity::class.java)
                    inten.putExtra("device", device)
                    val r = Runnable {
                        if(maskDB?.MaskDao()?.findMaskWithName(device.name)?.name != device.name){
                            maskDB?.MaskDao()?.insert(MaskData(device.address, device.name))
                        }
                    }
                    val thread = Thread(r)
                    thread.start()
                    thread.destroy()

                    deContoller.deController.cancel()
                    connectingDialog.hide()
                    startActivity(inten)
                    finish()
                }
                else {
                    deContoller.deController.cancel()
                    connectingDialog.hide()
                    Toast.makeText(applicationContext, "${device.name}이 연결되지 않습니다.", Toast.LENGTH_LONG).show()
                }
            }

        })
        deviceBLE.adapter = leDeviceListAdapter
    }

    private fun checkMask() {
        maskDB = MaskDB.getInstance(this)

        val r = Runnable {
            val savedMask = maskDB!!.MaskDao().getAll()
            Log.d("MaskDB", "maskDB : $savedMask[0]")
            if(savedMask.isNotEmpty()){

                for(dev in leDeviceListAdapter.items!!){
                    if(dev.name == savedMask[0].name){
                        val intent = Intent(this, MainActivity::class.java)
                        intent.putExtra("device", dev)
                        startActivity(intent)
                        finish()
                    }
                }
            }
        }
        val thread = Thread(r)
        thread.start()
        thread.destroy()
    }

    // device scan
    private fun scanDevice() {
        bluetoothAdapter?.startDiscovery()
        load.visibility = View.VISIBLE
        val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter?.bondedDevices
        pairedDevices?.forEach { device ->
            leDeviceListAdapter.addDevice(device)
        }
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
        if (!deviceConnectivity) {
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

                if(existence) {
                    leDeviceListAdapter.addDevice(device)
                    Log.d("제발나와라ㅇㅖ~~","items : ${leDeviceListAdapter.items}")
                }
                leDeviceListAdapter.notifyDataSetChanged()
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