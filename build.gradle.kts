import java.nio.file.Paths

// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

val kotlin_version: String by project
val logback_version: String by project

plugins {
  kotlin("jvm") version "2.0.10"
  kotlin("plugin.serialization") version "2.0.10"
  id("java")
  id("org.jetbrains.intellij") version "1.17.4"
  id("io.ktor.plugin") version "2.3.12"
}

group = "dev.nordix.yt_integration"
version = "1.0.0"

repositories {
  mavenCentral()
}

application {
  mainClass.set("io.ktor.server.netty.EngineMain")

  val isDevelopment: Boolean = project.ext.has("development")
  applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

java {
  sourceCompatibility = JavaVersion.VERSION_17
}

// See https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html
intellij {
  version.set("2024.1")
}

tasks {
  buildSearchableOptions {
    enabled = false
  }

  patchPluginXml {
    version.set("${project.version}")
    sinceBuild.set("233")
    untilBuild.set("242.*")
  }

  compileKotlin {
    kotlinOptions.jvmTarget = "17"
  }

  compileTestKotlin {
    kotlinOptions.jvmTarget = "17"
  }
}

dependencies {
  implementation("io.ktor:ktor-client-core")
  implementation("io.ktor:ktor-client-cio")
  implementation("io.ktor:ktor-client-websockets")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
  implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.7.1")
  implementation("org.jmdns:jmdns:3.5.8")
}

tasks {
  run {
    // workaround for https://youtrack.jetbrains.com/issue/IDEA-285839/Classpath-clash-when-using-coroutines-in-an-unbundled-IntelliJ-plugin
    buildPlugin {
      exclude { "coroutines" in it.name }
      exclude { "serialization" in it.name }
    }
    prepareSandbox {
      exclude { "coroutines" in it.name }
      exclude { "serialization" in it.name }
    }
  }
}