package io.mverse.jsonschema

import io.mverse.jsonschema.enums.JsonSchemaType
import io.mverse.jsonschema.utils.calculateType

/**
 * Walks the schema and finds all recursive leaf properties.
 */
val Draft7Schema.allProperties: Map<String, SchemaWithRequiredStatus>
  get() {
    val holder = mutableMapOf<String, SchemaWithRequiredStatus>()
    return recurseProperties(this, "", true, holder)
  }

data class SchemaWithRequiredStatus(val schema: Draft7Schema, val isRequired: Boolean)

infix fun Draft7Schema.with(isRequired: Boolean): SchemaWithRequiredStatus = SchemaWithRequiredStatus(this, isRequired)

fun recurseProperties(schema: Draft7Schema, currentPath: String, isRequired: Boolean,
                      holder: MutableMap<String, SchemaWithRequiredStatus>): Map<String, SchemaWithRequiredStatus> {

  // If this property is a deterministic primitive, add it
  val calculateType = schema.calculateType()
  if (calculateType != null && calculateType != JsonSchemaType.OBJECT && calculateType != JsonSchemaType.ARRAY &&
      calculateType != JsonSchemaType.NULL) {
    holder[currentPath] = schema with isRequired
  } else {

    // Iterate properties
    schema.properties.forEach { (path, prop) ->
      val propIsRequired = isRequired && schema.requiredProperties.contains(path)
      val propPath = "$currentPath/$path"
      recurseProperties(prop.asDraft7(), propPath, propIsRequired, holder)
    }

    schema.allOfSchemas.forEach { allOf ->
      recurseProperties(allOf.asDraft7(), currentPath, isRequired, holder)
    }

    schema.oneOfSchemas.forEach { oneOf ->
      recurseProperties(oneOf.asDraft7(), currentPath, false, holder)
    }

    schema.anyOfSchemas.forEach { anyOf ->
      recurseProperties(anyOf.asDraft7(), currentPath, false, holder)
    }

    if (schema.thenSchema != null) {
      recurseProperties(schema.thenSchema!!.asDraft7(), currentPath, false, holder)
    }

    if (schema.elseSchema != null) {
      recurseProperties(schema.elseSchema!!.asDraft7(), currentPath, false, holder)
    }

    schema.allItemSchema?.run {
      recurseProperties(this.asDraft7(), currentPath, isRequired && this.minItems ?: 0 > 0, holder)
    }
  }

  return holder
}