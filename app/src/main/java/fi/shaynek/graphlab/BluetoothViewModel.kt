package fi.shaynek.graphlab

import androidx.compose.runtime.collectAsState
import androidx.lifecycle.ViewModel
import com.patrykandpatrick.vico.core.entry.entryModelOf
import fi.shaynek.graphlab.AppBluetoothManager
import fi.shaynek.graphlab.AppBluetoothObserver
import kotlinx.coroutines.flow.map

class BluetoothViewModel(
    private val appBluetoothManager: AppBluetoothManager,
    private val appBluetoothObserver: AppBluetoothObserver,

    ) : ViewModel() {
    val scanResult = appBluetoothManager.scanResult
    val heartRateResult = appBluetoothManager.heartRateResult

    val heartRateGraphData = heartRateResult
        // Converts a list to a float List
        .map { it.map { heartRate -> heartRate.toFloat() } }
        .map { it.takeLast(20) }
        .map { entryModelOf(*it.toTypedArray()) }



    fun startScan() {
        appBluetoothManager.startScan()

    }

    fun stopScan() {
        appBluetoothManager.stopScan()
    }

    fun turnOn() {
        appBluetoothObserver.launchEnableBtAdapter()
    }

    fun connect(deviceAddress: String){
        appBluetoothManager.connect(deviceAddress)
    }


}