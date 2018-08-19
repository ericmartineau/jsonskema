mverse  {
  dependencies {
    compile(kotlinStdlib())
    compile("kotlin-reflect")
    compile(kotlinTest())
    compile(kotlinSerialization())
    compile(kotlinImmutable())
  }
}

dependencies {
  compile("org.jetbrains.kotlinx:kotlinx-collections-immutable:0.1")
}

dependencies {
  compile(project(":jsonskema-util-common"))
  // EqualsVerifier can be used in JUnit 4 unit tests to verify whether the contract for the equals and hashCode methods is met.
}
