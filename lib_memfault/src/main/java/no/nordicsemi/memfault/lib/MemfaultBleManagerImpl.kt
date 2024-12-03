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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import no.nordicsemi.android.common.permissions.internet.util.InternetPermissionState
import no.nordicsemi.memfault.lib.bluetooth.ChunkValidator
import no.nordicsemi.memfault.lib.bluetooth.ChunksBleManager
import no.nordicsemi.memfault.lib.data.MemfaultState
import no.nordicsemi.memfault.lib.db.toChunk
import no.nordicsemi.memfault.lib.db.toEntity
import no.nordicsemi.memfault.lib.internet.ChunkUploadManager
import no.nordicsemi.memfault.lib.internet.UploadingStatus

class MemfaultBleManagerImpl : MemfaultBleManager {

    private var manager: ChunksBleManager? = null

    private val _state = MutableStateFlow(MemfaultState())
    override val state: StateFlow<MemfaultState> = _state.asStateFlow()

    override suspend fun connect(context: Context, device: BluetoothDevice) {
        val factory = MemfaultFactory(context.applicationContext)
        val bleManager = factory.getMemfaultManager()
        val database = factory.getDatabase()
        val scope = factory.getScope()
        val chunkValidator = ChunkValidator()
        var uploadManager: ChunkUploadManager? = null
        this.manager = bleManager
        val internetStateManager = factory.getInternetStateManager(context)

        //Collect bluetooth connection status and upload exposed StateFlow
        scope.launch {
            bleManager.status.collect {
                _state.value = _state.value.copy(bleStatus = it)
            }
        }

        bleManager.start(device)

        //Store chunks in database and schedule update with some delay to aggregate requests
        scope.launch {
            bleManager.receivedChunk
                .buffer()
                .onEach { database.chunksDao().insert(it.toEntity()) }
//                .filter { chunkValidator.validateChunk(it) }
                .debounce(300)
                .collect { uploadManager?.uploadChunks() }
        }

        //Collect config and initialise UploadManager
        scope.launch {
            bleManager.config.collect { config ->
                config?.let {
                    _state.value = _state.value.copy(config = it)
                    uploadManager = factory.getUploadManager(config = it)

                    launch {
                        uploadManager!!.status
                            .combine(internetStateManager.networkState()) { status, internetState ->
                                status.mapWithInternet(internetState)
                            }
                            .collect { _state.value = _state.value.copy(uploadingStatus = it) }
                    }

                    launch {
                        uploadManager?.uploadChunks()
                    }

                    scope.launch {
                        database.chunksDao().getAll(config.deviceId)
                            .map { it.map { it.toChunk() } }
                            .collect { _state.value = _state.value.copy(chunks = it) }
                    }
                }
            }
        }
    }

    private fun UploadingStatus.mapWithInternet(internetState: InternetPermissionState): UploadingStatus {
        return when (internetState) {
            InternetPermissionState.Available -> this
            is InternetPermissionState.NotAvailable -> UploadingStatus.Offline
        }
    }

    override suspend fun disconnect() {
        manager?.disconnectWithCatch()
        manager = null
    }
}
