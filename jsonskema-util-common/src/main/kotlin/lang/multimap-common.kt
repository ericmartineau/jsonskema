package lang

expect interface SetMultimap<K, V> {
  fun asMap(): Map<K, Collection<V>>
  operator fun get(key: K?): Set<V>
  fun size():Int
}

expect class MutableSetMultimap<K, V>() : SetMultimap<K, V> {
  /**
   * Weird naming due to conflicts with the name "put"
   */
  fun add(key: K?, value: V?): Boolean
  operator fun plusAssign(pair: Pair<K, V>)
  fun <K, V> freeze(): SetMultimap<K, V>
}

expect fun <K, V> SetMultimap<K, V>.toMutableSetMultimap(): MutableSetMultimap<K, V>
expect operator fun <K, V> SetMultimap<K, V>.plus(set:SetMultimap<K, V>): SetMultimap<K, V>
expect operator fun <K, V> SetMultimap<K, V>.plus(pair:Pair<K, V>): SetMultimap<K, V>

expect interface ListMultimap<K, V> {
  fun asMap(): Map<K, Collection<V>>
  operator fun get(key: K?): List<V>
  fun size():Int
}

expect class MutableListMultimap<K, V>() : ListMultimap<K, V> {
  /**
   * Weird naming due to conflicts with the name "put"
   */
  fun add(key: K?, value: V?): Boolean
  operator fun plusAssign(pair: Pair<K, V>)
  fun <K, V> freeze(): ListMultimap<K, V>
}

expect fun <K, V> ListMultimap<K, V>.toMutableListMultimap(): MutableListMultimap<K, V>

expect object Multimaps {
  fun <K, V> emptySetMultimap():SetMultimap<K, V>
  fun <K, V> emptyListMultimap():ListMultimap<K, V>
}

val <K, V> SetMultimap<K, V>.size get() = size()
val <K, V> ListMultimap<K, V>.size get() = size()

