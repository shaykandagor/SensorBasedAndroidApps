package fi.shaynek.graphlab

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Main responsibility of this class is to observe Bluetooth status stop scan if it turned off.
 */
class AppBluetoothObserver(
    private val activity: ComponentActivity,
    private val appBluetoothManager: AppBluetoothManager,
    private val bluetoothAdapter: BluetoothAdapter
) : DefaultLifecycleObserver {
    // Adapter State Flow. Updates via broadcastReceiver
    lateinit var bluetoothAdapterState: MutableStateFlow<Int>

    // broadcastReceiver reference. It still should be registered in onResume and unregistered on onPause lifecycle.
    private lateinit var broadcastReceiver: BroadcastReceiver

    // Intent Launcher. We use it show a popup to user to turn the bluetooth on.
    private lateinit var intentActivityResultLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        println("[BluetoothObserver] onCreate")

        // Initialization
        bluetoothAdapterState = MutableStateFlow(bluetoothAdapter.state)

        // Initialization
        broadcastReceiver = broadcastReceiverFactory {
            onBluetoothAdapterStateChange(bluetoothAdapter.state)
        }

        // Initialization
        intentActivityResultLauncher =
            activityResultLauncherFactory(activity, owner, "AppBluetoothObserverLauncher")
    }

    override fun onPause(owner: LifecycleOwner) {
        super.onPause(owner)
        println("[BluetoothObserver] onPause called")

        try {
            activity.unregisterReceiver(broadcastReceiver)
        } catch (e: Exception) {
            println(e)
        } finally {
            appBluetoothManager.stopScan()
        }
    }

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
        println("[BluetoothObserver] onResume called")

        println("[BluetoothObserver] registerReceiver")
        ContextCompat.registerReceiver(
            activity, broadcastReceiver,
            IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED),
            ContextCompat.RECEIVER_EXPORTED
        )
    }


    fun launchEnableBtAdapter() {
        try {
            intentActivityResultLauncher.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
        } catch (e: Exception) {
            println(e)
        }


    }

    companion object {
        private fun broadcastReceiverFactory(onStateChange: () -> Unit): BroadcastReceiver {
            println("[BluetoothObserver] broadcastReceiverFactory")
            return object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent) {
                    println("[BluetoothObserver] broadcastReceiver onReceive ${intent.action}")

                    // It means the user has changed their bluetooth state.
                    if (intent.action == BluetoothAdapter.ACTION_STATE_CHANGED) {
                        onStateChange()
                    }
                }
            }
        }

        private fun activityResultLauncherFactory(
            activity: ComponentActivity,
            owner: LifecycleOwner,
            key: String
        ) =
            activity.activityResultRegistry.register(
                key, owner, ActivityResultContracts.StartActivityForResult()
            ) { result ->
                val resultOk: Boolean = result.resultCode == Activity.RESULT_OK

                println("[BluetoothObserver] registerHandler:")
                println("[BluetoothObserver] Activity.RESULT_OK: $resultOk")
                if (resultOk) {
                    // There are no request codes
                    val data: Intent? = result.data
                    //bleManager.scan()
                }
            }
    }

    private fun onBluetoothAdapterStateChange(state: Int) {
        bluetoothAdapterState.value = state
        if (state == BluetoothAdapter.STATE_OFF) {
            appBluetoothManager.stopScan()
        }
        if (state == BluetoothAdapter.STATE_ON) {
            println("[BluetoothObserver] btadapter back on...")
            //delay(300L)
            //bleManager.scan()
        }

    }

}