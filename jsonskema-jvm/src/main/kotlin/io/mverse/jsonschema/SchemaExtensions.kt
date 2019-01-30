package io.mverse.jsonschema

import io.mverse.jsonschema.enums.JsonSchemaType
import io.mverse.jsonschema.utils.calculateJsonSchemaType

/**
 * Walks the schema and finds all recursive leaf properties.
 */
val Draft7Schema.allProperties: Map<String, SchemaWithRequiredStatus>
  get() {
    val holder = mutableMapOf<String, SchemaWithRequiredStatus>()
    return recurseProperties(this, "", true, holder, mutableSetOf())
  }

data class SchemaWithRequiredStatus(val schema: Draft7Schema, val isRequired: Boolean)

infix fun Draft7Schema.with(isRequired: Boolean): SchemaWithRequiredStatus = SchemaWithRequiredStatus(this, isRequired)

fun recurseProperties(draftSchema: Draft7Schema, currentPath: String, isRequired: Boolean,
                      holder: MutableMap<String, SchemaWithRequiredStatus>,
                      processed: MutableSet<String>): Map<String, SchemaWithRequiredStatus> {


  if (currentPath !in processed && !draftSchema.isRefSchema) {
    processed += currentPath

    // If this property is a deterministic primitive, add it
    val calculateType = draftSchema.calculateJsonSchemaType()
    if (calculateType != null && calculateType != JsonSchemaType.OBJECT && calculateType != JsonSchemaType.ARRAY &&
        calculateType != JsonSchemaType.NULL) {
      holder[currentPath] = draftSchema with isRequired
    } else {

      // Iterate properties
      draftSchema.properties.forEach { (path, prop) ->
        val propIsRequired = isRequired && draftSchema.requiredProperties.contains(path)
        val propPath = "$currentPath/$path"
        recurseProperties(prop.draft7(), propPath, propIsRequired, holder, processed)
      }

      draftSchema.allOfSchemas.forEach { allOf ->
        recurseProperties(allOf.draft7(), currentPath, isRequired, holder, processed)
      }

      draftSchema.oneOfSchemas.forEach { oneOf ->
        recurseProperties(oneOf.draft7(), currentPath, false, holder, processed)
      }

      draftSchema.anyOfSchemas.forEach { anyOf ->
        recurseProperties(anyOf.draft7(), currentPath, false, holder, processed)
      }

      if (draftSchema.thenSchema != null) {
        recurseProperties(draftSchema.thenSchema!!.draft7(), currentPath, false, holder, processed)
      }

      if (draftSchema.elseSchema != null) {
        recurseProperties(draftSchema.elseSchema!!.draft7(), currentPath, false, holder, processed)
      }

      draftSchema.allItemSchema?.run {
        val d7 = this.draft7()
        recurseProperties(d7, currentPath, isRequired && d7.minItems ?: 0 > 0, holder, processed)
      }
    }
  }

  return holder
}