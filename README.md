# Android Memfault Library

An Android library that can connect to a Bluetooth LE device, download dump logs and upload them to [the Memfault console](https://docs.memfault.com).

The device should contain characteristics defined in [the Memfault documentation](https://memfault.notion.site/Memfault-Diagnostic-GATT-Service-MDS-ffd5a430062649cd9bf6edbf64e2563b).

## Example usage

```kotlin
val memfaultManager = MemfaultManager()

viewModelScope.launch {
    memfaultManager.connect(context, device).collect {
        //Consume status
    }
}

//when finished
memfaultManager.disconnect()
```

## App

The repository contains also [the app](https://play.google.com/store/apps/details?id=com.nordicsemi.memfault) which shows example usage of the library. It connects to a selected device and maintains connection as long as user decided. The app presents basic statistics about number of uploaded chunks and a delay since the last chunk has been sent.

<img src="https://play-lh.googleusercontent.com/73Y1WEr-Yx2y3m0iQbyWhHAVCv5WLQxMcZBQqchDo4z5kFxa_bMhFRpsiJBeFfr-nQI=w2560-h1440" width="200"> <img src="https://play-lh.googleusercontent.com/yzN9kmcg0a8tFqi9wWT4qZkTjubAm3mvYDLGBTT4S80jKNUUyjpR4jbfAbFysI34Kzw=w2560-h1440" width="200"> <img src="https://play-lh.googleusercontent.com/4hEHXCxexumy1Lr0q8C_HGOMAxWvU3sQni6H4B7Aold0osAju7HdTuvXaKsFBfVOTVg=w2560-h1440" width="200">

## Dependencies
Application under the hood uses:
1. [BLE Library](https://github.com/NordicSemiconductor/Android-BLE-Library) - for managing BLE connection and reading data from the remote device.
2. [Retrofit](https://square.github.io/retrofit) - for uploading chunks to the cloud.
