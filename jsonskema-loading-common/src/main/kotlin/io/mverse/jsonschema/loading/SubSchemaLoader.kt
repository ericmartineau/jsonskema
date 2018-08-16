package io.mverse.jsonschema.loading

import io.mverse.jsonschema.JsonValueWithPath
import io.mverse.jsonschema.Schema
import io.mverse.jsonschema.SchemaBuilder
import io.mverse.jsonschema.SchemaLocation
import io.mverse.jsonschema.builder.JsonSchemaBuilder
import io.mverse.jsonschema.enums.JsonSchemaVersion
import io.mverse.jsonschema.keyword.Keywords
import io.mverse.jsonschema.keyword.Keywords.REF
import io.mverse.jsonschema.utils.JsonUtils
import kotlinx.serialization.json.JsonObject
import lang.URI

/**
 * Schema factories are responsible for extracting values from a json-object to produce an immutable [Schema]
 * instance.  This also includes looking up any referenced schemas.
 */
data class SubSchemaLoader(val extraKeywordLoaders: List<KeywordDigester<*>>,
                           private val defaultVersions: Set<JsonSchemaVersion>?,
                           val strict: Boolean,
                           val schemaLoader: SchemaLoader) {


  val versions:Set<JsonSchemaVersion> = when {
    defaultVersions == null || defaultVersions.isEmpty() -> JsonSchemaVersion.publicVersions().toSet()
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
  fun subSchemaBuilder(schemaJson: JsonValueWithPath, rootDocument: kotlinx.serialization.json.JsonObject, loadingReport: LoadingReport): SchemaBuilder<*> {
    // #############################################
    // #####  $ref: Overrides everything    ########
    // #############################################

    if (schemaJson.containsKey(REF.key)) {
      //Ignore all other keywords when encountering a ref
      val ref = schemaJson[REF.key]
      return refSchemaBuilder(URI(ref.primitive.content), rootDocument, schemaJson.location)
    }

    val schemaBuilder = JsonUtils.extractIdFromObject(schemaJson.jsonObject)
        ?.let { schemaBuilder(schemaJson.location, it)}
        ?: schemaBuilder(schemaJson.location)
    schemaBuilder
        .withCurrentDocument(rootDocument)
        .withLoadingReport(loadingReport)

    allKeywordLoader.loadKeywordsForSchema(schemaJson, schemaBuilder, loadingReport)
    return schemaBuilder
  }

  internal fun schemaBuilder(location: SchemaLocation, `$id`: URI): SchemaBuilder<*> {
    return JsonSchemaBuilder(location, `$id`).withSchemaLoader(schemaLoader)
  }

  internal fun schemaBuilder(location: SchemaLocation): SchemaBuilder<*> {
    return JsonSchemaBuilder(location).withSchemaLoader(schemaLoader)
  }

  internal fun refSchemaBuilder(`$ref`: URI, currentDocument: kotlinx.serialization.json.JsonObject, location: SchemaLocation): SchemaBuilder<*> {
    return JsonSchemaBuilder(location)
        .withCurrentDocument(currentDocument)
        .withSchemaLoader(schemaLoader)
        .ref(`$ref`)
  }
}
