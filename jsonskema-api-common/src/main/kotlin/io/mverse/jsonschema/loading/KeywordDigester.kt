package io.mverse.jsonschema.loading

import io.mverse.jsonschema.JsonValueWithPath
import io.mverse.jsonschema.SchemaBuilder
import io.mverse.jsonschema.keyword.Keyword
import io.mverse.jsonschema.keyword.KeywordInfo

/**
 * Responsible for extracting a keyword or keywords from a json document and loading them into a [SchemaBuilder]
 *
 * These loaders specify the keywords they process via [.getIncludedKeywords], which allows for flexible or
 * strict loading
 */
interface KeywordDigester<K : Keyword<*>> {

  /**
   * This list of keywords allows the processing system to handle most of the validation around "flexible" schema
   * parsing - knowing when to raise certain schema incompatibilities as validation or warnings.
   * @return
   */
  val includedKeywords: List<KeywordInfo<K>>

  /**
   * Extracts the specified keyword(s) from the source json document.  This method can either a) return the keywords
   * as a digest object (return value), or load them directly into the provided builder.
   *
   * It's recommended to return the values as a digest.
   *
   * @param jsonObject The source document to retrieve keyword for
   * @param builder The target schema builder
   * @param schemaLoader A loader that can be used to load subschemas or ref schemas
   * @param report A loading report object that stores any validation
   * @return Optionally, a keyword digest that contains the results of the processing.
   */
  fun extractKeyword(jsonObject: JsonValueWithPath,
                     builder: SchemaBuilder,
                     schemaLoader: SchemaLoader,
                     report: LoadingReport): KeywordDigest<K>?
}
