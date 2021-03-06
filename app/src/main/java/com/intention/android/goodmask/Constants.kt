package com.intention.android.goodmask


class Constants {

}
//사용자 BLE UUID Service/Rx/Tx
const val SERVICE_UUID = "49535343-fe7d-4ae5-8fa9-9fafd205e455"

const val CHARACTERISTIC_COMMAND_STRING = "49535343-8841-43F4-A8D4-ECBE34729BB3"
const val CHARACTERISTIC_RESPONSE_STRING = "49535343-8841-43F4-A8D4-ECBE34729BB3"
const val SERVICE_STRING = "49535343-8841-43F4-A8D4-ECBE34729BB3"
const val GENERIC_ACCESS = "00001800-0000-1000-8000-00805f9b34fb"
const val GENERIC_ATTRIBUTE = "00001801-0000-1000-8000-00805f9b34fb"
const val SERVICE_CHANGED = "00002a05-0000-1000-8000-00805f9b34fb"
const val SERVICE_FAN = "00002a00-0000-1000-8000-00805f9b34fb"
const val CHARACTERISTIC_READ_WRITE = "49535343-6DAA-4D02-ABF6-19569ACA69FE"
const val READ_NOTIFY = "49535343-1e4d-4bd9-ba61-23c647249616"
const val WRITE_NOTIFY = "49535343-aca3-481c-91ec-d85e28a60318"
const val WRITE = "49535343-8841-43F4-A8D4-ECBE34729BB3"
const val WRITE_NOTIFIED = "49535343-026E-3A9B-954C-97DAEF17E26E"

//BluetoothGattDescriptor 고정
const val CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb"