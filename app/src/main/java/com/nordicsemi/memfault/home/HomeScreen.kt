package com.nordicsemi.memfault.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nordicsemi.memfault.R

@Composable
fun HomeScreen() {
    val viewModel: HomeViewModel = hiltViewModel()

    Column {
        CloseIconAppBar(text = stringResource(id = R.string.app_bar_title)) { viewModel.navigateBack() }

        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(stringResource(id = R.string.app_info))

            Spacer(modifier = Modifier.fillMaxSize().weight(1f))

            Button(onClick = { viewModel.navigateNext() }) {
                Text(stringResource(id = R.string.start))
            }
        }
    }
}
