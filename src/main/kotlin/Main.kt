fun getRandomString(length: Int): String {
    val chars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
    return (1..length).map { chars.random() }.joinToString("")
}

fun main(args: Array<String>) {
    val map = HashMap<String, String>()
    repeat(1000) {
        val key = getRandomString(15)
        val value = getRandomString(100)
        map[key] = value
    }
}