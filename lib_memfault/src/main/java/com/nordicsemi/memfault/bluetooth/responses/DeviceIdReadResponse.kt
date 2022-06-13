package com.nordicsemi.memfault.bluetooth.responses

import android.bluetooth.BluetoothDevice
import no.nordicsemi.android.ble.callback.profile.ProfileReadResponse
import no.nordicsemi.android.ble.data.Data

class DeviceIdReadResponse : ProfileReadResponse() {

    var authorisation: String? = null

    override fun onDataReceived(device: BluetoothDevice, data: Data) {
        super.onDataReceived(device, data)

        authorisation = data.getStringValue(0)
    }
}
