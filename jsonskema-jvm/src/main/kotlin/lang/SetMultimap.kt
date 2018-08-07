package lang

actual open class SetMultimap<K, V> actual constructor(protected open val internal: Map<K, Set<V>>) {
  actual fun asMap(): Map<K, Set<V>> = internal

  actual operator fun get(key: K): Set<V> {
    return internal.getOrElse(key) { hashSetOf() }
  }

  actual fun toMutableSetMultimap(): MutableSetMultimap<K, V> = MutableSetMultimap(internal
      .mapValues { it.value.toHashSet() }
      .toMutableMap())
}

actual open class MutableSetMultimap<K, V> actual constructor(override val internal: MutableMap<K, HashSet<V>>)
  : SetMultimap<K, V>(internal) {

  actual fun put(key: K, value: V): Boolean {
    return internal.getOrPut(key) { HashSet() }.add(value)
  }

  actual operator fun plusAssign(pair: Pair<K, V>) {
    put(pair.first, pair.second)
  }
}
