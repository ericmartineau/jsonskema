import io.mverse.gradle.kotlinx
import io.spring.gradle.dependencymanagement.dsl.DependenciesHandler
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  id("kotlin-multiplatform")
  id("io.mverse.project")
  id("io.mverse.multi-platform")
  id("kotlinx-serialization")
}

allprojects {
  plugins.apply("kotlinx-serialization")
  mverse {
    isDefaultDependencies = false
    coverageRequirement = 0.60
    bom = "io.mverse:mverse-bom:0.5.13"
    dependencies {
      compile(kotlinSerialization())
    }
  }

  jacoco {

  }

  tasks.withType<KotlinCompile> {
    kotlinOptions {
      jvmTarget = "1.8"
      freeCompilerArgs += "-Xuse-experimental=kotlin.Experimental"
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
  val kotlinCoroutines: String by project
  val kotlin: String by project
  val kotlinSerialization: String by project
  val kotlinIO: String by project

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
  val mverseShared: String by project

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


