package io.mverse.jsonschema

import io.mverse.jsonschema.loading.readFully
import io.mverse.jsonschema.loading.parseJson
import io.mverse.jsonschema.loading.parseJsonObject
import kotlinx.io.InputStream
import kotlinx.serialization.json.JsonElement
import kotlin.reflect.KClass


fun JsonSchema.resourceLoader():JsonResourceLoader = JsonResourceLoader(JsonResourceLoader::class)
fun JsonSchema.resourceLoader(klass:KClass<*>):JsonResourceLoader = JsonResourceLoader(klass)

class JsonResourceLoader(private val loadFrom: KClass<*>) {

  fun getStream(relPath: String): InputStream {
    return loadFrom.java.getResourceAsStream(relPath)
  }

  fun readJson(relPath: String): JsonElement {
    return loadFrom.java.getResourceAsStream(relPath).readFully().parseJson()
  }

  fun readJsonObject(relPath: String): kotlinx.serialization.json.JsonObject {
    return loadFrom.java.getResourceAsStream(relPath).readFully().parseJsonObject()
  }
}
