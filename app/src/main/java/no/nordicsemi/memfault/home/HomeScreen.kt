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

package no.nordicsemi.memfault.home

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withLink
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import no.nordicsemi.android.common.ui.view.NordicAppBar
import no.nordicsemi.memfault.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen() {
    val viewModel: HomeViewModel = hiltViewModel()

    Scaffold(
        topBar = {
            NordicAppBar(
                title = { Text(text = stringResource(id = R.string.title_home)) }
            )
        }
    ) { innerPadding ->
        val isLandscape =
            LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
        val insets = WindowInsets.displayCutout
            .union(WindowInsets.navigationBars)
            .only(WindowInsetsSides.Horizontal)

        if (isLandscape) {
            TwoPane(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .windowInsetsPadding(insets),
                onStart = { viewModel.navigateToScanner() }
            )
        } else {
            OnePane(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(innerPadding)
                    .windowInsetsPadding(insets),
                onStart = { viewModel.navigateToScanner() }
            )
        }
    }
}

@Composable
private fun OnePane(
    modifier: Modifier = Modifier,
    onStart: () -> Unit
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(1f))

        MemfaultLogo()

        Spacer(modifier = Modifier.weight(0.3f))

        Content(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .widthIn(max = 600.dp),
            onStart = onStart
        )

        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
private fun TwoPane(
    modifier: Modifier = Modifier,
    onStart: () -> Unit
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        MemfaultLogo(
            modifier = Modifier.weight(0.3f),
        )

        Spacer(modifier = Modifier.size(16.dp))

        Content(
            modifier = Modifier
                .weight(0.7f)
                .verticalScroll(rememberScrollState())
                .padding(vertical = 16.dp)
                .padding(end = 16.dp)
                .widthIn(max = 600.dp),
            onStart = onStart
        )
    }
}

@Composable
private fun MemfaultLogo(
    modifier: Modifier = Modifier,
) {
    Image(
        modifier = modifier,
        painter = painterResource(id = R.drawable.ic_memfault),
        contentDescription = stringResource(id = R.string.cd_memfault),
        contentScale = ContentScale.Fit,
        alignment = Alignment.TopEnd
    )
}

@Composable
private fun Content(
    modifier: Modifier = Modifier,
    onStart: () -> Unit,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.app_info_header),
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.size(32.dp))

        Text(
            text = buildAnnotatedString {
                append(stringResource(id = R.string.app_info))
                append(" ")
                withLink(
                    LinkAnnotation.Url(
                        url = "https://memfault.com/",
                        styles = TextLinkStyles(style = SpanStyle(color = MaterialTheme.colorScheme.secondary)),
                    )
                ) {
                    append(stringResource(id = R.string.app_info_memfault_console))
                }
                append(stringResource(id = R.string.app_info_2))
                append(" ")
                withLink(
                    LinkAnnotation.Url(
                        url = "https://docs.memfault.com/docs/mcu/mds",
                        styles = TextLinkStyles(style = SpanStyle(color = MaterialTheme.colorScheme.secondary)),
                    )
                ) {
                    append(stringResource(id = R.string.app_info_gatt))
                }
                append(stringResource(id = R.string.app_info_3))
            },
            style = MaterialTheme.typography.bodyLarge,
        )

        Spacer(modifier = Modifier.size(32.dp))

        Button(onClick = onStart) {
            Text(text = stringResource(id = R.string.start))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun OnePanePreview() {
    OnePane(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 16.dp)
            .widthIn(max = 600.dp),
        onStart = {  }
    )
}

@Preview(widthDp = 800, heightDp = 600, showBackground = true)
@Composable
private fun TwoPanePreview() {
    TwoPane(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 16.dp)
            .widthIn(max = 600.dp),
        onStart = {  }
    )
}