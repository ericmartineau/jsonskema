package io.mverse.jsonschema

import io.mverse.jsonschema.keyword.JsrIterable
import lang.json.JsrValue

interface Draft6Schema : Draft4Schema {
  val examples: JsrIterable
  val definitions: Map<String, Schema>

  val constValue: JsrValue?
  val containsSchema: Schema?
  val propertyNameSchema: Schema?
}
