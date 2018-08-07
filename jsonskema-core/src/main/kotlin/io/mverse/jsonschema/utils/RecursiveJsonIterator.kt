package io.mverse.jsonschema.utils

import io.mverse.jsonschema.JsonPath
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import lang.json.ValueType

object RecursiveJsonIterator {
  fun visitDocument(obj: JsonObject, iterator: Visitor) {
    val rootPath = JsonPath.rootPath()
    visitObject(obj, rootPath, iterator)
  }

  internal fun visitArray(array: JsonArray, path: JsonPath, iterator: Visitor) {
    var idx: Int = 0
    array.forEach { v ->
      val currIdx = idx++
      iterator.visitProperty(currIdx, array, path)
      // iterator.visitArrayElement(currIdx, v, path);
      if (v.valueType === ValueType.OBJECT) {
        visitObject(v.jsonObject, path.child(currIdx), iterator)
      } else if (v.valueType === ValueType.ARRAY) {
        visitArray(v.jsonArray, path.child(currIdx), iterator)
      }
    }
  }

  internal fun visitObject(obj: JsonObject, path: JsonPath, iterator: Visitor) {
    obj.content.forEach { (k, v) ->
      iterator.visitProperty(k, v, path)
      if (v.valueType === ValueType.OBJECT) {
        visitObject(v.jsonObject, path.child(k), iterator)
      } else if (v.valueType === ValueType.ARRAY) {
        visitArray(v.jsonArray, path.child(k), iterator)
      }
    }
  }

  interface Visitor {
    fun visitProperty(key: Any, value: JsonElement, path: JsonPath)
  }
}
