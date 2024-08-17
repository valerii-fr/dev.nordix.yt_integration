package dev.nordix.yt_integration.services

import dev.nordix.yt_integration.Constants

enum class YtActions(val alias: String, val actionId: String) {
    StopTrackerAction(
        alias = Constants.STOP_TRACKER_ACTION,
        actionId = "com.github.jk1.ytplugin.timeTracker.actions.StopTrackerAction"
    ),
    StartTrackerAction(
        alias = Constants.START_TRACKER_ACTION,
        actionId = "com.github.jk1.ytplugin.timeTracker.actions.StartTrackerAction"
    ),
}