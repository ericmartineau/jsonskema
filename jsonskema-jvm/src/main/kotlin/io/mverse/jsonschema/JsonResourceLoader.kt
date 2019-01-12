package io.mverse.jsonschema

import io.mverse.jsonschema.loading.parseKtJson
import io.mverse.jsonschema.loading.parseKtObject
import kotlinx.io.InputStream
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlin.reflect.KClass


fun JsonSchema.resourceLoader():JsonResourceLoader = JsonResourceLoader(JsonResourceLoader::class)
fun JsonSchema.resourceLoader(klass:KClass<*>):JsonResourceLoader = JsonResourceLoader(klass)

class JsonResourceLoader(private val loadFrom: KClass<*>) {

  fun getStream(relPath: String): InputStream {
    return loadFrom.java.getResourceAsStream(relPath)
  }

  fun readJson(relPath: String): JsonElement {
    return loadFrom.java.getResourceAsStream(relPath).bufferedReader().readText().parseKtJson()
  }

  fun readJsonObject(relPath: String): JsonObject {
    return loadFrom.java.getResourceAsStream(relPath).bufferedReader().readText().parseKtObject()
  }
}
