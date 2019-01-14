package io.mverse.jsonschema.loading

import com.google.common.base.Preconditions
import io.mverse.jsonschema.Draft6Schema
import io.mverse.jsonschema.JsonSchema
import io.mverse.jsonschema.createSchemaReader
import io.mverse.jsonschema.resourceLoader
import io.mverse.jsonschema.createSchemaReader
import lang.json.JsrObject

open class BaseLoaderTest(resourceURL: String) {

  protected val testsForType: JsrObject

  init {
    Preconditions.checkNotNull(resourceURL, "resourceURL must not be null")
    this.testsForType = JsonSchema.resourceLoader().readJsonObject("loading/$resourceURL")
  }

  protected fun getJsonObjectForKey(schemaName: String): JsrObject {
    val jsonObject = testsForType[schemaName] as JsrObject
    return jsonObject
  }

  protected fun getSchemaForKey(propertyKey: String): Draft6Schema {
    val jsonObjectForKey = getJsonObjectForKey(propertyKey)
    return JsonSchema.createSchemaReader().readSchema(jsonObjectForKey).asDraft6()
  }

  protected fun readResource(relativeURL: String): JsrObject {
    return JsonSchema.resourceLoader().readJsonObject(relativeURL)
  }
}
