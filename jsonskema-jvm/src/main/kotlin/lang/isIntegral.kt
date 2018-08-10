package lang

import com.google.common.hash.HashCode
import com.google.common.math.DoubleMath
import java.math.BigDecimal

actual fun Number.isIntegral(): Boolean {
  return DoubleMath.isMathematicalInteger(this.toDouble())
}

actual fun Number.toHex(): String = HashCode.fromInt(toInt()).toString()

actual fun Number.isDivisibleBy(op: Number, precision:Int): Boolean {
  val mod = this.toDouble().toBigDecimal() % op.toDouble().toBigDecimal()
  return mod.compareTo(BigDecimal.ZERO) == 0
}
