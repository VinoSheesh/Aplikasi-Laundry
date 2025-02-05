package com.tugasss.laundryapp

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.TextView
import com.tugasss.laundryapp.pegawai.Activity_data_pegawai
import com.tugasss.laundryapp.pelanggan.DataPelangganActivity
import java.text.SimpleDateFormat
import java.util.*

class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)

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

        val greetingTextView: TextView = findViewById(R.id.greetingTextView)
        val dateTextView: TextView = findViewById(R.id.dateTextView)

        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val greeting = when (currentHour) {
            in 0..11 -> "Selamat Pagi,Vino"
            in 12..15 -> "Selamat Siang,Vino"
            in 16..18 -> "Selamat Sore,Vino"
            else -> "Selamat Malam,Vino"
        }

        greetingTextView.text = greeting

        val dateFormat = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault())
        val currentDate = dateFormat.format(Date())
        dateTextView.text = currentDate

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}