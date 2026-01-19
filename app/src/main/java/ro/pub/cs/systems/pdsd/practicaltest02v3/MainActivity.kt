package ro.pub.cs.systems.pdsd.practicaltest02v3

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Socket

class MainActivity : AppCompatActivity() {

    private var serverThread: DictionaryServerThread? = null
    private val cache = DictionaryCache()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Elemente UI Server
        val serverPortEditText = findViewById<EditText>(R.id.serverPortEditText)
        val startServerButton = findViewById<Button>(R.id.startServerButton)
        val stopServerButton = findViewById<Button>(R.id.stopServerButton)
        val serverStatusTextView = findViewById<TextView>(R.id.serverStatusTextView)

        // Elemente UI Client
        val serverAddressEditText = findViewById<EditText>(R.id.serverAddressEditText)
        val serverPortClientEditText = findViewById<EditText>(R.id.serverPortClientEditText)
        val queryEditText = findViewById<EditText>(R.id.queryEditText)
        val sendRequestButton = findViewById<Button>(R.id.sendRequestButton)
        val resultTextView = findViewById<TextView>(R.id.resultTextView)

        // Logica Server
        startServerButton.setOnClickListener {
            val port = serverPortEditText.text.toString().toIntOrNull()
            if (port != null) {
                serverThread = DictionaryServerThread(port, cache)
                serverThread?.start()
                serverStatusTextView.text = "Server status: ON (Port $port)"
                Log.d("EIM_DEBUG", "Server started on port $port")
            } else {
                serverStatusTextView.text = "Invalid port!"
            }
        }

        stopServerButton.setOnClickListener {
            serverThread?.stopServer()
            serverThread = null
            serverStatusTextView.text = "Server status: OFF"
            Log.d("EIM_DEBUG", "Server stopped")
        }

        // Logica Client
        sendRequestButton.setOnClickListener {
            val host = serverAddressEditText.text.toString()
            val port = serverPortClientEditText.text.toString().toIntOrNull()
            val prefix = queryEditText.text.toString()

            if (host.isEmpty() || port == null || prefix.isEmpty()) {
                resultTextView.text = "Please fill all client fields"
                return@setOnClickListener
            }

            // Ștergem rezultatul anterior
            resultTextView.text = "Fetching suggestions..."

            // Operațiunile de rețea trebuie să ruleze pe un thread separat
            Thread {
                try {
                    val socket = Socket(host, port)
                    val out = PrintWriter(socket.getOutputStream(), true)
                    val reader = BufferedReader(InputStreamReader(socket.getInputStream()))

                    // Trimitem prefixul către serverul nostru local
                    out.println(prefix)
                    out.flush()

                    // Citim răspunsul (lista de sugestii primită de la server)
                    val response = reader.readLine()

                    socket.close()

                    // Actualizăm UI-ul pe thread-ul principal
                    runOnUiThread {
                        resultTextView.text = response ?: "No response from server"
                    }

                } catch (e: Exception) {
                    Log.e("EIM_DEBUG", "Client error: ${e.message}")
                    runOnUiThread {
                        resultTextView.text = "Error: ${e.message}"
                    }
                }
            }.start()
        }
    }

    override fun onDestroy() {
        serverThread?.stopServer()
        super.onDestroy()
    }
}