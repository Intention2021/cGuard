package com.intention.android.goodmask.activity

import DeviceController
import android.Manifest
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationRequest
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.intention.android.goodmask.R
import com.intention.android.goodmask.databinding.ActivityMainBinding
import com.intention.android.goodmask.fragment.HomeFragment
import com.intention.android.goodmask.fragment.MaskFragment
import com.intention.android.goodmask.fragment.NotificationFragment
import com.intention.android.goodmask.fragment.StaticsFragment
import java.util.*
import java.io.IOException




class MainActivity : AppCompatActivity() {
    public lateinit var binding: ActivityMainBinding
    val homeFragment = HomeFragment()
    val staticsFragment = StaticsFragment()
    val notificationFragment = NotificationFragment()
    lateinit var deviceController : DeviceController
    val maskFragment = MaskFragment()
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    var addressList: List<String> = listOf("서울시", "중구", "명동")
    var addressInfo: String = "서울시 중구 명동"
    private lateinit var device : BluetoothDevice
    private val multiplePermissionCode = 100
    lateinit var geocoder: Geocoder


    // 권한 목록
    private val requiredPermissionsList = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        device = intent.getParcelableExtra<BluetoothDevice>("device")!!
        deviceController = DeviceController(Handler(), device)

        if(deviceController.btSocket!!.isConnected) Toast.makeText(this, "${deviceController.device.name}가 연결되었습니다.", Toast.LENGTH_LONG).show()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        allowPermissions()
        binding.bnvMain.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.frag_homeground -> {
                    replaceFragment(homeFragment)
                    deviceController.deController.write("homefrag".toByteArray())
                    true
                }
                R.id.frag_stat -> {
                    deviceController.deController.write("statfrag".toByteArray())
                    replaceFragment(staticsFragment)
                    true
                }
                R.id.frag_noti -> {
                    deviceController.deController.write("notifrag".toByteArray())
                    replaceFragment(notificationFragment)
                    true
                }
                R.id.frag_masks -> {
                    deviceController.deController.write("maskfrag".toByteArray())
                    replaceFragment(maskFragment)
                    true
                }
                else -> false
            }
        }
    }

    // 최초 실행시 사용자에게 권한 묻기
    private fun allowPermissions() {
        ActivityCompat.requestPermissions(this, requiredPermissionsList, multiplePermissionCode)
    }


    // 권한이 허가되어있으면 좌표 가져오기
    private fun getLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    geocoder = Geocoder(this, Locale.KOREA)
                    // 위치 좌표 구하기
                    val locationRequest =
                        com.google.android.gms.location.LocationRequest.create()
                    locationRequest.priority =
                        com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
                    // 10분마다 좌표 갱신
                    // locationRequest.interval = 600 * 1000
                    fusedLocationClient.requestLocationUpdates(
                        locationRequest,
                        locationCallback,
                        Looper.getMainLooper()
                    )

                }
        }

    }

    val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            for (location in locationResult.locations) {
                if (location != null) {
                    var latitude = location.latitude
                    var longtitude = location.longitude

                    val addrList = geocoder.getFromLocation(latitude, longtitude, 1)
                    for (addr in addrList) {
                        val splitedAddr = addr.getAddressLine(0).split(" ")
                        addressList = splitedAddr
                    }
                    addressInfo = "${addressList[1]} ${addressList[2]} ${addressList[3]}"
                    Log.d("Test", addressInfo)
                    dataToFragHome(latitude, longtitude, addressInfo)

                    // 포그라운드에 현 위치 데이터 전달하기 위함
                    val intent = Intent(this@MainActivity, ForegroundActivity::class.java)
                    intent.putExtra("address", addressInfo)
                    intent.putExtra("device", device)
                    Log.e("Give Data", "데이터 전달 $addressInfo")
                    replaceFragment(homeFragment)
                }
            }
        }
    }

    // homeFragment로 데이터 전달
    private fun dataToFragHome(lat: Double, long: Double, addressInfo: String) {
        var bundle = Bundle()
        bundle.putDouble("latitude", lat)
        bundle.putDouble("longitude", long)
        bundle.putString("address", addressInfo)
        homeFragment.arguments = bundle
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        val locationPermissionGranted =
            requestCode == multiplePermissionCode && grantResults[0] == PackageManager.PERMISSION_GRANTED
        // 권한을 허락하지 않으면 종료
        if (!locationPermissionGranted) {
            Toast.makeText(this, "앱 설정에서 위치정보 권한에 동의해야 사용 가능합니다.", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            getLocation()
        }
    }

    // fragment 변경
    public fun replaceFragment(fragment: Fragment) {
        val tran = supportFragmentManager.beginTransaction()
        tran.replace(R.id.fl_container, fragment)
        tran.commit()
    }
}