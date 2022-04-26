package com.intention.android.goodmask.model

import android.Manifest

class Constant{
    companion object{
        // used to identify adding bluetooth names
        const val REQUEST_ENABLE_BT = 1
        // used to request fine location permission
        const val REQUEST_ALL_PERMISSION = 2
        val PERMISSIONS = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION
        )

        const val CHARACTERISTIC_COMMAND_STRING = "49535343-8841-43F4-A8D4-ECBE34729BB3"
        const val CHARACTERISTIC_RESPONSE_STRING = "49535343-8841-43F4-A8D4-ECBE34729BB3"
        const val SERVICE_UUID = "49535343-8841-43F4-A8D4-ECBE34729BB3"

    }
}