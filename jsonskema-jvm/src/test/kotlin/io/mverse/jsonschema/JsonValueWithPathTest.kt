package io.mverse.jsonschema

import assertk.assert
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import io.mverse.jsonschema.JsonValueWithPath.Companion.fromJsonValue
import io.mverse.jsonschema.TestUtils.createJsonArrayWithLocation
import io.mverse.jsonschema.TestUtils.createJsonNumberWithLocation
import io.mverse.jsonschema.TestUtils.createJsonObjectWithLocation
import io.mverse.jsonschema.TestUtils.createJsonStringWithLocation
import io.mverse.jsonschema.TestUtils.createValue
import io.mverse.jsonschema.enums.JsonSchemaType
import kotlinx.serialization.json.ElementType.NULL
import kotlinx.serialization.json.ElementType.OBJECT
import kotlinx.serialization.json.JsonElementTypeMismatchException
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.json
import lang.json.toJsonLiteral
import nl.jqno.equalsverifier.EqualsVerifier
import nl.jqno.equalsverifier.Warning
import kotlin.test.Test

class JsonValueWithPathTest {

  @Test
  fun testEquals() {
    EqualsVerifier.forClass(JsonValueWithPath::class.java)
        .suppress(Warning.STRICT_INHERITANCE)
        .withOnlyTheseFields("wrapped", "location")
        .verify()
  }

  @Test
  fun testValueType() {
    val jsonValue = json {
      "foo" to "bar"
      "num" to 3
    }
    val value = fromJsonValue(jsonValue)
    assert(value.type).isEqualTo(OBJECT)
  }

  @Test
  fun testGetValueType_WhenNull_ThenNULL() {
    val value = JsonNull
    assert(value.type).isEqualTo(NULL)
  }

  @Test
  fun testAsJsonObject_HasValue() {
    val value = createJsonObjectWithLocation()
    val jsonObject = value.jsonObject
    assert(jsonObject["foo"].primitive.contentOrNull).isEqualTo("bar")
  }

  @Test
  fun testAsJsonArray_HasValue() {
    val value = createJsonArrayWithLocation()
    val jsonArray = value.jsonArray
    val expected = "foo".toJsonLiteral()
    assert(jsonArray[0]).isEqualTo(expected)
  }

  @Test
  fun testGetJsonSchemaType_WhenObject_ReturnObject() {
    val value = createJsonObjectWithLocation()
    assert(value.jsonSchemaType).isEqualTo(JsonSchemaType.OBJECT)
  }

  @Test
  fun testGetJsonSchemaType_WhenArray_ReturnArray() {
    val value = createJsonArrayWithLocation()
    assert(value.jsonSchemaType).isEqualTo(JsonSchemaType.ARRAY)
  }

  @Test
  fun testArraySize_WhenArray_ReturnsSize() {
    val value = createJsonArrayWithLocation()
    assert(value.jsonArray.size).isEqualTo(5)
  }

  @Test
  fun testArraySize_WhenNotArray_ReturnsSize() {
    assert {
      createJsonObjectWithLocation().jsonArray
    }.thrownError {
      isInstanceOf(JsonElementTypeMismatchException::class)
    }
  }

  @Test
  fun testAsJsonNumber_WhenNumber_ReturnsJsonNumber() {
    assert(createJsonNumberWithLocation(34.4).number).isNotNull()
  }

  @Test(expected = JsonElementTypeMismatchException::class)
  fun testAsJsonNumber_WhenNotNumber_ThrowsUVE() {
    createJsonObjectWithLocation().number
  }

  @Test
  fun testAsJsonObject_WhenObject_ReturnsJsonObject() {
    assert(createJsonObjectWithLocation().jsonObject).isNotNull()
  }

  @Test(expected = JsonElementTypeMismatchException::class)
  fun testAsJsonObject_WhenNotObject_ThrowsUVE() {
    createJsonArrayWithLocation().jsonObject
  }

  @Test
  fun testAsJsonString_WhenString_ReturnsJsonString() {
    assert(createJsonStringWithLocation("joe").string).isEqualTo("joe")
  }

