package com.nordicsemi.memfault.dumping

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import com.nordicsemi.memfault.R
import com.nordicsemi.memfault.lib.data.Chunk

@Composable
fun ChunkItem(chunk: Chunk) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Box(modifier = Modifier.weight(1f)) {
            TitleItem(
                title = stringResource(id = R.string.next_item, chunk.chunkNumber),
                description = stringResource(id = R.string.bytes, chunk.data.size)
            )
        }

        val icon = if (chunk.isUploaded) {
            Icons.Default.Done
        } else {
            Icons.Default.Close
        }

        val iconColor = if (chunk.isUploaded) {
            no.nordicsemi.android.common.theme.R.color.nordicGrass
        } else {
            no.nordicsemi.android.common.theme.R.color.nordicRed
        }

        Icon(imageVector = icon, contentDescription = "", tint = colorResource(id = iconColor))
    }
}

@Composable
fun TitleItem(title: String, description: String) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.fillMaxWidth(),
        )

        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
