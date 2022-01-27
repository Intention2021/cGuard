package com.intention.android.goodmask.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.intention.android.goodmask.R
import com.intention.android.goodmask.databinding.ActivitySplashBinding
import java.lang.Thread.sleep

class SplashActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySplashBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        var handler = Handler()
        handler.postDelayed({
            var intent = Intent(this, DeviceActivity::class.java)
            startActivity(intent)
            finish()
        }, 1500)

    }
}