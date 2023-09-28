class HashMap<K, V>(initialSize: Int = 100) : MutableMap<K, V> {
    companion object Factory {
        const val RESIZE_FACTOR = 3
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

    private fun findKeyValue(key: K): KeyValue<K, V>? {
        return map[getIndexOf(key)].find { keyValue -> keyValue.key == key }
    }

    private fun getIndexOf(key: K): Int {
        return Math.floorMod(key.hashCode(), capacity)
    }

    private fun resize() {
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

    fun printMap() {
        for (list in map) {
            for (keyValue in list) {
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
        get() = elementsNumber

    override fun clear() {
        map = map.map { mutableListOf() }
        elementsNumber = 0
    }

    override fun isEmpty(): Boolean {
        print(elementsNumber)
        return elementsNumber == 0
    }

    override fun remove(key: K): V? {
        val entriesWithEqualHash = map[getIndexOf(key)]
        val index = entriesWithEqualHash.indexOfFirst { keyValue -> keyValue.key == key }
        if (index == -1) return null
        val entry = entriesWithEqualHash[index]
        entriesWithEqualHash.removeAt(index)
        elementsNumber--
        keysPrivate.remove(key)
        return entry.value
    }

    override fun putAll(from: Map<out K, V>) {
        from.entries.forEach { entry ->
            this[entry.key] = entry.value
        }
    }

    override fun put(key: K, value: V): V? {
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

    override fun containsValue(value: V): Boolean {
        return this.values.contains(value)
    }

    override fun containsKey(key: K): Boolean {
        return findKeyValue(key) != null
    }

}