plugins {
  kotlin("jvm")
  id("kotlinx-serialization")
}

mverse  {
  dependencies {
    compile("kotlin-reflect")
    compile(kotlinIO())
    compile("kotlinx-serialization-runtime")
    compile("mverse-json")
    compile("mverse-lang-jvm")
    compile("mverse-log-jvm")
    compile("mverse-coroutines-jvm")

    compile("kotlinx-io-jvm")
    compile("kotlinx-coroutines-io-jvm")

    testCompile("mverse-test-jvm")
    // Default provider for JSR 353:Java API for Processing JSON
    testCompile("org.glassfish:javax.json:1.1.4")
    testCompile("logback-classic")
    testCompile("logback-core")
  }
}

dependencies {
  // Default provider for JSR 353:Java API for Processing JSON
//  compile("org.glassfish:javax.json:1.1.2")


  compile("io.mverse:hashkode:1.0.1")

  // Guava is a suite of core and expanded libraries that include
  //utility classes, google's collections, io classes, and much
  //much more.
  compile("com.google.guava:guava:26.0-jre")

  // Commons Validator provides the building blocks for both client side validation and server side data validation.
  // It may be used standalone or with a framework like Struts.
  compile("commons-validator:commons-validator:1.6")
  compile("com.googlecode.libphonenumber:libphonenumber:8.9.0")
  compile("com.damnhandy:handy-uri-templates:2.1.6")

  compile("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

  expectedBy(project(":jsonskema-api-common"))
  expectedBy(project(":jsonskema-core-common"))
  expectedBy(project(":jsonskema-loading-common"))
  expectedBy(project(":jsonskema-validation-common"))

  testCompile("junit:junit:4.12")

  compile("org.assertj:assertj-core:3.10.0")

  // The core jetty server artifact.
  compile("org.eclipse.jetty:jetty-server:9.4.12.RC1")

  // Jetty Servlet Container
  compile("org.eclipse.jetty:jetty-servlet:9.4.12.RC1")

  testCompile("nl.jqno.equalsverifier:equalsverifier:2.5.1")
  testCompile("org.jetbrains.kotlin:kotlin-test")
  testCompile("org.jetbrains.kotlin:kotlin-test-junit")
  testCompile ("com.willowtreeapps.assertk:assertk-jvm:0.11")

  testCompile("pl.pragmatists:JUnitParams:1.1.1")


  // Reflections - a Java runtime metadata analysis
  compile("org.reflections:reflections:0.9.11")
  // All versions of Mockito library that were automatically synced to JCenter and Maven Central. Starting from April 2017 "mockito" Bintray package only contains notable versions of Mockito. For more information see the details of Mockito Continuous Delivery Pipeline 2.0 (https://github.com/mockito/mockito/issues/911)
  testCompile("org.mockito:mockito-all:1.+")
}

