package fi.shaynek.graphlab

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.core.entry.entryModelOf
import fi.shaynek.graphlab.ui.theme.GraphLabTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.lifecycle.addObserver(
            (application as MyApplication).initializedAppBluetoothObserver(
                this
            )
        )

        setContent {
            GraphLabTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {

                    Column(modifier = Modifier.padding(10.dp)) {
                        FeatureThatRequiresLocationPermission()
                        FeatureThatRequiresBluetoothConnectPermission()
                        FeatureThatRequiresBluetoothScanPermission()

                        val viewModel: BluetoothViewModel =
                            viewModel(factory = AppViewModelProvider.Factory)

                        Row {
                            Button(onClick = { viewModel.startScan() }) {
                                Text(text = "Start Scanning")
                            }
                            Button(onClick = { viewModel.stopScan() }) {
                                Text(text = "Stop Scanning")
                            }

                            Button(onClick = { viewModel.turnOn() }) {
                                Text(text = "BT on")
                            }
                        }
                        ShowHeartRateGraph(viewModel)
                        ShowScannedDevices(viewModel)
                    }

                }
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun FeatureThatRequiresLocationPermission() {

    // Location permission state
    val locationPermissionState = rememberPermissionState(
        android.Manifest.permission.ACCESS_FINE_LOCATION
    )

    if (locationPermissionState.status.isGranted) {
        Text("Location permission Granted")
    } else {
        Column {
            val textToShow = if (locationPermissionState.status.shouldShowRationale) {
                // If the user has denied the permission but the rationale can be shown,
                // then gently explain why the app requires this permission
                "The location is important for this app. Please grant the permission."
            } else {
                // If it's the first time the user lands on this feature, or the user
                // doesn't want to be asked again for this permission, explain that the
                // permission is required
                "Location permission required for this feature to be available. " +
                        "Please grant the permission"
            }
            Text(textToShow)
            Button(onClick = { locationPermissionState.launchPermissionRequest() }) {
                Text("Request permission")
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun FeatureThatRequiresBluetoothScanPermission() {

    // Location permission state
    val bluetoothPermissionState = rememberPermissionState(
        android.Manifest.permission.BLUETOOTH_SCAN
    )

    if (bluetoothPermissionState.status.isGranted) {
        Text("Bluetooth permission Granted")
    } else {
        Column {
            val textToShow = if (bluetoothPermissionState.status.shouldShowRationale) {
                // If the user has denied the permission but the rationale can be shown,
                // then gently explain why the app requires this permission
                "The bluetooth is important for this app. Please grant the permission."
            } else {
                // If it's the first time the user lands on this feature, or the user
                // doesn't want to be asked again for this permission, explain that the
                // permission is required
                "Bluetooth permission required for this feature to be available. " +
                        "Please grant the permission"
            }
            Text(textToShow)
            Button(onClick = { bluetoothPermissionState.launchPermissionRequest() }) {
                Text("Request permission")
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun FeatureThatRequiresBluetoothConnectPermission() {

    // Location permission state
    val bluetoothPermissionState = rememberPermissionState(
        android.Manifest.permission.BLUETOOTH_CONNECT
    )

    if (bluetoothPermissionState.status.isGranted) {
        Text("Bluetooth permission Granted")
    } else {
        Column {
            val textToShow = if (bluetoothPermissionState.status.shouldShowRationale) {
                // If the user has denied the permission but the rationale can be shown,
                // then gently explain why the app requires this permission
                "The bluetooth is important for this app. Please grant the permission."
            } else {
                // If it's the first time the user lands on this feature, or the user
                // doesn't want to be asked again for this permission, explain that the
                // permission is required
                "Bluetooth permission required for this feature to be available. " +
                        "Please grant the permission"
            }
            Text(textToShow)
            Button(onClick = { bluetoothPermissionState.launchPermissionRequest() }) {
                Text("Request permission")
            }
        }
    }
}

@SuppressLint("MissingPermission")
@Composable
fun ShowScannedDevices(viewModel: BluetoothViewModel) {

    val state = viewModel.scanResult.collectAsState()
    val heartRateState = viewModel.heartRateResult.collectAsState()
    Text(heartRateState.value.lastOrNull()?.toString() ?: "no data is available")
    LazyColumn(
        modifier = Modifier
            .fillMaxHeight()
            .padding(horizontal = 16.dp)
    ) {
        items(state.value.values.toList().sortedBy { it.device.name ?: "no name" }) {
            Button(onClick = { viewModel.connect(it.device.address) }) {
                Text(text = "Connect")
            }
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(5.dp)
            ) {
                Text(
                    it.device.address, fontSize = 12.sp
                )
                Text(
                    it.device.name ?: "no name", fontSize = 15.sp
                )
                Text(
                    it.rssi.toString() + "dBm", fontSize = 18.sp
                )

            }

        }
    }
}

@Composable
fun ShowHeartRateGraph(viewModel: BluetoothViewModel){

    val heartRateGraphData = viewModel.heartRateGraphData.collectAsState(initial = entryModelOf(0f))

    Chart(
        chart = lineChart(),
        model = heartRateGraphData.value,
        startAxis = rememberStartAxis(),
        bottomAxis = rememberBottomAxis(),
    )

}


@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    GraphLabTheme {

    }
}