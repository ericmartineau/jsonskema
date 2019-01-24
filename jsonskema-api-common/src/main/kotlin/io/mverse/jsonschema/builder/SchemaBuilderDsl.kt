package io.mverse.jsonschema.builder

import io.mverse.jsonschema.JsonSchema.createSchemaBuilder
import io.mverse.jsonschema.Schema
import io.mverse.jsonschema.SchemaBuilder
import io.mverse.jsonschema.enums.FormatType
import io.mverse.jsonschema.enums.JsonSchemaType
import io.mverse.jsonschema.utils.jsonSchemaType
import lang.collection.asList
import lang.json.JsrArray
import lang.json.KtArray
import lang.json.createJsrArray
import lang.json.jsrString
import lang.json.toJsrArray
import lang.json.toJsrValue
import lang.net.URI

open class SchemaBuilderDsl(val schemaBuilder: SchemaBuilder = createSchemaBuilder(),
                            val parent: SchemaBuilder? = null,
                            var name: String? = null) :
    SchemaBuilder by schemaBuilder {

  /**
   * Section
   */
  operator fun String.invoke(title: String? = null, propBlock: SchemaBuilderDsl.() -> Unit) {
    val key = this
    val propSchema = SchemaBuilderDsl()
    if (title != null) {
      propSchema.title = title
    }
    propSchema.propBlock()
    this@SchemaBuilderDsl.properties[key] = propSchema
  }

  /**
   * Within the context of a builder, this creates a property schema with the name of the [String]
   * being invoked
   */
  infix fun String.required(schema: SchemaBuilderDsl) {
    val key = this
    this@SchemaBuilderDsl.properties[key] = schema
    this@SchemaBuilderDsl.requiredProperties += key
  }

  infix fun String.required(schema: () -> SchemaBuilderDsl) {
    val key = this
    this@SchemaBuilderDsl.properties[key] = schema()
    this@SchemaBuilderDsl.requiredProperties += key
  }

  infix fun String.list(block: SchemaBuilderDsl.() -> Unit) {
    this@SchemaBuilderDsl.properties[this] = propertySchema@{
      type = JsonSchemaType.ARRAY

      val allItemsSchema = SchemaBuilderDsl()
      allItemsSchema.block()
      this@propertySchema.allItemSchema = allItemsSchema
    }
  }

  infix fun String.optional(schema: SchemaBuilderDsl) {
    val key = this
    if (!schema.types.isEmpty()) {
      schema.types += JsonSchemaType.NULL
    }

    this@SchemaBuilderDsl.properties[key] = schema
  }

  infix fun String.optional(schema: () -> SchemaBuilderDsl) {
    return optional(schema())
  }

  infix fun String.ref(uri: URI) {
    val key = this
    this@SchemaBuilderDsl.properties[key] = {
      ref = uri
    }
    this@SchemaBuilderDsl.requiredProperties += key
  }

  infix fun String.ref(refSchema: Schema) {
    val key = this
    this@SchemaBuilderDsl.properties[key] = {
      this.refSchema = refSchema
    }
    this@SchemaBuilderDsl.requiredProperties += key
  }

  infix fun String.required(refSchema: Schema) {
    val key = this
    this@SchemaBuilderDsl.properties[key] = {
      this.refSchema = refSchema
    }
    this@SchemaBuilderDsl.requiredProperties += key
  }

  infix fun String.required(uri: URI) {
    val key = this
    this@SchemaBuilderDsl.properties[key] = {
      ref = uri
    }
    this@SchemaBuilderDsl.requiredProperties += key
  }

  infix fun String.optional(refSchema: Schema) {
    val key = this
    this@SchemaBuilderDsl.properties[key] = {
      this.refSchema = refSchema
    }
  }

  infix fun String.optional(uri: URI) {
    val key = this
    this@SchemaBuilderDsl.properties[key] = {
      ref = uri
    }
  }

  infix fun String.optional(format: FormatType) {
    val key = this
    this@SchemaBuilderDsl.properties[key] = {
      this.type = JsonSchemaType.STRING
      this.format = format.value
    }
  }

  infix fun String.required(format: FormatType) {
    val key = this
    this@SchemaBuilderDsl.properties[key] = {
      this.type = JsonSchemaType.STRING
      this.format = format.value
    }
    this@SchemaBuilderDsl.requiredProperties += key
  }

  val string: () -> SchemaBuilderDsl
    get() = {
      SchemaBuilderDsl().apply {
        type = JsonSchemaType.STRING
      }
    }

  val number: () -> SchemaBuilderDsl
    get() = {
      SchemaBuilderDsl().apply {
        type = JsonSchemaType.NUMBER
      }
    }

  val integer: () -> SchemaBuilderDsl
    get() = {
      SchemaBuilderDsl().apply {
        type = JsonSchemaType.INTEGER
      }
    }

  val boolean: () -> SchemaBuilderDsl
    get() = {
      SchemaBuilderDsl().apply {
        type = JsonSchemaType.BOOLEAN
      }
    }

  val array: () -> SchemaBuilderDsl
    get() = {
      SchemaBuilderDsl().apply {
        type = JsonSchemaType.ARRAY
      }
    }

  operator fun Pair<String, String>.invoke(propBlock: SchemaBuilderDsl.() -> Unit) {
    val key = this.first
    val title = this.second
    val propSchema = SchemaBuilderDsl().apply {
      this.title = title
      propBlock()
    }
    properties[key] = propSchema
  }

  fun number(title: String? = null, block: SchemaBuilderDsl.() -> Unit = {}): SchemaBuilderDsl {
    return SchemaBuilderDsl().apply {
      this.type = JsonSchemaType.NUMBER
      this.title = title
      this.block()
    }
  }

  fun integer(title: String? = null, block: SchemaBuilderDsl.() -> Unit = {}): SchemaBuilderDsl {
    return SchemaBuilderDsl().apply {
      this.type = JsonSchemaType.INTEGER
      this.title = title
      this.block()
    }
  }

  fun string(title: String? = null, block: SchemaBuilderDsl.() -> Unit = {}): SchemaBuilderDsl {
    return SchemaBuilderDsl().apply {
      this.type = JsonSchemaType.STRING
      this.title = title
      this.block()
    }
  }

  fun boolean(title: String? = null, block: SchemaBuilderDsl.() -> Unit = {}): SchemaBuilderDsl {
    return SchemaBuilderDsl().apply {
      this.type = JsonSchemaType.BOOLEAN
      this.title = title
      this.block()
    }
  }

  fun array(block: SchemaBuilderDsl.() -> Unit = {}): SchemaBuilderDsl {
    return SchemaBuilderDsl().apply {
      this.type = JsonSchemaType.ARRAY
      this.block()
    }
  }

  fun enum(vararg values: String): SchemaBuilderDsl = enum(values.toList())

  fun enum(enumValues: Iterable<Any?>): SchemaBuilderDsl {
    return SchemaBuilderDsl().apply {
      this.enumValues = createJsrArray(enumValues.map { toJsrValue(it) })
      this.type = this.enumValues?.jsonSchemaType
    }
  }

  fun enum(values: JsrArray, block: SchemaBuilderDsl.() -> Unit = {}): SchemaBuilderDsl {
    return SchemaBuilderDsl().apply {
      enumValues = values
      block()
    }
  }

  fun enum(values: KtArray, block: SchemaBuilderDsl.() -> Unit = {}): SchemaBuilderDsl {
    return SchemaBuilderDsl().apply {
      enumValues = values.toJsrArray()
      block()
    }
  }

  fun datetime(title: String? = null, block: SchemaBuilderDsl.() -> Unit = {}): SchemaBuilderDsl {
    return SchemaBuilderDsl().apply {
      this.type = JsonSchemaType.STRING
      this.title = title
      this.format = FormatType.DATE_TIME.toString()
      block()
    }
  }

  fun date(title: String? = null, block: SchemaBuilderDsl.() -> Unit = {}): SchemaBuilderDsl {
    return SchemaBuilderDsl(this).apply {
      this.type = JsonSchemaType.STRING
      this.format = FormatType.DATE.toString()
      this.title = title
      block()
    }
  }

  fun schemaBuilder(id: URI? = null, block: SchemaBuilderDsl.() -> Unit): SchemaBuilderDsl = when (id) {
    null -> SchemaBuilderDsl(createSchemaBuilder(), this).apply(block)
    else -> SchemaBuilderDsl(createSchemaBuilder(id), this).apply(block)
  }

  fun allItemsSchema(block: SchemaBuilderDsl.() -> Unit) {
    this.allItemSchema = SchemaBuilderDsl(createSchemaBuilder(), this).apply(block)
  }

  fun ifSchema(block: SchemaBuilderDsl.() -> Unit) {
    ifSchema = SchemaBuilderDsl(createSchemaBuilder(), this).apply(block)
  }

  fun thenSchema(block: SchemaBuilderDsl.() -> Unit) {
    thenSchema = SchemaBuilderDsl(createSchemaBuilder(), this).apply(block)
  }

  fun elseSchema(block: SchemaBuilderDsl.() -> Unit) {
    elseSchema = SchemaBuilderDsl(createSchemaBuilder(), this).apply(block)
  }

  fun schemaOfAdditionalProperties(block: SchemaBuilderDsl.() -> Unit) {
    schemaOfAdditionalProperties = SchemaBuilderDsl(createSchemaBuilder(), this).apply(block)
  }
}
