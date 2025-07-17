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

package no.nordicsemi.memfault.dumping

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.DeveloperBoard
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.outlined.CloudDone
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import no.nordicsemi.android.common.ui.view.NordicAppBar
import no.nordicsemi.android.common.ui.view.SectionTitle
import no.nordicsemi.memfault.R
import no.nordicsemi.memfault.observability.MemfaultState
import no.nordicsemi.memfault.observability.bluetooth.DeviceState
import no.nordicsemi.memfault.observability.data.Chunk
import no.nordicsemi.memfault.observability.data.MemfaultConfig
import no.nordicsemi.memfault.observability.internet.UploadingStatus
import no.nordicsemi.memfault.util.placeholder
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DumpingScreen() {
    val viewModel: DumpingViewModel = hiltViewModel()

    Scaffold(
        topBar = {
            NordicAppBar(
                title = { Text(stringResource(id = R.string.title_connection)) },
                onNavigationButtonClick = { viewModel.navigateUp() },
            )
        },
        contentWindowInsets = WindowInsets.displayCutout
            .union(WindowInsets.navigationBars)
            .union(WindowInsets(left = 16.dp, right = 16.dp))
            .only(WindowInsetsSides.Horizontal),
    ) { innerPadding ->
        // We want the padding to be at least 16.dp on the sides and 16.dp on the top,
        // but if the side insets are larger, we want to use them.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.TopCenter,
        ) {
            val state by viewModel.state.collectAsStateWithLifecycle()

            Column(
                modifier = Modifier
                    .widthIn(max = 600.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                StatsView(state = state)

                (state.bleStatus as? DeviceState.Disconnected)?.reason?.let { reason ->
                    ErrorView(reason = reason)
                }

                AnimatedVisibility(
                    visible = state.config != null ||
                              state.bleStatus == DeviceState.Connected ||
                              state.bleStatus == DeviceState.Initializing
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        ConfigView(config = state.config)

                        ChunksView(state)
                    }
                }
            }
        }
    }
}

@Composable
private fun StatsView(state: MemfaultState) {
    OutlinedCard {
        Column(modifier = Modifier.padding(16.dp)) {
            SectionTitle(
                icon = Icons.Default.BarChart,
                title = stringResource(id = R.string.status)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceAround,
                modifier = Modifier.fillMaxWidth()
            ) {
                val isConnected = state.bleStatus == DeviceState.Connected
                val isOnline = state.uploadingStatus !is UploadingStatus.Suspended
                val startTime by rememberSaveable(inputs = arrayOf(isConnected, isOnline)) {
                    mutableLongStateOf(System.currentTimeMillis())
                }
                var uptime by remember { mutableIntStateOf(0) }
                LaunchedEffect(isConnected, isOnline) {
                    while (isConnected && isOnline) {
                        delay(1000)
                        uptime = ((System.currentTimeMillis() - startTime) / 1000).toInt()
                    }
                }
                StatsItem(
                    imageVector = Icons.Default.Bluetooth,
                    title = stringResource(id = R.string.bluetooth_status),
                    description = state.bleStatus.name(),
                    enabled = true,
                )
                StatsItem(
                    imageVector = Icons.Outlined.CloudDone,
                    title = stringResource(id = R.string.upload_status),
                    description = state.uploadingStatus.name(state.bytesUploaded),
                    enabled = isConnected,
                )
                StatsItem(
                    imageVector = Icons.Default.AccessTime,
                    title = stringResource(id = R.string.uptime_title),
                    description = uptime.seconds.toString(),// stringResource(R.string.uptime, uptime),
                    enabled = isConnected,
                )
            }
        }
    }
}

@Composable
private fun ErrorView(reason: DeviceState.Disconnected.Reason) {
    OutlinedCard {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                imageVector = Icons.Default.Error,
                contentDescription = "",
                modifier = Modifier.size(24.dp),
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.error)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = reason.name())
        }
    }
}

