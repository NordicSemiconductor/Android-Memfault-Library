package com.nordicsemi.memfault.home

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.nordicsemi.memfault.R

@Composable
fun HomeScreen() {
    val viewModel: HomeViewModel = hiltViewModel()

    Column {
        BackIconAppBar(text = stringResource(id = R.string.app_bar_title)) {

        }
    }
}
