package lang

expect open class SetMultimap<K, V>(internal: Map<K, Set<V>> =  mapOf()) {
  fun asMap(): Map<K, Set<V>>
  operator fun get(key: K): Set<V>
  fun toMutableSetMultimap(): MutableSetMultimap<K, V>
}

expect open class MutableSetMultimap<K, V>(internal: MutableMap<K, HashSet<V>> =  mutableMapOf()) : SetMultimap<K, V> {
  fun put(key: K, value: V): Boolean
  operator fun plusAssign(pair: Pair<K, V>)
}
