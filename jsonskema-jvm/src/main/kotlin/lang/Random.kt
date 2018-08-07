package lang

import java.util.Random

actual class Random(val jrandom:java.util.Random = java.util.Random(System.currentTimeMillis())) {
  actual constructor(): this(Random())
  actual fun nextDouble(): Double {
    return jrandom.nextDouble()
  }

  actual fun nextInt(range:IntRange): Int {
    return jrandom.nextInt(range.count()) + range.first
  }
}
