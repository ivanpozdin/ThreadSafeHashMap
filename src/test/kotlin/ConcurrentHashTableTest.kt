import kotlinx.coroutines.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.HashMap
import kotlin.system.measureTimeMillis

fun getRandomString(length: Int): String {
    val chars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
    return (1..length).map { chars.random() }.joinToString("")
}

class ConcurrentHashTableTest {
    @Test
    fun `Writing time compared to std ConcurrentHashMap`() {
        val elementsNumberToAdd = 100_000
        val coroutineCount = 100
        val stdMap = ConcurrentHashMap<Int, String>()
        val myMap = HashMap<Int, String>()
        runBlocking(Dispatchers.Default) {

            val timeStdMapWrite = measureTimeMillis {
                val tasks = List(coroutineCount) {
                    async {
                        for (i in 1..elementsNumberToAdd) {
                            stdMap[i] = getRandomString(2)
                        }
                    }
                }
                tasks.awaitAll()
            }
            println("STD ConcurrentHashMap write time = $timeStdMapWrite ms")
            val timeMyMapWrite = measureTimeMillis {
                val tasks = List(coroutineCount) {
                    async {
                        for (i in 1..elementsNumberToAdd) {
                            myMap[i] = getRandomString(2)
                        }
                    }
                }
                tasks.awaitAll()
            }
            println("MY ConcurrentHashMap write time = $timeMyMapWrite ms")
            assertTrue(timeMyMapWrite < 2 * timeStdMapWrite)
        }
    }

    @Test
    fun `Reading time compared to std ConcurrentHashMap`() {
        val elementsNumberToAdd = 100_000
        val coroutineCount = 100
        val stdMap = ConcurrentHashMap<Int, String>()
        val myMap = HashMap<Int, String>()
        runBlocking(Dispatchers.Default) {
            for (i in 1..elementsNumberToAdd) {
                stdMap[i] = getRandomString(2)
            }
            for (i in 1..elementsNumberToAdd) {
                myMap[i] = getRandomString(2)
            }

            val timeStdMapRead = measureTimeMillis {
                val tasks = List(coroutineCount) {
                    async {
                        for (i in 1..elementsNumberToAdd) {
                            stdMap[i]
                        }
                    }
                }
                tasks.awaitAll()
            }
            println("STD ConcurrentHashMap read time = $timeStdMapRead ms")
            val timeMyMapRead = measureTimeMillis {
                val tasks = List(coroutineCount) {
                    async {
                        for (i in 1..elementsNumberToAdd) {
                            myMap[i]
                        }
                    }
                }
                tasks.awaitAll()
            }
            println("MY ConcurrentHashMap read time = $timeMyMapRead ms")
            assertTrue(timeMyMapRead < 2 * timeStdMapRead)
        }
    }


    @Test
    fun `Get and Set`() {
        val map = HashMap<String, String>()
        repeat(1000) {
            val key = getRandomString(15)
            val value = getRandomString(100)
            map[key] = value
            assertEquals(value, map[key])
        }
    }

    @Test
    fun getEntries() {
        val map = HashMap<String, String>()
        val stdMap = mutableMapOf<String, String>()
        repeat(1000) {
            val key = getRandomString(15)
            val value = getRandomString(100)
            map[key] = value
            stdMap[key] = value
        }
        assertEquals(stdMap.entries, map.entries)
    }

    @Test
    fun getKeys() {
        val map = HashMap<String, String>()
        val stdMap = mutableMapOf<String, String>()
        repeat(1000) {
            val key = getRandomString(15)
            val value = getRandomString(100)
            map[key] = value
            stdMap[key] = value
        }
        assertEquals(stdMap.keys, map.keys)

    }

    @Test
    fun getValues() {
        val map = HashMap<String, String>()
        val stdMap = mutableMapOf<String, String>()
        repeat(1000) {
            val key = getRandomString(15)
            val value = getRandomString(100)
            map[key] = value
            stdMap[key] = value
        }
        assertEquals(stdMap.values.toMutableSet(), map.values.toMutableSet())
    }

