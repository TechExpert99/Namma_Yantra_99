package com.nayak.nammayantara.ui.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

data class ChatMessage(
    val text: String,
    val isUser: Boolean
)

class ChatViewModel : ViewModel() {

    private val apiKey = "AIzaSyB0ksc6KCSuGTaqNjjTpTX85mVWmigTOkw"

    // ✅ FIXED: using gemini-2.0-flash with v1beta
    private val apiUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=$apiKey"

    val messages = mutableStateListOf<ChatMessage>()
    var isLoading by mutableStateOf(false)

    private val conversationHistory = mutableListOf<JSONObject>()

    private val systemContext = """
        You are an AI assistant for Namma Yantra Share — an agricultural equipment 
        rental app in Karnataka, India. You help small farmers rent tractors, 
        harvesters and sprayers from equipment owners.
        Equipment: Tractors (Mahindra 575 DI - 350Rs/hr, John Deere 5050D - 400Rs/hr), 
        Harvesters (Kubota - 600Rs/hr), Sprayers (VST Shakti - 200Rs/hr).
        Daily rates are 8x hourly rate approx.
        How to book: Browse, tap machine, select hours/days, Send Request, owner approves.
        Crops in Karnataka: Paddy, Ragi, Sugarcane, Cotton, Maize.
        Best machines: Paddy/wheat needs harvester, ploughing needs tractor, pest control needs sprayer.
        Keep answers short, simple and helpful. Reply in Kannada or Hindi if asked in those languages.
    """.trimIndent()

    init {
        messages.add(
            ChatMessage(
                text = "🌾 Namaskara! I'm your farming assistant. Ask me anything about renting tractors, harvesters or sprayers near you!",
                isUser = false
            )
        )
    }

    fun sendMessage(userMessage: String) {
        if (userMessage.isBlank()) return

        messages.add(ChatMessage(text = userMessage, isUser = true))
        isLoading = true

        conversationHistory.add(JSONObject().apply {
            put("role", "user")
            put("parts", JSONArray().put(JSONObject().put("text", userMessage)))
        })

        viewModelScope.launch {
            try {
                val reply = callGeminiApi()
                messages.add(ChatMessage(text = reply, isUser = false))
                conversationHistory.add(JSONObject().apply {
                    put("role", "model")
                    put("parts", JSONArray().put(JSONObject().put("text", reply)))
                })
            } catch (e: Exception) {
                android.util.Log.e("ChatError", "Error: ${e.message}", e)
                messages.add(
                    ChatMessage(
                        text = "Sorry, couldn't connect. Check your internet and try again.",
                        isUser = false
                    )
                )
            }
            isLoading = false
        }
    }

    private suspend fun callGeminiApi(): String {
        return withContext(Dispatchers.IO) {
            val url = URL(apiUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.doOutput = true
            connection.connectTimeout = 15000
            connection.readTimeout = 15000

            val contents = JSONArray()

            // System context
            contents.put(JSONObject().apply {
                put("role", "user")
                put("parts", JSONArray().put(JSONObject().put("text", systemContext)))
            })
            contents.put(JSONObject().apply {
                put("role", "model")
                put("parts", JSONArray().put(JSONObject().put("text", "Understood! I am ready to help farmers on Namma Yantra Share.")))
            })

            // Last 6 messages for context
            val historyToSend = if (conversationHistory.size > 6)
                conversationHistory.takeLast(6) else conversationHistory
            for (msg in historyToSend) {
                contents.put(msg)
            }

            val requestBody = JSONObject().apply {
                put("contents", contents)
            }

            val writer = OutputStreamWriter(connection.outputStream)
            writer.write(requestBody.toString())
            writer.flush()
            writer.close()

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val response = StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    response.append(line)
                }
                reader.close()
                val jsonResponse = JSONObject(response.toString())
                val candidates = jsonResponse.getJSONArray("candidates")
                val content = candidates.getJSONObject(0).getJSONObject("content")
                val parts = content.getJSONArray("parts")
                parts.getJSONObject(0).getString("text")
            } else {
                val reader = BufferedReader(InputStreamReader(connection.errorStream))
                val error = reader.readText()
                reader.close()
                android.util.Log.e("ChatError", "HTTP $responseCode: $error")
                "Sorry, server error ($responseCode). Please try again."
            }
        }
    }
}