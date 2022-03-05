import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.ContentValues.TAG
import android.os.Bundle
import android.os.Handler
import android.util.Log
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*

class DeviceController(
    // handler that gets info from Bluetooth service
    public val handler: Handler,
    public val device: BluetoothDevice
) {
    val MESSAGE_READ: Int = 0
    val MESSAGE_WRITE: Int = 1
    val MESSAGE_TOAST: Int = 2
    public var flag = false
    val btSocket : BluetoothSocket? by lazy(LazyThreadSafetyMode.NONE) {
        device.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"))
    }
    public var deController = ConnectedThread(btSocket!!)

    init {
        try {
            btSocket?.connect()
            Log.d("제발", "device : $device connected")
            flag = true
        } catch (e: IOException) {
            e.printStackTrace()
            flag = false
            Log.d("제발", "device : ${device.name} not connected")
        }
        if (flag) {
            deController.start()
            deController.write("abcdefg".toByteArray())
        }
    }

    public inner class ConnectedThread(private val mmSocket: BluetoothSocket) : Thread() {
        private val mmInStream: InputStream = mmSocket.inputStream
        private val mmOutStream: OutputStream = mmSocket.outputStream
        private val mmBuffer: ByteArray = ByteArray(1024) // mmBuffer store for the stream

        override fun run() {
            var numBytes: Int // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs.
            while (true) {
                // Read from the InputStream.
                numBytes = try {
                    mmInStream.read(mmBuffer)
                } catch (e: IOException) {
                    Log.d("제발", "Input stream was disconnected", e)
                    break
                }
                Log.d("제발", "read : "+numBytes.toString())

                // Send the obtained bytes to the UI activity.
                val readMsg = handler.obtainMessage(
                    MESSAGE_READ, numBytes, -1,
                    mmBuffer)
                readMsg.sendToTarget()
            }
        }

        // Call this from the main activity to send data to the remote device.
        fun write(bytes: ByteArray) {
            try {
                Log.d("제발", "write : " + bytes.toString())
                mmOutStream.write(bytes)
            } catch (e: IOException) {
                Log.e("제발", "Error occurred when sending data", e)

                // Send a failure message back to the activity.
                val writeErrorMsg = handler.obtainMessage(MESSAGE_TOAST)
                val bundle = Bundle().apply {
                    putString("toast", "Couldn't send data to the other device")
                }
                writeErrorMsg.data = bundle
                handler.sendMessage(writeErrorMsg)
                return
            }

            // Share the sent message with the UI activity.
            val writtenMsg = handler.obtainMessage(
                MESSAGE_WRITE, -1, -1, mmBuffer)
            writtenMsg.sendToTarget()
        }

        // Call this method from the main activity to shut down the connection.
        fun cancel() {
            try {
                mmSocket.close()
            } catch (e: IOException) {
                Log.e(TAG, "Could not close the connect socket", e)
            }
        }
    }
}