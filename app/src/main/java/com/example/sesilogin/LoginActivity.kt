package com.example.sesilogin

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.sesilogin.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var sessionManager: SessionManager
    private var selectedAvatarIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.mainLogin) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        sessionManager = SessionManager(this)

        // Syarat 5: Pre-fill email jika ada email tersimpan dari sesi sebelumnya (setelah logout)
        val savedEmail = sessionManager.ambilEmail()
        if (savedEmail.isNotEmpty()) {
            binding.etEmail.setText(savedEmail)
        }

        // Load avatar index terpilih sebelumnya
        selectedAvatarIndex = sessionManager.ambilAvatarIndex()
        updateAvatarSelection(selectedAvatarIndex)

        setupAvatarListeners()

        binding.btnLogin.setOnClickListener {
            handleLogin()
        }
    }

    private fun setupAvatarListeners() {
        binding.ivAvatar0.setOnClickListener { updateAvatarSelection(0) }
        binding.ivAvatar1.setOnClickListener { updateAvatarSelection(1) }
        binding.ivAvatar2.setOnClickListener { updateAvatarSelection(2) }
        binding.ivAvatar3.setOnClickListener { updateAvatarSelection(3) }
    }

    private fun updateAvatarSelection(index: Int) {
        selectedAvatarIndex = index
        val avatarViews = listOf(
            binding.ivAvatar0,
            binding.ivAvatar1,
            binding.ivAvatar2,
            binding.ivAvatar3
        )

        avatarViews.forEachIndexed { i, view ->
            if (i == index) {
                view.setBackgroundResource(R.drawable.bg_avatar_selected)
            } else {
                view.setBackgroundResource(R.drawable.bg_avatar_unselected)
            }
        }
    }

    private fun handleLogin() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        if (email.isEmpty()) {
            binding.etEmail.error = getString(R.string.err_email_empty)
            return
        }

        if (password.length < 6) {
            binding.etPassword.error = getString(R.string.err_password_short)
            return
        }

        val inputPasswordHash = HashUtils.hashPassword(password)
        val storedHash = sessionManager.ambilPasswordHash()

        // Validasi Kredensial:
        // Jika sudah ada hash tersimpan di SessionManager, bandingkan hash input dengan storedHash.
        // Jika belum ada hash tersimpan (pertama kali), izinkan login untuk akun demo/apa saja dan simpan hash-nya.
        val isValidCredential = if (storedHash.isNotEmpty()) {
            inputPasswordHash == storedHash
        } else {
            // Default demo account: rani@example.com / 123456 atau password apa saja >= 6 karakter untuk pertama kali
            true
        }

        if (isValidCredential) {
            val nama = extractNameFromEmail(email)
            val ingatSaya = binding.cbRemember.isChecked

            // Syarat 3: Simpan avatar_index di sesi yang sama
            // Syarat 6: Simpan sandi dengan hash sederhana
            sessionManager.simpanSesi(
                nama = nama,
                email = email,
                ingatSaya = ingatSaya,
                avatarIndex = selectedAvatarIndex,
                passwordHash = inputPasswordHash
            )

            val intent = Intent(this, DashboardActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            Toast.makeText(this, getString(R.string.err_login_failed), Toast.LENGTH_SHORT).show()
        }
    }

    private fun extractNameFromEmail(email: String): String {
        val rawName = email.substringBefore("@")
        return rawName.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    }
}