  @Test
  fun testAsJsonString_WhenNull_ReturnsNull() {
    assert(createValue(JsonNull).string).isNull()
  }

  @Test
  fun testAsJsonString_WhenNotString_Coerced() {
    val value = createJsonNumberWithLocation(32.4).string
    assert(value).isEqualTo("32.4")
  }

  @Test
  fun testAsJsonString_WhenNotString_Coerced_With_Scale() {
    val value = createJsonNumberWithLocation(32.0).string
    assert(value).isEqualTo("32.0")
  }

  //
  // @Nullable
  // public String asString() {
  //     if (is(JsonValue.ElementType.NULL)) {
  //         return null;
  //     } else {
  //         return ((JsonString) wrapped).string();
  //     }
  // }
  //
  // public JsonPath getPath() {
  //     return location.getJsonPath();
  // }
  //
  // public boolean containsKey(String key) {
  //     return asJsonObject().containsKey(key);
  // }
  //
  // public JsonArray expectArray(JsonSchemaKeyword property) {
  //     return findArray(property).orElseThrow(() -> new MissingExpectedPropertyException(asJsonObject(), property.key()));
  // }
  //
  // public Optional<JsonArray> findArray(JsonSchemaKeyword property) {
  //     checkNotNull(property, "property must not be null");
  //
  //     return findByKey(property.key(), JsonArray.class);
  // }
  //
  // public Optional<Boolean> findBoolean(JsonSchemaKeyword property) {
  //     checkNotNull(property, "property must not be null");
  //
  //     JsonObject jsonObject = asJsonObject();
  //     if (asJsonObject().containsKey(property.key()) && !jsonObject.isNull(property.key())) {
  //         try {
  //             return Optional.of(jsonObject.getBoolean(property.key()));
  //         } catch (IllegalStateException e) {
  //             throw new UnexpectedValueException(location.getJsonPath(), jsonObject.get(property.key()), JsonValue.ElementType.TRUE, JsonValue.ElementType.FALSE);
  //         }
  //     }
  //     return Optional.empty();
  // }
  //
  // public Optional<JsonValue> findByKey(JsonSchemaKeyword prop) {
  //     if (asJsonObject().containsKey(prop.key())) {
  //         return Optional.of(asJsonObject().get(prop.key()));
  //     }
  //     return Optional.empty();
  // }
  //
  // public Optional<Integer> findInt(JsonSchemaKeyword property) {
  //     checkNotNull(property, "property must not be null");
  //
  //     return findByKey(property.key(), JsonNumber.class)
  //             .map(JsonNumber::intValue);
  // }
  //
  // public Optional<Integer> findInteger(JsonSchemaKeyword property) {
  //     checkNotNull(property, "property must not be null");
  //
  //     return findByKey(property.key(), JsonNumber.class)
  //             .map(JsonNumber::intValue);
  // }
  //
  // public Optional<Number> findNumber(JsonSchemaKeyword property) {
  //     checkNotNull(property, "property must not be null");
  //
  //     return findByKey(property.key(), JsonNumber.class)
  //             .map(JsonNumber::bigDecimalValue);
  // }
  //
  // public Optional<JsonObject> findObject(String property) {
  //     checkNotNull(property, "property must not be null");
  //     return findByKey(property, JsonObject.class);
  // }
  //
  // public Optional<JsonValueWithLocation> findPathAwareObject(JsonSchemaKeyword keyword) {
  //     checkNotNull(keyword, "keyword must not be null");
  //     return findPathAwareObject(keyword.key());
  // }
  //
  // public Optional<JsonValueWithLocation> findPathAwareObject(String childKey) {
  //     checkNotNull(childKey, "childKey must not be null");
  //     return findObject(childKey).map(json -> fromJsonValue(json, location.child(childKey)));
  // }
  //
  // public JsonValueWithLocation path(JsonSchemaKeyword keyword) {
  //     return path(keyword.key());
  // }
  //
  // public Optional<String> findString(JsonSchemaKeyword property) {
  //     checkNotNull(property, "property must not be null");
  //     return findByKey(property.key(), JsonString.class)
  //             .map(JsonString::string);
  // }
  //
  // public void forEachIndex(BiConsumer<? super Integer, ? super JsonValueWithLocation> action) {
  //     AtomicInteger i = new AtomicInteger(0);
  //     wrapped.jsonArray.forEach((v) -> {
  //         int idx = i.getAndIncrement();
  //         action.accept(idx, new JsonValueWithLocation(v, location.child(idx)));
  //     });
  // }
  //
  // public void forEachKey(BiConsumer<? super String, ? super JsonValueWithLocation> action) {
  //     wrapped.asJsonObject().forEach((k, v) -> {
  //         action.accept(k, fromJsonValue(v, location.child(k)));
  //     });
  // }
  //
  // @Override
  // @Deprecated
  // public JsonValue get(Object key) {
  //     return asJsonObject().get(key);
  // }
  //
  // public JsonValueWithLocation getItem(int idx) {
  //     JsonValue jsonValue = wrapped.jsonArray.get(idx);
  //     return fromJsonValue(jsonValue, location.child(idx));
  // }
  //
  // @Override
  // public JsonArray getJsonArray(String name) {
  //     return asJsonObject().getJsonArray(name);
  // }
  //
  // @Override
  // public JsonObject getJsonObject(String name) {
  //     return asJsonObject().getJsonObject(name);
  // }
  //
  // @Override
  // public JsonNumber getJsonNumber(String name) {
  //     return asJsonObject().getJsonNumber(name);
  // }
  //
  // @Override
  // public JsonString getJsonString(String name) {
  //     return asJsonObject().getJsonString(name);
  // }
  //
  // @Override
  // public String string(String name) {
  //     return asJsonObject().string(name);
  // }
  //
  // @Override
  // public String string(String name, String defaultValue) {
  //     return asJsonObject().string(name, defaultValue);
  // }
  //
  // @Override
  // public int getInt(String name) {
  //     return asJsonObject().getInt(name);
  // }
  //
  // @Override
  // public int getInt(String name, int defaultValue) {
  //     return asJsonObject().getInt(name, defaultValue);
  // }
  //
  // @Override
  // public boolean getBoolean(String name) {
  //     return asJsonObject().getBoolean(name);
  // }
  //
  // @Override
  // public boolean getBoolean(String name, boolean defaultValue) {
  //     return asJsonObject().getBoolean(name, defaultValue);
  // }
  //
  // @Override
  // public boolean isNull(String name) {
  //     return asJsonObject().isNull(name);
  // }
  //
  // public JsonValueWithLocation path(String childKey) {
  //     checkNotNull(childKey, "childKey must not be null");
  //     return fromJsonValue(asJsonObject().get(childKey), location.child(childKey));
  // }
  //
  // public String string(JsonSchemaKeyword property) {
  //     try {
  //         if (asJsonObject().isNull(property.key())) {
  //             return null;
  //         }
  //         return asJsonObject().string(property.key());
  //     } catch (IllegalStateException e) {
  //         throw new UnexpectedValueException(location, asJsonObject().get(property.key()), ElementType.STRING);
  //     }
  // }
  //
  // @Override
  // public JsonValue keywordValue(String jsonPointer) {
  //     return asJsonObject().keywordValue(jsonPointer);
  // }
  //
  // public boolean has(JsonSchemaKeyword property, JsonValue.ElementType... ofType) {
  //     final Map<String, JsonValue> jsonObject = asJsonObject();
  //     if (!jsonObject.containsKey(property.key())) {
  //         return false;
  //     }
  //     if (ofType != null && ofType.length > 0) {
  //         final JsonValue jsonValue = jsonObject.get(property.key());
  //         for (JsonValue.ElementType valueType : ofType) {
  //             if (jsonValue.type == valueType) {
  //                 return true;
  //             }
  //         }
  //         return false;
  //     }
  //     return true;
  // }
  //
  // @Override
  // public int hashCode() {
  //     return wrapped.hashCode();
  // }
  //
  // @Override
  // public boolean equals(Object o) {
  //     return wrapped.equals(o);
  // }
  //
  // /**
  //  * Returns JSON text for this JSON value.
  //  *
  //  * @return JSON text
  //  */
  // @Override
  // public String toString() {
  //     return location.getJsonPointerFragment() + " -> " + wrapped;
  // }
  //
  // public boolean is(JsonValue.ElementType... types) {
  //     checkNotNull(types, "keywords must not be null");
  //     checkArgument(types.length > 0, "keywords must be >0");
  //     for (JsonValue.ElementType type : types) {
  //         if (wrapped.type == type) {
  //             return true;
  //         }
  //     }
  //     return false;
  // }
  //
  // public int numberOfProperties() {
  //     return asJsonObject().keySet().size();
  // }
  //
  // public Set<String> propertyNames() {
  //     return asJsonObject().keySet();
  // }
  //
  // @Override
  // public int size() {
  //     return asJsonObject().size();
  // }
  //
  // public boolean isEmpty() {
  //     return asJsonObject().isEmpty();
  // }
  //
  // @Override
  // public boolean containsKey(Object key) {
  //     return asJsonObject().containsKey(key);
  // }
  //
  // @Override
  // public Set<String> keySet() {
  //     return asJsonObject().keySet();
  // }
  //
  // @Override
  // public Collection<JsonValue> values() {
  //     return asJsonObject().values();
  // }
  //
  // @Override
  // public Set<Map.Entry<String, JsonValue>> entrySet() {
  //     return asJsonObject().entrySet();
  // }
  //
  // public Stream<JsonValueWithLocation> streamPathAwareArrayItems(JsonSchemaKeyword keyword) {
  //     AtomicInteger i = new AtomicInteger(0);
  //     if (!has(keyword, JsonValue.ElementType.ARRAY)) {
  //         return Stream.empty();
  //     } else {
  //         return expectArray(keyword)
  //                 .stream()
  //                 .map(jsonValue -> fromJsonValue(jsonValue, location.child(i.incrementAndGet())));
  //     }
  // }
  //
  // /**
  //  * Returns an Optional instance for a key in this object.  This method also takes care of registering any
  //  * validation with the appropriate path and/or context.
  //  *
  //  * @param property The name of the property you are retrieving
  //  * @param expected The type of JsonValue to be returned.
  //  * @param <X>      Method capture vararg to ensure type-safety for callers.
  //  * @return Optional.empty if the key doesn't exist, otherwise returns the value at the specified key.
  //  */
  // private <X extends JsonValue> Optional<X> findByKey(String property, Class<X> expected) {
  //     checkNotNull(property, "property must not be null");
  //     if (asJsonObject().containsKey(property)) {
  //         JsonValue jsonValue = asJsonObject().get(property);
  //         if (!expected.isAssignableFrom(jsonValue.getClass())) {
  //             final JsonValue.ElementType valueType = JsonUtils.jsonTypeForClass(expected);
  //             throw new UnexpectedValueException(location.child(property), jsonValue, valueType);
  //         }
  //         return Optional.of(expected.cast(jsonValue));
  //     }
  //     return Optional.empty();
  // }
  //
  // public static JsonValueWithLocation fromJsonValue(JsonValue jsonObject, SchemaLocation parentLocation) {
  //     final URI uri;
  //     SchemaLocation location = parentLocation;
  //     if (jsonObject.type == ElementType.OBJECT) {
  //         final JsonObject asJsonObject = jsonObject.asJsonObject();
  //         if (asJsonObject.keySet().contains($ID.key())) {
  //             final JsonValue $id = asJsonObject.get($ID.key());
  //             if ($id.type == ElementType.STRING) {
  //                 uri = URI(((JsonString) $id).string());
  //                 location = location.withId(uri);
  //             }
  //         }
  //     }
  //     return new JsonValueWithLocation(jsonObject, location);
  // }
  //
  // public static JsonValueWithLocation fromJsonValue(JsonObject jsonObject) {
  //     final SchemaLocation rootSchemaLocation;
  //     if (jsonObject.containsKey($ID.key())) {
  //         String $id = jsonObject.string($ID.key());
  //         rootSchemaLocation = SchemaLocation.documentRoot($id);
  //     } else {
  //         rootSchemaLocation = SchemaLocation.anonymousRoot();
  //     }
  //
  //     return  new JsonValueWithLocation(jsonObject, rootSchemaLocation);
  // }
  //
}
