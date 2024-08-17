package dev.nordix.yt_integration.services

import dev.nordix.yt_integration.helpers.JsonHelper
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

data class NordixPresentation(
    val type: ActionType,
    val deviceType: DeviceType,
    val name: String,
) {
    enum class DeviceType(val typeName: String) {
        YtIntegration("yt_integration"),
    }
    enum class ActionType(val typeName: String) {
        Presentation("presentation"),
    }

    private val asPresentation get() = NordixPresentationWrapper(
        type = type.typeName,
        deviceType = deviceType.typeName,
        name = name,
    )

    @Serializable
    internal data class NordixPresentationWrapper(
        val type: String,
        @SerialName("device_type")
        val deviceType: String,
        val name: String,
    )

    val json get() = JsonHelper.json.encodeToString(NordixPresentationWrapper.serializer(), asPresentation)
}