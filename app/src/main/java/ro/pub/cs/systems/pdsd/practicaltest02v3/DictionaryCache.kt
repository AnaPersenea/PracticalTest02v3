package ro.pub.cs.systems.pdsd.practicaltest02v3

import java.util.concurrent.ConcurrentHashMap

class DictionaryCache {
    private val map = ConcurrentHashMap<String, DictionaryData>()

    fun get(word: String): DictionaryData? = map[word.lowercase().trim()]
    fun put(word: String, data: DictionaryData) { map[word.lowercase().trim()] = data }
}