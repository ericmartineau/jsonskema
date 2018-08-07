package lang

import com.google.common.hash.HashCode
import java.math.BigDecimal

actual fun Number.isIntegral(): Boolean {
  return BigDecimal.valueOf(this.toDouble()).scale()==0
}

actual fun Number.toHex(): String = HashCode.fromInt(toInt()).toString()
