package io.mverse.jsonschema.loading

import io.mverse.jsonschema.JsonValueWithPath
import io.mverse.jsonschema.Schema
import io.mverse.jsonschema.SchemaLocation
import io.mverse.jsonschema.builder.MutableJsonSchema
import io.mverse.jsonschema.builder.MutableSchema
import io.mverse.jsonschema.enums.JsonSchemaVersion
import io.mverse.jsonschema.keyword.Keywords.REF
import io.mverse.jsonschema.utils.JsonUtils
import lang.json.JsrObject
import lang.json.unbox
import lang.net.URI

/**
 * Schema factories are responsible for extracting values from a json-object to produce an immutable [Schema]
 * instance.  This also includes looking up any referenced schemas.
 */
data class SubSchemaLoader(val extraKeywordLoaders: List<KeywordDigester<*>>,
                           private val defaultVersions: Set<JsonSchemaVersion>?,
                           val strict: Boolean,
                           val schemaLoader: SchemaLoader) {

  val versions: Set<JsonSchemaVersion> = when {
    defaultVersions == null || defaultVersions.isEmpty() -> JsonSchemaVersion.publicVersions.toSet()
    else -> defaultVersions
  }

  private val allKeywordLoader: AllKeywordLoader = AllKeywordLoader(
      allExtractors = KeywordDigesters.defaultKeywordLoaders() + extraKeywordLoaders,
      defaultVersions = versions,
      strict = strict,
      schemaLoader = schemaLoader)

  /**
   * Extracts all keywords from a json document, reports any issues to the provided LoadingReport.
   *
   * @param schemaJson    The subschema to load.
   * @param rootDocument  The document the subschema came from
   * @param loadingReport The report to write validation into
   * @return A builder loaded up with all the keywords.
   */
  fun subSchemaBuilder(schemaJson: JsonValueWithPath, rootDocument: JsrObject, loadingReport: LoadingReport): MutableSchema {
    // #############################################
    // #####  $ref: Overrides everything    ########
    // #############################################

    if (schemaJson.containsKey(REF.key)) {
      //Ignore all other keywords when encountering a ref
      val ref = schemaJson[REF.key]
      return refSchemaBuilder(URI(ref.unbox()), rootDocument, schemaJson.location)
    }

    val schemaBuilder = JsonUtils.extractIdFromObject(schemaJson.jsonObject!!)
        ?.let { schemaBuilder(schemaJson.location, it) }
        ?: schemaBuilder(schemaJson.location)
    schemaBuilder.also {
      it.currentDocument = rootDocument
      it.loadingReport = loadingReport
    }

    allKeywordLoader.loadKeywordsForSchema(schemaJson, schemaBuilder, loadingReport)
    return schemaBuilder
  }

  internal fun schemaBuilder(location: SchemaLocation, `$id`: URI): MutableSchema {
    val loader = this.schemaLoader
    return MutableJsonSchema(location, `$id`).also { it.schemaLoader = loader }
  }

  internal fun schemaBuilder(location: SchemaLocation): MutableSchema {
    return MutableJsonSchema(location).also { it.schemaLoader = this.schemaLoader }
  }

  internal fun refSchemaBuilder(ref: URI, currentDocument: JsrObject, location: SchemaLocation): MutableSchema {
    return MutableJsonSchema(location).also {
      it.currentDocument = currentDocument
      it.schemaLoader = this.schemaLoader
      it.ref = ref
    }
  }
}
