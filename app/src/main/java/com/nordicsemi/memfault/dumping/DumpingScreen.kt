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

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nordicsemi.memfault.R
import com.nordicsemi.memfault.lib.bluetooth.BluetoothLEStatus
import com.nordicsemi.memfault.lib.data.Chunk
import no.nordicsemi.android.common.theme.view.NordicAppBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DumpingScreen() {
    val viewModel: DumpingViewModel = hiltViewModel()
    val state = viewModel.state.collectAsState().value


    if (state.bleStatus == BluetoothLEStatus.ERROR) {
        ErrorItem(viewModel = viewModel)
        return
    }

    Scaffold(
        topBar = {
            NordicAppBar(text = stringResource(id = R.string.app_bar_title), actions = {
                TextButton(onClick = { viewModel.disconnect() }) {
                    Text(stringResource(id = R.string.disconnect), color = MaterialTheme.colorScheme.onPrimary)
                }
            })
        }
    ) {
        Box(modifier = Modifier.padding(it)) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
//                (state as? WorkingResult)?.let {
//                    ScreenSection {
//                        SectionTitle(
//                            painter = painterResource(id = R.drawable.ic_chart),
//                            title = stringResource(id = R.string.statistics)
//                        )
//
//                        Spacer(modifier = Modifier.size(16.dp))
//
//                        StatsView(it)
//                    }
//
//                    Spacer(modifier = Modifier.size(16.dp))
//                }
//
//                ScreenSection {
//                    SectionTitle(
//                        painter = painterResource(R.drawable.ic_chunk),
//                        title = stringResource(id = R.string.chunks_received)
//                    )
//
//                    Spacer(modifier = Modifier.size(16.dp))
//
//                    if (state is IdleResult || state is ConnectingResult || state is ConnectedResult) {
//                        LoadingView()
//                    } else if (state is WorkingResult) {
//                        UploadingItem(state)
//                    }
//                }
            }
        }
    }
}

@Composable
private fun UploadingItem(chunks: List<Chunk>) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(chunks.size) {
            val chunk = chunks[it]
            ScreenItem(
                title = stringResource(id = R.string.next_item, chunk.number),
                description = stringResource(id = R.string.bytes, chunk.data.size)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ErrorItem(viewModel: DumpingViewModel) {
    Scaffold(
        topBar = {
            NordicAppBar(text = stringResource(id = R.string.app_bar_title), actions = {
                TextButton(onClick = { viewModel.disconnect() }) {
                    Text(stringResource(id = R.string.disconnect), color = MaterialTheme.colorScheme.onPrimary)
                }
            })
        }
    ) {
        Column(modifier = Modifier.padding(it)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = stringResource(id = R.string.error),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
    }
}

//@Composable
//private fun StatsView(stats: WorkingResult) {
//    Row(
//        verticalAlignment = Alignment.CenterVertically,
//        modifier = Modifier.fillMaxWidth(),
//        horizontalArrangement = Arrangement.SpaceEvenly
//    ) {
//        StatsItem(
//            R.drawable.ic_chunk,
//            stringResource(id = R.string.chunks_received),
//            stats.chunksReceived.toString()
//        )
//        Spacer(modifier = Modifier.size(16.dp))
//        StatsItem(
//            R.drawable.ic_chunk_send,
//            stringResource(id = R.string.chunks_sent),
//            stats.chunksSent.toString()
//        )
//        Spacer(modifier = Modifier.size(16.dp))
//        val label = when (stats.uploadStatus) {
//            UploadStatus.WORKING -> stringResource(id = R.string.upload_status_working)
//            UploadStatus.SUSPENDED -> stringResource(id = R.string.upload_status_suspended)
//        }
//        StatsItem(
//            R.drawable.ic_cloud_upload,
//            stringResource(id = R.string.upload_status),
//            label
//        )
//    }
//}

@Composable
private fun StatsItem(
    @DrawableRes
    iconRes: Int,
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
            modifier = Modifier.widthIn(max = 60.dp),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.size(8.dp))
        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall
        )
    }
}
