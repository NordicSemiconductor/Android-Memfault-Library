package com.nordicsemi.memfault.home

import androidx.lifecycle.ViewModel
import com.nordicsemi.memfault.DumpingDestinationId
import dagger.hilt.android.lifecycle.HiltViewModel
import no.nordicsemi.android.navigation.NavigationManager
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val navigationManager: NavigationManager
) : ViewModel() {

    fun navigateBack() {
        navigationManager.navigateUp()
    }

    fun navigateNext() {
        navigationManager.navigateTo(DumpingDestinationId)
    }
}
