package com.intention.android.goodmask.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.intention.android.goodmask.R
import com.intention.android.goodmask.databinding.ActivitySplashBinding
import java.lang.Thread.sleep

class SplashActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySplashBinding
    val PERMISSIONS_REQUEST_CODE = 100
    var REQUIRED_PERMISSIONS = arrayOf<String>( Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)

    private fun requestPermission(){
        ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, PERMISSIONS_REQUEST_CODE )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        val locationPermissionGranted =
            requestCode == PERMISSIONS_REQUEST_CODE && grantResults[0] == PackageManager.PERMISSION_GRANTED
        // 권한을 허락하지 않으면 종료
        if (!locationPermissionGranted) {
            Toast.makeText(this, "앱 설정에서 위치정보 권한에 동의해야 사용 가능합니다.", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            var handler = Handler()
            handler.postDelayed({
                var intent = Intent(this, DeviceActivity::class.java)
                startActivity(intent)
                finish()
            }, 1500)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        requestPermission()
    }
}