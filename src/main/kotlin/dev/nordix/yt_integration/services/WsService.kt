package dev.nordix.yt_integration.services

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
class WsService(private val project: Project) {

    private val scope = CoroutineScope(Dispatchers.IO)
    private val jmdns = JmDNS.create(InetAddress.getLocalHost())
    private var serviceState = MutableStateFlow<ServiceEventWrapper?>(null)
    private val actionManager = ActionManager.getInstance()
    private var wsSession: Job? = null

    private val client = HttpClient(CIO) {
        install(WebSockets)
    }

    init {
        println("WsService initialized for project: ${project.name}")
        notify("Service is launched")
        val actionIds = actionManager.getActionIds("com.github.jk1.ytplugin.timeTracker")
        notify(actionIds.joinToString { "$it\n" })
        jmdns.addServiceListener("$SERVICE_NAME.$SERVICE_PROTOCOL.local.", object : ServiceListener {
            override fun serviceAdded(event: ServiceEvent) {
                serviceState.update {
                    ServiceEventWrapper(
                        event = event,
                        source = ServiceEventSource.SERVICE_EVENT_ADDED
                    )
                }
            }

            override fun serviceRemoved(event: ServiceEvent) {
                notify("Служба удалена: ${event.info}")
                if (serviceState.value?.event?.info?.name == event.info.name) {
                    wsSession?.cancel()
                    wsSession = null
                }
                serviceState.update {
                    ServiceEventWrapper(
                        event = event,
                        source = ServiceEventSource.SERVICE_EVENT_REMOVED
                    )
                }
            }

            override fun serviceResolved(event: ServiceEvent) {
                if (serviceState.value?.source != ServiceEventSource.SERVICE_EVENT_RESOLVED) {
                    notify("Nordix attach service is connected")
                    wsSession = scope.launch {
                        client.webSocket(host = event.info.hostAddress, port = event.info.port, path = "/ws") {
                            delay(500)
                            val tframe = NordixPresentation(
                                type = NordixPresentation.ActionType.Presentation,
                                deviceType = NordixPresentation.DeviceType.YtIntegration,
                                name = System.getProperty("user.name")
                            ).json
                            println(tframe)
                            send(tframe)
                            observeIncoming()
                        }
                    }

                }

                serviceState.update {
                    if (it?.source != ServiceEventSource.SERVICE_EVENT_RESOLVED) {
                        ServiceEventWrapper(
                            event = event,
                            source = ServiceEventSource.SERVICE_EVENT_RESOLVED
                        )
                    } else it
                }
            }
        })

    }

    private suspend fun DefaultClientWebSocketSession.observeIncoming() {
        for (frame in incoming) {
            when (frame) {
                is Frame.Text -> {
                    val text = frame.readText()
                    println("Received: $text")
                    when (text) {
                        "start_timer" -> {
                            val action = actionManager.getAction(YtActions.StartTrackerAction.actionId)
                            if (action != null) {
                                val dataContext: DataContext = SimpleDataContext.getProjectContext(project)
                                val event = AnActionEvent.createFromAnAction(action, null, "", dataContext)
                                action.actionPerformed(event)
                            } else {
                                notify("action not found: ${YtActions.StartTrackerAction}")
                            }
                            notify("starting timer by Nordix integration")
                        }
                        "stop_timer" -> {
                            val action = actionManager.getAction(YtActions.StopTrackerAction.actionId)
                            if (action != null) {
                                val dataContext = SimpleDataContext.getProjectContext(project)
                                val event = AnActionEvent.createFromAnAction(action, null, "", dataContext)
                                action.actionPerformed(event)
                            } else {
                                notify("action not found: ${YtActions.StopTrackerAction}")
                            }
                            notify("stopping timer by Nordix integration")
                        }
                    }
                }
                else -> {}
            }
        }
    }
}