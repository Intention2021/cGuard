package com.intention.android.goodmask.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.intention.android.goodmask.R
import com.intention.android.goodmask.databinding.ActivityMainBinding
import com.intention.android.goodmask.fragment.HomeFragment
import com.intention.android.goodmask.fragment.MaskFragment
import com.intention.android.goodmask.fragment.NotificationFragment
import com.intention.android.goodmask.fragment.StaticsFragment

class MainActivity : AppCompatActivity() {
    public lateinit var binding: ActivityMainBinding
    val homeFragment = HomeFragment()
    val staticsFragment = StaticsFragment()
    val notificationFragment = NotificationFragment()
    val maskFragment = MaskFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        replaceFragment(homeFragment)
        binding.bnvMain.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.frag_homeground -> {
                    replaceFragment(homeFragment)
                    true
                }
                R.id.frag_stat -> {
                    replaceFragment(staticsFragment)
                    true
                }
                R.id.frag_noti -> {
                    replaceFragment(notificationFragment)
                    true
                }
                R.id.frag_masks -> {
                    replaceFragment(maskFragment)
                    true
                }
                else -> false
            }
        }
    }

    // fragment 변경
    public fun replaceFragment(fragment: Fragment) {
        val tran = supportFragmentManager.beginTransaction()
        tran.replace(R.id.fl_container, fragment)
        tran.commit()
    }
}