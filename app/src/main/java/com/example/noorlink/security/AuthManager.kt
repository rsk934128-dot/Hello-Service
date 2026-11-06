package com.example.noorlink.security

import com.example.noorlink.model.AuthToken
import org.json.JSONArray
import org.json.JSONObject
import java.nio.charset.StandardCharsets
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

object AuthManager {
    private const val DEFAULT_SECRET = "NOORLINK_SECRET_HMAC_KEY_2026"

    /**
     * URL-safe Base64 encoding without padding
     */
    private fun base64UrlEncode(bytes: ByteArray): String {
        val table = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_".toCharArray()
        val sb = StringBuilder()
        var i = 0
        while (i < bytes.size) {
            val b1 = bytes[i].toInt() and 0xFF
            val b2 = if (i + 1 < bytes.size) bytes[i + 1].toInt() and 0xFF else -1
            val b3 = if (i + 2 < bytes.size) bytes[i + 2].toInt() and 0xFF else -1

            val d1 = b1 ushr 2
            val d2 = ((b1 and 0x03) shl 4) or (if (b2 >= 0) b2 ushr 4 else 0)
            val d3 = if (b2 >= 0) ((b2 and 0x0F) shl 2) or (if (b3 >= 0) b3 ushr 6 else 0) else -1
            val d4 = if (b3 >= 0) b3 and 0x3F else -1

            sb.append(table[d1])
            sb.append(table[d2])
            if (d3 >= 0) sb.append(table[d3])
            if (d4 >= 0) sb.append(table[d4])

            i += 3
        }
        return sb.toString()
    }

    /**
     * URL-safe Base64 decoding
     */
    private fun base64UrlDecode(str: String): ByteArray {
        val clean = str.replace("=", "")
        val len = clean.length
        val outLen = (len * 3) / 4
        val out = ByteArray(outLen)
        var outIdx = 0

        fun charToVal(c: Char): Int = when (c) {
            in 'A'..'Z' -> c - 'A'
            in 'a'..'z' -> c - 'a' + 26
            in '0'..'9' -> c - '0' + 52
            '-', '+' -> 62
            '_', '/' -> 63
            else -> 0
        }

        var i = 0
        while (i < len) {
            val c1 = charToVal(clean[i])
            val c2 = if (i + 1 < len) charToVal(clean[i + 1]) else 0
            val c3 = if (i + 2 < len) charToVal(clean[i + 2]) else 0
            val c4 = if (i + 3 < len) charToVal(clean[i + 3]) else 0

            if (outIdx < outLen) out[outIdx++] = ((c1 shl 2) or (c2 ushr 4)).toByte()
            if (i + 2 < len && outIdx < outLen) out[outIdx++] = (((c2 and 0x0F) shl 4) or (c3 ushr 2)).toByte()
            if (i + 3 < len && outIdx < outLen) out[outIdx++] = (((c3 and 0x03) shl 6) or c4).toByte()

            i += 4
        }
        return out.copyOf(outIdx)
    }

    /**
     * Generates a standard JWT format: header.payload.signature
     */
    fun createJwtToken(
        deviceId: String,
        role: String = "device",
        scopes: List<String> = listOf("telemetry:read", "heartbeat:ping", "command:exec"),
        validityMinutes: Long = 120,
        secret: String = DEFAULT_SECRET
    ): AuthToken {
        val now = System.currentTimeMillis()
        val exp = now + (validityMinutes * 60 * 1000)

        // 1. Header
        val headerJson = JSONObject().apply {
            put("alg", "HS256")
            put("typ", "JWT")
        }
        val encodedHeader = base64UrlEncode(headerJson.toString().toByteArray(StandardCharsets.UTF_8))

        // 2. Payload
        val payloadJson = JSONObject().apply {
            put("sub", deviceId)
            put("role", role)
            put("scopes", JSONArray(scopes))
            put("iat", now / 1000)
            put("exp", exp / 1000)
            put("iss", "noorlink-core")
        }
        val encodedPayload = base64UrlEncode(payloadJson.toString().toByteArray(StandardCharsets.UTF_8))

        // 3. Signature
        val signatureInput = "$encodedHeader.$encodedPayload"
        val signature = hmacSha256(signatureInput, secret)

        val rawToken = "$encodedHeader.$encodedPayload.$signature"

        return AuthToken(
            rawToken = rawToken,
            deviceId = deviceId,
            role = role,
            scopes = scopes,
            issuedAt = now,
            expiresAt = exp,
            isValid = true
        )
    }

    /**
     * Validates and parses a JWT string
     */
    fun parseAndValidateToken(rawToken: String, secret: String = DEFAULT_SECRET): AuthToken? {
        val parts = rawToken.split(".")
        if (parts.size != 3) return null

        try {
            val (encodedHeader, encodedPayload, signature) = parts

            // Verify signature
            val expectedSig = hmacSha256("$encodedHeader.$encodedPayload", secret)
            val isSigValid = expectedSig == signature

            val payloadBytes = base64UrlDecode(encodedPayload)
            val payloadStr = String(payloadBytes, StandardCharsets.UTF_8)
            val json = JSONObject(payloadStr)

            val deviceId = json.optString("sub", "UNKNOWN")
            val role = json.optString("role", "guest")
            val iat = json.optLong("iat", 0L) * 1000
            val exp = json.optLong("exp", 0L) * 1000

            val scopesList = mutableListOf<String>()
            val scopesArr = json.optJSONArray("scopes")
            if (scopesArr != null) {
                for (i in 0 until scopesArr.length()) {
                    scopesList.add(scopesArr.getString(i))
                }
            }

            val isNotExpired = System.currentTimeMillis() <= exp

            return AuthToken(
                rawToken = rawToken,
                deviceId = deviceId,
                role = role,
                scopes = scopesList,
                issuedAt = iat,
                expiresAt = exp,
                isValid = isSigValid && isNotExpired
            )
        } catch (e: Exception) {
            return null
        }
    }

    private fun hmacSha256(data: String, secret: String): String {
        return try {
            val sha256Hmac = Mac.getInstance("HmacSHA256")
            val secretKey = SecretKeySpec(secret.toByteArray(StandardCharsets.UTF_8), "HmacSHA256")
            sha256Hmac.init(secretKey)
            val signedBytes = sha256Hmac.doFinal(data.toByteArray(StandardCharsets.UTF_8))
            base64UrlEncode(signedBytes)
        } catch (e: Exception) {
            "sig_err"
        }
    }
}
