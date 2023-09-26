package fi.shaynek.graphlab

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.util.Log

class BleGatt(private val application: MyApplication){
    private var bleGatt: BluetoothGatt? = null


    private  val gattClientCallback = object: BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {


            super.onConnectionStateChange(gatt, status, newState)

            if (status == BluetoothGatt.GATT_FAILURE) {
                Log.d("DBG", "GATT connection failure")
                return
            } else if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d("DBG", "GATT connection success")
                return
            }
        }
        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            super.onServicesDiscovered(gatt, status)
            if (status != BluetoothGatt.GATT_SUCCESS) {
                return
            }
            Log.d("DBG", "onServicesDiscovered()")
            for (gattService in gatt.services) {

            }
        }
        override fun onDescriptorWrite(gatt: BluetoothGatt, descriptor: BluetoothGattDescriptor, status: Int) {Log.d("DBG", "onDescriptorWrite")
        }
        override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {Log.d("DBG", "Characteristic data received")
        }
    }
}