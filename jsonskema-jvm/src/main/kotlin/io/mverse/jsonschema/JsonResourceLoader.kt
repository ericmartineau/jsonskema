package io.mverse.jsonschema

import io.mverse.jsonschema.loading.parseJsrJson
import io.mverse.jsonschema.loading.parseJsrObject
import kotlinx.io.InputStream
import lang.json.JsrValue
import lang.json.JsrObject
import kotlin.reflect.KClass


fun JsonSchemas.resourceLoader():JsonResourceLoader = JsonResourceLoader(JsonResourceLoader::class)
fun JsonSchemas.resourceLoader(klass:KClass<*>):JsonResourceLoader = JsonResourceLoader(klass)

class JsonResourceLoader(private val loadFrom: KClass<*>) {

  fun getPath(rel: String):String {
    val testPackage = loadFrom.java.`package`
    return testPackage.name.replace('.', '/') + "/$rel"
  }
  fun getStream(relPath: String): InputStream {
    return loadFrom.java.getResourceAsStream(relPath)
  }

  fun readJson(relPath: String): JsrValue {
    return loadFrom.java.getResourceAsStream(relPath).bufferedReader().readText().parseJsrJson()
  }

  fun readJsonObject(relPath: String): JsrObject {
    return loadFrom.java.getResourceAsStream(relPath).bufferedReader().readText().parseJsrObject()
  }
}
