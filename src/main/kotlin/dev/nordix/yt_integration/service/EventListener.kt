package dev.nordix.yt_integration.service

import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.impl.SimpleDataContext
import com.intellij.openapi.project.Project
import dev.nordix.yt_integration.helpers.NotificationHelper.notify
import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.jmdns.ServiceEvent
import javax.jmdns.ServiceListener

class EventListener(
    private val serviceState: MutableStateFlow<ServiceEventWrapper?>,
    private val scope: CoroutineScope,
    private val client: HttpClient,
    private val project: Project
) : ServiceListener {

    private val actionManager = ActionManager.getInstance()
    private var wsSession: Job? = null

    override fun serviceAdded(event: ServiceEvent) {
        serviceState.update {
            ServiceEventWrapper(
                event = event,
                source = ServiceEventSource.SERVICE_EVENT_ADDED
            )
        }
    }

    override fun serviceRemoved(event: ServiceEvent) {
        notify("Service was removed: ${event.info}")
        wsSession?.cancel()
        wsSession = null
        serviceState.update {
            ServiceEventWrapper(
                event = event,
                source = ServiceEventSource.SERVICE_EVENT_REMOVED
            )
        }
    }

    override fun serviceResolved(event: ServiceEvent) {
        if (
            serviceState.value?.source != ServiceEventSource.SERVICE_EVENT_RESOLVED ||
            wsSession == null
        ) {
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

    private suspend fun DefaultClientWebSocketSession.observeIncoming() {
        for (frame in incoming) {
            when (frame) {
                is Frame.Text -> {
                    val text = frame.readText()
                    when (text) {
                        YtActions.StartTrackerAction.alias -> {
                            val action = actionManager.getAction(YtActions.StartTrackerAction.actionId)
                            if (action != null) {
                                val dataContext: DataContext = SimpleDataContext.getProjectContext(project)
                                val event = AnActionEvent.createFromAnAction(action, null, "", dataContext)
                                action.actionPerformed(event)
                            } else {
                                notify("Action not found: ${YtActions.StartTrackerAction}")
                            }
                            notify("Starting timer by Nordix integration")
                        }
                        YtActions.StopTrackerAction.alias -> {
                            val action = actionManager.getAction(YtActions.StopTrackerAction.actionId)
                            if (action != null) {
                                val dataContext = SimpleDataContext.getProjectContext(project)
                                val event = AnActionEvent.createFromAnAction(action, null, "", dataContext)
                                action.actionPerformed(event)
                            } else {
                                notify("Action not found: ${YtActions.StopTrackerAction}")
                            }
                            notify("Stopping timer by Nordix integration")
                        }
                    }
                }
                else -> {}
            }
        }
    }

}