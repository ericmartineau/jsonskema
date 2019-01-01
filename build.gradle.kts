import io.mverse.gradle.kotlinx

plugins {
  id("io.mverse.project") version "0.5.32"
  id("io.mverse.multi-platform") version "0.5.32"
  kotlin("jvm").version("1.3.10")
  id("kotlinx-serialization").version("1.3.10")
}

allprojects  {
  plugins.apply("kotlinx-serialization")
  mverse {
    isDefaultDependencies = false
    coverageRequirement = 0.63
    bom = "io.mverse:mverse-bom:0.5.13"
    dependencies {
      compile(kotlinSerialization())
    }
  }

  afterEvaluate {
    group = "io.mverse.jsonskema"
  }

  repositories {
    jcenter()
    kotlinx()
  }

  dependencyManagement {
    dependencies {
      // None

      dependencySet("org.jetbrains.kotlin:1.3.10") {
        entry("kotlin-stdlib")
        entry("kotlin-runtime")
        entry("kotlin-stdlib-common")
        entry("kotlin-stdlib-jdk7")
        entry("kotlin-stdlib-jdk8")
        entry("kotlin-reflect")
        entry("kotlin-test-annotations-common")
        entry("kotlin-test")
        entry("kotlin-test-junit")
      }

      dependencySet("org.jetbrains.kotlinx:0.90.1") {
        entry("kotlinx-serialization-runtime")
        entry("kotlinx-serialization-runtime-common")
        entry("kotlinx-serialization-runtime-jsonparser")
      }

      dependencySet("org.jetbrains.kotlinx:0.9.1") {
        entry("kotlinx-serialization-runtime-jsonparser")
      }

      dependency("org.jetbrains.kotlinx:kotlinx-io:0.1.2-dev-6")
      dependency("org.jetbrains.kotlinx:kotlinx-io-common:0.1.2-dev-6")

      dependency("org.jetbrains.kotlinx:kotlinx-io-js:0.1.2-dev-6")


      // Immutable Collections Library for Kotlin
      dependency("org.jetbrains.kotlinx:kotlinx-collections-immutable:0.1")
    }
  }
}




