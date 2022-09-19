package no.nordicsemi.memfault.lib.bluetooth

import android.bluetooth.BluetoothDevice
import no.nordicsemi.android.ble.callback.profile.ProfileReadResponse
import no.nordicsemi.android.ble.data.Data

internal class ByteReadResponse : ProfileReadResponse() {

    var value: ByteArray? = null

    override fun onDataReceived(device: BluetoothDevice, data: Data) {
        super.onDataReceived(device, data)

        value = data.value
    }
}
