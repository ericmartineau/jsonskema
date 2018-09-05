package lang

import kotlin.js.Math
import kotlin.math.floor

actual fun Number.isIntegral(): Boolean {
  return floor(this.toDouble()) == this
}

actual fun Number.isDivisibleBy(op: Number, precision: Int): Boolean {
  return op.toDouble() % this.toDouble() == 0.0
}

actual fun Number.toHex(): String {
  //todo:Implement
  return this.toString()
}
