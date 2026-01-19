package ro.pub.cs.systems.pdsd.practicaltest02v3

import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.ServerSocket
import java.net.Socket

class DictionaryServerThread(
    private val port: Int,
    private val cache: DictionaryCache
) : Thread() {

    private var serverSocket: ServerSocket? = null
    private val api = DictionaryClient()

    override fun run() {
        try {
            serverSocket = ServerSocket(port)

            while (!isInterrupted) {
                val clientSocket = serverSocket?.accept() ?: break
                ClientHandlerThread(clientSocket, cache, api).start()
            }
        } catch (_: Exception) {
            // server stopped or error
        } finally {
            try { serverSocket?.close() } catch (_: Exception) {}
        }
    }

    fun stopServer() {
        interrupt()
        try { serverSocket?.close() } catch (_: Exception) {}
    }
}

private class ClientHandlerThread(
    private val socket: Socket,
    private val cache: DictionaryCache,
    private val api: DictionaryClient
) : Thread() {

    override fun run() {
        socket.use { s ->
            try {
                val reader = BufferedReader(InputStreamReader(s.getInputStream()))
                val writer = PrintWriter(s.getOutputStream(), true)

                // 1) Primește cuvantul de la client
                val word = reader.readLine()?.trim()
                if (word.isNullOrBlank()) {
                    writer.println("ERROR: empty word")
                    return
                }

                val data = cache.get(word) ?: run {
                    try {
                        val fresh = api.fetch(word)
                        cache.put(word, fresh)
                        fresh
                    } catch (e: Exception) {
                        writer.println("ERROR: Could not fetch suggestions for '$word'")
                        return
                    }
                }

                // 3) Construiește răspunsul și îl trimite
                // Transformăm lista de sugestii într-un singur String separat prin virgulă
                if (data.definitions.isEmpty()) {
                    writer.println("No suggestions found for '$word'")
                } else {
                    val response = data.definitions.joinToString(", ")
                    writer.println(response)
                }

                writer.flush()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
