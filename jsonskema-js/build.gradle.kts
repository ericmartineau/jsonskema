import com.moowork.gradle.node.npm.NpmTask
import com.moowork.gradle.node.task.NodeTask
import org.jetbrains.kotlin.gradle.tasks.Kotlin2JsCompile

plugins {
  // Gradle plugin for executing node scripts (supports NPM, Yarn, Grunt and Gulp).
  id("com.moowork.node") version "1.2.0"
}

//plugins.apply("kotlin-dce-js")

repositories {
  jcenter()
}

mverse  {
  isDefaultDependencies = false

  dependencies {
    compile("kotlin-reflect")
    compile(kotlinStdlib())
    compile(kotlinSerialization())
    compile(kotlinImmutable())
//    compile("kotlinx-io-js")
  }
}

dependencies {
  compile("com.willowtreeapps.assertk:assertk-js:0.12")
  compile("org.jetbrains.kotlinx:kotlinx-serialization-runtime-js:0.60.1")
  compile("org.jetbrains.kotlin:kotlin-stdlib-js")
  expectedBy(project(":jsonskema-util-common"))
  expectedBy(project(":jsonskema-api-common"))
  expectedBy(project(":jsonskema-core-common"))
  expectedBy(project(":jsonskema-loading-common"))
  expectedBy(project(":jsonskema-validation-common"))
  testCompile("org.jetbrains.kotlin:kotlin-test")
  testCompile("org.jetbrains.kotlin:kotlin-test-js")

  // None
  testCompile("org.jetbrains.kotlin:kotlin-script-runtime:1.2.61")
}

node {
  download = true
}


afterEvaluate {
  val compileKotlin2Js by tasks.getting(Kotlin2JsCompile::class)
  val compileTestKotlin2Js by tasks.getting(Kotlin2JsCompile::class)

  compileKotlin2Js.kotlinOptions.moduleKind = "commonjs"
  compileTestKotlin2Js.kotlinOptions.moduleKind = "commonjs"

  val populateNodeModules by tasks.creating(Copy::class) {
    group = "verification"
    dependsOn("compileKotlin2Js")
    from(compileKotlin2Js.destinationDir)
    configurations["testCompile"].forEach {
      from(zipTree(it.absolutePath).matching { include("*.js") })
    }
    into("$buildDir/node_modules")
  }

  val installMocha by tasks.creating(NpmTask::class) {
    //  group = "verification"
    setArgs(listOf("install", "mocha"))
  }

  val runMocha by tasks.creating(NodeTask::class) {
    //  group = "verification"
    dependsOn(compileTestKotlin2Js, populateNodeModules, installMocha)
    setScript(file("node_modules/mocha/bin/mocha"))
    setArgs(listOf(compileTestKotlin2Js.outputFile))
  }

  tasks["test"].dependsOn(runMocha)

}
