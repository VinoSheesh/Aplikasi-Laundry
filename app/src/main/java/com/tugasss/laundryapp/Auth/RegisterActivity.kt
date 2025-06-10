package com.tugasss.laundryapp.Auth

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase // Import Firebase Database
import com.tugasss.laundryapp.R
import com.tugasss.laundryapp.MainActivity // Ganti dengan Activity utama aplikasi Anda setelah login

class RegisterActivity : AppCompatActivity() {

    private lateinit var etUsername: EditText // Tambahkan ini
    private lateinit var etRegEmail: EditText
    private lateinit var etRegPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var btnRegister: Button
    private lateinit var tvLogin: TextView
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase // Inisialisasi Firebase Database

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance() // Inisialisasi Database

        initViews()
        setupListeners()
    }

    private fun initViews() {
        etUsername = findViewById(R.id.etUsername) // Inisialisasi view username
        etRegEmail = findViewById(R.id.etRegEmail)
        etRegPassword = findViewById(R.id.etRegPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        btnRegister = findViewById(R.id.btnRegister)
        tvLogin = findViewById(R.id.tvLogin)
    }

    private fun setupListeners() {
        btnRegister.setOnClickListener {
            performRegistration()
        }

        tvLogin.setOnClickListener {
            finish() // Kembali ke LoginActivity
        }
    }

    private fun performRegistration() {
        val username = etUsername.text.toString().trim() // Ambil username
        val email = etRegEmail.text.toString().trim()
        val password = etRegPassword.text.toString().trim()
        val confirmPassword = etConfirmPassword.text.toString().trim()

        if (username.isEmpty()) { // Validasi username
            etUsername.error = "Username tidak boleh kosong"
            etUsername.requestFocus()
            return
        }

        if (email.isEmpty()) {
            etRegEmail.error = "Email tidak boleh kosong"
            etRegEmail.requestFocus()
            return
        }

        if (password.isEmpty() || password.length < 6) {
            etRegPassword.error = "Password minimal 6 karakter"
            etRegPassword.requestFocus()
            return
        }

        if (password != confirmPassword) {
            etConfirmPassword.error = "Konfirmasi password tidak cocok"
            etConfirmPassword.requestFocus()
            return
        }

        // Lakukan pendaftaran user dengan Firebase Auth
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Pendaftaran Auth berhasil
                    val user = auth.currentUser
                    user?.let {
                        // Simpan username ke Firebase Database
                        val userId = it.uid
                        val userRef = database.getReference("users").child(userId)
                        userRef.child("username").setValue(username)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Pendaftaran berhasil!", Toast.LENGTH_SHORT).show()
                                // Langsung login setelah daftar dan simpan username
                                val intent = Intent(this, LoginActivity::class.java)
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                startActivity(intent)
                                finish()
                            }
                            .addOnFailureListener { dbException ->
                                Toast.makeText(this, "Gagal menyimpan username: ${dbException.message}", Toast.LENGTH_LONG).show()
                                // Opsional: Hapus user yang baru dibuat jika penyimpanan username gagal
                                user.delete()
                            }
                    }
                } else {
                    // Pendaftaran Auth gagal
                    Toast.makeText(this, "Pendaftaran gagal: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }
}