package dev.nordix.yt_integration.service

enum class YtActions(val alias: String, val actionId: String) {
    StopTrackerAction(
        alias = "stop_timer",
        actionId = "com.github.jk1.ytplugin.timeTracker.actions.StopTrackerAction"
    ),
    StartTrackerAction(
        alias = "start_timer",
        actionId = "com.github.jk1.ytplugin.timeTracker.actions.StartTrackerAction"
    ),
}