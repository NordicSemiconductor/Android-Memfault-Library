/*
 * Copyright (c) 2022, Nordic Semiconductor
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of
 * conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list
 * of conditions and the following disclaimer in the documentation and/or other materials
 * provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be
 * used to endorse or promote products derived from this software without specific prior
 * written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
 * OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.nordicsemi.memfault.dumping

import android.bluetooth.BluetoothDevice
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nordicsemi.memfault.lib.MemfaultManager
import com.nordicsemi.memfault.lib.bluetooth.*
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.ticker
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import no.nordicsemi.android.common.navigation.*
import no.nordicsemi.android.common.ui.scanner.ScannerDestinationId
import no.nordicsemi.android.common.ui.scanner.model.getDevice
import javax.inject.Inject

@HiltViewModel
class DumpingViewModel @Inject constructor(
    @ApplicationContext
    private val context: Context,
    private val navigationManager: NavigationManager,
    private val memfaultManager: MemfaultManager
) : ViewModel() {

    private val _status = MutableStateFlow<BleManagerResult>(IdleResult)
    val status = _status.asStateFlow()

    private val _stats = MutableStateFlow(StatsViewEntity())
    val stats = _stats.asStateFlow()

    init {
        requestBluetoothDevice()

        ticker(1000).consumeAsFlow().onEach {
            val stats = _stats.value
            _stats.value = stats.copy(
                workingTime = stats.workingTime+1,
                lastChunkUpdateTime = stats.lastChunkUpdateTime+1
            )
        }.launchIn(viewModelScope)
    }

    fun disconnect() {
        viewModelScope.launch {
            memfaultManager.disconnect()
        }
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
        }
    }

    private fun installBluetoothDevice(device: BluetoothDevice) {
        viewModelScope.launch {
            memfaultManager.install(context, device).collect {
                when (it) {
                    IdleResult,
                    ConnectedResult,
                    ConnectingResult,
                    is ErrorResult -> _status.value = it
                    DisconnectedResult -> navigationManager.navigateUp()
                    is WorkingResult -> {
                        _stats.value = _stats.value.copy(chunks = it.chunks.size, lastChunkUpdateTime = 0)
                        _status.value = it
                    }
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        memfaultManager.disconnect()
    }
}
