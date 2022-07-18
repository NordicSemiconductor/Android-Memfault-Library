package com.nordicsemi.memfault.dumping

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

@Composable
fun LoadingView() {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(6) {
            LoadingItem()
        }
    }
}

@Composable
private fun LoadingItem() {
    Column(
        modifier = Modifier
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.outline)
                .fillMaxWidth()
                .height(14.dp)
                .applyPlaceholder()
        )

        Spacer(modifier = Modifier.size(4.dp))

        Box(
            modifier = Modifier
                .padding(end = 16.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.outline)
                .fillMaxWidth()
                .height(12.dp)
                .applyPlaceholder()
        )
    }
}
