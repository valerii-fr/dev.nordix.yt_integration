package dev.nordix.yt_integration.services

import javax.jmdns.ServiceEvent

data class ServiceEventWrapper(
    val event: ServiceEvent,
    val source: ServiceEventSource,
)
