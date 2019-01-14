package io.mverse.jsonschema.utils

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import lang.json.JsonPath
import lang.json.JsrArray
import lang.json.JsrObject
import lang.json.JsrValue
import lang.json.KtArray
import lang.json.KtObject
import lang.json.properties
import lang.json.values

fun KtObject.recurse(visit: KtVisitor) {
  val rootPath = JsonPath.rootPath
  visitObject(rootPath, visit)
}

internal fun KtArray.visitArray(path: JsonPath, visitIndex: KtVisitor) {
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

internal fun KtObject.visitObject(path: JsonPath, visitProperty: KtVisitor) {

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

typealias KtVisitor = (/*Key*/Any, /*Visited*/JsonElement, /*Location*/JsonPath) -> Unit

fun JsrObject.recurse(visit: JsrVisitor) {
  val rootPath = JsonPath.rootPath
  visitObject(rootPath, visit)
}

internal fun JsrArray.visitArray(path: JsonPath, visitIndex: JsrVisitor) {
  var idx: Int = 0
  this.values.forEach { v ->
    val currIdx = idx++
    visitIndex(currIdx, this, path)
    when (v) {
      is JsrObject -> v.visitObject(path.child(currIdx), visitIndex)
      is JsrArray -> v.visitArray(path.child(currIdx), visitIndex)
      else -> {
      }
    }
  }
}

internal fun JsrObject.visitObject(path: JsonPath, visitProperty: JsrVisitor) {
  properties.forEach { (k, v) ->
    visitProperty(k, v, path)
    when (v) {
      is JsrObject -> v.visitObject(path.child(k), visitProperty)
      is JsrArray -> v.visitArray(path.child(k), visitProperty)
      else -> {
      }
    }
  }
}

typealias JsrVisitor = (/*Key*/Any, /*Visited*/JsrValue, /*Location*/JsonPath) -> Unit