mverse  {
  isDefaultDependencies = false

  dependencies {
    compile("kotlin-reflect")
    compile(kotlinIO())
    compile(kotlinStdlib())
    compile("kotlinx-serialization-runtime-common")
    compile("mverse-lang-common")
    compile("kotlinx-coroutines-core-common")
  }
}

dependencies {
  compile(project(":jsonskema-api-common"))
  compile(project(":jsonskema-core-common"))
}
