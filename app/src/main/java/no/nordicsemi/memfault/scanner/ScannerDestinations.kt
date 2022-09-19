package no.nordicsemi.memfault.scanner

import android.os.ParcelUuid
import no.nordicsemi.android.common.navigation.ComposeDestination
import no.nordicsemi.android.common.navigation.ComposeDestinations
import no.nordicsemi.android.common.navigation.DestinationId
import no.nordicsemi.android.common.navigation.NavigationArgument
import no.nordicsemi.android.common.navigation.NavigationResult
import no.nordicsemi.android.common.ui.scanner.DeviceSelected
import no.nordicsemi.android.common.ui.scanner.ScannerScreen
import no.nordicsemi.android.common.ui.scanner.ScanningCancelled
import no.nordicsemi.android.common.ui.scanner.model.DiscoveredBluetoothDevice
import java.util.*

val ScannerDestinationId = DestinationId("uiscanner-destination")

private val ScannerDestination = ComposeDestination(ScannerDestinationId) { navigationManager ->
    val argument = navigationManager.getArgument(ScannerDestinationId) as ScannerArgument

    ScannerScreen(
        uuid = argument.uuid,
        onResult = {
            when (it) {
                is DeviceSelected -> navigationManager.navigateUp(ScannerSuccessResult(ScannerDestinationId, it.device))
                ScanningCancelled -> navigationManager.navigateUp(ScannerCancelResult(ScannerDestinationId))
            }
        }
    )
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
