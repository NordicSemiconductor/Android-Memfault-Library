package com.nordicsemi.memfault.bluetooth

import android.bluetooth.BluetoothDevice
import android.util.Log
import no.nordicsemi.android.ble.callback.profile.ProfileReadResponse
import no.nordicsemi.android.ble.data.Data

private const val TAG = "DATA-RECEIVER"

class ByteReadResponse : ProfileReadResponse() {

    var value: ByteArray? = null
    var chunkNumber: Byte? = null

    override fun onDataReceived(device: BluetoothDevice, data: Data) {
        super.onDataReceived(device, data)

        value = data.value
        chunkNumber = data.value!![0]
        Log.d(TAG,"Receive chunk: ${data.value?.get(0)}")
        value = data.value?.let {
            it.copyOfRange(1, it.size)
        }
        Log.d(TAG,"Chunk: ${Data(value!!)}")
    }
}
