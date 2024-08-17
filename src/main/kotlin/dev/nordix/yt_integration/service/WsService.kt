package dev.nordix.yt_integration.service

import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.impl.SimpleDataContext
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import dev.nordix.yt_integration.Constants.SERVICE_NAME
import dev.nordix.yt_integration.Constants.SERVICE_PROTOCOL
import dev.nordix.yt_integration.helpers.NotificationHelper.notify
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import java.net.InetAddress
import javax.jmdns.JmDNS
import javax.jmdns.ServiceEvent
import javax.jmdns.ServiceListener

@Service(Service.Level.PROJECT)
class WsService(project: Project) {

    private val jmdns = JmDNS.create(InetAddress.getLocalHost())
    private var serviceState = MutableStateFlow<ServiceEventWrapper?>(null)

    private val client = HttpClient(CIO) {
        install(WebSockets)
    }

    private val eventListener = EventListener(
        serviceState = serviceState,
        scope = CoroutineScope(Dispatchers.IO),
        client = client,
        project = project
    )

    init {
        notify("Service is launched")
        jmdns.addServiceListener("$SERVICE_NAME.$SERVICE_PROTOCOL.local.", eventListener)
    }
}