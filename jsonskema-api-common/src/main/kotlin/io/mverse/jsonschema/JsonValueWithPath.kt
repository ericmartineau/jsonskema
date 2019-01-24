package io.mverse.jsonschema

import io.mverse.jsonschema.enums.JsonSchemaType
import io.mverse.jsonschema.keyword.KeywordInfo
import io.mverse.jsonschema.utils.JsonUtils.extractIdFromObject
import io.mverse.jsonschema.utils.SchemaPaths
import io.mverse.jsonschema.utils.toJsonSchemaType
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import lang.hashKode
import lang.json.JsonPath
import lang.json.JsrArray
import lang.json.JsrNull
import lang.json.JsrObject
import lang.json.JsrType
import lang.json.JsrValue
import lang.json.get
import lang.json.jkey
import lang.json.propNames
import lang.json.propValues
import lang.json.properties
import lang.json.type
import lang.json.unboxAsAny
import lang.json.unboxOrNull
import lang.json.values
import lang.serializer.JsrValueSerializer
import kotlinx.serialization.json.JsonArray as KJsonArray
import lang.json.JsrObject as KJsonObject

/**
 * This class is used for convenience in accessing data within a JsrObject.
 *
 * It wraps a kotlin.serialization [JsrValue] and adds some extra methods that allow more fluent usage.
 */
data class JsonValueWithPath(
    val root: JsrValue,
    val wrapped: JsrValue,
    val location: SchemaLocation) : Map<String, JsrValue> {

  @Transient val jsonObject: JsrObject? get() = wrapped as? JsrObject
  @Transient val jsonArray: JsrArray? get() = wrapped as? JsrArray

  @Transient
  val rootObject
    get() = root as JsrObject

  @Transient
  val jsonSchemaType: JsonSchemaType
    get() = wrapped.type.toJsonSchemaType()

  @Transient
  val isBoolean: Boolean
    get() = wrapped.type.name == "BOOLEAN"

  @Transient val isNull: Boolean
    get() = wrapped.type.name == "NULL"

  @Transient val isNotNull: Boolean
    get() = wrapped.type.name != "NULL"

  @Transient val path: JsonPath
    get() = location.jsonPath

  override fun isEmpty(): Boolean {
    return if (jsonObject == null) true else jsonObject!!.properties.isEmpty()
  }

  override fun equals(other: Any?): Boolean = other is JsonValueWithPath &&
      (other.wrapped == wrapped && other.location == location)

  override fun hashCode(): Int = hashKode(wrapped, location)

  fun containsKey(keywordType: KeywordInfo<*>): Boolean {
    return containsKey(keywordType.key)
  }

  override val entries: Set<Map.Entry<String, JsrValue>>
    get() = jsonObject?.properties ?: emptySet()
  override val keys: Set<String>
    get() = jsonObject?.properties?.map { it.key }?.toSet() ?: emptySet()
  override val values: Collection<JsrValue>
    get() = jsonObject?.properties?.map { it.value } ?: emptySet()

  override fun containsKey(key: String): Boolean {
    return jsonObject?.propNames?.contains(key) == true
  }

  override fun containsValue(value: JsrValue): Boolean {
    return jsonObject?.propValues?.contains(value) == true
  }

  override operator fun get(key: String): JsrValue {
    return if (containsKey(key)) jsonObject!![key.jkey] else JsrNull
  }

  operator fun get(prop: KeywordInfo<*>): JsrValue {
    return this[prop.key]
  }

  fun path(keyword: KeywordInfo<*>): JsonValueWithPath {
    return path(keyword.key)
  }

  @Transient override val size: Int get() = jsonObject?.propNames?.size ?: 0
  @Transient val type: JsrType get() = wrapped.type
  @Transient val number: Number? get() = wrapped.unboxOrNull() as? Number
  @Transient
  val string: String?
    get() = if (wrapped == JsrNull) null else wrapped.unboxAsAny()?.toString()
  @Transient val boolean: Boolean? get() = wrapped.unboxAsAny() as? Boolean
  @Transient val int: Int? get() = wrapped.unboxAsAny() as? Int
  @Transient val double: Double? get() = wrapped.unboxOrNull() as? Double
  @Transient val arraySize: Int get() = (wrapped as JsrArray).values.size

  fun forEachIndex(action: (Int, JsonValueWithPath) -> Unit) {
    var i = 0
    (wrapped as JsrArray).values.forEach { v ->
      val idx = i++
      action(idx, JsonValueWithPath(root, v, location.child(idx)))
    }
  }

  fun forEachKey(action: (String, JsonValueWithPath) -> Unit) {
    (wrapped as JsrObject).properties.forEach { (k, v) ->
      action(k, fromJsonValue(root, v, location.child(k)))
    }
  }

  operator fun get(idx: Int): JsonValueWithPath {
    val json: JsrValue = (wrapped as JsrArray).values[idx]
    return JsonValueWithPath(root, json, location.child(idx))
  }

  fun path(childKey: String): JsonValueWithPath {
    return fromJsonValue(root, this[childKey], location.child(childKey))
  }

  /**
   * Returns JSON text for this JSON value.
   *
   *
   * @return JSON text
   */
  override fun toString(): String {
    return "${location.jsonPointerFragment} -> $wrapped"
  }

  fun numberOfProperties(): Int {
    return jsonObject?.propNames?.size ?: 0
  }

  fun propertyNames(): Set<String> {
    return jsonObject?.propNames ?: emptySet()
  }

  companion object {

    fun fromJsonValue(root: JsrValue, jsonObject: JsrValue, location: SchemaLocation): JsonValueWithPath {
      var locationVar = location
      if (jsonObject is JsrObject) {

        locationVar = extractIdFromObject(jsonObject, "\$id", "id")
            ?.let { locationVar.withId(it) }
            ?: locationVar
      }
      return JsonValueWithPath(root, jsonObject, locationVar)
    }

    fun fromJsonValue(jsonObject: JsrValue): JsonValueWithPath {

      if (jsonObject is JsrObject) {
        val schemaLocation = SchemaPaths.fromDocument(jsonObject, "\$id", "id")
        return JsonValueWithPath(jsonObject, jsonObject, schemaLocation)
      }

      return JsonValueWithPath(jsonObject, jsonObject, SchemaPaths.fromNonSchemaSource(jsonObject))
    }
  }
}
