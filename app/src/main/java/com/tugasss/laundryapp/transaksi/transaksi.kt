package com.tugasss.laundryapp.transaksi

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.FirebaseApp
import com.tugasss.laundryapp.R
import com.tugasss.laundryapp.modeldata.modelTransaksiTambahan

class transaksi : AppCompatActivity() {
    private lateinit var tvNAMA_PELANGGAN: TextView
    private lateinit var tvNO_HP: TextView
    private lateinit var tvNAMA_LAYANAN: TextView
    private lateinit var tvHARGA_LAYANAN: TextView
    private lateinit var rvLAYANAN_TAMBAHAN: RecyclerView
    private lateinit var btPILIH_PELANGGAN: Button
    private lateinit var btPILIH_LAYANAN: Button
    private lateinit var btPROSES: Button
    private lateinit var btTAMBAHAN: Button
    private val dataList = mutableListOf<modelTransaksiTambahan>()

    private val pilihPelanggan = 1
    private val pilihLayanan = 2
    private val pilihLayananTambahan = 3

    private var idPelanggan: String = ""
    private var idCabang: String = ""
    private var namaPelanggan: String = ""
    private var noHP: String = ""
    private var idLayanan: String = ""
    private var namaLayanan: String = ""
    private var hargaLayanan: String = ""
    private var idPegawai: String = ""

    private lateinit var sharedPref: SharedPreferences


    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_transaksi)

        sharedPref = getSharedPreferences("user_data", MODE_PRIVATE)
        idCabang = sharedPref.getString("idCabang", null).toString()
        idPegawai = sharedPref.getString("idPegawai", null).toString()

        setContentView(R.layout.activity_transaksi)
        FirebaseApp.initializeApp(this)
        init()

        val layoutManager = LinearLayoutManager(this)
        layoutManager.reverseLayout = true
        rvLAYANAN_TAMBAHAN.layoutManager = layoutManager
        rvLAYANAN_TAMBAHAN.setHasFixedSize(true)

        btPILIH_PELANGGAN.setOnClickListener {
            val intent = Intent(this, activity_pilih_pelanggan::class.java)
            startActivityForResult(intent, pilihPelanggan)
        }

        btPILIH_LAYANAN.setOnClickListener {
            val intent = Intent(this, activity_pilih_layanan::class.java)
            startActivityForResult(intent, pilihLayanan)
        }

        btTAMBAHAN.setOnClickListener {
            val intent = Intent(this, pilihLayananTambahan::class.java)
            startActivityForResult(intent, pilihLayananTambahan)
        }
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    fun init() {
        tvNAMA_PELANGGAN = findViewById(R.id.tvNAMA_PELANGGAN)
        tvNO_HP = findViewById(R.id.tvNO_HP)
        tvNAMA_LAYANAN = findViewById(R.id.tvNAMA_LAYANAN)
        tvHARGA_LAYANAN = findViewById(R.id.tvHARGA_LAYANAN)
        rvLAYANAN_TAMBAHAN = findViewById(R.id.rvLAYANAN_TAMBAHAN)
        btPILIH_PELANGGAN = findViewById(R.id.btPILIH_PELANGGAN)
        btPILIH_LAYANAN = findViewById(R.id.btPILIH_LAYANAN)
        btTAMBAHAN = findViewById(R.id.btTAMBAHAN)
    }


    @Deprecated("This method has been deprecated in favor of using the Activity Result API")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == pilihPelanggan) {
            if (resultCode == RESULT_OK && data != null) {
                idPelanggan = data.getStringExtra("idPelanggan").toString()
                val nama = data.getStringExtra("nama")
                val nomorHP = data.getStringExtra("noHP")

                tvNAMA_PELANGGAN.text = "Nama Pelanggan : $nama"
                tvNO_HP.text = "No HP : $nomorHP"

                namaPelanggan = nama.toString()
                noHP = nomorHP.toString()
            }
            if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Batal Memilih Pelanggan", Toast.LENGTH_SHORT).show()

            }
        }
    }
}