package fi.shaynek.graphlab

import android.app.Application
import android.bluetooth.BluetoothManager

class MyApplication : Application() {
    lateinit var bluetoothManager: BluetoothManager

    // lets my application use appBluetoothManager - instead of BluetoothManager
    // AppBluetoothManager is a bridge btn the app and the bluetooth Manager
    lateinit var appBluetoothManager: AppBluetoothManager
    lateinit var appBluetoothObserver: AppBluetoothObserver
    override fun onCreate() {
        super.onCreate()
        bluetoothManager = getSystemService(BluetoothManager::class.java)
        appBluetoothManager = AppBluetoothManager(bluetoothManager.adapter, this)

    }

    fun initializedAppBluetoothObserver(activity: MainActivity): AppBluetoothObserver {
        appBluetoothObserver = AppBluetoothObserver(
            activity = activity,
            bluetoothAdapter = bluetoothManager.adapter,
            appBluetoothManager = appBluetoothManager,
        )
        return appBluetoothObserver
    }
}