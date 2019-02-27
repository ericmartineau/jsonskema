package io.mverse.jsonschema.builder

import io.mverse.jsonschema.JsonSchemas
import io.mverse.jsonschema.Schema
import io.mverse.jsonschema.enums.FormatType
import io.mverse.jsonschema.enums.JsonSchemaType
import io.mverse.jsonschema.keyword.JsrIterable
import io.mverse.jsonschema.keyword.Keywords.PROPERTIES
import io.mverse.jsonschema.keyword.SchemaMapKeyword
import io.mverse.jsonschema.keyword.iterableOf
import io.mverse.jsonschema.utils.jsonSchemaType
import lang.json.JsrArray
import lang.json.KtArray
import lang.json.toJsrArray
import lang.json.toJsrValue
import lang.json.values
import lang.net.URI

class MutableProperties(val builder: MutableSchema) {
  val keyword = PROPERTIES

  operator fun minusAssign(key:String) {
    val properties = builder[keyword] ?: return
    builder[keyword] = properties - key
  }
  operator fun get(key: String): Schema {
    return (builder[keyword] ?: SchemaMapKeyword()).value.getValue(key)
  }

  operator fun set(key: String, block: MutableSchema.() -> Unit) {
    val existing = builder[keyword] ?: SchemaMapKeyword()
    val childLocation = builder.location.child(PROPERTIES).child(key)
    val childBuilder = JsonSchemas.schemaBuilder(childLocation, builder.schemaLoader, block)
    builder[keyword] = existing + (key to builder.buildSubSchema(childBuilder, keyword, key))
  }

  @Deprecated("Use set with block parameter")
  operator fun set(key: String, schema: MutableSchema) {
    val existing = builder[keyword] ?: SchemaMapKeyword()
    val childLocation = builder.location.child(PROPERTIES).child(key)
    val childBuilder = schema.withLocation(childLocation)
    builder[keyword] = existing + (key to childBuilder.build())
  }

  operator fun invoke(block: MutableProperties.() -> Unit) {
    block()
  }

  infix fun String.required(block: MutableSchema.() -> Unit) {
    val key = this
    set(key, block)
    builder.requiredProperties += key
  }

  infix fun String.list(block: MutableSchema.() -> Unit) {
    val key = this
    set(key) propSchema@{
      type = JsonSchemaType.ARRAY
      builder.allItemsSchema(block)
    }
  }

  infix fun String.optional(schema: MutableSchema.() -> Unit) {
    val key = this
    set(key) {
      schema()
      if (!types.isEmpty()) {
        types += JsonSchemaType.NULL
      }
    }
  }

  infix fun String.ref(uri: URI) {
    val key = this
    set(key) {
      ref = uri
    }
    builder.requiredProperties += key
  }

  infix fun String.ref(refSchema: Schema) {
    val key = this
    set(key) {
      this.refSchema = refSchema
    }
    builder.requiredProperties += key
  }

  infix fun String.required(refSchema: Schema) {
    val key = this
    set(key) {
      this.refSchema = refSchema
    }
    builder.requiredProperties += key
  }

  infix fun String.required(uri: URI) {
    val key = this
    set(key) {
      ref = uri
    }
    builder.requiredProperties += key
  }

  infix fun String.optional(refSchema: Schema) {
    val key = this
    set(key) {
      this.refSchema = refSchema
    }
  }

  infix fun String.optional(uri: URI) {
    val key = this
    set(key) {
      ref = uri
    }
  }

  infix fun String.optional(format: FormatType) {
    val key = this
    set(key) {
      this.type = JsonSchemaType.STRING
      this.format = format.value
    }
  }

  infix fun String.required(format: FormatType) {
    val key = this
    set(key) {
      this.type = JsonSchemaType.STRING
      this.format = format.value
    }
    builder.requiredProperties += key
  }

  val string: MutableSchema.() -> Unit
    get() = {
      type = JsonSchemaType.STRING
    }

  val number: MutableSchema.() -> Unit
    get() = {
      type = JsonSchemaType.NUMBER
    }

  val integer: MutableSchema.() -> Unit
    get() = { type = JsonSchemaType.INTEGER }

  val boolean: MutableSchema.() -> Unit
    get() = {
      type = JsonSchemaType.BOOLEAN
    }

  val array: MutableSchema.() -> Unit
    get() = {
      type = JsonSchemaType.ARRAY
    }

  fun number(block: MutableSchema.() -> Unit): (MutableSchema.() -> Unit) {
    return {
      this.type = JsonSchemaType.NUMBER
      this.block()
    }
  }

  fun integer(block: MutableSchema.() -> Unit): (MutableSchema.() -> Unit) {
    return {
      this.type = JsonSchemaType.INTEGER
      this.block()
    }
  }

  fun string(block: MutableSchema.() -> Unit): (MutableSchema.() -> Unit) {
    return {
      this.type = JsonSchemaType.STRING
      this.block()
    }
  }

  fun boolean(block: MutableSchema.() -> Unit): (MutableSchema.() -> Unit) {
    return {
      this.type = JsonSchemaType.BOOLEAN
      this.block()
    }
  }

  fun array(block: MutableSchema.() -> Unit = {}): (MutableSchema.() -> Unit) {
    return {
      this.type = JsonSchemaType.ARRAY
      this.block()
    }
  }

  fun enum(vararg values: String): (MutableSchema.() -> Unit) = enum(values.toList())

  fun enum(enumValues: Iterable<Any?>): (MutableSchema.() -> Unit) {
    return {
      this.enumValues = enumValues.map { toJsrValue(it) }
      this.type = this.enumValues?.jsonSchemaType
    }
  }

  fun enum(values: JsrArray, block: MutableSchema.() -> Unit = {}): (MutableSchema.() -> Unit) {
    return {
      enumValues = values.values
      block()
    }
  }

  fun enum(values: KtArray, block: MutableSchema.() -> Unit = {}): (MutableSchema.() -> Unit) {
    return enum(values.toJsrArray(),  block)
  }

  fun enum(block:()-> JsrIterable): (MutableSchema.() -> Unit) {
    return enum(iterableOf(block))
  }

  fun datetime(block: MutableSchema.() -> Unit = {}): (MutableSchema.() -> Unit) {
    return {
      this.type = JsonSchemaType.STRING
      this.format = FormatType.DATE_TIME.toString()
      block()
    }
  }

  fun date(block: MutableSchema.() -> Unit = {}): (MutableSchema.() -> Unit) {
    return {
      this.type = JsonSchemaType.STRING
      this.format = FormatType.DATE.toString()
      block()
    }
  }

  fun toSchemaMap(): Map<String, Schema> = builder[keyword]?.value ?: emptyMap()
}
