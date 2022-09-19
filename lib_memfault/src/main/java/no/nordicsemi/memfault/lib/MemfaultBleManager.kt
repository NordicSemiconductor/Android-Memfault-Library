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

package no.nordicsemi.memfault.lib

import android.bluetooth.BluetoothDevice
import android.content.Context
import no.nordicsemi.memfault.lib.data.MemfaultState
import kotlinx.coroutines.flow.StateFlow
import no.nordicsemi.android.ble.BleManager

/**
 * Class responsible for managing connection with the remote IoT device which supports Memfault GATT characteristics.
 * [MemfaultBleManager] connects to the device and uploads all downloaded chunks to the cloud.
 *
 * Data can be emitted any time so the connection should be maintained as long as needed.
 *
 * @see <a href="https://app.memfault.com">Memfault console</a>
 * @see <a href="https://memfault.notion.site/Memfault-Diagnostic-GATT-Service-MDS-ffd5a430062649cd9bf6edbf64e2563b">Memfault GATT characteristics</a>
 */
interface MemfaultBleManager {

    /**
     * Contains all the information exposed by the library like:
     *  - Bluetooth connection status with the selected IoT device.
     *  - Uploading status which may be suspended due to server overload.
     *  - Received chunks information.
     *
     * If the device supports required GATT characteristics then uploaded chunks will be reported
     * by [WorkingResult]. Otherwise [ErrorResult] is sent.
     *
     * [WorkingResult] can report [UploadStatus.SUSPENDED] which indicates that the Memfault server
     * is overloaded and upload is postponed to the future.
     */
    val state: StateFlow<MemfaultState>

    /**
     * Function used to connect the phone to a selected [BluetoothDevice].
     * Chunks upload will start immediately.
     *
     * @param context applicationContext needed to set up [BleManager]
     * @param device [BluetoothDevice] to which manager should connect
     */
    suspend fun connect(context: Context, device: BluetoothDevice)

    /**
     * Disconnects a previously connected BLE device.
     * If success then [DisconnectedResult] is emitted by a flow returned by [MemfaultBleManager.connect].
     */
    suspend fun disconnect()

    companion object {

        /**
         * This function creates a new instance of [MemfaultBleManager] each time it's called.
         *
         * @return new [MemfaultBleManager] instance
         */
        fun create(): MemfaultBleManager {
            return MemfaultBleManagerImpl()
        }
    }
}
