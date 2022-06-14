package com.nordicsemi.memfault.home

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.nordicsemi.memfault.R

@Composable
fun HomeScreen() {
    val viewModel: HomeViewModel = hiltViewModel()
    val status = viewModel.status.collectAsState()

    Column {
        BackIconAppBar(text = stringResource(id = R.string.app_bar_title)) {

        }

        Text(status.value.toString())
    }
}
