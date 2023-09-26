package fi.shaynek.graphlab

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import fi.shaynek.graphlab.MyApplication
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.UUID

// Handles all the scanning of the bluetooth devices
class AppBluetoothManager(
    private val bluetoothAdapter: BluetoothAdapter,
    private val application: MyApplication
) {
    // displays a list of scanned devices
    val scanResult: MutableStateFlow<MutableMap<String, ScanResult>> =
        MutableStateFlow(mutableMapOf())
    val heartRateResult: MutableStateFlow<MutableList<Int>> =
        MutableStateFlow(mutableListOf())

    val userMessage = MutableStateFlow<String?>(null)

    val HEART_RATE_SERVICE_UUID = convertFromInteger(0x180D)
    val HEART_RATE_MEASUREMENT_CHAR_UUID = convertFromInteger(0x2A37)
    val CLIENT_CHARACTERISTIC_CONFIG_UUID = convertFromInteger(0x2902)

    /* Generates 128-bit UUID from the Protocol Indentifier (16-bit number)* and the BASE_UUID (00000000-0000-1000-8000-00805F9B34FB)
    */
    private fun convertFromInteger(i: Int): UUID {
        val MSB = 0x0000000000001000L
        val LSB = -0x7fffff7fa064cb05L
        val value = (i and -0x1).toLong()
        return UUID(MSB or (value shl 32), LSB)
    }

    fun UUID.toGss() =
        this.toString()
            .replaceFirst(Regex("^0+(?!$)"), "")
            .replace("-0000-1000-8000-00805f9b34fb", "")

    fun ByteArray.toHex(): String =
        "0x" + joinToString(separator = "") { eachByte -> "%02X".format(eachByte).uppercase() }

    fun ByteArray.toInteger(): Int = Integer.decode(this.toHex())

    private val scanCallback = object : ScanCallback() {
        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)

            println("[AppBluetoothManager] onScanFailed")
            println("There is a scan failed/error $errorCode")
        }

        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)

            val copied = scanResult.value.toMutableMap()
            copied[result.device.address] = result
            scanResult.value = copied
        }
    }

    private val gattCallback = object : BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            println("[AppBluetoothManager] newState:$newState")
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                gatt.discoverServices()
            }

        }

        @SuppressLint("MissingPermission")
        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            super.onServicesDiscovered(gatt, status)
            println("[AppBluetoothManager] onServiceDiscovered status:$status")
            if (status != BluetoothGatt.GATT_SUCCESS) {
                println("onServicesDiscovered error")
                return
            }
            val heartRateService =
                gatt.services.find { it.uuid.toGss().toString().equals("180D", ignoreCase = true) }
            println(gatt.services.map { it.uuid })
            if (heartRateService == null) {
                println("Heart Rate Service not found")
                return
            } else {
                println("Heart Rate Service found")
            }
            heartRateService?.let { gattService ->
                gattService.characteristics.forEach { characteristic ->
                    val configDescriptor = characteristic.descriptors.find {
                        it.uuid.toString()
                            .equals("00002902-0000-1000-8000-00805f9b34fb", ignoreCase = true)
                    }
                    if (configDescriptor == null) {
                        println("Descriptor not found")
                    } else {
                        println("Descriptor found")
                    }
                    configDescriptor?.let {
                        gatt.setCharacteristicNotification(characteristic, true)
                        if (characteristic.properties and BluetoothGattCharacteristic.PROPERTY_NOTIFY > 0) {
                            it.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
                            gatt.writeDescriptor(it)
                        }
                    }
                }
            }

        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            super.onCharacteristicRead(gatt, characteristic, status)
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray,
            status: Int
        ) {
            super.onCharacteristicRead(gatt, characteristic, value, status)
        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            super.onCharacteristicWrite(gatt, characteristic, status)
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?
        ) {
            super.onCharacteristicChanged(gatt, characteristic)

            println(characteristic?.value?.toInteger())
            val copied = heartRateResult.value.toMutableList()
            copied.add(characteristic?.value?.toInteger() ?: 0)
            heartRateResult.value = copied
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray
        ) {
            super.onCharacteristicChanged(gatt, characteristic, value)
            println(characteristic?.value?.toInteger())
        }

        override fun onDescriptorRead(
            gatt: BluetoothGatt?,
            descriptor: BluetoothGattDescriptor?,
            status: Int
        ) {
            super.onDescriptorRead(gatt, descriptor, status)
        }

        override fun onDescriptorRead(
            gatt: BluetoothGatt,
            descriptor: BluetoothGattDescriptor,
            status: Int,
            value: ByteArray
        ) {
            super.onDescriptorRead(gatt, descriptor, status, value)
        }

        override fun onDescriptorWrite(
            gatt: BluetoothGatt?,
            descriptor: BluetoothGattDescriptor?,
            status: Int
        ) {
            super.onDescriptorWrite(gatt, descriptor, status)
        }

        override fun onServiceChanged(gatt: BluetoothGatt) {
            super.onServiceChanged(gatt)
        }
    }

    @SuppressLint("MissingPermission")
    fun startScan() {
        val prefix = "[AppBluetoothManager][scan]"
        println(prefix)

        val scanSettings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

        try {
            bluetoothAdapter.bluetoothLeScanner.startScan(null, scanSettings, scanCallback)
        } catch (e: Exception) {
            println(e)
        }
    }

    @SuppressLint("MissingPermission")
    fun stopScan() {

        println("[AppBluetoothManager] stopScan")

        try {
            bluetoothAdapter.bluetoothLeScanner.stopScan(scanCallback)
        } catch (e: Exception) {
            println(e)
        }
    }
    @SuppressLint("MissingPermission")
    fun connect(deviceAddress: String) {
        val device = bluetoothAdapter.getRemoteDevice(deviceAddress)
        val mBluetoothGatt = device.connectGatt(application, false, gattCallback);
    }
}



