package com.example.sesilogin

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREF_NAME = "sesi_login"
        
        const val KEY_SUDAH_LOGIN = "sudah_login"
        const val KEY_NAMA = "nama"
        const val KEY_EMAIL = "email"
        const val KEY_WAKTU_LOGIN = "waktu_login"
        const val KEY_AVATAR_INDEX = "avatar_index"
        const val KEY_PASSWORD_HASH = "password_hash"

        // 7 hari dalam milidetik (7 hari * 24 jam * 60 menit * 60 detik * 1000 ms)
        const val SESSION_EXPIRY_MS = 7 * 24 * 60 * 60 * 1000L
    }

    /**
     * Menyimpan data sesi login pengguna.
     */
    fun simpanSesi(
        nama: String,
        email: String,
        ingatSaya: Boolean,
        avatarIndex: Int = 0,
        passwordHash: String? = null
    ) {
        val editor = prefs.edit()
        editor.putBoolean(KEY_SUDAH_LOGIN, ingatSaya)
        editor.putString(KEY_NAMA, nama)
        editor.putString(KEY_EMAIL, email)
        editor.putLong(KEY_WAKTU_LOGIN, System.currentTimeMillis())
        editor.putInt(KEY_AVATAR_INDEX, avatarIndex)
        
        if (passwordHash != null) {
            editor.putString(KEY_PASSWORD_HASH, passwordHash)
        } else if (ambilPasswordHash().isEmpty()) {
            // Jika belum ada hash sandi, simpan default hash untuk "123456"
            editor.putString(KEY_PASSWORD_HASH, HashUtils.hashPassword("123456"))
        }
        
        editor.apply()
    }

    /**
     * Memeriksa apakah pengguna sudah login (Ingat Saya aktif).
     */
    fun sudahLogin(): Boolean {
        return prefs.getBoolean(KEY_SUDAH_LOGIN, false)
    }

    /**
     * Memeriksa apakah waktu login sudah kadaluarsa (lebih dari 7 hari).
     * Jika kadaluarsa, status sudah_login akan dihapus otomatis.
     */
    fun isSesiExpired(): Boolean {
        if (!sudahLogin()) return false

        val waktuLogin = ambilWaktuLogin()
        if (waktuLogin == 0L) return false

        val selisihWaktu = System.currentTimeMillis() - waktuLogin
        if (selisihWaktu > SESSION_EXPIRY_MS) {
            // Paksa logout jika melebihi 7 hari
            hapusStatusLogin()
            return true
        }
        return false
    }

    /**
     * Memeriksa kelengkapan data profil sebelum membuka Dashboard.
     */
    fun adaDataProfil(): Boolean {
        val email = ambilEmail()
        return !email.isNullOrEmpty()
    }

    fun ambilNama(): String {
        return prefs.getString(KEY_NAMA, "") ?: ""
    }

    fun ambilEmail(): String {
        return prefs.getString(KEY_EMAIL, "") ?: ""
    }

    fun ambilWaktuLogin(): Long {
        return prefs.getLong(KEY_WAKTU_LOGIN, 0L)
    }

    fun ambilAvatarIndex(): Int {
        return prefs.getInt(KEY_AVATAR_INDEX, 0)
    }

    fun simpanAvatarIndex(index: Int) {
        prefs.edit().putInt(KEY_AVATAR_INDEX, index).apply()
    }

    fun ambilPasswordHash(): String {
        return prefs.getString(KEY_PASSWORD_HASH, "") ?: ""
    }

    fun simpanPasswordHash(newHash: String) {
        prefs.edit().putString(KEY_PASSWORD_HASH, newHash).apply()
    }

    /**
     * Menghapus status login ("sudah_login" & "waktu_login"),
     * TETAPI Tetap Menyimpan Email (Sesuai Syarat "Ingat Email Saja").
     */
    fun logout() {
        prefs.edit().apply {
            remove(KEY_SUDAH_LOGIN)
            remove(KEY_WAKTU_LOGIN)
            apply()
        }
    }

    /**
     * Menghapus status login saja.
     */
    private fun hapusStatusLogin() {
        prefs.edit().remove(KEY_SUDAH_LOGIN).apply()
    }

    /**
     * Menghapus seluruh data sesi (jika diperlukan reset total).
     */
    fun hapusSesiTotal() {
        prefs.edit().clear().apply()
    }
}
