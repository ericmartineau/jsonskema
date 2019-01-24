mverse  {
  isDefaultDependencies = false

  dependencies {
    compile("kotlin-reflect")
    compile(kotlinImmutable())
    compile("kotlinx-serialization-runtime-common")
  }
}

dependencies {
  compile(project(":jsonskema-api-common"))
}
