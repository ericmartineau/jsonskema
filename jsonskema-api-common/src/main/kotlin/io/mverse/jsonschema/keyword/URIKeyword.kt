package io.mverse.jsonschema.keyword

import io.mverse.jsonschema.MergeReport
import io.mverse.jsonschema.mergeConflict
import lang.json.JsonPath
import lang.net.URI

open class URIKeyword(override val value: URI) : KeywordImpl<URI>() {
  override fun withValue(value: URI): Keyword<URI> {
    return URIKeyword(value = value)
  }

  override fun merge(path: JsonPath, keyword: KeywordInfo<*>, other: Keyword<URI>, report: MergeReport): Keyword<URI> {
    report += mergeConflict(path, keyword, this, other)
    return URIKeyword(value)
  }
}
