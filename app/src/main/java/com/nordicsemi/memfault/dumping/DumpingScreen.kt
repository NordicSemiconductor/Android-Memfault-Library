package com.nordicsemi.memfault.dumping

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.nordicsemi.memfault.R
import com.nordicsemi.memfault.home.BackIconAppBar

@Composable
fun DumpingScreen() {
    val viewModel: DumpingViewModel = hiltViewModel()
    val status = viewModel.status.collectAsState()

    Column {
        BackIconAppBar(text = stringResource(id = R.string.app_bar_title)) { viewModel.navigateBack() }

        Text(status.value.toString())
    }
}
