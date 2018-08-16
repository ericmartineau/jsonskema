package io.mverse.jsonschema

import io.mverse.jsonschema.keyword.JsonSchemaKeyword
import io.mverse.jsonschema.keyword.KeywordInfo
import kotlin.properties.ReadOnlyProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

abstract class KeywordContainer(open val keywords: Map<KeywordInfo<*>, JsonSchemaKeyword<*>> = emptyMap()) {

  inline fun <reified X : JsonSchemaKeyword<*>> keyword(keyword: KeywordInfo<X>): X? {
    return keywords[keyword] as X?
  }

  inline fun <reified T, reified K:JsonSchemaKeyword<T>> keywords(info:KeywordInfo<K>): ReadOnlyProperty<KeywordContainer, T?> {
    return object: ReadOnlyProperty<KeywordContainer, T?> {
      override fun getValue(thisRef: KeywordContainer, property: KProperty<*>): T? {
        val keyword = thisRef.keyword(info) ?: return null
        return keyword.value
      }
    }
  }

  inline fun <reified T, reified K:JsonSchemaKeyword<T>> keywords(info:KeywordInfo<K>, default:T): ReadOnlyProperty<KeywordContainer, T> {
    return object: ReadOnlyProperty<KeywordContainer, T> {
      override fun getValue(thisRef: KeywordContainer, property: KProperty<*>): T {
        val keyword = thisRef.keyword(info)
        return keyword?.value ?: default
      }
    }
  }

  inline fun <reified N:Number, reified K:JsonSchemaKeyword<Number>> numberKeyword(info:KeywordInfo<K>): ReadOnlyProperty<KeywordContainer, N?> {
    return object: ReadOnlyProperty<KeywordContainer, N?> {
      override fun getValue(thisRef: KeywordContainer, property: KProperty<*>): N? {
        val value = thisRef.keyword(info)?.value ?: return null

        return when(N::class) {
          Int::class-> value.toInt() as N
          Long::class-> value.toLong() as N
          Double::class-> value.toDouble() as N
          Short::class -> value.toShort() as N
          Number::class-> value as N
          else-> throw IllegalStateException("Can't convert $value to type ${N::class}")
        }
      }
    }
  }
}

abstract class MutableKeywordContainer(override val keywords: MutableMap<KeywordInfo<*>, JsonSchemaKeyword<*>> = mutableMapOf()):KeywordContainer() {
  inline fun <reified T, reified K:JsonSchemaKeyword<T>> mutableKeyword(info:KeywordInfo<K>,
                                                                        crossinline supplier: () -> K = { K::class.constructors.first().call() },
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
