package lang

expect fun <T> Iterable<T>.freezeList(): List<T>
expect fun <T> Iterable<T>.freezeSet(): Set<T>
expect fun <K, V> Map<K, V>.freezeMap(): Map<K, V>
