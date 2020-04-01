mverse  {
  isDefaultDependencies = false

  dependencies {
    implementation("kotlin-reflect")
    implementation(kotlinImmutable())
    implementation("mverse-lang-common")
    implementation("mverse-log-common")
    implementation("kotlinx-serialization-runtime-common")
  }
}

dependencies {
  compile(project(":jsonskema-api-common"))
}
