package com.example.sesilogin

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class SplashActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_splash)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        sessionManager = SessionManager(this)

        // Syarat 1: Tampilkan splash logo selama 1,5 detik (1500 ms) sebelum memeriksa sesi
        Handler(Looper.getMainLooper()).postDelayed({
            checkSessionAndNavigate()
        }, 1500)
    }

    private fun checkSessionAndNavigate() {
        // Syarat 2: Cek apakah sesi sudah kadaluarsa (lebih dari 7 hari)
        val isExpired = sessionManager.isSesiExpired()
        
        if (isExpired) {
            Toast.makeText(this, getString(R.string.msg_session_expired), Toast.LENGTH_LONG).show()
        }

        if (sessionManager.sudahLogin() && !isExpired && sessionManager.adaDataProfil()) {
            // Sesi aktif & valid -> langsung ke DashboardActivity
            val intent = Intent(this, DashboardActivity::class.java)
            startActivity(intent)
        } else {
            // Belum login / Sesi kadaluarsa -> ke LoginActivity
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
        finish()
    }
}
