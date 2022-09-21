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

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import no.nordicsemi.memfault.DumpingDestinationArgs
import no.nordicsemi.memfault.DumpingDestinationId
import no.nordicsemi.memfault.lib.bluetooth.MDS_SERVICE_UUID
import no.nordicsemi.memfault.scanner.ScannerArgument
import no.nordicsemi.memfault.scanner.ScannerDestinationId
import no.nordicsemi.memfault.scanner.ScannerSuccessResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import no.nordicsemi.android.common.navigation.NavigationManager
import no.nordicsemi.android.common.ui.scanner.model.DiscoveredBluetoothDevice
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val navigationManager: NavigationManager
) : ViewModel() {

    init {
        navigationManager.getResultForIds(ScannerDestinationId).onEach {
            if (it is ScannerSuccessResult) {
                navigateToDumpingScreen(it.device)
            }
        }.launchIn(viewModelScope)
    }

    fun navigateToScanner() {
        navigationManager.navigateTo(ScannerDestinationId, ScannerArgument(ScannerDestinationId, MDS_SERVICE_UUID))
    }

    private fun navigateToDumpingScreen(device: DiscoveredBluetoothDevice) {
        navigationManager.navigateTo(DumpingDestinationId, DumpingDestinationArgs(DumpingDestinationId, device))
    }
}