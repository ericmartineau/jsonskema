mverse  {
  isDefaultDependencies = false

  dependencies {
    compile("kotlin-reflect")
    compile(kotlinIO())
    compile("kotlinx-serialization-runtime-common")
  }
}

dependencies {
  compile(project(":jsonskema-api-common"))
  compile(project(":jsonskema-core-common"))
}
