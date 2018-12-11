package io.mverse.jsonschema.builder

import io.mverse.jsonschema.JsonSchema.createSchemaBuilder
import io.mverse.jsonschema.Schema
import io.mverse.jsonschema.SchemaBuilder
import io.mverse.jsonschema.enums.FormatType
import io.mverse.jsonschema.enums.JsonSchemaType
import kotlinx.serialization.json.JsonArray
import lang.URI
import lang.json.toJsonArray

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

  infix fun String.optionalref(uri: URI) {
    val key = this
    this@SchemaBuilderDsl.properties[key] = {
      ref = uri
    }
  }

  val datetime: () -> SchemaBuilderDsl
    get() = {
      SchemaBuilderDsl().apply {
        type = JsonSchemaType.STRING
        format = FormatType.DATE_TIME.toString()
      }
    }

  val ip: () -> SchemaBuilderDsl
    get() = {
      SchemaBuilderDsl().apply {
        type = JsonSchemaType.STRING
        format = FormatType.IPV4.toString()
      }
    }

  val date: () -> SchemaBuilderDsl
    get() = {
      SchemaBuilderDsl().apply {
        type = JsonSchemaType.STRING
        format = FormatType.DATE.toString()
      }
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
    return  SchemaBuilderDsl().apply {
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

  fun enum(vararg enumValues: String): SchemaBuilderDsl {
    return SchemaBuilderDsl().apply {
      this.type = JsonSchemaType.STRING
      this.enumValues = enumValues.toList().toJsonArray()
    }
  }

  fun enum(values: JsonArray, block: SchemaBuilderDsl.() -> Unit = {}): SchemaBuilderDsl {
    return SchemaBuilderDsl().apply {
      enumValues = values
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

  fun schemaBuilder(id:URI? = null, block: SchemaBuilderDsl.()->Unit): SchemaBuilderDsl = when (id) {
    null-> SchemaBuilderDsl(createSchemaBuilder(), this).apply(block)
    else-> SchemaBuilderDsl(createSchemaBuilder(id), this).apply(block)
  }
}
