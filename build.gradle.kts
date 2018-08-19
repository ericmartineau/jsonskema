import io.mverse.gradle.kotlinx

plugins {
  id("io.mverse.project") version "0.5.32"
  id("io.mverse.multi-platform") version "0.5.32"
  id("kotlinx-serialization") version "0.6.1"
}

allprojects  {
  plugins.apply("kotlinx-serialization")
  afterEvaluate {
    group = "io.mverse.jsonskema"
  }
  repositories {
    kotlinx()
  }
  dependencyManagement {
    dependencies {
      // None
      dependency("org.jetbrains.kotlinx:kotlinx-serialization-runtime:0.60.1")
      dependency("org.jetbrains.kotlinx:kotlinx-serialization-runtime-common:0.60.1")

      dependency("org.jetbrains.kotlinx:kotlinx-io:1.0.42")
      dependency("org.jetbrains.kotlinx:kotlinx-io-js:1.0.42")
      dependency("org.jetbrains.kotlinx:kotlinx-io-common:1.0.42")
      dependency("org.jetbrains.kotlinx:kotlinx-io-js:1.0.42")

      // Immutable Collections Library for Kotlin
      dependency("org.jetbrains.kotlinx:kotlinx-collections-immutable:0.1")
    }
  }
}

mverse {
}


