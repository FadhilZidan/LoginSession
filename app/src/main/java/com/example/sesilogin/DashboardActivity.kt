package com.example.sesilogin

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.sesilogin.databinding.ActivityDashboardBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class DashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.mainDashboard) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        sessionManager = SessionManager(this)

        // Guard Check: Validasi data profil & waktu kadaluarsa sesi (Syarat 2)
        if (!sessionManager.adaDataProfil() || sessionManager.isSesiExpired()) {
            redirectToLogin()
            return
        }

        displayUserData()
        setupListeners()
    }

    override fun onResume() {
        super.onResume()
        // Refresh data jika ada perubahan (misal setelah ganti avatar/sandi)
        displayUserData()
    }

    private fun displayUserData() {
        val nama = sessionManager.ambilNama()
        val email = sessionManager.ambilEmail()
        val waktuLoginMillis = sessionManager.ambilWaktuLogin()
        val avatarIndex = sessionManager.ambilAvatarIndex()

        // Syarat 4: Sapaan waktu sesuai jam perangkat
        val sapaanWaktu = getTimeGreeting()
        binding.tvGreeting.text = sapaanWaktu
        binding.tvUserName.text = nama.ifEmpty { "Pengguna" }
        binding.tvUserEmail.text = email

        // Format timestamp waktu_login
        val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
        val formattedDate = if (waktuLoginMillis > 0) {
            dateFormat.format(Date(waktuLoginMillis))
        } else {
            "-"
        }
        binding.tvWaktuLogin.text = formattedDate

        // Syarat 3: Tampilkan foto profil avatar dari indeks yang tersimpan
        updateAvatarImage(avatarIndex)
    }

    // Syarat 4: Fungsi penentu sapaan waktu berdasarkan jam
    private fun getTimeGreeting(): String {
        return when (Calendar.getInstance()[Calendar.HOUR_OF_DAY]) {
            in 4..10 -> "Selamat Pagi"
            in 11..14 -> "Selamat Siang"
            in 15..17 -> "Selamat Sore"
            else -> "Selamat Malam"
        }
    }

    private fun updateAvatarImage(avatarIndex: Int) {
        val avatarRes = when (avatarIndex) {
            0 -> R.drawable.ic_avatar_1
            1 -> R.drawable.ic_avatar_2
            2 -> R.drawable.ic_avatar_3
            3 -> R.drawable.ic_avatar_4
            else -> R.drawable.ic_avatar_1
        }
        binding.ivAvatarProfile.setImageResource(avatarRes)
    }

    private fun setupListeners() {
        // Syarat 3: Opsi ganti avatar profil
        binding.btnChangeAvatar.setOnClickListener {
            showAvatarSelectionDialog()
        }

        // Syarat 6: Buka Halaman Pengaturan Ganti Sandi
        binding.btnChangePassword.setOnClickListener {
            val intent = Intent(this, ChangePasswordActivity::class.java)
            startActivity(intent)
        }

        // Syarat 5: Logout - Hapus hanya kunci "sudah_login" supaya email tetap terisi di form login
        binding.btnLogout.setOnClickListener {
            sessionManager.logout()
            Toast.makeText(this, "Berhasil keluar. Email tetap diingat.", Toast.LENGTH_SHORT).show()
            redirectToLogin()
        }
    }

    private fun showAvatarSelectionDialog() {
        val avatars = arrayOf("Avatar 1 (Biru)", "Avatar 2 (Hijau)", "Avatar 3 (Ungu)", "Avatar 4 (Oranye)")
        val currentAvatar = sessionManager.ambilAvatarIndex()

        AlertDialog.Builder(this)
            .setTitle("Pilih Avatar Profil")
            .setSingleChoiceItems(avatars, currentAvatar) { dialog, which ->
                sessionManager.simpanAvatarIndex(which)
                updateAvatarImage(which)
                Toast.makeText(this, "Avatar berhasil diubah!", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun redirectToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}
