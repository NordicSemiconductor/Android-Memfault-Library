plugins {
    alias(libs.plugins.nordic.application.compose)
    alias(libs.plugins.nordic.hilt)
}

group = "no.nordicsemi.memfault"

android {
    namespace = "no.nordicsemi.memfault"
}

dependencies {
    implementation(project(":lib_memfault"))

    implementation(libs.accompanist.placeholder)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.iconsExtended)
    implementation(libs.androidx.activity.compose)

    implementation(libs.androidx.hilt.navigation.compose)

    implementation(libs.androidx.lifecycle.runtime)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    implementation(libs.nordic.theme)
    implementation(libs.nordic.navigation)
    implementation(libs.nordic.uilogger)
    implementation(libs.nordic.uiscanner)
    implementation(libs.nordic.permission)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
}
