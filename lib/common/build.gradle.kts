plugins {
    alias(libs.plugins.nordic.library)
    alias(libs.plugins.nordic.kotlin.android)
    alias(libs.plugins.nordic.nexus.android)
}

group = "no.nordicsemi.memfault"

nordicNexusPublishing {
    POM_ARTIFACT_ID = "memfault-common"
    POM_NAME = "Android Memfault Library"

    POM_DESCRIPTION = "A library for interacting with Memfault Cloud."
    POM_URL = "https://github.com/NordicSemiconductor/Android-Memfault-Library.git"
    POM_SCM_URL = "https://github.com/NordicSemiconductor/Android-Memfault-Library.git"
    POM_SCM_CONNECTION = "scm:git@github.com:NordicSemiconductor/Android-Memfault-Library.git"
    POM_SCM_DEV_CONNECTION = "scm:git@github.com:NordicSemiconductor/Android-Memfault-Library.git"
}

android {
    namespace = "no.nordicsemi.memfault.common"
}

dependencies {
    api(libs.memfault.cloud)
    api(libs.androidx.annotation)

    implementation(libs.kotlinx.coroutines.android)
}