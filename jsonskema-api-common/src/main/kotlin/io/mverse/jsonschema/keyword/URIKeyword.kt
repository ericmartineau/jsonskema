package io.mverse.jsonschema.keyword

import lang.URI

open class URIKeyword(override val value: URI) : KeywordImpl<URI>() {
  override fun withValue(value: URI): Keyword<URI> {
    return URIKeyword(value = value)
  }
}