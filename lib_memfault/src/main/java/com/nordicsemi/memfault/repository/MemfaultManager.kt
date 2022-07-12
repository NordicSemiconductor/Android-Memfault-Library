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

package com.nordicsemi.memfault.repository

import android.bluetooth.BluetoothDevice
import android.content.Context
import com.nordicsemi.memfault.bluetooth.BleManagerResult
import com.nordicsemi.memfault.bluetooth.MemfaultBleManager
import com.nordicsemi.memfault.bluetooth.MemfaultEntity
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.StateFlow
import no.nordicsemi.android.ble.ktx.suspend
import javax.inject.Inject

class MemfaultManager @Inject constructor() {

    private var manager: MemfaultBleManager? = null
    val status: StateFlow<BleManagerResult>?
        get() = manager?.dataHolder?.status

    suspend fun install(context: Context, device: BluetoothDevice) {
        val bleManager = MemfaultBleManager(context, GlobalScope)
        manager = bleManager
        bleManager.start(device)
    }

    suspend fun disconnect() {
        manager?.disconnect()?.suspend()
        manager = null
    }
}
