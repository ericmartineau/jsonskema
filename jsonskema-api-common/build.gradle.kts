plugins {
}

mverse  {
  isDefaultDependencies = false
  dependencies {
    implementation(kotlinStdlib())
    implementation("kotlin-reflect")
    implementation(kotlinTest())
    implementation(kotlinImmutable())
    implementation("mverse-lang-common")
    implementation("kotlin-logging")
    implementation("ktor-client-core")
    implementation("ktor-client-cio")
    implementation("mverse-log-common")
    implementation("kotlinx-io")
    implementation("kotlinx-coroutines-io")
    implementation("kotlinx-collections-immutable")
    implementation("kotlinx-serialization-runtime-common")
  }
}

dependencies {
//  compile("io.github.jffiorillo:jvmbuilder-annotations:0.0.4")
}
