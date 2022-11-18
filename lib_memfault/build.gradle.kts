plugins {
    alias(libs.plugins.nordic.feature)
    alias(libs.plugins.nordic.hilt)
    alias(libs.plugins.nordic.nexus)
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
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.compose.material3)

    implementation(libs.memfault.cloud)

    implementation(libs.nordic.ble.ktx)
    implementation(libs.nordic.ble.common)
    implementation(libs.nordic.scanner)
    implementation(libs.nordic.permission)

    implementation(libs.kotlinx.coroutines.android)

    kapt(libs.room.compiler)
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
}
