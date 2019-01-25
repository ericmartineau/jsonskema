package io.mverse.jsonschema.loading

import com.google.common.base.Preconditions
import io.mverse.jsonschema.Draft6Schema
import io.mverse.jsonschema.JsonSchema
import io.mverse.jsonschema.createSchemaReader
import io.mverse.jsonschema.resourceLoader
import io.mverse.jsonschema.createSchemaReader
import io.mverse.logging.mlogger
import lang.json.JsrObject
import lang.net.URI
import lang.net.toURI
import lang.net.withNewFragment
import java.io.File

open class BaseLoaderTest(resourceURL: String) {

  protected val testsForType: JsrObject
  val resourceLoader = JsonSchema.resourceLoader()
  val filePath = File("src/test/resources" + resourceLoader.getPath("loading/$resourceURL")).toURI()

  init {
    this.testsForType = resourceLoader.readJsonObject("loading/$resourceURL")
  }

  protected fun getJsonObjectForKey(schemaName: String): JsrObject {
    val jsonObject = testsForType[schemaName] as JsrObject
    return jsonObject
  }

  protected fun path(key:String):URI {
    return filePath.withNewFragment(URI("#/$key"))
  }

  protected fun getSchemaForKey(propertyKey: String): Draft6Schema {
    log.info {"Loading schema from ${path(propertyKey)}"}
    val jsonObjectForKey = getJsonObjectForKey(propertyKey)
    return JsonSchema.createSchemaReader().readSchema(jsonObjectForKey).asDraft6()
  }

  protected fun readResource(relativeURL: String): JsrObject {
    return JsonSchema.resourceLoader().readJsonObject(relativeURL)
  }

  companion object {
    val log = mlogger{}
  }
}
