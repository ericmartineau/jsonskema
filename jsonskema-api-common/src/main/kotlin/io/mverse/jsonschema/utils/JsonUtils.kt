package io.mverse.jsonschema.utils

import io.mverse.jsonschema.enums.JsonSchemaType
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonLiteral
import kotlinx.serialization.json.JsonObject
import lang.Logger
import lang.URI

object JsonUtils {

  private val log = Logger(JsonUtils::class.qualifiedName!!)

//  private val PRETTY_PRINT_OPTS = ImmutableMap.of(PRETTY_PRINTING, true)
//  private val PRETTY_PRINT_WRITER_FACTORY = JsonProvider.provider().createWriterFactory(PRETTY_PRINT_OPTS)
//  private val PRETTY_PRINT_GENERATOR_FACTORY = JsonProvider.provider().createGeneratorFactory(PRETTY_PRINT_OPTS)

//  fun isBoolean(value: JsonElement): Boolean {
//    return value === TRUE || value === FALSE
//  }
//
//  fun emptyJsonArray(): JsonArray {
//    return EMPTY_JSON_ARRAY
//  }
//
//  fun blankJsonArray(): JsonArray {
//    return provider().createArrayBuilder().build()
//  }
//
//  fun blankJsonObject(): JsonObject {
//    return provider().createObjectBuilder().build()
//  }


  fun extractIdFromObject(json: kotlinx.serialization.json.JsonObject, id: String = "\$id", vararg otherIdKeys: String): URI? {
    if (json.containsKey(id)) {
      return tryParseURI(json[id])
    }

    for (idKeyword in otherIdKeys.asIterable() + "id") {
      if (json.containsKey(idKeyword)) {
        return tryParseURI(json[idKeyword])
      }
    }
    return null
  }

  /**
   * Safely parses URI from a JsonElement.  Logs any URI parsing failure, but will not log if the JsonElement is
   * not a JsonString instance
   */
  fun tryParseURI(uriValue: JsonElement): URI? {
    if (uriValue is JsonLiteral) {
      uriValue.contentOrNull
    }
    return when(uriValue) {
      is JsonLiteral-> try {
        URI(uriValue.contentOrNull)
      } catch(e:Exception) {
        log.warn("Failed to parse URI: ${uriValue.contentOrNull}")
        return null
      }
      else->null
    }
  }

  fun prettyPrintArgs(args: Iterable<Any>): Array<Any> {
    return args.map {
      when (it) {
        is JsonElement->it.toString()
        else-> it
      }
    }.toTypedArray()
  }

  fun prettyPrintArgs(vararg args: Any): Array<Any> {
    return args.map {
      when (it) {
        is JsonElement->it.toString()
        else-> it
      }
    }.toTypedArray()
  }

//  fun jsonArray(values: List<Any>): JsonArray {
//    return provider().createArrayBuilder(values).build()
//  }
//
//  fun jsonArray(vararg values: Any): JsonArray {
//    return provider().createArrayBuilder(Arrays.asList(values)).build()
//  }
//
//  fun jsonObjectBuilder(): JsonObjectBuilder {
//    return provider().createObjectBuilder()
//  }
//
//  fun jsonStringValue(value: String): JsonString {
//    return provider().createValue(value)
//  }
//
//  fun jsonNumberValue(num: Double): JsonNumber {
//    return provider().createValue(num)
//  }

//  fun toNumber(num: JsonNumber?): Number? {
//    return if (num == null) {
//      null
//    } else if (num!!.isIntegral()) {
//      num!!.intValueExact()
//    } else {
//      num!!.doubleValue()
//    }
//  }
//
//  fun jsonNumberValue(num: Long): JsonNumber {
//    return provider().createValue(num)
//  }
//
//  fun readJsonObject(json: String): JsonObject {
//    checkNotNull(json, "json must not be null")
//    return provider()
//        .createReader(StringReader(json))
//        .readObject()
//  }
//
//  @SneakyThrows
//  fun readJsonObject(stream: InputStream): JsonObject {
//    checkNotNull(stream, "stream must not be null")
//    stream.use({ streamX ->
//      return provider()
//          .createReader(streamX)
//          .readObject()
//    })
//  }

//  fun prettyPrintGeneratorFactory(): JsonGeneratorFactory {
//    return PRETTY_PRINT_GENERATOR_FACTORY
//  }

//  fun toPrettyString(value: JsonElement, indent: Boolean): String {
//    checkNotNull(value, "value must not be null")
//    val strings = StringWriter()
//    val actualWriter: Writer
//    if (indent) {
//      actualWriter = IndentingWriter(strings, "\t")
//    } else {
//      actualWriter = strings
//    }
//
//    PRETTY_PRINT_WRITER_FACTORY.createWriter(actualWriter).write(value)
//    strings.flush()
//    return strings.toString()
//  }
//
//  fun readJsonObject(file: File): JsonObject {
//    FileInputStream(file).use({ fileInputStream -> return provider().createReader(fileInputStream).readObject() })
//  }

//  fun <V : JsonElement> readResourceAsJson(resourceURL: String, JsonElement: Class<V>): V {
//    return readValue(JsonUtils::class.java!!.getResourceAsStream(resourceURL), JsonElement)
//  }
//
//  fun <V : JsonElement> readValue(json: String, expected: Class<V>): V {
//    return provider()
//        .createReader(StringReader(json))
//        .readValue()
//  }
//
//  fun readValue(json: String): JsonElement {
//    checkNotNull(json, "json must not be null")
//    return provider()
//        .createReader(StringReader(json))
//        .readValue()
//  }
//
//  fun <V : JsonElement> readValue(json: InputStream, expected: Class<V>): V {
//    return provider()
//        .createReader(json)
//        .readValue()
//  }
//
//  fun jsonTypeForClass(clazz: KClass<out JsonElement>): ElementType {
//    return if (clazz.isAssignableFrom(JsonNumber::class)) {
//      ElementType.NUMBER
//    } else if (clazz.isAssignableFrom(JsonString::class)) {
//      ElementType.STRING
//    } else if (clazz.isAssignableFrom(JsonObject::class)) {
//      ElementType.Any
//    } else if (clazz.isAssignableFrom(JsonArray::class)) {
//      ElementType.ARRAY
//    } else {
//      throw IllegalArgumentException("Unable to determine type for class: $clazz")
//    }
//  }



//  fun toJsonSchemaType(valueType: ElementType): JsonSchemaType {
//    when (valueType) {
//      ElementType.ARRAY -> return JsonSchemaType.ARRAY
//      ElementType.Any -> return JsonSchemaType.Any
//      ElementType.STRING -> return JsonSchemaType.STRING
//      ElementType.NUMBER -> return JsonSchemaType.NUMBER
//      FALSE -> return JsonSchemaType.BOOLEAN
//      TRUE -> return JsonSchemaType.BOOLEAN
//      JsonElement.NULL -> return JsonSchemaType.NULL
//      else -> throw IllegalArgumentException("Unable to determine type")
//    }
//  }
}
