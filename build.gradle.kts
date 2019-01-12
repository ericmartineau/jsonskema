import io.mverse.gradle.kotlinx
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  id("io.mverse.project") version "0.5.32"
  id("io.mverse.multi-platform") version "0.5.32"
  kotlin("jvm").version("1.3.10")
  id("kotlinx-serialization").version("1.3.10")
}

allprojects  {
  val msharedVersion by extra { "0.5.75" }

  plugins.apply("kotlinx-serialization")
  mverse {
    isDefaultDependencies = false
    coverageRequirement = 0.63
    bom = "io.mverse:mverse-bom:0.5.13"
    dependencies {
      compile(kotlinSerialization())
    }
  }

  tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
    kotlinOptions.suppressWarnings = true
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

      dependencySet("io.mverse:$msharedVersion") {
        entry("mverse-json")
        entry("mverse-lang-jvm")
        entry("mverse-lang-common")
      }

      /**
       * 0.90.3 is a custom version that preserves order of keys
       */
      dependencySet("org.jetbrains.kotlinx:0.90.3") {
        entry("kotlinx-serialization-runtime")
        entry("kotlinx-serialization-runtime-common")
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




