package com.tugasss.laundryapp.transaksi

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.FirebaseApp
import com.tugasss.laundryapp.HomeActivity
import com.tugasss.laundryapp.R
import com.tugasss.laundryapp.adapter.AdapterDataTambahan
import com.tugasss.laundryapp.adapter.AdapterPilihLayananTambahan
import com.tugasss.laundryapp.modeldata.modelTambahan
import java.io.Serializable // Pastikan ini diimpor

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
    private lateinit var adapterTambahan: AdapterPilihLayananTambahan
    private val listTambahanDipilih = mutableListOf<modelTambahan>()

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

        FirebaseApp.initializeApp(this)
        init()

        val layoutManager = LinearLayoutManager(this)
        rvLAYANAN_TAMBAHAN.layoutManager = layoutManager
        rvLAYANAN_TAMBAHAN.setHasFixedSize(true)

        adapterTambahan = AdapterPilihLayananTambahan(listTambahanDipilih)
        rvLAYANAN_TAMBAHAN.adapter = adapterTambahan

        btPILIH_PELANGGAN.setOnClickListener {
            val intent = Intent(this, activity_pilih_pelanggan::class.java)
            startActivityForResult(intent, pilihPelanggan)
        }

        btPILIH_LAYANAN.setOnClickListener {
            val intent = Intent(this, activity_pilih_layanan::class.java)
            startActivityForResult(intent, pilihLayanan)
        }

        btTAMBAHAN.setOnClickListener {
            val intent = Intent(this, Activity_pilih_layanan_tambahan::class.java)
            if (idCabang != "null" && idCabang.isNotEmpty()) {
                intent.putExtra("idCabang", idCabang)
            } else {
                Toast.makeText(this, "ID Cabang tidak ditemukan, data mungkin tidak difilter.", Toast.LENGTH_LONG).show()
                Log.w("TransaksiActivity", "idCabang is null or empty when launching Activity_pilih_layanan_tambahan")
            }
            startActivityForResult(intent, pilihLayananTambahan)
        }

        // --- Logika untuk Tombol PROSES ---
        btPROSES.setOnClickListener {
            // Validasi data yang diperlukan sebelum melanjutkan
            if (namaPelanggan.isEmpty() || namaLayanan.isEmpty()) {
                Toast.makeText(this, "Mohon lengkapi data pelanggan dan layanan utama.", Toast.LENGTH_SHORT).show()
            } else {
                val intent = Intent(this, KonfirmasiDataTransaksi::class.java)

                // Kirim data pelanggan dengan ID
                intent.putExtra("idPelanggan", idPelanggan) // TAMBAHKAN INI
                intent.putExtra("nama_pelanggan", namaPelanggan)
                intent.putExtra("nomor_hp", noHP)

                // Kirim data layanan utama dengan ID
                intent.putExtra("idLayanan", idLayanan) // TAMBAHKAN INI
                intent.putExtra("nama_layanan", namaLayanan)
                intent.putExtra("harga_layanan", hargaLayanan)

                // Kirim daftar layanan tambahan
                intent.putExtra("layanan_tambahan", ArrayList(listTambahanDipilih) as Serializable)

                startActivity(intent)
            }
        }

        // --- Akhir Logika Tombol PROSES ---

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val transaksiActivity = findViewById<ImageView>(R.id.Back)
        transaksiActivity.setOnClickListener{
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
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
        btPROSES = findViewById(R.id.btPROSES) // Pastikan ini diinisialisasi di sini
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
        } else if (requestCode == pilihLayanan) {
            if (resultCode == RESULT_OK && data != null) {
                idLayanan = data.getStringExtra("idLayanan").toString()
                val layanan = data.getStringExtra("nama")
                val harga = data.getStringExtra("harga")

                tvNAMA_LAYANAN.text = "Nama Layanan : $layanan"
                tvHARGA_LAYANAN.text = "Harga Layanan : $harga"

                namaLayanan = layanan.toString()
                hargaLayanan = harga.toString()
            }
            if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Batal Memilih Layanan", Toast.LENGTH_SHORT).show()
            }
        } else if (requestCode == pilihLayananTambahan) {
            if (resultCode == RESULT_OK && data != null) {
                val selectedTambahan = data.getSerializableExtra("selectedTambahan") as? modelTambahan

                selectedTambahan?.let { newTambahan ->
                    if (!listTambahanDipilih.any { it.idLayananTambahan == newTambahan.idLayananTambahan }) {
                        listTambahanDipilih.add(newTambahan)
                        adapterTambahan.notifyItemInserted(listTambahanDipilih.size - 1)
                        Toast.makeText(this, "${newTambahan.namaLayananTambahan} berhasil ditambahkan", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "${newTambahan.namaLayananTambahan} sudah ada di daftar", Toast.LENGTH_SHORT).show()
                    }
                } ?: run {
                    Toast.makeText(this, "Gagal mendapatkan data layanan tambahan", Toast.LENGTH_SHORT).show()
                }
            }
            if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Batal Memilih Layanan Tambahan", Toast.LENGTH_SHORT).show()
            }
        }
    }
}