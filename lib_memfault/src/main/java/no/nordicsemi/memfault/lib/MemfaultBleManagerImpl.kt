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
                        uploadManager!!.status.combine(internetStateManager.networkState()) { status, isOnline ->
                            status.mapWithInternet(isOnline)
                        }.collect { _state.value = _state.value.copy(uploadingStatus = it) }
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

    private fun UploadingStatus.mapWithInternet(isInternetEnabled: Boolean): UploadingStatus {
        return when (isInternetEnabled) {
            true -> this
            false -> UploadingStatus.Offline
        }
    }

    override suspend fun disconnect() {
        manager?.disconnectWithCatch()
        manager = null
    }
}
