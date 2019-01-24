package lang

import kotlin.js.Math.random
import kotlin.math.roundToInt

actual class Random actual constructor() {

  actual fun nextDouble(): Double = random()

  actual fun nextInt(range: IntRange): Int = random()
      .times(range.count())
      .roundToInt()
      .plus(range.first)
}
