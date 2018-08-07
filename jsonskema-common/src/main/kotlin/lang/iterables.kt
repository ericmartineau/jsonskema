package lang

fun <X> Iterable<X>.runLengths():Iterable<Pair<X, Int>> {
  var x:X? = null
  var count:Int = 0

  val result = mutableListOf<Pair<X, Int>>()
  for (i in this) {
    if (x != i) {
      if (x != null) {
        result += x to count
      }
      count = 0
      x = i
    }

    count++
  }
  if (x != null) {
    result += x to count
  }
  return result
}
