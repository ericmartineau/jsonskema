plugins {
  id("kotlinx-serialization")
}

mverse  {
  isDefaultDependencies = false
  dependencies {
    compile(kotlinStdlib())
    compile("kotlin-reflect")
    compile(kotlinTest())
    compile(kotlinImmutable())
    compile("mverse-lang-common")
    compile("kotlin-logging")
    compile("ktor-client-core")
    compile("ktor-client-cio")
    compile("mverse-log-common")
    compile(kotlinSerialization())
    compile("kotlinx-io")
    compile("kotlinx-coroutines-io")
  }
}

dependencies {
  compile("io.github.jffiorillo:jvmbuilder-annotations:0.0.4")
  compile("org.jetbrains.kotlinx:kotlinx-collections-immutable:0.1")
}
