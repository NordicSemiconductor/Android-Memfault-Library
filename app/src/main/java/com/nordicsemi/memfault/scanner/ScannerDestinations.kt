package com.nordicsemi.memfault.scanner

import android.os.ParcelUuid
import no.nordicsemi.android.common.navigation.*
import no.nordicsemi.android.common.permission.view.PermissionScreen
import no.nordicsemi.android.common.ui.scanner.model.DiscoveredBluetoothDevice
import java.util.*

val ScannerDestinationId = DestinationId("uiscanner-destination")

private val ScannerDestination =
    ComposeDestination(ScannerDestinationId) { navigationManager ->
        PermissionScreen(onNavigateBack = { navigationManager.navigateUp() }) {
            ScannerContent(navigationManager = navigationManager)
        }
    }

val ScannerDestinations = ComposeDestinations(listOf(ScannerDestination))

data class ScannerArgument(
    override val destinationId: DestinationId,
    val uuid: ParcelUuid
) : NavigationArgument {
    constructor(destinationId: DestinationId, uuid: UUID) : this(destinationId, ParcelUuid(uuid))
}

data class ScannerSuccessResult(
    override val destinationId: DestinationId,
    val device: DiscoveredBluetoothDevice
) : NavigationResult

data class ScannerCancelResult(
    override val destinationId: DestinationId
) : NavigationResult
