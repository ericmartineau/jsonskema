package lang.json

expect interface JsrValue
expect interface JsrStructure:JsrValue

expect interface JsrObject:JsrStructure
expect val JsrObject.properties: Set<Map.Entry<String, JsrValue>>

expect interface JsrArray : JsrStructure
expect val JsrArray.values:List<JsrValue>
expect interface JsrNumber:JsrValue
expect interface JsrString:JsrValue

expect val JsrString.stringValue:String
expect val JsrNumber.numberValue:Number

expect val JsrTrue:JsrValue
expect val JsrFalse:JsrValue
expect val JsrNull:JsrValue

expect fun createJsrObject(values:Map<String, JsrValue>):JsrObject
expect fun createJsrArray(values:Iterable<JsrValue>):JsrArray
expect fun createJsrString(string:String):JsrString
expect fun createJsrNumber(int:Int):JsrNumber
expect fun createJsrNumber(double:Double):JsrNumber
expect fun createJsrNumber(long:Long):JsrNumber

