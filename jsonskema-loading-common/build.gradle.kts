mverse  {
  isDefaultDependencies = false

  dependencies {
    implementation("kotlin-reflect")
    implementation(kotlinIO())
    implementation(kotlinStdlib())
    implementation("kotlinx-serialization-runtime-common")
    implementation("mverse-lang-common")
    implementation("mverse-log-common")
    implementation("mverse-coroutines-common")
    implementation("kotlinx-coroutines-core-common")
  }
}

dependencies {
  compile(project(":jsonskema-api-common"))
  compile(project(":jsonskema-core-common"))
}
