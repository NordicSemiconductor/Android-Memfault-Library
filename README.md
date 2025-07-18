# Android Memfault Library

An Android library that can connect to a Bluetooth LE device, download diagnostics data and upload them to [the Memfault console](https://docs.memfault.com).

The device should contain characteristics defined in [the Memfault documentation](https://docs.memfault.com/docs/mcu/mds).

## Example usage

```kotlin
val manager = MemfaultDiagnosticsManager.create(context)

//Receive status and data
manager.state.collect {

}

//To start
viewModelScope.launch {
    manager.connect(context, device)
}

//When finished
manager.disconnect()
```

## Application

<a href='https://play.google.com/store/apps/details?id=no.nordicsemi.memfault'><img alt='Get it on Google Play' src='https://play.google.com/intl/en_us/badges/static/images/badges/en_badge_web_generic.png' width='250'/></a>

<img src="https://play-lh.googleusercontent.com/Mu4RHwGVpQtvkgq5ExluQNw3ZemvGZlCnrpNnEHfTznx4-7wBYWD48ZM6R0iPbiopw=w2560-h1440" width="200"> <img src="https://play-lh.googleusercontent.com/H4zNJPS3Wa6XfWYpEC-1_WZ7i2KGI4HkiymornuR3cqOS94cb6grrbqI49D2b-AdckM=w2560-h1440" width="200"> <img src="https://play-lh.googleusercontent.com/uH9jN5JnRq3s796j3IE0Ab28z_POuiy3_FSIXbjZASqQUGMuz49CR665FER9HpBqO-w=w2560-h1440" width="200">

## Library

The library is available on Maven Central repository. Add it to your project by adding the following dependency:

```Groovy
implementation 'no.nordicsemi.android:memfault-observability:1.1.0'
```

## Dependencies
Application under the hood uses:
1. [Kotlin BLE Library](https://github.com/NordicSemiconductor/Kotlin-BLE-Library) - for managing BLE connection and reading data from the remote device.
2. [MemfaultCloud Android](https://github.com/memfault/memfault-cloud-android) - for uploading chunks to the cloud.