@Composable
private fun ConfigView(config: MemfaultConfig?) {
    OutlinedCard {
        Column(modifier = Modifier.padding(16.dp)) {
            SectionTitle(
                icon = Icons.Default.DeveloperBoard,
                title = stringResource(id = R.string.config_title)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Column {
                TitleItem(
                    title = stringResource(id = R.string.config_device_id),
                    description = config?.deviceId ?: "",
                    modifier = Modifier.fillMaxWidth().placeholder(config == null),
                )
                Spacer(modifier = Modifier.size(8.dp))
                TitleItem(
                    title = stringResource(id = R.string.config_authorisation),
                    description = config?.authorisationToken ?: "",
                    modifier = Modifier.fillMaxWidth().placeholder(config == null),
                )
                Spacer(modifier = Modifier.size(8.dp))
                TitleItem(
                    title = stringResource(id = R.string.config_url),
                    description = config?.url ?: "",
                    modifier = Modifier.fillMaxWidth().placeholder(config == null),
                )
            }
        }
    }
}

@Composable
private fun ChunksView(state: MemfaultState) {
    OutlinedCard(
        modifier = Modifier
            .padding(bottom = 16.dp)
            .heightIn(max = 400.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(top = 16.dp)
        ) {
            SectionTitle(
                icon = Icons.Default.Medication,
                title = stringResource(id = R.string.diagnostics_title)
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (state.chunks.isEmpty()) {
                Text(
                    text = stringResource(id = R.string.no_chunks_received),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .placeholder(visible = state.config == null),
                )
            } else {
                LazyColumn {
                    items(state.chunks) { chunk ->
                        ChunkItem(
                            chunk = chunk,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DeviceState.name(): String = when (this) {
    DeviceState.Connecting -> stringResource(R.string.ble_connecting)
    DeviceState.Initializing -> stringResource(R.string.ble_initializing)
    DeviceState.Connected -> stringResource(R.string.ble_connected)
    DeviceState.Disconnecting -> stringResource(R.string.ble_disconnecting)
    is DeviceState.Disconnected -> stringResource(R.string.ble_disconnected)
}

@Composable
private fun DeviceState.Disconnected.Reason.name(): String = when (this) {
    DeviceState.Disconnected.Reason.TIMEOUT -> stringResource(R.string.error_timeout)
    DeviceState.Disconnected.Reason.FAILED_TO_CONNECT -> stringResource(R.string.error_connection)
    DeviceState.Disconnected.Reason.BONDING_FAILED -> stringResource(R.string.error_bonding_failed)
    DeviceState.Disconnected.Reason.NOT_SUPPORTED -> stringResource(R.string.error_not_supported)
    DeviceState.Disconnected.Reason.CONNECTION_LOST -> stringResource(R.string.error_connection_lost)
}

@Composable
private fun UploadingStatus.name(bytesUploaded: Int): String = when (this) {
    UploadingStatus.Idle -> stringResource(R.string.bytes_sent, bytesUploaded / 1024f)
    UploadingStatus.InProgress -> stringResource(id = R.string.status_in_progress)
    is UploadingStatus.Suspended -> stringResource(id = R.string.status_suspended,delayInSeconds)
}

@Composable
private fun StatsItem(
    imageVector: ImageVector,
    title: String,
    description: String,
    enabled: Boolean,
) {
    CompositionLocalProvider(LocalContentColor provides
            if (enabled)
                MaterialTheme.colorScheme.onSurface
            else
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
    ) {
        Column(
            modifier = Modifier.widthIn(100.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(imageVector = imageVector, contentDescription = title)
            Spacer(modifier = Modifier.size(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
            )
            Spacer(modifier = Modifier.size(8.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Preview
@Composable
private fun PreviewStatsView() {
    StatsView(
        state = MemfaultState(
            bleStatus = DeviceState.Connected,
            uploadingStatus = UploadingStatus.Idle,
            config = MemfaultConfig(
                deviceId = "nRF54L",
                authorisationToken = "0102030405060708090A0B0C0D0E0F",
                url = "https://chunks.memfault.com/api/v0/chunks/nRF54L",
            ),
            chunks = listOf(
                Chunk(
                    deviceId = "nRF54",
                    chunkNumber = 0,
                    data = byteArrayOf(0,1,2,3,4,5,6,7,8,9),
                    isUploaded = false,
                ),
                Chunk(
                    deviceId = "nRF54",
                    chunkNumber = 0,
                    data = byteArrayOf(0,1,2,3,4,5,6,7,8,9),
                    isUploaded = true,
                ),
            ),
        )
    )
}

@Preview
@Composable
private fun PreviewStatsView_NotSupported() {
    StatsView(
        state = MemfaultState(
            bleStatus = DeviceState.Disconnected(DeviceState.Disconnected.Reason.NOT_SUPPORTED),
            uploadingStatus = UploadingStatus.Idle,
            config = null,
            chunks = emptyList(),
        )
    )
}

@Preview
@Composable
private fun PreviewConfigView() {
    ConfigView(
        config = MemfaultConfig(
            deviceId = "nRF54L",
            authorisationToken = "0102030405060708090A0B0C0D0E0F",
            url = "https://chunks.memfault.com/api/v0/chunks/nRF54L",
        )
    )
}

@Preview
@Composable
private fun PreviewChunksView() {
    ChunksView(
        state = MemfaultState(
            bleStatus = DeviceState.Connected,
            uploadingStatus = UploadingStatus.Idle,
            config = MemfaultConfig(
                deviceId = "nRF54L",
                authorisationToken = "0102030405060708090A0B0C0D0E0F",
                url = "https://chunks.memfault.com/api/v0/chunks/nRF54L",
            ),
            chunks = listOf(
                Chunk(
                    deviceId = "nRF54",
                    chunkNumber = 0,
                    data = byteArrayOf(0,1,2,3,4,5,6,7,8,9),
                    isUploaded = false,
                ),
                Chunk(
                    deviceId = "nRF54",
                    chunkNumber = 0,
                    data = byteArrayOf(0,1,2,3,4,5,6,7,8,9),
                    isUploaded = true,
                ),
            ),
        )
    )
}

@Preview
@Composable
private fun PreviewErrorView() {
    ErrorView(
        reason = DeviceState.Disconnected.Reason.TIMEOUT
    )
}