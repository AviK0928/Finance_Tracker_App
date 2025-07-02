package com.example.finance_tracker.features.notification.data

import com.example.finance_tracker.core.network.model.notification.NotificationDTO
import com.example.finance_tracker.core.websockets.NotificationWebSocketClient
import com.example.finance_tracker.features.notification.domain.NotificationWebSocketManager
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString

class NotificationWebSocketManagerImpl : NotificationWebSocketManager {

    private val client = OkHttpClient()
    private var webSocket: WebSocket? = null
    private val gson = Gson()

    private val _newNotifications = MutableSharedFlow<NotificationDTO>(extraBufferCapacity = 10)
    override val newNotifications: SharedFlow<NotificationDTO> = _newNotifications

    override fun start(userId: Long) {
        val request = Request.Builder()
            .url("ws://your-server-domain/ws/notifications/$userId") // update to your backend host
            .build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {

            override fun onOpen(webSocket: WebSocket, response: Response) {
                println("WebSocket connected")
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                try {
                    val notification = gson.fromJson(text, NotificationDTO::class.java)
                    _newNotifications.tryEmit(notification)
                } catch (e: Exception) {
                    println("WebSocket parse error: ${e.message}")
                }
            }

            override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                onMessage(webSocket, bytes.utf8())
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                println("WebSocket error: ${t.message}")
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                webSocket.close(code, reason)
                println("WebSocket closing: $reason")
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                println("WebSocket closed: $reason")
            }
        })
    }

    override fun stop() {
        webSocket?.close(1000, "Client closed")
        webSocket = null
    }
}