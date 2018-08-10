package lang

import com.google.common.hash.HashCode
import com.google.common.math.DoubleMath
import kotlinx.serialization.json.ElementType
import kotlinx.serialization.json.ElementType.*
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.content
import kotlinx.serialization.json.doubleOrNull
import java.math.BigDecimal

actual fun Number.isIntegral(): Boolean {
  return DoubleMath.isMathematicalInteger(this.toDouble())
}

actual fun Number.toHex(): String = HashCode.fromInt(toInt()).toString()

