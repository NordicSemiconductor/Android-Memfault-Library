package com.nordicsemi.memfault.dumping

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nordicsemi.memfault.R

@Composable
fun ScreenItem(
    title: String,
    @DrawableRes leftIcon: Int? = null,
    info: String? = null,
    isSelected: Boolean = false
) {

    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        Color.Transparent
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(32.dp))
            .background(backgroundColor)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        leftIcon?.let {
            Icon(
                painter = painterResource(id = leftIcon),
                contentDescription = stringResource(id = R.string.cd_progress_icon)
            )

            Spacer(modifier = Modifier.size(16.dp))
        }

        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.weight(1f)
        )

        info?.let {
            Spacer(modifier = Modifier.size(16.dp))

            Text(
                text = info,
                style = MaterialTheme.typography.titleSmall
            )
        }
    }
}

@Preview
@Composable
private fun ScreenItemPreview() {
    ScreenItem("Test", R.drawable.ic_circle, "99%", true)
}
