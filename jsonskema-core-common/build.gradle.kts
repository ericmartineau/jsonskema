mverse  {
  dependencies {
    compile("kotlin-reflect")
    compile(kotlinImmutable())
  }
}

dependencies {
  compile(project(":jsonskema-api-common"))
}