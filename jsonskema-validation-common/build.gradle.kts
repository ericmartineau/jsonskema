mverse {
  isDefaultDependencies = false
  dependencies {
    compile("mverse-lang-common")
  }
}
dependencies {
  compile(project(":jsonskema-api-common"))
  compile(project(":jsonskema-core-common"))
}
