package io.mverse.jsonschema.keyword

import io.mverse.jsonschema.Schema

/**
 * A keyword that contains subschemas.  Provides an easier way to collect subschemas for processing
 */
interface SubschemaKeyword {
  val subschemas: List<Schema>
}
