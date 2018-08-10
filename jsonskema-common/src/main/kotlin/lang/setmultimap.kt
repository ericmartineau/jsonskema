package lang

expect open class SetMultimap<K, V>(internal: Map<K, Set<V>> =  mapOf()) {
  fun asMap(): Map<K, Set<V>>
  operator fun get(key: K): Set<V>
  fun toMutableSetMultimap(): MutableSetMultimap<K, V>
}

expect open class MutableSetMultimap<K, V>(internal: MutableMap<K, HashSet<V>> =  mutableMapOf()) : SetMultimap<K, V> {
  fun put(key: K, value: V): Boolean
  operator fun plusAssign(pair: Pair<K, V>)
  fun toSetMultimap(): SetMultimap<K, V>
}

expect open class ListMultimap<K, V>(internal: Map<K, List<V>> =  mapOf()) {
  fun asMap(): Map<K, List<V>>
  operator fun get(key: K): List<V>
  fun toMutableListMultimap(): MutableListMultimap<K, V>
}

expect open class MutableListMultimap<K, V>(internal: MutableMap<K, ArrayList<V>> =  mutableMapOf()) : ListMultimap<K, V> {
  fun put(key: K, value: V): Boolean
  operator fun plusAssign(pair: Pair<K, V>)
  fun toListMultimap(): ListMultimap<K, V>
}
