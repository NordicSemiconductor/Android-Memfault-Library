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

package com.nordicsemi.memfault.lib

import android.bluetooth.BluetoothDevice
import android.content.Context
import com.nordicsemi.memfault.lib.bluetooth.BleManagerResult
import com.nordicsemi.memfault.lib.bluetooth.MemfaultBleManager
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.StateFlow
import no.nordicsemi.android.ble.BleManager

/**
 * Class responsible for managing connection with the remote IoT device which supports Memfault GATT characteristics.
 * [MemfaultManager] connects to the device and uploads all downloaded chunks to the cloud.
 *
 * Data can be emitted any time so connection should be maintained as long as needed.
 *
 * @see <a href="https://app.memfault.com">Memfault console</a>
 * @see <a href="https://memfault.notion.site/Memfault-Diagnostic-GATT-Service-MDS-ffd5a430062649cd9bf6edbf64e2563b">Memfault GATT characteristics</a>
 */
class MemfaultManager {

    /**
     * Bluetooth manager which uses Nordic BLE library [BleManager] to connect to a device which supports Memfault GATT characteristics.
     *
     * @see <a href="https://github.com/NordicSemiconductor/Android-BLE-Library">BLE Library</a>
     */
    private var manager: MemfaultBleManager? = null

    /**
     * Function used to connect the phone to a selected [BluetoothDevice].
     * If the device supports required GATT characteristics then uploaded chunks will be reported
     * by [WorkingResult]. Otherwise [ErrorResult] is sent.
     *
     * [WorkingResult] can report [UploadStatus.SUSPENDED] which indicates that the Memfault server
     * is overloaded and upload is postponed to the future.
     *
     * @param context applicationContext needed to set up [BleManager]
     * @param device [BluetoothDevice] to which manager should connect
     *
     * @return Returns [BleManagerResult] which indicates status connection.
     * This is the place where library informs about eventual errors or disconnection.
     */
    suspend fun connect(context: Context, device: BluetoothDevice): StateFlow<BleManagerResult> {
        val bleManager = MemfaultBleManager(context, GlobalScope)
        manager = bleManager
        bleManager.start(device)
        return bleManager.dataHolder.status
    }

    /**
     * Disconnects a previously connected BLE device.
     * If success then [DisconnectedResult] is emitted by a flow returned by [MemfaultManager.connect].
     */
    fun disconnect() {
        manager?.disconnectWithCatch()
        manager = null
    }
}
