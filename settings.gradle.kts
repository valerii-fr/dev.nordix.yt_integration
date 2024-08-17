// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
pluginManagement {
  repositories {
    mavenCentral()
    gradlePluginPortal()
  }
}

buildscript {
  configurations.all {
    resolutionStrategy {
      sortArtifacts(ResolutionStrategy.SortOrder.DEPENDENCY_FIRST)
    }
  }
}
rootProject.name = "dev.nordix.yt_integration"

