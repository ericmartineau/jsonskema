package io.mverse.jsonschema.keyword

import lang.URI

open class URIKeyword(override val value: URI) : JsonSchemaKeywordImpl<URI>() {
  override fun withValue(value: URI): JsonSchemaKeyword<URI> {
    return URIKeyword(value = value)
  }
}
