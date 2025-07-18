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

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import no.nordicsemi.memfault.R
import no.nordicsemi.memfault.observability.data.Chunk

@Composable
fun ChunkItem(
    chunk: Chunk,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row {
                Text(
                    text = stringResource(id = R.string.chunk_number, chunk.chunkNumber),
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.width(32.dp)
                )
                Text(
                    text = stringResource(id = R.string.bytes_count, chunk.data.size),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Light,
                    overflow = TextOverflow.Ellipsis
                )
            }

            SelectionContainer {
                Text(
                    text = chunk.data.toHexString(HexFormat.UpperCase),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }

        if (chunk.isUploaded) {
            Icon(
                imageVector = Icons.Outlined.CheckCircleOutline,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
        } else {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp).padding(2.dp),
                strokeWidth = 2.dp,
            )
        }
    }
}

@Composable
fun TitleItem(
    title: String,
    description: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
        )

        SelectionContainer {
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

// Create a Preview
@Preview(showBackground = true)
@Composable
fun ChunkItemPreview() {
    ChunkItem(
        chunk = Chunk(
            deviceId = "nRF54",
            chunkNumber = 0,
            data = byteArrayOf(0,1,2,3,4,5,6,7,8,9),
            isUploaded = false,
        )
    )
}

@Preview(showBackground = true)
@Composable
fun ChunkItemUploadedPreview() {
    ChunkItem(
        chunk = Chunk(
            deviceId = "nRF54",
            chunkNumber = 1,
            data = byteArrayOf(10,11,12,13,14,15,16,17,18,19),
            isUploaded = true,
        )
    )
}

@Preview(showBackground = true)
@Composable
fun TitleItemPreview() {
    TitleItem(
        title = "Device ID",
        description = "nRF54"
    )
}
