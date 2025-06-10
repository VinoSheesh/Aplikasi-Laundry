package com.tugasss.laundryapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.tugasss.laundryapp.Auth.LoginActivity // Pastikan path import ini benar
import com.tugasss.laundryapp.tambahan.DataTambahanActivity

class AkunActivity : AppCompatActivity() {

    private lateinit var tvAkunUsername: TextView
    private lateinit var tvAkunEmail: TextView
    private lateinit var btnLogout: Button
    private lateinit var layoutEditProfile: LinearLayout
    private lateinit var layoutSettings: LinearLayout
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_akun)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        // Pastikan pengguna sudah login sebelum menampilkan info akun
        if (auth.currentUser == null) {
            navigateToLogin()
            return
        }

        initViews()
        loadUserInfo()
        setupListeners()

        val back = findViewById<ImageView>(R.id.Back)
        back.setOnClickListener{
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        }
    }

    private fun initViews() {
        tvAkunUsername = findViewById(R.id.tvAkunUsername)
        tvAkunEmail = findViewById(R.id.tvAkunEmail)
        btnLogout = findViewById(R.id.btnLogout)
        layoutEditProfile = findViewById(R.id.layoutEditProfile)
        layoutSettings = findViewById(R.id.layoutSettings)
    }

    private fun loadUserInfo() {
        val user = auth.currentUser
        user?.let {
            // Menggunakan string resource untuk email tidak tersedia
            tvAkunEmail.text = it.email ?: getString(R.string.email_not_available)

            val userId = it.uid
            val userRef = database.getReference("users").child(userId).child("username")

            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val username = snapshot.getValue(String::class.java)
                    // Format username lebih bersih dengan string resource
                    tvAkunUsername.text = username ?: getString(R.string.username_not_available)
                }

                override fun onCancelled(error: DatabaseError) {
                    // Menggunakan string resource untuk error
                    tvAkunUsername.text = getString(R.string.error_loading_username)
                    Toast.makeText(
                        this@AkunActivity,
                        getString(R.string.failed_load_username, error.message),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
        }
    }

    private fun setupListeners() {
        // Logout button dengan string resource
        btnLogout.setOnClickListener {
            auth.signOut() // Melakukan logout dari Firebase
            Toast.makeText(this, getString(R.string.logout_success), Toast.LENGTH_SHORT).show()
            navigateToLogin() // Kembali ke halaman login
        }

        // Edit Profile click listener dengan string resource
        layoutEditProfile.setOnClickListener {
            Toast.makeText(this, getString(R.string.edit_profile_coming_soon), Toast.LENGTH_SHORT).show()
        }

        // Settings click listener dengan string resource
        layoutSettings.setOnClickListener {
            Toast.makeText(this, getString(R.string.settings_coming_soon), Toast.LENGTH_SHORT).show()
        }
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
        finish() // Tutup AkunActivity
    }
}