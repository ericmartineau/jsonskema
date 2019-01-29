package io.mverse.jsonschema

import lang.json.JsrArray
import lang.json.JsrValue

interface Draft6Schema : Draft4Schema {
  val examples: JsrArray
  val definitions: Map<String, Schema>

  val constValue: JsrValue?
  val containsSchema: Schema?
  val propertyNameSchema: Schema?
}
