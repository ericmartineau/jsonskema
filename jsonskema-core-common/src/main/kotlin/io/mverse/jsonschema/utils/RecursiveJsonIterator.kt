package io.mverse.jsonschema.utils

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import lang.json.JsonPath
import lang.json.KtArray
import lang.json.KtObject

fun KtObject.recurse(visit: Visitor) {
  val rootPath = JsonPath.rootPath
  visitObject(rootPath, visit)
}

internal fun KtArray.visitArray(path: JsonPath, visitIndex: Visitor) {
  var idx: Int = 0
  this.forEach { v ->
    val currIdx = idx++
    visitIndex(currIdx, this, path)
    when (v) {
      is JsonObject -> v.visitObject(path.child(currIdx), visitIndex)
      is JsonArray -> v.visitArray(path.child(currIdx), visitIndex)
      else -> {
      }
    }
  }
}

internal fun KtObject.visitObject(path: JsonPath, visitProperty: Visitor) {

  content.forEach { (k, v) ->
    visitProperty(k, v, path)
    when (v) {
      is JsonObject -> v.visitObject(path.child(k), visitProperty)
      is JsonArray -> v.visitArray(path.child(k), visitProperty)
      else -> {
      }
    }
  }
}

typealias Visitor = (/*Key*/Any, /*Visited*/JsonElement, /*Location*/JsonPath) -> Unit
