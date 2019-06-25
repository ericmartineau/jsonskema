plugins {
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
    compile("kotlinx-collections-immutable")
  }
}

dependencies {
//  compile("io.github.jffiorillo:jvmbuilder-annotations:0.0.4")
}
