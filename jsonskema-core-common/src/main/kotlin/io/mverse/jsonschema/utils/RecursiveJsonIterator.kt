package io.mverse.jsonschema.utils

import io.mverse.jsonschema.JsonPath
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.ElementType

fun kotlinx.serialization.json.JsonObject.recurse(visit: Visitor) {
  val rootPath = JsonPath.rootPath()
  visitObject(rootPath, visit)
}

internal fun kotlinx.serialization.json.JsonArray.visitArray(path: JsonPath, visitIndex: Visitor) {
  var idx: Int = 0
  this.forEach { v ->
    val currIdx = idx++
    visitIndex(currIdx, this, path)
    when(v) {
      is kotlinx.serialization.json.JsonObject -> v.visitObject(path.child(currIdx), visitIndex)
      is kotlinx.serialization.json.JsonArray -> v.visitArray(path.child(currIdx), visitIndex)
    }
  }
}

internal fun kotlinx.serialization.json.JsonObject.visitObject(path: JsonPath, visitProperty: Visitor) {

  content.forEach { (k, v) ->
    visitProperty(k, v, path)
    when(v) {
      is kotlinx.serialization.json.JsonObject -> v.visitObject(path.child(k), visitProperty)
      is kotlinx.serialization.json.JsonArray -> v.visitArray(path.child(k), visitProperty)
    }
  }
}

typealias Visitor = (/*Key*/Any, /*Visited*/JsonElement, /*Location*/JsonPath) -> Unit
