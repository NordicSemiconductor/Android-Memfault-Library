package com.nordicsemi.memfault.bluetooth.responses

import android.bluetooth.BluetoothDevice
import no.nordicsemi.android.ble.callback.profile.ProfileReadResponse
import no.nordicsemi.android.ble.data.Data

class DataUriResponse : ProfileReadResponse() {

    var uri: String? = null

    override fun onDataReceived(device: BluetoothDevice, data: Data) {
        super.onDataReceived(device, data)

        uri = data.getStringValue(0)
    }
}
