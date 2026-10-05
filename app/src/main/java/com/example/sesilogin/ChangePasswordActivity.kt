package com.example.sesilogin

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.sesilogin.databinding.ActivityChangePasswordBinding

class ChangePasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChangePasswordBinding
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityChangePasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.mainChangePassword) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        sessionManager = SessionManager(this)

        binding.btnSavePassword.setOnClickListener {
            handleChangePassword()
        }
    }

    private fun handleChangePassword() {
        val oldPassword = binding.etOldPassword.text.toString().trim()
        val newPassword = binding.etNewPassword.text.toString().trim()
        val confirmPassword = binding.etConfirmPassword.text.toString().trim()

        if (oldPassword.isEmpty()) {
            binding.etOldPassword.error = "Sandi saat ini tidak boleh kosong"
            return
        }

        val oldPasswordHash = HashUtils.hashPassword(oldPassword)
        val storedHash = sessionManager.ambilPasswordHash()

        // Syarat 6: Validasi hash sandi lama
        if (storedHash.isNotEmpty() && (oldPasswordHash != storedHash)) {
            binding.etOldPassword.error = getString(R.string.err_old_password_wrong)
            return
        }

        if (newPassword.length < 6) {
            binding.etNewPassword.error = getString(R.string.err_password_short)
            return
        }

        if (newPassword != confirmPassword) {
            binding.etConfirmPassword.error = getString(R.string.err_confirm_password_mismatch)
            return
        }

        // Syarat 6: Simpan sandi baru memakai hash sederhana (SHA-256) — tidak pernah menyimpan sandi asli
        val newPasswordHash = HashUtils.hashPassword(newPassword)
        sessionManager.simpanPasswordHash(newPasswordHash)

        Toast.makeText(this, getString(R.string.msg_password_changed), Toast.LENGTH_SHORT).show()
        finish()
    }
}
