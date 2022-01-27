package com.intention.android.goodmask.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.intention.android.goodmask.R
import com.intention.android.goodmask.databinding.ActivityDeviceBinding

class DeviceActivity : AppCompatActivity() {

    var deviceConnctivity: Boolean = true
    var deviceID: String = ""
    private lateinit var binding: ActivityDeviceBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDeviceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkDeviceConnectivity(deviceID)

        if (deviceConnctivity) {
            var handler = Handler()
            handler.postDelayed({
                var intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }, 0)
        }
    }

    // device가 연결되어 있는지 확인
    public fun checkDeviceConnectivity(deviceID: String) {

    }

    // device 연결
    public fun deviceConnector() {

    }
}