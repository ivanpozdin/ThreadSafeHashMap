import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

class HashMap<K, V>(initialSize: Int = 100) : MutableMap<K, V> {
    companion object Factory {
        const val RESIZE_FACTOR = 3
    }

    class KeyValue<K, V>(key: K, value: V) : MutableMap.MutableEntry<K, V> {
        override var key: K private set
        override var value: V

        init {
            this.key = key
            this.value = value
        }

        override fun setValue(newValue: V): V {
            val oldValue = value
            value = newValue
            return oldValue
        }
    }

    private val lock = ReentrantReadWriteLock()
    private var elementsNumber: Int = 0
    private var capacity: Int = initialSize
    private var map: List<MutableList<KeyValue<K, V>>>
    private val needResize: Boolean get() = elementsNumber == capacity
    private val keysPrivate = mutableSetOf<K>()

    init {
        map = List(capacity) { mutableListOf() }
    }

    override operator fun get(key: K): V? {
        return findKeyValue(key)?.value
    }

    operator fun set(key: K, value: V) {
        put(key, value)
    }

    private fun findKeyValue(key: K): KeyValue<K, V>? = lock.read {
        return map[getIndexOf(key)].find { keyValue -> keyValue.key == key }
    }

    private fun getIndexOf(key: K): Int =lock.read {
        return Math.floorMod(key.hashCode(), capacity)
    }

    private fun resize() = lock.write {
        val oldMap = map
        capacity *= RESIZE_FACTOR
        map = List(capacity) { mutableListOf() }
        elementsNumber = 0
        oldMap.forEach { list ->
            for (keyValue in list) {
                this[keyValue.key] = keyValue.value
            }
        }
    }


    fun printMap() = lock.read {
        for (list in map) {
            for (keyValue in list) {
                print("${keyValue.key} ${keyValue.value} -->  ")
            }
            println()
        }
    }

    override val entries: MutableSet<MutableMap.MutableEntry<K, V>>
        get() = lock.read {
            keysPrivate.map { key ->
                findKeyValue(key)!!
            }.toMutableSet()
        }

    override val keys
        get() = lock.read {keysPrivate.toMutableSet()}
    override val values: MutableCollection<V>
        get() = lock.read {keysPrivate.map { key ->
            this[key]!!
        }.toMutableSet()}

    override val size: Int
        get() = lock.read {elementsNumber}

    override fun clear() = lock.write {
        map = map.map { mutableListOf() }
        elementsNumber = 0
    }

    override fun isEmpty(): Boolean  = lock.read {
        return elementsNumber == 0
    }

    override fun remove(key: K): V?  = lock.write {
        val entriesWithEqualHash = map[getIndexOf(key)]
        val index = entriesWithEqualHash.indexOfFirst { keyValue -> keyValue.key == key }
        if (index == -1) return null
        val entry = entriesWithEqualHash[index]
        entriesWithEqualHash.removeAt(index)
        elementsNumber--
        keysPrivate.remove(key)
        return entry.value
    }

    override fun putAll(from: Map<out K, V>)  = lock.write {
        from.entries.forEach { entry ->
            this[entry.key] = entry.value
        }
    }

    override fun put(key: K, value: V): V?  = lock.write {
        val keyValue = findKeyValue(key)
        if (keyValue == null) {
            val index = getIndexOf(key)
            map[index].add(KeyValue(key, value))
            elementsNumber++
            keysPrivate.add(key)
            if (needResize) resize()
            return null
        }
        val oldValue = keyValue.value
        keyValue.value = value
        return oldValue
    }

    override fun containsValue(value: V): Boolean = lock.read {
        return this.values.contains(value)
    }

    override fun containsKey(key: K): Boolean = lock.read {
        return findKeyValue(key) != null
    }

}