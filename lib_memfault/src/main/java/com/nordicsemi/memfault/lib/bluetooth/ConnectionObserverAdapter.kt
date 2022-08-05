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

package com.nordicsemi.memfault.lib.bluetooth

import android.bluetooth.BluetoothDevice
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import no.nordicsemi.android.ble.exception.ConnectionException
import no.nordicsemi.android.ble.observer.ConnectionObserver

internal class ConnectionObserverAdapter<T> : ConnectionObserver {

    private val TAG = "BLE-CONNECTION"

    private val _status = MutableStateFlow<BleManagerResult>(IdleResult)
    val status = _status.asStateFlow()

    override fun onDeviceConnecting(device: BluetoothDevice) {
        Log.d(TAG, "onDeviceConnecting()")
        _status.value = ConnectingResult
    }

    override fun onDeviceConnected(device: BluetoothDevice) {
        Log.d(TAG, "onDeviceConnected()")
    }

    override fun onDeviceFailedToConnect(device: BluetoothDevice, reason: Int) {
        Log.d(TAG, "onDeviceFailedToConnect(), reason: $reason")
        updateError(ConnectionException())
    }

    override fun onDeviceReady(device: BluetoothDevice) {
        Log.d(TAG, "onDeviceReady()")
        _status.value = ConnectedResult
    }

    override fun onDeviceDisconnecting(device: BluetoothDevice) {
        Log.d(TAG, "onDeviceDisconnecting()")
    }

    override fun onDeviceDisconnected(device: BluetoothDevice, reason: Int) {
        Log.d(TAG, "onDeviceDisconnected(), reason: $reason")
        _status.value = DisconnectedResult
    }

    fun updateProgress(chunkNumber: Int, data: ByteArray) {
        val chunk = UploadedChunk(chunkNumber, data)
        _status.value = (_status.value as? WorkingResult)?.let {
            it.copy(chunks = it.chunks + chunk)
        } ?: WorkingResult(listOf(chunk))
    }

    fun updateError(e: Throwable) {
        _status.value = ErrorResult(e)
    }
}
