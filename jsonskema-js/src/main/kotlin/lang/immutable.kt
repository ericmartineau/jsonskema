package lang

actual fun <T> Iterable<T>.freezeList(): List<T> {
  //todo:Immutables in js?
  return this.toList()
}

actual fun <T> Iterable<T>.freezeSet(): Set<T> {
  //todo:Immutables in js?
  return this.toSet()
}

actual fun <K, V> Map<K, V>.freezeMap(): Map<K, V> {
  //todo:Immutables in js?
  return this
}
