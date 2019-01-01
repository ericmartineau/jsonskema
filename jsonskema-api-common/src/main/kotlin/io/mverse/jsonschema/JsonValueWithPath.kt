package io.mverse.jsonschema

import io.mverse.jsonschema.enums.JsonSchemaType
import io.mverse.jsonschema.keyword.KeywordInfo
import io.mverse.jsonschema.utils.JsonUtils.extractIdFromObject
import io.mverse.jsonschema.utils.SchemaPaths
import io.mverse.jsonschema.utils.toJsonSchemaType
import kotlinx.serialization.Transient
import kotlinx.serialization.json.ElementType
import kotlinx.serialization.json.JsonArray as KJsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonObject as KJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.int
import lang.Serializable
import lang.convert
import lang.hashKode

/**
 * This class is used for convenience in accessing data within a JsonObject.
 *
 * It wraps a kotlin.serialization [JsonElement] and adds some extra methods that allow more fluent usage.
 */
@Serializable
data class JsonValueWithPath(
    val root: JsonElement,
    val wrapped: JsonElement,
    val location: SchemaLocation) : Map<String, JsonElement> {

  @Transient val jsonObject:KJsonObject? get() = if(wrapped is JsonObject) wrapped else null
  @Transient val jsonArray: KJsonArray? get() = if(wrapped is KJsonArray) wrapped else null

  @Transient
  val rootObject get() = root.jsonObject

  @Transient
  val jsonSchemaType: JsonSchemaType
    get() = wrapped.type.toJsonSchemaType()

  @Transient
  val isBoolean: Boolean
    get() = wrapped.booleanOrNull ?: false

  @Transient val isNull: Boolean
    get() = wrapped.isNull

  @Transient val isNotNull: Boolean
    get() = !wrapped.isNull

  @Transient val path: JsonPath
    get() = location.jsonPath

  override fun isEmpty(): Boolean {
    return if(jsonObject==null) true else jsonObject!!.isEmpty()
  }

  override fun equals(other: Any?): Boolean = other is JsonValueWithPath &&
      (other.wrapped == wrapped && other.location == location)

  override fun hashCode(): Int = hashKode(wrapped, location)

  fun containsKey(keywordType: KeywordInfo<*>): Boolean {
    return containsKey(keywordType.key)
  }

  override val entries: Set<Map.Entry<String, JsonElement>> get() = jsonObject?.entries ?: emptySet()
  override val keys: Set<String> get() = jsonObject?.keys ?: emptySet()
  override val values: Collection<JsonElement> get() = jsonObject?.values ?: emptySet()

  override fun containsKey(key: String): Boolean {
    return jsonObject?.containsKey(key)==true
  }

  override fun containsValue(value: JsonElement): Boolean {
    return jsonObject?.containsValue(value)==true
  }

  override operator fun get(key: String): JsonElement {
    return if (containsKey(key)) jsonObject!![key] else JsonNull
  }

  operator fun get(prop: KeywordInfo<*>): JsonElement {
    return this[prop.key]
  }

  fun path(keyword: KeywordInfo<*>): JsonValueWithPath {
    return path(keyword.key)
  }

  @Transient override val size: Int get() = jsonObject?.size ?: 0
  @Transient val type: ElementType get() = wrapped.type
  @Transient val number: Number? get() = wrapped.primitive.doubleOrNull
  @Transient val string: String? get() = wrapped.primitive.contentOrNull
  @Transient val boolean: Boolean? get() = wrapped.primitive.booleanOrNull
  @Transient val int: Int? get() = wrapped.primitive.intOrNull
  @Transient val double: Double? get() = wrapped.primitive.doubleOrNull
  @Transient val arraySize: Int get() = wrapped.jsonArray.size

  fun forEachIndex(action: (Int, JsonValueWithPath) -> Unit) {
    var i = 0
    wrapped.jsonArray.forEach { v ->
      val idx = i++
      action(idx, JsonValueWithPath(root, v, location.child(idx)))
    }
  }

  fun forEachKey(action: (String, JsonValueWithPath) -> Unit) {
    wrapped.jsonObject.forEach { (k, v) ->
      action(k, fromJsonValue(root, v, location.child(k)))
    }
  }

  operator fun get(idx: Int): JsonValueWithPath {
    val json: JsonElement = wrapped.jsonArray[idx]
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
    return jsonObject?.keys?.size ?: 0
  }

  fun propertyNames(): Set<String> {
    return jsonObject?.keys ?: emptySet()
  }

  companion object {

    fun fromJsonValue(root: JsonElement, jsonObject: JsonElement, location: SchemaLocation): JsonValueWithPath {
      var locationVar = location
      if (jsonObject is JsonObject) {
        val asJsonObject = jsonObject.jsonObject
        locationVar = extractIdFromObject(asJsonObject, "\$id", "id")
            .convert { locationVar.withId(it) }
            ?: locationVar
      }
      return JsonValueWithPath(root, jsonObject, locationVar)
    }

    fun fromJsonValue(jsonObject: JsonElement): JsonValueWithPath {

      if (jsonObject is kotlinx.serialization.json.JsonObject) {
        val schemaLocation = SchemaPaths.fromDocument(jsonObject, "\$id", "id")
        return JsonValueWithPath(jsonObject.jsonObject, jsonObject, schemaLocation)
      }

      return JsonValueWithPath(jsonObject, jsonObject, SchemaPaths.fromNonSchemaSource(jsonObject))
    }
  }
}