    @Test
    fun getSize() {
        val map = HashMap<String, String>()
        val stdMap = mutableMapOf<String, String>()
        repeat(1000) {
            val key = getRandomString(15)
            val value = getRandomString(100)
            map[key] = value
            stdMap[key] = value
        }
        assertEquals(stdMap.size, map.size)
    }

    @Test
    fun `Size resets after clear()`() {
        val map = HashMap<String, String>()
        val stdMap = mutableMapOf<String, String>()
        repeat(1000) {
            val key = getRandomString(15)
            val value = getRandomString(100)
            map[key] = value
            stdMap[key] = value
        }
        map.clear()
        assertEquals(0, map.size)
    }


    @Test
    fun isEmpty() {
        val map = HashMap<String, String>()
        val keys = mutableListOf<String>()
        repeat(1000) {
            val key = getRandomString(15)
            keys.add(key)
            val value = getRandomString(100)
            map[key] = value
        }
        keys.forEach { key ->
            map.remove(key)
        }
        assertEquals(true, map.isEmpty())
    }

    @Test
    fun `isEmpty when map is not empty`() {
        val map = HashMap<String, String>()
        val keys = mutableListOf<String>()
        repeat(1000) {
            val key = getRandomString(15)
            keys.add(key)
            val value = getRandomString(100)
            map[key] = value
        }
        assertEquals(false, map.isEmpty())
    }

    @Test
    fun `remove key`() {
        val map = HashMap<String, String>()
        map["key"] = "hello!"
        repeat(1000) {
            val key = getRandomString(15)
            val value = getRandomString(100)
            map[key] = value
        }
        map.remove("key")
        assertEquals(false, map.containsValue("hello!"))
    }

    @Test
    fun `putAll size`() {
        val map = HashMap<String, String>()
        val stdMap = mutableMapOf<String, String>()
        repeat(1000) {
            val key = getRandomString(15)
            val value = getRandomString(100)
            stdMap[key] = value
        }
        map.putAll(stdMap)
        assertEquals(stdMap.size, map.size)
    }

    @Test
    fun `putAll keys`() {
        val map = HashMap<String, String>()
        val stdMap = mutableMapOf<String, String>()
        repeat(1000) {
            val key = getRandomString(15)
            val value = getRandomString(100)
            stdMap[key] = value
        }
        map.putAll(stdMap)
        stdMap.keys.forEach { key -> assertEquals(stdMap[key], map[key]) }
    }

    @Test
    fun put() {
        val map = HashMap<String, String>()
        repeat(1000) {
            val key = getRandomString(15)
            val value = getRandomString(100)
            map[key] = value
            assertEquals(value, map[key])
        }
    }

    @Test
    fun `doesn't contain value`() {
        val map = HashMap<String, String>()
        repeat(1000) {
            val key = getRandomString(15)
            val value = getRandomString(100)
            map[key] = value
        }
        assertEquals(false, map.containsValue("hello!"))
    }

    @Test
    fun `contains value`() {
        val map = HashMap<String, String>()
        map["key"] = "hello!"
        repeat(1000) {
            val key = getRandomString(15)
            val value = getRandomString(100)
            map[key] = value
        }
        assertEquals(true, map.containsValue("hello!"))
    }

    @Test
    fun `contains key`() {
        val map = HashMap<String, String>()
        map["key"] = "hello!"
        repeat(1000) {
            val key = getRandomString(15)
            val value = getRandomString(100)
            map[key] = value
        }
        assertEquals(true, map.containsKey("key"))
    }

    @Test
    fun `doesn't contain key`() {
        val map = HashMap<String, String>()
        map["not key"] = "hello!"
        repeat(1000) {
            val key = getRandomString(15)
            val value = getRandomString(100)
            map[key] = value
        }
        assertEquals(false, map.containsKey("key"))
    }
}