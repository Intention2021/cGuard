package com.intention.android.goodmask.util

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.util.Log
import com.intention.android.goodmask.*
import java.util.*

class BluetoothUtils {
    companion object {

        /**
         * Find command characteristic of the peripheral device
         * @param gatt gatt instance
         * @return found characteristic
         */
        fun findCommandCharacteristic(gatt: BluetoothGatt): BluetoothGattCharacteristic? {
            Log.d("helloe", "gatt : ${gatt}, ")
            return findCharacteristic(gatt, WRITE_NOTIFY)
        }

        /**
         * Find response characteristic of the peripheral device
         * @param gatt gatt instance
         * @return found characteristic
         */
        fun findResponseCharacteristic(gatt: BluetoothGatt): BluetoothGattCharacteristic? {
            return findCharacteristic(gatt, WRITE_READ_NOTIFY)
        }

        /**
         * Find the given uuid characteristic
         * @param gatt gatt instance
         * @param uuidString uuid to query as string
         */
        private fun findCharacteristic(
            gatt: BluetoothGatt,
            uuidString: String
        ): BluetoothGattCharacteristic? {
            val serviceList = gatt.services
            val service = findGattService(serviceList)
            val characteristicList = service?.characteristics
            Log.d("blecharacter", "character: ${characteristicList}")
            if(characteristicList !=null){
                for (characteristic in characteristicList!!) {
                    Log.d("character", "${characteristic}")
                    Log.d("readcmd", "characteristic.uuid :  ${characteristic.uuid}, uuidString : ${uuidString}")
                    if (characteristic.uuid.toString() == uuidString) {
                        return characteristic
                    }
                }
            }
            return null
        }

        /**
         * Match the given characteristic and a uuid string
         * @param characteristic one of found characteristic provided by the server
         * @param uuidString uuid as string to match
         * @return true if matched
         */
        private fun matchCharacteristic(
            characteristic: BluetoothGattCharacteristic?,
            uuidString: String
        ): Boolean {
            if (characteristic == null) {
                return false
            }
            val uuid: UUID = characteristic.uuid
            Log.d("UUID", "UUID : ${uuid}, uuidString : ${uuidString}")
            return matchUUIDs(uuid.toString(),
                CHARACTERISTIC_COMMAND_STRING,
                CHARACTERISTIC_RESPONSE_STRING,
                SERVICE_UUID,
                SERVICE_STRING,
                GENERIC_ACCESS,
                GENERIC_ATTRIBUTE,
                SERVICE_CHANGED,
                SERVICE_FAN,
                WRITE_READ_NOTIFY,
                WRITE_NOTIFY
            )
        }

        /**
         * Find Gatt service that matches with the server's service
         * @param serviceList list of services
         * @return matched service if found
         */
        private fun findGattService(serviceList: List<BluetoothGattService>): BluetoothGattService? {
            for (service in serviceList) {
                Log.d("service", "service : ${service}")
                val serviceUuidString = service.uuid.toString()
                Log.d("service", "serviceuuidstring : ${serviceUuidString}")
                if (matchServiceUUIDString(serviceUuidString)) {
                    return service
                }
            }
            return null
        }

        /**
         * Try to match the given uuid with the service uuid
         * @param serviceUuidString service UUID as string
         * @return true if service uuid is matched
         */
        private fun matchServiceUUIDString(serviceUuidString: String): Boolean {
            return matchUUIDs(serviceUuidString,
                SERVICE_UUID)
        }

        /**
         * Check if there is any matching characteristic
         * @param characteristic query characteristic
         */
        private fun isMatchingCharacteristic(characteristic: BluetoothGattCharacteristic?): Boolean {
            if (characteristic == null) {
                return false
            }
            val uuid: UUID = characteristic.uuid
            return matchCharacteristicUUID(uuid.toString())
        }

        /**
         * Query the given uuid as string to the provided characteristics by the server
         * @param characteristicUuidString query uuid as string
         * @return true if the matched is found
         */
        private fun matchCharacteristicUUID(characteristicUuidString: String): Boolean {
            return matchUUIDs(
                characteristicUuidString,
                CHARACTERISTIC_COMMAND_STRING,
                CHARACTERISTIC_RESPONSE_STRING,
                SERVICE_UUID,
                SERVICE_STRING,
                GENERIC_ACCESS,
                GENERIC_ATTRIBUTE,
                SERVICE_CHANGED,
                SERVICE_FAN,
                WRITE_NOTIFY,
                WRITE_READ_NOTIFY
            )
        }

        /**
         * Try to match a uuid with the given set of uuid
         * @param uuidString uuid to query
         * @param matches a set of uuid
         * @return true if matched
         */
        private fun matchUUIDs(uuidString: String, vararg matches: String): Boolean {
            for (match in matches) {
                if (uuidString.equals(match, ignoreCase = true)) {
                    return true
                }
            }
            return false
        }
    }
}
