package io.mverse.jsonschema

import io.mverse.jsonschema.keyword.Keyword
import io.mverse.jsonschema.keyword.KeywordInfo
import lang.reflect.newInstance
import kotlin.properties.ReadOnlyProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

interface KeywordContainer {
  val keywords: Map<KeywordInfo<*>, Keyword<*>>
  val values get() = Values(this)

  class Values(val container: KeywordContainer) {
    operator fun <X, K : Keyword<X>> get(keyword: KeywordInfo<K>): X? {
      return container[keyword]?.value
    }
  }

  operator fun <X, K : Keyword<X>> get(keyword: KeywordInfo<K>): K? {
    @Suppress("UNCHECKED_CAST")
    return keywords[keyword] as K?
  }
}

interface MutableKeywordContainer : KeywordContainer {
  operator fun <K : Keyword<*>> set(key: KeywordInfo<K>, value: K?)
  operator fun minusAssign(key: KeywordInfo<*>)
}

inline fun <reified X : Keyword<*>> KeywordContainer.keyword(keyword: KeywordInfo<X>): X? {
  return keywords[keyword] as X?
}

