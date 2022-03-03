package com.intention.android.goodmask.activity

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.widget.RemoteViews
import android.widget.TextView
import com.intention.android.goodmask.R
import com.intention.android.goodmask.databinding.ForegroundBinding
import com.intention.android.goodmask.fragment.HomeFragment

class MyService : Service() {
    companion object{
        const val NOTIFICATION_ID = 10
        const val CHANNEL_ID = "notification channel"
    }
    override fun onCreate() {
        super.onCreate()
        Log.d("Service Test", "MyService class is started!")
        startNotification()
    }

    private fun startNotification(){
        channelRegister()
        val contentIntent = PendingIntent.getActivity(this, 0, Intent(this, HomeFragment::class.java), PendingIntent.FLAG_IMMUTABLE)
        val view = RemoteViews(packageName, R.layout.foreground)

        // 오레오 버전 이상 (요즘 안드로이드는 다 오레오 버전 이상임)
        val notification = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.goodmask_icon)
                .setCustomContentView(view)
                .setContentIntent(contentIntent)
                .build()
        }
        // 오레오 버전 미만
        else{
            Notification.Builder(this)
                .setSmallIcon(R.drawable.goodmask_icon)
                .setContent(view)
                .setContentIntent(contentIntent)
                .build()
        }
        startForeground(NOTIFICATION_ID, notification)
    }

    // 오레오 버전 이상이면 체널 등록
    private fun channelRegister(){
        val channelName = "Service channel name"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            // 체널
            val channel = NotificationChannel(CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_DEFAULT)
            // 알림 매니저
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            // 알림 매니저에 체널 등록
            manager.createNotificationChannel(channel)
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}