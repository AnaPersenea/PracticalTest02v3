package ro.pub.cs.systems.pdsd.practicaltest02v3

import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import android.util.Log

class DictionaryClient {
    private val client = OkHttpClient()
    private val TAG = "EIM_DEFINITION"

    fun fetch(word: String): DictionaryData {
        val url = "https://api.dictionaryapi.dev/api/v2/entries/en/$word"
        val request = Request.Builder().url(url).build()

//        Log.d(TAG, "HELP")
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw Exception("HTTP Error ${response.code}")

            val body = response.body?.string() ?: ""
            val jsonArray = JSONArray(body)
            Log.d(TAG, "Primirea informatiilor:\n$jsonArray")


            val entry0 = jsonArray.getJSONObject(0)
            Log.d(TAG, "entry 0:\n$entry0")
            val meanings0 = entry0.getJSONArray("meanings").getJSONObject(0)
            Log.d(TAG, "meanings0:\n$meanings0")
            val def0 = meanings0.getJSONArray("definitions").getJSONObject(0)
            Log.d(TAG, "def0:\n$def0")
            val definition = def0.getString("definition")
            Log.d(TAG, "Raspuns parsat:\n$definition")

            val list = mutableListOf<String>()
            list.add(definition)
            return DictionaryData(list)
        }
    }
}