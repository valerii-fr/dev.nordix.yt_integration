package dev.nordix.yt_integration.activity

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import dev.nordix.yt_integration.services.WsService

class NordixProjectActivity : ProjectActivity {

    override suspend fun execute(project: Project) {
        println("${this.javaClass.simpleName}} launched")
        project.service<WsService>()
    }

}