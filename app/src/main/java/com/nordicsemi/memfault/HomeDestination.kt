package com.nordicsemi.memfault

import com.nordicsemi.memfault.dumping.DumpingScreen
import com.nordicsemi.memfault.home.HomeScreen
import no.nordicsemi.android.navigation.ComposeDestination
import no.nordicsemi.android.navigation.ComposeDestinations
import no.nordicsemi.android.navigation.DestinationId

val HomeDestinationId = DestinationId("home-destination")
val DumpingDestinationId = DestinationId("dumping-destination")

val HomeDestinations = ComposeDestinations(listOf(
    ComposeDestination(HomeDestinationId) { HomeScreen() },
    ComposeDestination(DumpingDestinationId) { DumpingScreen() }
))
