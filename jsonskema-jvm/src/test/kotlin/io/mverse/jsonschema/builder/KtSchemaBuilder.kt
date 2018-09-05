package io.mverse.jsonschema.builder

import io.mverse.jsonschema.Draft3Schema
import io.mverse.jsonschema.Draft4Schema
import io.mverse.jsonschema.Draft6Schema
import io.mverse.jsonschema.Draft7Schema
import io.mverse.jsonschema.DraftSchema
import io.mverse.jsonschema.JsonSchema
import io.mverse.jsonschema.Schema
import io.mverse.jsonschema.SchemaBuilder
import io.mverse.jsonschema.builder.KtJsonSchemaBuilder.KtNumberSchema
import io.mverse.jsonschema.builder.KtJsonSchemaBuilder.KtStringSchema
import io.mverse.jsonschema.enums.FormatType.DATE
import io.mverse.jsonschema.enums.FormatType.DATE_TIME
import io.mverse.jsonschema.enums.FormatType.IPV4
import io.mverse.jsonschema.enums.JsonSchemaType.ARRAY
import io.mverse.jsonschema.enums.JsonSchemaType.INTEGER
import io.mverse.jsonschema.enums.JsonSchemaType.NULL
import io.mverse.jsonschema.enums.JsonSchemaType.NUMBER
import io.mverse.jsonschema.enums.JsonSchemaType.STRING
import io.mverse.jsonschema.schemaBuilder
import io.mverse.jsonschema.utils.Schemas
import lang.URI
import lang.illegalState

interface KtSchemaBuilder : SchemaBuilder {
  val schemaBuilder: SchemaBuilder
  val parent: SchemaBuilder?
  val name: String?

  /**
   * Section
   */
  operator fun String.invoke(title: String? = null, propBlock: KtSchemaBuilder.() -> Unit) {
    val key = this
    val propSchema = newBuilderDsl()
    if (title != null) {
      propSchema.title = title
    }
    propSchema.propBlock()
    this@KtSchemaBuilder.properties[key] = propSchema
  }

  /**
   * Within the context of a builder, this creates a property schema with the name of the [String]
   * being invoked
   */
  infix fun String.required(schema: KtSchemaBuilder) {
    val key = this
    this@KtSchemaBuilder.properties[key] = schema
    this@KtSchemaBuilder.requiredProperties += key
  }

  infix fun String.required(schema: () -> KtSchemaBuilder) {
    val key = this
    this@KtSchemaBuilder.properties[key] = schema()
    this@KtSchemaBuilder.requiredProperties += key
  }

  infix fun String.list(block: KtSchemaBuilder.() -> Unit) {
    this@KtSchemaBuilder.properties[this] = propertySchema@{
      type = ARRAY

      val allItemsSchema = newBuilderDsl()
      allItemsSchema.block()
      this@propertySchema.allItemSchema = allItemsSchema
    }
  }

  infix fun String.optional(schema: KtSchemaBuilder) {
    val key = this
    schema.types += NULL
    this@KtSchemaBuilder.properties[key] = schema
  }

  infix fun String.optional(schema: () -> KtSchemaBuilder) {
    return optional(schema())
  }

  infix fun String.ref(uri: URI) {
    val key = this
    this@KtSchemaBuilder.properties[key] = {
      ref(uri)
    }
    this@KtSchemaBuilder.requiredProperties += key
  }

  infix fun String.optionalref(uri: URI) {
    val key = this
    this@KtSchemaBuilder.properties[key] = {
      ref(uri)
    }
  }

  val datetime: () -> KtJsonSchemaBuilder.KtStringSchema
    get() = {
      KtStringSchema().apply { format = DATE_TIME.toString() }
    }

  val ip: () -> KtStringSchema
    get() = {
      KtStringSchema().apply { format = IPV4.toString() }
    }

  val date: () -> KtStringSchema
    get() = {
      KtStringSchema().apply { format = DATE.toString() }
    }

  val string: () -> KtStringSchema
    get() = {
      KtStringSchema()
    }

  val number: () -> KtNumberSchema
    get() = {
      KtNumberSchema()
    }

  val integer: () -> KtNumberSchema
    get() = {
      KtNumberSchema().apply { isInteger = true }
    }

  operator fun Pair<String, String>.invoke(propBlock: KtSchemaBuilder.() -> Unit) {
    val key = this.first
    val title = this.second
    val propSchema = newBuilderDsl().apply {
      this.title = title
      propBlock()
    }
    properties[key] = propSchema
  }

  fun number(title: String? = null, block: KtNumberSchema.() -> Unit = {}): KtNumberSchema {
    val number = KtNumberSchema()
    number.block()
    return number
  }

  fun integer(title: String? = null, block: KtNumberSchema.() -> Unit = {}): KtNumberSchema {
    val number = KtNumberSchema()
    number.isInteger = true
    number.block()
    return number
  }

  fun string(title: String? = null, block: KtStringSchema.() -> Unit = {}): KtStringSchema {
    val schema = KtStringSchema()
    if (title != null) {
      schema.title = title
    }
    schema.block()
    return schema
  }

  fun datetime(title: String? = null, block: KtStringSchema.() -> Unit = {}): KtStringSchema {
    val schema = KtStringSchema()
    if (title != null) {
      schema.title = title
    }
    schema.format = DATE_TIME.toString()
    schema.block()
    return schema
  }

  fun date(title: String? = null, block: KtStringSchema.() -> Unit = {}): KtStringSchema {
    val schema = KtStringSchema(this)
    if (title != null) {
      schema.title = title
    }
    schema.format = DATE.toString()
    schema.block()
    return schema
  }

  fun section(title: String? = null, block: KtSchemaBuilder.() -> Unit = {}): KtSchemaBuilder {
    val schema = KtJsonSchemaBuilder(this)
    if (title != null) {
      schema.title = title
    }
    schema.block()
    return schema
  }
}

open class KtJsonSchemaBuilder(override val schemaBuilder: SchemaBuilder,
                               override val parent: SchemaBuilder? = null,
                               override var name: String? = null) :
    KtSchemaBuilder,
    SchemaBuilder by schemaBuilder {

  class KtNumberSchema(val kt: KtSchemaBuilder = newBuilderDsl()) : KtSchemaBuilder by kt {
    init {
      kt.type = NUMBER
    }
    var isInteger: Boolean
      set(isInteger) {
        if (isInteger) kt.type = INTEGER
      }
      get() = illegalState("Not implemented")

  }

  class KtStringSchema(val kt: KtSchemaBuilder = newBuilderDsl()) : KtSchemaBuilder by kt {
    init {
      kt.type = STRING
    }
  }
}

fun newBuilderDsl(uri: URI? = null): KtSchemaBuilder {
  @Suppress("unchecked_cast")
  return when (uri) {
    null -> KtJsonSchemaBuilder(JsonSchema.schemaBuilder())
    else -> KtJsonSchemaBuilder(JsonSchema.schemaBuilder(uri))
  }
}

inline fun <reified D: DraftSchema<D>> Schema.asVersion():D {
  @Suppress("unchecked_cast")
  return when (D::class) {
    Draft7Schema::class -> asDraft7() as D
    Draft6Schema::class -> asDraft6() as D
    Draft4Schema::class -> asDraft4() as D
    Draft3Schema::class -> asDraft3() as D
    else -> this as D
  }
}
