package com.nordicsemi.memfault.dumping

import android.bluetooth.BluetoothDevice
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nordicsemi.memfault.bluetooth.BleManagerResult
import com.nordicsemi.memfault.bluetooth.IdleResult
import com.nordicsemi.memfault.bluetooth.MDS_SERVICE_UUID
import com.nordicsemi.memfault.bluetooth.MemfaultEntity
import com.nordicsemi.memfault.repository.MemfaultManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import no.nordicsemi.android.navigation.*
import no.nordicsemi.ui.scanner.ScannerDestinationId
import no.nordicsemi.ui.scanner.ui.exhaustive
import no.nordicsemi.ui.scanner.ui.getDevice
import java.util.*
import javax.inject.Inject

@HiltViewModel
class DumpingViewModel @Inject constructor(
    @ApplicationContext
    private val context: Context,
    private val navigationManager: NavigationManager,
    private val memfaultManager: MemfaultManager
) : ViewModel() {

    private val _status = MutableStateFlow<BleManagerResult<MemfaultEntity>>(IdleResult())
    val status = _status.asStateFlow()

    init {
        requestBluetoothDevice()
    }

    fun navigateBack() {
        navigationManager.navigateUp()
    }

    private fun requestBluetoothDevice() {
        navigationManager.navigateTo(ScannerDestinationId, UUIDArgument(MDS_SERVICE_UUID))

        navigationManager.recentResult.onEach {
            if (it.destinationId == ScannerDestinationId) {
                handleArgs(it)
            }
        }.launchIn(viewModelScope)
    }

    private fun handleArgs(args: DestinationResult) {
        when (args) {
            is CancelDestinationResult -> navigationManager.navigateUp()
            is SuccessDestinationResult -> installBluetoothDevice(args.getDevice().device)
        }.exhaustive
    }

    private fun installBluetoothDevice(device: BluetoothDevice) {
        viewModelScope.launch {
            memfaultManager.install(context, device)

            memfaultManager.status?.onEach {
                _status.value = it
            }?.launchIn(viewModelScope)
        }
    }
}

private val HRS_SERVICE_UUID: UUID = UUID.fromString("0000180D-0000-1000-8000-00805f9b34fb")
