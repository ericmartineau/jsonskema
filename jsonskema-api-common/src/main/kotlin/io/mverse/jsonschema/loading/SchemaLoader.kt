package io.mverse.jsonschema.loading

import io.mverse.jsonschema.JsonValueWithPath
import io.mverse.jsonschema.Schema
import io.mverse.jsonschema.SchemaBuilder
import lang.json.JsrObject
import lang.net.URI

/**
 * If you're trying to load a schema from an input source, you want [SchemaReader].
 *
 * This interface is used within the internals of the library.  Instead of working on raw [JsrObject]
 * instances, it works on [JsonValueWithPath] which means that the source document or fragment is already
 * being traversed.
 */
interface SchemaLoader {

  /**
   * Loads all the values of a schema into a builder.  This builder can then be modified before calling build()
   *
   * @param forSchema     The source json, path-aware
   * @param loadingReport A place to log loading validation
   * @return A schema builder instance.
   */
  fun schemaBuilder(forSchema: JsonValueWithPath, loadingReport: LoadingReport): SchemaBuilder {
    return subSchemaBuilder(forSchema, forSchema.jsonObject!!, loadingReport)
  }

  /**
   * Loads a $ref schema
   * @param referencedFrom The schema where the $ref resides
   * @param refURI The URI to the $ref
   * @param currentDocument The current document being processed (from which `#referencedFrom` was loaded)
   * @param report A place to log loading validation
   * @return A loaded ref schema
   */
  fun loadRefSchema(referencedFrom: Schema, refURI: URI, currentDocument: JsrObject?, report: LoadingReport): Schema

  /**
   * Loads a subschema within a document
   * @param schemaJson The path-aware json object representing the subschema
   * @param inDocument The document the schema resides in
   * @param loadingReport A place to log validation
   * @return The loaded schema
   */
  fun loadSubSchema(schemaJson: JsonValueWithPath, inDocument: JsrObject, loadingReport: LoadingReport): Schema

  /**
   * Loads all the values of a subschema into a builder.  This builder can then be modified before calling build()
   * @param schemaJson The source json, path-aware
   * @param inDocument The document containing hte subschema
   * @param loadingReport A place to log loading validation
   * @return A schema builder instance.
   */
  fun subSchemaBuilder(schemaJson: JsonValueWithPath, inDocument: JsrObject, loadingReport: LoadingReport): SchemaBuilder

  /**
   * Looks for a schema that's already been loaded by this loader.
   *
   * @param schemaLocation The absolute URI for the schema
   * @return A schema, if one has been loaded
   */
  fun findLoadedSchema(schemaLocation: URI): Schema?

  /**
   * Registers a schema that's been loaded
   *
   * @param schema The schema to register
   */
  fun withPreloadedSchema(schema: Schema): SchemaLoader

  /**
   * Returns a new immutable builder with the provided document client.
   * @param client The new [JsonDocumentClient] to use for loading schemas
   * @return A new copy of this loader.
   */
  fun withDocumentClient(client: JsonDocumentClient): SchemaLoader

  operator fun plusAssign(preloadedSchema: JsrObject)
}
