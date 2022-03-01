package com.intention.android.goodmask.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.intention.android.goodmask.databinding.ForegroundBinding

class ForegroundActivity : AppCompatActivity() {
    private lateinit var binding : ForegroundBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ForegroundBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val address = intent.getStringExtra("address")
        val status = intent.getStringExtra("dustStatus")
        // 서비스 클래스에서 가면 xml에 접근이 안되고, 여기서 해봐도 안되네여??ㅋㅋㅋㅋ(여긴 로그도 안찍힘)
        binding.content.text = address
        binding.title.text = status
    }
}