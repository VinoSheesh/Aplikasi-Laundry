package com.tugasss.laundryapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.TextView
import android.widget.Switch
import com.tugasss.laundryapp.layanan.DataLayananActivity
import com.tugasss.laundryapp.pegawai.Activity_data_pegawai
import com.tugasss.laundryapp.pelanggan.DataPelangganActivity
import com.tugasss.laundryapp.transaksi.transaksi
import com.tugasss.laundryapp.tambahan.DataTambahanActivity
import java.text.SimpleDateFormat
import java.util.*

// --- IMPORT YANG DIBUTUHKAN UNTUK FIREBASE ---
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.tugasss.laundryapp.Auth.LoginActivity
import com.tugasss.laundryapp.cabang.DataCabangActivity

class HomeActivity : AppCompatActivity() {

    // --- DEKLARASIKAN VARIABEL FIREBASE ---
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    // --------------------------------------

    private lateinit var greetingTextView: TextView
    private lateinit var languageSwitch: Switch // Tambahkan variabel untuk switch

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // --- TERAPKAN BAHASA YANG TERSIMPAN SEBELUM SETCONTENTVIEW ---
        applyStoredLanguage()

        enableEdgeToEdge()
        setContentView(R.layout.activity_home)

        // --- INISIALISASI FIREBASE ---
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        // -----------------------------

        // --- PASTIKAN PENGGUNA SUDAH LOGIN ---
        if (auth.currentUser == null) {
            navigateToLogin()
            return
        }
        // -------------------------------------

        // --- INISIALISASI SWITCH BAHASA ---
        initLanguageSwitch()

        // Setup click listeners untuk menu
        setupMenuClickListeners()

        greetingTextView = findViewById(R.id.greetingTextView)
        val dateTextView: TextView = findViewById(R.id.dateTextView)

        displayWelcomeMessage()

        val dateFormat = SimpleDateFormat("EEEE, dd MMMM yyyy", getLocaleForDate())
        val currentDate = dateFormat.format(Date())
        dateTextView.text = currentDate

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    // --- FUNGSI UNTUK MENERAPKAN BAHASA YANG TERSIMPAN ---
    private fun applyStoredLanguage() {
        val savedLanguage = LanguageUtils.getLanguagePreference(this)
        LanguageUtils.setAppLanguage(this, savedLanguage)
    }

    // --- FUNGSI UNTUK INISIALISASI SWITCH BAHASA ---
    private fun initLanguageSwitch() {
        languageSwitch = findViewById(R.id.languageSwitch)

        // Set posisi switch berdasarkan bahasa yang tersimpan
        languageSwitch.isChecked = LanguageUtils.isEnglish(this)

        // Set listener untuk perubahan switch
        languageSwitch.setOnCheckedChangeListener { _, isChecked ->
            val newLanguage = if (isChecked) {
                LanguageUtils.Languages.ENGLISH
            } else {
                LanguageUtils.Languages.INDONESIAN
            }

            // Terapkan bahasa baru
            LanguageUtils.setAppLanguage(this, newLanguage)

            // Restart activity untuk menerapkan perubahan bahasa
            recreateActivity()
        }
    }

    // --- FUNGSI UNTUK RESTART ACTIVITY ---
    private fun recreateActivity() {
        val intent = intent
        finish()
        startActivity(intent)
    }

    // --- FUNGSI UNTUK MENDAPATKAN LOCALE SESUAI BAHASA ---
    private fun getLocaleForDate(): Locale {
        return if (LanguageUtils.isEnglish(this)) {
            Locale.ENGLISH
        } else {
            Locale("id", "ID")
        }
    }

    // --- FUNGSI UNTUK SETUP CLICK LISTENERS ---
    private fun setupMenuClickListeners() {
        val pelangganLayout = findViewById<LinearLayout>(R.id.PELANGGAN)
        pelangganLayout.setOnClickListener {
            val intent = Intent(this, DataPelangganActivity::class.java)
            startActivity(intent)
        }

        val pegawaiLayout = findViewById<LinearLayout>(R.id.PEGAWAI)
        pegawaiLayout.setOnClickListener {
            val intent = Intent(this, Activity_data_pegawai::class.java)
            startActivity(intent)
        }

        val layananLayout = findViewById<LinearLayout>(R.id.LAYANAN)
        layananLayout.setOnClickListener {
            val intent = Intent(this, DataLayananActivity::class.java)
            startActivity(intent)
        }

        val transaksiLayout = findViewById<LinearLayout>(R.id.TRANSAKSI)
        transaksiLayout.setOnClickListener{
            val intent = Intent(this, transaksi::class.java)
            startActivity(intent)
        }

        val tambahanlayout = findViewById<LinearLayout>(R.id.TAMBAHAN)
        tambahanlayout.setOnClickListener{
            val intent = Intent(this, DataTambahanActivity::class.java)
            startActivity(intent)
        }

        val cabanglayout = findViewById<LinearLayout>(R.id.CABANG)
        cabanglayout.setOnClickListener{
            val intent = Intent(this, DataCabangActivity::class.java)
            startActivity(intent)
        }

        val laporanlayout = findViewById<LinearLayout>(R.id.LAPORAN)
        laporanlayout.setOnClickListener{
            val intent = Intent(this, Activity_Data_laporan::class.java)
            startActivity(intent)
        }

        val akunLayout = findViewById<LinearLayout>(R.id.AKUN)
        akunLayout.setOnClickListener {
            val intent = Intent(this, AkunActivity::class.java)
            startActivity(intent)
        }
    }

    // --- FUNGSI UNTUK MENAMPILKAN PESAN SELAMAT DATANG DENGAN USERNAME ---
    private fun displayWelcomeMessage() {
        val user = auth.currentUser
        user?.let {
            val userId = it.uid
            val userRef = database.getReference("users").child(userId).child("username")

            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val username = snapshot.getValue(String::class.java)
                    if (username != null) {
                        val greeting = getGreetingMessage(username)
                        greetingTextView.text = greeting
                    } else {
                        greetingTextView.text = getGreetingMessage(getDefaultUserText())
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    greetingTextView.text = getGreetingMessage(getDefaultUserText())
                }
            })
        } ?: run {
            greetingTextView.text = getGreetingMessage(getDefaultUserText())
        }
    }

    // --- FUNGSI UNTUK MENDAPATKAN TEXT DEFAULT USER SESUAI BAHASA ---
    private fun getDefaultUserText(): String {
        return if (LanguageUtils.isEnglish(this)) {
            "User"
        } else {
            "Pengguna"
        }
    }

    // --- FUNGSI UNTUK MENDAPATKAN SALAM SESUAI WAKTU DAN BAHASA ---
    private fun getGreetingMessage(username: String): String {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)

        return if (LanguageUtils.isEnglish(this)) {
            // English greetings
            when (hour) {
                in 0..11 -> "Good Morning, $username"
                in 12..15 -> "Good Afternoon, $username"
                in 16..18 -> "Good Evening, $username"
                else -> "Good Night, $username"
            }
        } else {
            // Indonesian greetings
            when (hour) {
                in 0..11 -> "Selamat Pagi, $username"
                in 12..15 -> "Selamat Siang, $username"
                in 16..18 -> "Selamat Sore, $username"
                else -> "Selamat Malam, $username"
            }
        }
    }

    // --- FUNGSI UNTUK NAVIGASI KEMBALI KE LOGIN JIKA BELUM LOGIN ---
    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
        finish()
    }
}