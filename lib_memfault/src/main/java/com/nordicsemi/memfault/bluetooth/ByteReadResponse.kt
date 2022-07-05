package com.nordicsemi.memfault.bluetooth

import android.bluetooth.BluetoothDevice
import android.util.Log
import no.nordicsemi.android.ble.callback.profile.ProfileReadResponse
import no.nordicsemi.android.ble.data.Data

class ByteReadResponse : ProfileReadResponse() {

    var value: ByteArray? = null

    override fun onDataReceived(device: BluetoothDevice, data: Data) {
        super.onDataReceived(device, data)

        value = data.value
        Log.d("DATA-RECEIVER","Receive chunk: ${data.value?.get(0)}")
        value = data.value?.let {
            it.copyOfRange(1, it.size)
        }
    }
}
