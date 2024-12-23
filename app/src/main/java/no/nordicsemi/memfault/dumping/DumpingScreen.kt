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

import androidx.annotation.DrawableRes
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
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import no.nordicsemi.android.common.ui.view.NordicAppBar
import no.nordicsemi.android.common.ui.view.SectionTitle
import no.nordicsemi.memfault.R
import no.nordicsemi.memfault.lib.bluetooth.DeviceState
import no.nordicsemi.memfault.lib.data.Chunk
import no.nordicsemi.memfault.lib.data.MemfaultConfig
import no.nordicsemi.memfault.lib.data.MemfaultState
import no.nordicsemi.memfault.lib.internet.UploadingStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DumpingScreen() {
    val viewModel: DumpingViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            NordicAppBar(
                title = { Text(stringResource(id = R.string.app_bar_title)) },
                actions = { ConnectButton(state = state) }
            )
        }
    ) { innerPadding ->
        // We want the padding to be at least 16.dp on the sides and 16.dp on the top,
        // but if the side insets are larger, we want to use them.
        val insets = WindowInsets.displayCutout
            .union(WindowInsets.navigationBars)
            .union(WindowInsets(left = 16.dp, right = 16.dp))
            .only(WindowInsetsSides.Horizontal)
            .union(WindowInsets(top = 16.dp))

        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(insets),
            contentAlignment = Alignment.TopCenter,
        ) {
            LazyColumn(
                modifier = Modifier
                    .widthIn(max = 600.dp),
                contentPadding = innerPadding,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item { StatsView(data = state) }

                state.config?.let {
                    item { ConfigView(config = it) }
                }

                val status = state.bleStatus
                if (status is DeviceState.Disconnected) {
                    item { ErrorItem(status.reason) }
                } else if (state.chunks.isNotEmpty()) {
                    ChunksItem(chunks = state.chunks)
                } else if (status == DeviceState.Connecting || state.bleStatus == DeviceState.Connected) {
                    if (state.chunks.isEmpty()) {
                        LoadingView()
                    } else {
                        ChunksItem(chunks = state.chunks)
                    }
                }
            }
        }
    }
}

@Composable
private fun ConnectButton(state: MemfaultState) {
    val viewModel: DumpingViewModel = hiltViewModel()

    if (state.bleStatus == DeviceState.Connected) {
        TextButton(onClick = { viewModel.disconnect() }) {
            Text(
                stringResource(id = R.string.disconnect),
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    } else if (state.bleStatus.canConnect()) {
        TextButton(onClick = { viewModel.connect() }) {
            Text(
                stringResource(id = R.string.connect),
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    } else {
        CircularProgressIndicator(
            color = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .size(32.dp),
        )
    }
}

private fun LazyListScope.ChunksItem(chunks: List<Chunk>) {
    item {
        Text(
            text = stringResource(id = R.string.chunks_received),
            style = MaterialTheme.typography.labelSmall
        )
    }
    items(chunks.size) {
        ChunkItem(chunk = chunks[it])
    }
}

@Composable
private fun ErrorItem(reason: DeviceState.Disconnected.Reason) {
    Text(
        text = stringResource(id = reason.toStringRes()),
        color = MaterialTheme.colorScheme.error,
        style = MaterialTheme.typography.labelLarge,
    )
}

private fun DeviceState.Disconnected.Reason.toStringRes(): Int {
    return when (this) {
        DeviceState.Disconnected.Reason.TIMEOUT -> R.string.error_timeout
        DeviceState.Disconnected.Reason.FAILED_TO_CONNECT -> R.string.error_connection
        DeviceState.Disconnected.Reason.NOT_SUPPORTED -> R.string.error_not_supported
        DeviceState.Disconnected.Reason.CONNECTION_LOST -> R.string.error_connection_lost
    }
}

@Composable
private fun ConfigView(config: MemfaultConfig) {
    OutlinedCard {
        Column(modifier = Modifier.padding(16.dp)) {
            SectionTitle(
                painter = painterResource(R.drawable.ic_board),
                title = stringResource(id = R.string.configuration)
            )

            Spacer(modifier = Modifier.size(16.dp))

            Column {
                TitleItem(
                    title = stringResource(id = R.string.config_device_id),
                    description = config.deviceId
                )
                Spacer(modifier = Modifier.size(8.dp))
                TitleItem(
                    title = stringResource(id = R.string.config_authorisation),
                    description = config.authorisationHeader.value
                )
                Spacer(modifier = Modifier.size(8.dp))
                TitleItem(
                    title = stringResource(id = R.string.config_url),
                    description = config.url
                )
            }
        }
    }
}

@Composable
private fun StatsView(data: MemfaultState) {
    OutlinedCard {
        Column(modifier = Modifier.padding(16.dp)) {
            SectionTitle(
                painter = painterResource(R.drawable.ic_chart),
                title = stringResource(id = R.string.status)
            )

            Spacer(modifier = Modifier.size(16.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                StatsItem(
                    iconRes = R.drawable.ic_bluetooth,
                    title = stringResource(id = R.string.bluetooth_status),
                    description = data.bleStatus.toString()
                )
                StatsItem(
                    iconRes = R.drawable.ic_wifi,
                    title = stringResource(id = R.string.upload_status),
                    description = getUploadingStatus(data.uploadingStatus)
                )
                StatsItem(
                    iconRes = R.drawable.ic_chunk,
                    title = stringResource(id = R.string.pending_chunks),
                    description = data.pendingChunksSize.toString()
                )
            }
        }
    }
}

@Composable
private fun getUploadingStatus(status: UploadingStatus): String {
    return when (status) {
        UploadingStatus.InProgress -> stringResource(id = R.string.status_in_progress)
        UploadingStatus.Offline -> stringResource(id = R.string.status_offline)
        is UploadingStatus.Suspended -> stringResource(
            id = R.string.status_suspended,
            status.delayInSeconds
        )
    }
}

@Composable
private fun StatsItem(
    @DrawableRes iconRes: Int,
    title: String,
    description: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row {
            Icon(painter = painterResource(id = iconRes), contentDescription = title)
        }

        Spacer(modifier = Modifier.size(8.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
        )
        Spacer(modifier = Modifier.size(8.dp))
        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall,
        )
    }
}
