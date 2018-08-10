package io.mverse.jsonschema.loading

import com.google.common.base.Preconditions
import io.mverse.jsonschema.Draft6Schema
import io.mverse.jsonschema.JsonSchema
import io.mverse.jsonschema.createSchemaReader
import io.mverse.jsonschema.resourceLoader
import io.mverse.jsonschema.schemaReader
import kotlinx.serialization.json.JsonObject

open class BaseLoaderTest(resourceURL: String) {

  protected val testsForType: JsonObject

  init {
    Preconditions.checkNotNull(resourceURL, "resourceURL must not be null")
    this.testsForType = JsonSchema.resourceLoader().readJsonObject("loading/$resourceURL")
  }

  protected fun getJsonObjectForKey(schemaName: String): JsonObject {
    val jsonObject = testsForType.getObject(schemaName)
    return jsonObject
  }

  protected fun getSchemaForKey(propertyKey: String): Draft6Schema {
    val jsonObjectForKey = getJsonObjectForKey(propertyKey)
    return JsonSchema.createSchemaReader().readSchema(jsonObjectForKey).asDraft6()
  }

  protected fun readResource(relativeURL: String): JsonObject {
    return JsonSchema.resourceLoader().readJsonObject(relativeURL)
  }
}
