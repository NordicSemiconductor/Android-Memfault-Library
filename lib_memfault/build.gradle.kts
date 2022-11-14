plugins {
    id("no.nordicsemi.android.gradle.feature")
    id("no.nordicsemi.android.gradle.library")
    id("no.nordicsemi.android.gradle.library.compose")
    id("no.nordicsemi.android.gradle.hilt")
    id("no.nordicsemi.android.gradle.nexus")
}

group = "no.nordicsemi.memfault"

nordicNexusPublishing {
    POM_ARTIFACT_ID = "memfault"
    POM_NAME = "Memfault Bluetooth Le Library for Android"
    GROUP = "no.nordicsemi.memfault"

    POM_DESCRIPTION = "Android Memfault Library"
    POM_URL = "https://github.com/NordicSemiconductor/Android-Memfault-Library.git"
    POM_SCM_URL = "https://github.com/NordicSemiconductor/Android-Memfault-Library.git"
    POM_SCM_CONNECTION = "scm:git@github.com:NordicSemiconductor/Android-Memfault-Library.git"
    POM_SCM_DEV_CONNECTION = "scm:git@github.com:NordicSemiconductor/Android-Memfault-Library.git"
}

android {
    namespace = "no.nordicsemi.memfault.lib"
}

dependencies {
    implementation("androidx.core:core-ktx:1.8.0")
    implementation("androidx.appcompat:appcompat:1.4.2")
    implementation("com.google.android.material:material:1.6.1")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.3")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")

    implementation("com.google.iot.cbor:cbor:0.01.02")

    implementation("com.memfault.cloud:cloud-android:2.0.3")
    implementation("com.squareup.tape2:tape:2.0.0-beta1")

    implementation("com.squareup.moshi:moshi-kotlin:1.13.0")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-moshi:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.9.3")

    implementation("no.nordicsemi.android:ble-common:2.6.0-alpha03")
    implementation("no.nordicsemi.android:ble-ktx:2.6.0-alpha03")
    implementation("no.nordicsemi.android.support.v18:scanner:1.6.0")
    implementation("no.nordicsemi.android.common:permission:1.0.24")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")

    kapt("androidx.room:room-compiler:2.4.3")
    implementation("androidx.room:room-runtime:2.4.3")
    implementation("androidx.room:room-ktx:2.4.3")
}
