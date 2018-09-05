rootProject.name = "jsonskema"

include("jsonskema-js")
include("jsonskema-util-common")
include("jsonskema-core-common")
include("jsonskema-loading-common")
include("jsonskema-validation-common")
include("jsonskema-jvm")
include("jsonskema-api-common")

pluginManagement {
  repositories {
    jcenter()
    gradlePluginPortal()
    google()
    maven("https://dl.bintray.com/mverse-io/mverse-public")
    maven("https://kotlin.bintray.com/kotlinx")
  }

  resolutionStrategy {
    this.eachPlugin {
      if (requested.id.id == "kotlinx-serialization") {
        this.useModule("org.jetbrains.kotlinx:kotlinx-gradle-serialization-plugin:${requested.version}")
      }
    }
  }
}

