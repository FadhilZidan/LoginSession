package com.example.sesilogin

import java.security.MessageDigest

object HashUtils {
    /**
     * Menghasilkan hash SHA-256 sederhana dari string kata sandi.
     */
    fun hashPassword(password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
