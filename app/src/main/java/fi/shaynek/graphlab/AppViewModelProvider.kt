package fi.shaynek.graphlab

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory

object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            BluetoothViewModel(
                MyApplication().appBluetoothManager,
                MyApplication().appBluetoothObserver
            )
        }
    }
}

/**
 * Extension function to queries for [Application] object and returns an instance of [MyApplication].
 */
fun CreationExtras.MyApplication(): MyApplication =
    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MyApplication)