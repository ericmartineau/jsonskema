mverse  {
  dependencies {
    compile("kotlin-reflect")
    compile(kotlinIO())
    compile(kotlinIO())
  }
}

dependencies {
  compile(project(":jsonskema-api-common"))
  compile(project(":jsonskema-core-common"))
}
