package lang

expect class Random() {
  fun nextDouble():Double
  fun nextInt(range:IntRange = 0..Int.MAX_VALUE): Int
}

