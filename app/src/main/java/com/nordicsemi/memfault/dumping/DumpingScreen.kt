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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
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
import com.nordicsemi.memfault.bluetooth.*
import com.nordicsemi.memfault.home.BackIconAppBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DumpingScreen() {
    val viewModel: DumpingViewModel = hiltViewModel()
    val state = viewModel.status.collectAsState().value
    val stats = viewModel.stats.collectAsState().value

    if (state is ErrorResult) {
        ErrorItem(viewModel = viewModel, error = state, stats = stats)
        return
    }

    Scaffold(
        topBar = { BackIconAppBar(text = stringResource(id = R.string.app_bar_title)) { viewModel.disconnect() } },
        floatingActionButton = {
            ExtendedFloatingActionButton(onClick = { viewModel.disconnect() }) {
                FabContent(Icons.Default.Stop, stringResource(id = R.string.abort))
            }
        }
    ) {
        Box(modifier = Modifier.padding(it)) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                StatsView(stats)

                Spacer(modifier = Modifier.size(16.dp))

                Text(
                    text = stringResource(id = R.string.uploaded_chunks),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.size(16.dp))

                if (state is IdleResult || state is ConnectingResult || state is ConnectedResult) {
                    LoadingView()
                } else if (state is WorkingResult) {
                    UploadingItem(state)
                }
            }
        }
    }
}

@Composable
private fun UploadingItem(state: WorkingResult) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(state.chunks) {
            ScreenItem(
                title = stringResource(id = R.string.next_item, it.number),
                description = it.getDisplayData()
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ErrorItem(viewModel: DumpingViewModel, error: ErrorResult, stats: StatsViewEntity) {
    Scaffold(
        topBar = { BackIconAppBar(text = stringResource(id = R.string.app_bar_title)) { viewModel.disconnect() } },
        floatingActionButton = {
            ExtendedFloatingActionButton(onClick = { viewModel.disconnect() }) {
                FabContent(Icons.Default.ArrowBack, stringResource(id = R.string.go_back))
            }
        }
    ) {
        Column(modifier = Modifier.padding(it)) {
            Column(modifier = Modifier.padding(16.dp)) {
                StatsView(stats)

                Spacer(modifier = Modifier.size(16.dp))

                Text(
                    text = stringResource(id = R.string.error),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelLarge,
                )

                Spacer(modifier = Modifier.size(16.dp))

                Text(
                    text = error.exception.toString(),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@Composable
private fun StatsView(stats: StatsViewEntity) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        StatsItem(
            R.drawable.ic_chunk,
            stringResource(id = R.string.chunks),
            stats.displayChunks()
        )
        Spacer(modifier = Modifier.size(16.dp))
        StatsItem(
            R.drawable.ic_time,
            stringResource(id = R.string.uptime),
            stats.displayWorkingTime()
        )
        Spacer(modifier = Modifier.size(16.dp))
        StatsItem(
            R.drawable.ic_bug_stop,
            stringResource(id = R.string.standby),
            stats.displayLastChunkUpdate()
        )
    }
}

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
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.widthIn(max = 60.dp),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.size(8.dp))
        Row {
            Icon(painter = painterResource(id = iconRes), contentDescription = title)
        }
        Spacer(modifier = Modifier.size(8.dp))
        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall
        )
    }
}
