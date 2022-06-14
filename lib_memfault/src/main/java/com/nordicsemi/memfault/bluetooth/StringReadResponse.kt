package com.nordicsemi.memfault.bluetooth

import android.bluetooth.BluetoothDevice
import no.nordicsemi.android.ble.callback.profile.ProfileReadResponse
import no.nordicsemi.android.ble.data.Data

class StringReadResponse : ProfileReadResponse() {

    var value: String? = null

    override fun onDataReceived(device: BluetoothDevice, data: Data) {
        super.onDataReceived(device, data)

        value = data.getStringValue(0)
    }
}
