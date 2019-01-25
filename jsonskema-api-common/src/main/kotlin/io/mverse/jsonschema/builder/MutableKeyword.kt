package io.mverse.jsonschema.builder

import io.mverse.jsonschema.keyword.Keyword

interface MutableKeyword<K : Keyword<K>> : Keyword<K> {
  fun freeze(): K
}