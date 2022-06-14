package com.nordicsemi.memfault

import com.nordicsemi.memfault.home.HomeScreen
import no.nordicsemi.android.navigation.ComposeDestination
import no.nordicsemi.android.navigation.ComposeDestinations

val HomeDestinations = ComposeDestinations(HomeDestination.values().map { it.destination })

enum class HomeDestination(val destination: ComposeDestination) {
    HOME(ComposeDestination("home-destination") { HomeScreen() }),
}
