package com.example.finance_tracker.core.websockets

import com.example.finance_tracker.core.network.model.notification.NotificationDTO
import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.util.concurrent.TimeUnit

class NotificationWebSocketClient(
    private val userId: Long,
    private val onNotificationReceived: (NotificationDTO) -> Unit
) {

    private val gson = Gson()
    private val client = OkHttpClient.Builder()
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .build()

    private var webSocket: WebSocket? = null

    fun connect() {
        val request = Request.Builder()
            .url("ws://<your-api-host>/ws") // 👈 Replace with your actual base URL
            .build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                // Subscribe to user-specific topic
                val subscribeFrame = """
                    CONNECT
                    accept-version:1.1,1.0
                    heart-beat:10000,10000

                    \u0000
                """.trimIndent()
                webSocket.send(subscribeFrame)

                val subscribeToTopic = """
                    SUBSCRIBE
                    id:sub-1
                    destination:/topic/notifications/$userId

                    \u0000
                """.trimIndent()
                webSocket.send(subscribeToTopic)
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                val jsonStartIndex = text.indexOf("{")
                if (jsonStartIndex != -1) {
                    val json = text.substring(jsonStartIndex)
                    runCatching {
                        gson.fromJson(json, NotificationDTO::class.java)
                    }.onSuccess(onNotificationReceived)
                }
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {}
            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                t.printStackTrace()
            }
        })
    }

    fun disconnect() {
        webSocket?.close(1000, "Client closed")
    }
}