# Android Memfault Library

An Android library that can connect to a Bluetooth LE device, download dump logs and upload them to [the Memfault console](https://docs.memfault.com).

The device should contain characteristics defined in [the Memfault documentation](https://memfault.notion.site/Memfault-Diagnostic-GATT-Service-MDS-ffd5a430062649cd9bf6edbf64e2563b).

## Example usage

```kotlin
val memfaultBleManager = MemfaultBleManager()

//Receive status and data
memfaultBleManager.state.collect {
    
}

//To start
viewModelScope.launch {
    memfaultBleManager.connect(context, device)
}

//When finished
memfaultBleManager.disconnect()
```

## App

The repository contains also [the app](https://play.google.com/store/apps/details?id=no.nordicsemi.memfault) which shows example usage of the library. It connects to a selected device and maintains connection as long as user decided. The app presents basic statistics about number of uploaded chunks and a delay since the last chunk has been sent.

<img src="https://play-lh.googleusercontent.com/JzsNTdvex7wmthZsLMzQbuSTlyxJw9dIHvDIBlRbFE7FQULj8rQblU0ukW0rwlg9tz8=w2560-h1440" width="200"> <img src="https://play-lh.googleusercontent.com/NcS26DPrspB-nMXYS6qaTgq7ilcsUfolr4tbjnl6H-lNFjR2WNe6Ncot2beGqiBKo6w=w2560-h1440" width="200"> <img src="https://play-lh.googleusercontent.com/B3PcyFzTNEGJsMknxKlLLg0OnVyy8nAyU7xc8MUVTaWwdW5JPj8e6Ws762GE35SFtGI=w2560-h1440" width="200">

## Dependencies
Application under the hood uses:
1. [BLE Library](https://github.com/NordicSemiconductor/Android-BLE-Library) - for managing BLE connection and reading data from the remote device.
2. [MemfaultCloud Android](https://github.com/memfault/memfault-cloud-android) - for uploading chunks to the cloud.
