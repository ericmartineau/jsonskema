package io.mverse.jsonschema

import io.mverse.jsonschema.loading.SchemaLoaderImpl
import io.mverse.jsonschema.loading.SchemaReader

fun JsonSchema.schemaReader(): SchemaReader {
  return SchemaLoaderImpl()
}


