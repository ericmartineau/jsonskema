package lang.json

import kotlinx.serialization.KOutput
import kotlinx.serialization.KSerialSaver
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

class JsonSaver(spaces: Boolean = false,
                private val space: String = if (spaces) " " else "",
                private val tab: String = if (spaces) "  " else "",
                private val newline: String = if (spaces) "\n" else "") {

  private fun StringBuilder.write(input:String) = this.append(input)

  fun serialize(obj: JsonElement): String {
    return save(StringBuilder(), obj)
  }

  fun save(output: StringBuilder, obj: JsonElement): String {
    when (obj) {
      is kotlinx.serialization.json.JsonObject -> writeObject(output, obj, 0)
      is kotlinx.serialization.json.JsonArray -> writeArray(output, obj, 0)
      is kotlinx.serialization.json.JsonPrimitive -> output.write(obj.content)
    }
    return output.toString()
  }

  fun save(output: StringBuilder, obj: JsonElement, indent: Int) {
    when (obj) {
      is kotlinx.serialization.json.JsonObject -> writeObject(output, obj, indent)
      is kotlinx.serialization.json.JsonArray -> writeArray(output, obj, indent)
      is kotlinx.serialization.json.JsonPrimitive -> output.write(obj.toString())
    }
  }

  private fun writeArray(output: StringBuilder, obj: kotlinx.serialization.json.JsonArray, indent: Int) {
    output.write("[")
    val iterator = obj.iterator()
    while (iterator.hasNext()) {
      output.write(newline + (tab.repeat(indent + 1)))
      val value = iterator.next()
      save(output, value, indent + 1)
      if (iterator.hasNext()) {
        output.write(",$space")
      }
    }
    output.write("]")
  }

  private fun writeObject(output: StringBuilder, obj: kotlinx.serialization.json.JsonObject, indent: Int) {
    output.write("{")
    val iterator = obj.entries.iterator()
    while (iterator.hasNext()) {
      output.write(newline + (tab.repeat(indent + 1)))
      val (key, value) = iterator.next()
      output.write("\"$key\":$space")
      save(output, value, indent + 1)
      if (iterator.hasNext()) {
        output.write(",$space")
      }
    }
    output.write("}")
  }
}

