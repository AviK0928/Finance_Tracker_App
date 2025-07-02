package com.example.finance_tracker.core.data.util

import android.util.Base64
import org.json.JSONObject
import java.nio.charset.Charset

object JwtUtils {

    fun decodePayload(token: String): JSONObject? {
        return try {
            val parts = token.split(".")
            if (parts.size != 3) return null
            val payload = String(Base64.decode(parts[1], Base64.URL_SAFE), Charset.forName("UTF-8"))
            JSONObject(payload)
        } catch (e: Exception) {
            null
        }
    }

    fun isExpired(token: String): Boolean {
        return try {
            val payload = decodePayload(token) ?: return true
            val exp = payload.optLong("exp", 0)
            val now = System.currentTimeMillis() / 1000
            now >= exp
        } catch (e: Exception) {
            true
        }
    }

    fun getUsername(token: String): String? {
        return decodePayload(token)?.optString("sub")
    }
}

