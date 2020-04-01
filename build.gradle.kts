import io.mverse.gradle.kotlinx
import io.spring.gradle.dependencymanagement.dsl.DependenciesHandler
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  kotlin("jvm")
  id("io.mverse.project")
  id("io.mverse.multi-platform")
  id("kotlinx-serialization")
}

allprojects {
  mverse {
    isDefaultDependencies = false
    coverageRequirement = 0.75
    bom = "io.mverse:mverse-bom:0.5.13"
  }
  tasks.withType<KotlinCompile> {
    kotlinOptions {
      jvmTarget = "1.8"
      freeCompilerArgs += "-Xopt-in=kotlin.RequiresOptIn"
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

      installKotlinDeps()
      installMverseShared()

      dependency("io.mverse:hashkode:1.0.1")

      val ktor:String by rootProject
      dependencySet("io.ktor:$ktor") {
        entry("ktor-server-servlet")
        entry("ktor-utils")
        entry("ktor-utils-jvm")
        entry("ktor-server-netty")
        entry("ktor-html-builder")
        entry("ktor-client-cio")
        entry("ktor-jackson")
        entry("ktor-server-core")
        entry("ktor-locations")
        entry("ktor-auth")
        entry("ktor-auth-jwt")
        entry("ktor-client-core")
      }

      dependency("io.github.microutils:kotlin-logging:1.6.22")

      // Immutable Collections Library for Kotlin
      dependency("org.jetbrains.kotlinx:kotlinx-collections-immutable:0.1")


      dependencySet("org.slf4j:1.7.+") {
        entry("slf4j-api")
        entry("jul-to-slf4j")
      }

      dependencySet("ch.qos.logback:1.2.+") {
        entry("logback-classic")
        entry("logback-core")
      }
    }
  }


}

fun DependenciesHandler.installKotlinDeps() {
  val kotlinCoroutines: String by rootProject
  val kotlin: String by rootProject
  val kotlinSerialization: String by rootProject
  val kotlinIO: String by rootProject

  // None
  dependencySet("org.jetbrains.kotlin:$kotlin") {
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

  dependencySet("org.jetbrains.kotlinx:$kotlinCoroutines") {
    entry("kotlinx-coroutines-core")
    entry("kotlinx-coroutines-core-common")
    entry("kotlinx-coroutines-jdk8")
  }

  dependencySet("org.jetbrains.kotlinx:$kotlinIO") {
    entry("kotlinx-io")
    entry("kotlinx-io-jvm")
    entry("kotlinx-coroutines-io")
    entry("kotlinx-coroutines-io-jvm")
  }

  dependencySet("org.jetbrains.kotlinx:$kotlinSerialization") {
    entry("kotlinx-serialization-runtime")
    entry("kotlinx-serialization-runtime-common")
    entry("kotlinx-serialization-runtime-jsonparser")
  }
}

fun DependenciesHandler.installMverseShared() {
  val mverseShared: String by rootProject

  dependencySet("io.mverse:$mverseShared") {
    entry("mverse-json")
    entry("mverse-lang-jvm")
    entry("mverse-lang-common")
    entry("mverse-log-common")
    entry("mverse-log-jvm")
    entry("mverse-test-common")
    entry("mverse-test-jvm")
    entry("mverse-coroutines-common")
    entry("mverse-coroutines-jvm")
  }
}


