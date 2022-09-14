package com.nordicsemi.memfault.lib

import android.bluetooth.BluetoothDevice
import android.content.Context
import com.nordicsemi.memfault.lib.bluetooth.ChunkValidator
import com.nordicsemi.memfault.lib.bluetooth.MemfaultBleManager
import com.nordicsemi.memfault.lib.data.MemfaultData
import com.nordicsemi.memfault.lib.data.toChunk
import com.nordicsemi.memfault.lib.db.toChunk
import com.nordicsemi.memfault.lib.db.toEntity
import com.nordicsemi.memfault.lib.internet.ChunkUploadManager
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class MemfaultManagerImpl : MemfaultManager {

    private var manager: MemfaultBleManager? = null

    private val _state = MutableStateFlow(MemfaultData())
    override val state: StateFlow<MemfaultData> = _state.asStateFlow()

    override suspend fun connect(context: Context, device: BluetoothDevice) = coroutineScope {
        val factory = MemfaultFactory(context.applicationContext)
        val bleManager = factory.getMemfaultManager()
        val database = factory.getDatabase()
        val chunkValidator = ChunkValidator()
        var uploadManager: ChunkUploadManager? = null

        bleManager.start(device)

        //Collect config and initialise UploadManager
        launch {
            bleManager.config.collect {
                it?.let {
                    _state.value = _state.value.copy(config = it)
                    uploadManager = factory.getUploadManager(config = it)

                    launch {
                        uploadManager?.status?.collect {
                            _state.value = _state.value.copy(uploadingStatus = it)
                        }
                    }
                }
            }
        }

        //Collect bluetooth connection status and upload exposed StateFlow
        launch {
            bleManager.status.collect {
                _state.value = _state.value.copy(bleStatus = it)
            }
        }

        //Store chunks in database and schedule update with some delay to aggregate requests
        launch {
            bleManager.receivedChunk
                .map { it.toChunk() }
                .onEach { database.chunksDao().insert(it.toEntity()) }
                .filter { chunkValidator.validateChunk(it) }
                .debounce(300)
                .collect { uploadManager?.uploadChunks() }
        }

        launch {
            database.chunksDao().getAll()
                .map { it.map { it.toChunk() } }
                .collect {
                    _state.value = _state.value.copy(chunks = it)
                }
        }

        Unit
    }

    override fun disconnect() {
        manager?.disconnectWithCatch()
        manager = null
        _state.value = MemfaultData()
    }
}
