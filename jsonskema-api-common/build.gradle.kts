plugins {
  id("kotlinx-serialization")
}

mverse  {
  isDefaultDependencies = false
  dependencies {
    compile(kotlinStdlib())
    compile("kotlin-reflect")
    compile(kotlinTest())
    compile(kotlinImmutable())
    compile("mverse-lang-common")
    compile(kotlinSerialization())
  }
}

dependencies {
  compile("org.jetbrains.kotlinx:kotlinx-collections-immutable:0.1")
}
