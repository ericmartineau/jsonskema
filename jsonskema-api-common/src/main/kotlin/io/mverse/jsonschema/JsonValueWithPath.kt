package io.mverse.jsonschema

import io.mverse.jsonschema.enums.JsonSchemaType
import io.mverse.jsonschema.keyword.KeywordInfo
import io.mverse.jsonschema.utils.JsonUtils.extractIdFromObject
import io.mverse.jsonschema.utils.SchemaPaths
import io.mverse.jsonschema.utils.toJsonSchemaType
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.ElementType
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.int
import lang.convert
import lang.hashKode

/**
 * This class is used for convenience in accessing data within a JsonObject.
 *
 * It wraps the JSR353 [JsonObject] and adds some extra methods that allow more fluent usage.
 */
@Serializable
data class JsonValueWithPath(
    val root: JsonElement,
    val wrapped: JsonElement,
    val location: SchemaLocation) : Map<String, JsonElement> {

  val jsonObject by lazy {
    wrapped.jsonObject
  }

  val rootObject by lazy {
    root.jsonObject
  }

  val jsonSchemaType: JsonSchemaType
    get() = wrapped.type.toJsonSchemaType()

  val isBoolean: Boolean
    get() = wrapped.booleanOrNull ?: false

  val isNull: Boolean
    get() = wrapped.isNull

  val isNotNull: Boolean
    get() = !wrapped.isNull

  val path: JsonPath
    get() = location.jsonPath

  override fun isEmpty(): Boolean {
    return jsonObject.isEmpty()
  }

  override fun equals(other: Any?): Boolean = other is JsonValueWithPath &&
      (other.wrapped == wrapped && other.location == location)

  override fun hashCode(): Int = hashKode(wrapped, location)

  val jsonArray: kotlinx.serialization.json.JsonArray by lazy {
    wrapped.jsonArray
  }

  fun containsKey(keywordType: KeywordInfo<*>): Boolean {
    return containsKey(keywordType.key)
  }

  //  fun arraySize(): Int {
  //    verifyType(ARRAY)
  //    return wrapped.asJsonArray().size()
  //  }

  override val entries: Set<Map.Entry<String, JsonElement>> get() = jsonObject.entries
  override val keys: Set<String> get() = jsonObject.keys
  override val values: Collection<JsonElement> get() = jsonObject.values

  override fun containsKey(key: String): Boolean {
    return jsonObject.containsKey(key)
  }

  override fun containsValue(value: JsonElement): Boolean {
    return jsonObject.containsValue(value)
  }

  override operator fun get(key: String): JsonElement {
    return if (jsonObject.containsKey(key)) jsonObject[key] else JsonNull
  }

  operator fun get(prop: KeywordInfo<*>): JsonElement {
    return this[prop.key]
  }

  fun findObject(property: String): kotlinx.serialization.json.JsonObject? {
    return get(property) as? kotlinx.serialization.json.JsonObject
  }

  fun findPathAwareObject(keyword: KeywordInfo<*>): JsonValueWithPath? {
    return findPathAwareObject(keyword.key)
  }

  fun findPathAwareObject(childKey: String): JsonValueWithPath? {
    return findObject(childKey)?.convert { jsonObject -> fromJsonValue(root, jsonObject, location.child(childKey)) }
  }

  fun path(keyword: KeywordInfo<*>): JsonValueWithPath {
    return path(keyword.key)
  }

  override val size: Int get() = jsonObject.size
  val type: ElementType get() = wrapped.type
  val number: Number? get() = wrapped.primitive.doubleOrNull
  val string: String? get() = wrapped.primitive.contentOrNull
  val boolean: Boolean? get() = wrapped.primitive.booleanOrNull
  val int: Int? get() = wrapped.primitive.intOrNull
  val double: Double? get() = wrapped.primitive.doubleOrNull
  val arraySize: Int get() = wrapped.jsonArray.size

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

  //  override operator fun get(key: Any): JsonElement {
  //    return jsonObject[key]
  //  }

  operator fun get(idx: Int): JsonValueWithPath {
    val json: JsonElement = wrapped.jsonArray[idx]
    return JsonValueWithPath(root, json, location.child(idx))
  }

  fun getJsonArray(name: String): kotlinx.serialization.json.JsonArray = jsonObject[name].jsonArray

  fun getJsonObject(name: String): kotlinx.serialization.json.JsonObject = jsonObject[name].jsonObject

  fun getString(name: String): String? = jsonObject[name].contentOrNull

  fun getInt(name: String): Int {
    return jsonObject[name].int
  }

  fun getBoolean(name: String): Boolean {
    return jsonObject[name].boolean
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
    return jsonObject.keys.size
  }

  fun propertyNames(): Set<String> {
    return jsonObject.keys
  }

  //  fun values(): Collection<JsonElement> {
  //    return jsonObject.values()
  //  }

  //  fun entrySet(): Set<Entry<String, JsonElement>> {
  //    return jsonObject!!.entrySet()
  //  }

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
