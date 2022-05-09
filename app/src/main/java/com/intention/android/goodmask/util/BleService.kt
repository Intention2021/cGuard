package com.intention.android.goodmask.util

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder

class BleService : Service() {
    // Binder given to clients (notice class declaration below)
    private val mBinder: IBinder = LocalBinder()


    /**
     * Class used for the client Binder. The Binder object is responsible for returning an instance
     * of "MyService" to the client.
     */
    inner class LocalBinder : Binder() {
        // Return this instance of MyService so clients can call public methods
        val service: BleService
            get() = this@BleService
    }

    /**
     * This is how the client gets the IBinder object from the service. It's retrieve by the "ServiceConnection"
     * which you'll see later.
     */
    override fun onBind(intent: Intent): IBinder {
        return mBinder
    }

}