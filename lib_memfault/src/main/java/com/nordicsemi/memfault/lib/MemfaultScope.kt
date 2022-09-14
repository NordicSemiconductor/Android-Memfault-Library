package com.nordicsemi.memfault.lib

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

internal val MemfaultScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)