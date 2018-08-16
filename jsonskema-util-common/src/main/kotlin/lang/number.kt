package lang

import kotlinx.serialization.json.JsonElement

expect fun Number.isIntegral():Boolean
expect fun Number.toHex():String
expect fun Number.isDivisibleBy(op:Number, precision:Int = 12):Boolean
