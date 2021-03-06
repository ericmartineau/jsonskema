package io.mverse.jsonschema

import io.mverse.jsonschema.loading.parseJsrJson
import io.mverse.jsonschema.loading.parseJsrObject
import kotlinx.io.InputStream
import lang.json.JsrValue
import lang.json.JsrObject
import kotlin.reflect.KClass


fun JsonSchema.resourceLoader():JsonResourceLoader = JsonResourceLoader(JsonResourceLoader::class)
fun JsonSchema.resourceLoader(klass:KClass<*>):JsonResourceLoader = JsonResourceLoader(klass)

class JsonResourceLoader(private val loadFrom: KClass<*>) {

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
