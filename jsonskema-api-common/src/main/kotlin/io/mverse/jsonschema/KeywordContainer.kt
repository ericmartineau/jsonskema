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

inline fun <reified T, reified K : Keyword<T>> MutableKeywordContainer.mutableKeyword(
    info: KeywordInfo<K>,
    crossinline supplier: () -> K = { this::class.newInstance() as K },
    crossinline updater: K.(T) -> K = { this.withValue(it) as K })
    : ReadWriteProperty<MutableKeywordContainer, T?> {

  return object : ReadWriteProperty<MutableKeywordContainer, T?> {
    override fun setValue(thisRef: MutableKeywordContainer, property: KProperty<*>, value: T?) {
      if (value == null) {
        thisRef -= info
      } else {
        val jsonSchemaKeyword = (thisRef.keywords[info] ?: supplier()) as K
        thisRef[info] = jsonSchemaKeyword.updater(value)
      }
    }

    override fun getValue(thisRef: MutableKeywordContainer, property: KProperty<*>): T? {
      val keyword = thisRef.keyword(info) ?: return null
      return keyword.value
    }
  }
}

inline fun <reified X : Keyword<*>> KeywordContainer.keyword(keyword: KeywordInfo<X>): X? {
  return keywords[keyword] as X?
}

inline fun <reified T, reified K : Keyword<T>> KeywordContainer.keywords(info: KeywordInfo<K>): ReadOnlyProperty<KeywordContainer, T?> {
  return object : ReadOnlyProperty<KeywordContainer, T?> {
    override fun getValue(thisRef: KeywordContainer, property: KProperty<*>): T? {
      val keyword = thisRef.keyword(info) ?: return null
      return keyword.value
    }
  }
}

inline fun <reified T, reified K : Keyword<T>> KeywordContainer.keywords(info: KeywordInfo<K>, default: T): ReadOnlyProperty<KeywordContainer, T> {
  return object : ReadOnlyProperty<KeywordContainer, T> {
    override fun getValue(thisRef: KeywordContainer, property: KProperty<*>): T {
      val keyword = thisRef.keyword(info)
      return keyword?.value ?: default
    }
  }
}

