package io.mverse.jsonschema

import io.mverse.jsonschema.keyword.Keyword
import io.mverse.jsonschema.keyword.KeywordInfo
import lang.newInstance
import kotlin.properties.ReadOnlyProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

abstract class KeywordContainer(open val keywords: Map<KeywordInfo<*>, Keyword<*>> = emptyMap()) {
  inline fun <reified X : Keyword<*>> keyword(keyword: KeywordInfo<X>): X? {
    return keywords[keyword] as X?
  }

  protected inner class Values {
    operator fun <X, K:Keyword<X>> get(keyword: KeywordInfo<K>):X? {
      return this@KeywordContainer[keyword]?.value
    }
  }

  operator fun <X, K:Keyword<X>> get(keyword: KeywordInfo<K>):K? {
    @Suppress("UNCHECKED_CAST")
    return keywords[keyword] as K?
  }

  protected val values = Values()

  inline fun <reified T, reified K:Keyword<T>> keywords(info:KeywordInfo<K>): ReadOnlyProperty<KeywordContainer, T?> {
    return object: ReadOnlyProperty<KeywordContainer, T?> {
      override fun getValue(thisRef: KeywordContainer, property: KProperty<*>): T? {
        val keyword = thisRef.keyword(info) ?: return null
        return keyword.value
      }
    }
  }

  inline fun <reified T, reified K:Keyword<T>> keywords(info:KeywordInfo<K>, default:T): ReadOnlyProperty<KeywordContainer, T> {
    return object: ReadOnlyProperty<KeywordContainer, T> {
      override fun getValue(thisRef: KeywordContainer, property: KProperty<*>): T {
        val keyword = thisRef.keyword(info)
        return keyword?.value ?: default
      }
    }
  }
}

abstract class MutableKeywordContainer(override val keywords: MutableMap<KeywordInfo<*>, Keyword<*>> = mutableMapOf()):KeywordContainer() {
  inline fun <reified T, reified K:Keyword<T>> mutableKeyword(info:KeywordInfo<K>,
                                                              crossinline supplier: () -> K = {this::class.newInstance() as K},
                                                              crossinline updater: K.(T) -> K = { this.withValue(it) as K })
      : ReadWriteProperty<MutableKeywordContainer, T?> {
    return object: ReadWriteProperty<MutableKeywordContainer, T?> {
      override fun setValue(thisRef: MutableKeywordContainer, property: KProperty<*>, value: T?) {
        if (value == null) {
          thisRef.keywords.remove(info)
        } else {
          val jsonSchemaKeyword = (thisRef.keywords[info] ?: supplier()) as K
          keywords[info] = jsonSchemaKeyword.updater(value)
        }
      }

      override fun getValue(thisRef: MutableKeywordContainer, property: KProperty<*>): T? {
        val keyword = thisRef.keyword(info) ?: return null
        return keyword.value
      }
    }
  }
}
