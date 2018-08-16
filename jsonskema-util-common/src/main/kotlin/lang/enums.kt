package lang

fun <E : Enum<E>> Array<E>.range(start: E, end: E):Set<E> {
  return this.filter { it in start..end }.toHashSet()
}
