package lang.json

import io.mverse.jsonschema.JsonValueWithPath

actual interface JsrValue
actual interface JsrStructure:JsrValue

actual interface JsrObject:JsrStructure {
  val map:Map<String, JsrValue>
}
actual val JsrObject.properties: Set<Map.Entry<String, JsrValue>> get() = this.map.entries

actual interface JsrArray : JsrStructure {
  val list:List<JsrValue>
}
actual val JsrArray.values:List<JsrValue> get() = this.list

actual interface JsrNumber:JsrValue {
  val num:Number
}
actual interface JsrString:JsrValue {
  val str:String
}

actual val JsrString.stringValue:String get() = this.str
actual val JsrNumber.numberValue:Number get() = this.num

actual val JsrTrue:JsrValue = object:JsrValue{}
actual val JsrFalse:JsrValue = object:JsrValue{}
actual val JsrNull:JsrValue = object:JsrValue{}

actual fun createJsrObject(values:Map<String, JsrValue>):JsrObject {
  return JsrObjectImpl(values)
}
actual fun createJsrArray(values:Iterable<JsrValue>):JsrArray {
  return JsrArrayImpl(values.toList())
}

actual fun createJsrString(string:String):JsrString = JsrStringImpl(string)
actual fun createJsrNumber(int:Int):JsrNumber = JsrNumberImpl(int)
actual fun createJsrNumber(double:Double):JsrNumber = JsrNumberImpl(double)
actual fun createJsrNumber(long:Long):JsrNumber = JsrNumberImpl(long)

data class JsrObjectImpl(override val map:Map<String, JsrValue>):JsrObject
data class JsrArrayImpl(override val list:List<JsrValue>):JsrArray
data class JsrStringImpl(override val str:String):JsrString
data class JsrNumberImpl(override val num:Number):JsrNumber

