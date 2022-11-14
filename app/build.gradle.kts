plugins {
    id("no.nordicsemi.android.gradle.application")
    id("no.nordicsemi.android.gradle.application.compose")
    id("no.nordicsemi.android.gradle.hilt")
}

group = "no.nordicsemi.memfault"

android {
    namespace = "no.nordicsemi.memfault"
}

dependencies {
    implementation(project(":lib_memfault"))

    implementation("com.google.accompanist:accompanist-placeholder-material:0.24.13-rc")
    implementation("androidx.compose.ui:ui:1.2.1")
    // Tooling support (Previews, etc.)
    implementation("androidx.compose.ui:ui-tooling:1.2.1")
    // Foundation (Border, Background, Box, Image, Scroll, shapes, animations, etc.)
    implementation("androidx.compose.foundation:foundation:1.2.1")
    // Material Design
    implementation("androidx.compose.material3:material3:1.0.0-rc01")
    // Material design icons
    implementation("androidx.compose.material:material-icons-core:1.2.1")
    implementation("androidx.compose.material:material-icons-extended:1.2.1")
    // Integration with activities
    implementation("androidx.activity:activity-compose:1.6.1")
    // Integration with ViewModels
//    implementation 'androidx.lifecycle:lifecycle-viewmodel-compose:2.5.1'
    implementation("androidx.lifecycle:lifecycle-runtime:2.5.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.5.1")
    // Integration with observables
    implementation("androidx.compose.runtime:runtime-livedata:1.2.1")
    implementation("androidx.compose.runtime:runtime-rxjava2:1.2.1")

    implementation("no.nordicsemi.android.common:theme:1.0.24")
    implementation("no.nordicsemi.android.common:navigation:1.0.24")
    implementation("no.nordicsemi.android.common:uiscanner:1.0.24")
    implementation("no.nordicsemi.android.common:uilogger:1.0.24")
    implementation("no.nordicsemi.android.common:permission:1.0.24")

    implementation("androidx.hilt:hilt-navigation-compose:1.0.0")
    implementation("com.google.dagger:hilt-android:2.43.2")
    kapt("com.google.dagger:hilt-compiler:2.43.2")

    kapt("androidx.hilt:hilt-compiler:1.0.0")

    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.appcompat:appcompat:1.5.1")
    implementation("com.google.android.material:material:1.6.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.navigation:navigation-fragment-ktx:2.5.2")
    implementation("androidx.navigation:navigation-ui-ktx:2.5.2")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.3")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")
}
