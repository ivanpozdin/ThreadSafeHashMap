import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference

class HashMap<K, V>(initialSize: Int = 100) : MutableMap<K, V> {
    companion object Factory {
        const val RESIZE_FACTOR = 3
        const val MAX_LOAD_FACTOR = 0.75
    }

    inner class KeyValue<K, V>(key: K, value: V) : MutableMap.MutableEntry<K, V> {
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

    private var elementsNumber: AtomicInteger = AtomicInteger(0)
    private var capacity: Int = initialSize
    private var map: MutableList<AtomicReference<List<KeyValue<K, V>>>>
    private val needResize: Boolean get() = elementsNumber.get() >= MAX_LOAD_FACTOR * capacity
    private val keysPrivate = mutableSetOf<K>()

    init {
        map = MutableList(capacity) { AtomicReference(listOf()) }
    }

    override operator fun get(key: K): V? {
        return findKeyValue(key)?.value
    }

    operator fun set(key: K, value: V) {
        put(key, value)
    }

    private fun findKeyValue(key: K): KeyValue<K, V>? {
        val elementsWithEqualHash = map[getIndexOf(key)].get()
        return elementsWithEqualHash.find { keyValue -> keyValue.key == key }
    }

    private fun getIndexOf(key: K): Int {
        return Math.floorMod(key.hashCode(), capacity)
    }

    @Synchronized
    private fun resize() {
        val oldMap = map
        capacity *= RESIZE_FACTOR
        map = MutableList(capacity) { AtomicReference(listOf()) }
        elementsNumber.set(0)
        oldMap.forEach { list ->
            for (keyValue in list.get()) {
                this[keyValue.key] = keyValue.value
            }
        }
    }

    fun printMap() {
        for (list in map) {
            for (keyValue in list.get()) {
                print("${keyValue.key} ${keyValue.value} -->  ")
            }
            println()
        }
    }

    override val entries: MutableSet<MutableMap.MutableEntry<K, V>>
        get() = keysPrivate.map { key ->
            findKeyValue(key)!!
        }.toMutableSet()

    override val keys
        get() = keysPrivate.toMutableSet()
    override val values: MutableCollection<V>
        get() = keysPrivate.map { key ->
            this[key]!!
        }.toMutableSet()

    override val size: Int
        get() = elementsNumber.get()

    @Synchronized
    override fun clear() {
        map = MutableList(capacity) { AtomicReference(listOf()) }
        elementsNumber.set(0)
    }

    override fun isEmpty(): Boolean {
        print(elementsNumber)
        return elementsNumber.get() == 0
    }

    override fun remove(key: K): V? {
        while (true) {
            val entriesWithEqualHash = map[getIndexOf(key)].get()
            val entriesWithEqualHashMutable = entriesWithEqualHash.toMutableList()
            val index = entriesWithEqualHashMutable.indexOfFirst { keyValue -> keyValue.key == key }
            val oldValue = if (index == -1) {
                null
            } else {
                val entry = entriesWithEqualHashMutable[index]
                entriesWithEqualHashMutable.removeAt(index)
                entry.value
            }
            val updatedEntriesWithEqualHash = entriesWithEqualHashMutable.toList()
            if (map[getIndexOf(key)].compareAndSet(entriesWithEqualHash, updatedEntriesWithEqualHash)) {
                if (keysPrivate.remove(key)) {
                    elementsNumber.decrementAndGet()
                }
                return oldValue
            }

        }
    }

    override fun putAll(from: Map<out K, V>) {
        from.entries.forEach { entry ->
            this[entry.key] = entry.value
        }
    }

    override fun put(key: K, value: V): V? {
        while (true) {
            val entriesWithEqualHash = map[getIndexOf(key)].get()
            val entriesWithEqualHashMutable = entriesWithEqualHash.toMutableList()
            val keyValue = entriesWithEqualHashMutable.find { keyValue -> keyValue.key == key }
            val oldValue = if (keyValue == null) {
                entriesWithEqualHashMutable.add(KeyValue(key, value))
                elementsNumber.incrementAndGet()
                null
            } else {
                val oldValue = keyValue.value
                keyValue.value = value
                oldValue
            }
            val updatedEntriesWithEqualHash = entriesWithEqualHashMutable.toList()
            if (map[getIndexOf(key)].compareAndSet(entriesWithEqualHash, updatedEntriesWithEqualHash)) {
                keysPrivate.add(key)
                if (needResize) resize()
                return oldValue
            }
        }
    }

    override fun containsValue(value: V): Boolean {
        return this.values.contains(value)
    }

    override fun containsKey(key: K): Boolean {
        return findKeyValue(key) != null
    }

}