package com.tugasss.laundryapp.tambahan

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.database.FirebaseDatabase
import com.tugasss.laundryapp.HomeActivity
import com.tugasss.laundryapp.R
import com.tugasss.laundryapp.modeldata.modelTambahan

class TambahTambahanActivity : AppCompatActivity() {
    val database = FirebaseDatabase.getInstance()
    val myRef = database.getReference("tambahan")
    lateinit var tvJudul: TextView
    lateinit var etNama: EditText
    lateinit var etHarga: EditText
    lateinit var btSimpan: Button

    var tambahanId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_tambah_tambahan)
        init()

        val dataIntent = intent
        if (dataIntent != null) {
            tambahanId = dataIntent.getStringExtra("id")
            val nama = dataIntent.getStringExtra("nama")
            val harga = dataIntent.getStringExtra("harga")

            if (tambahanId != null) {
                tvJudul.text = "Edit Data Layanan Tambahan"
                btSimpan.text = "Update"
                etNama.setText(nama)
                etHarga.setText(harga)
            }
        }

        btSimpan.setOnClickListener{
            cekValidasi()
        }
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val back = findViewById<ImageView>(R.id.Back)
        back.setOnClickListener{
            val intent = Intent(this, DataTambahanActivity::class.java)
            startActivity(intent)
        }
    }

    fun init() {
        tvJudul = findViewById(R.id.tvJudulHalaman)
        etNama = findViewById(R.id.etNAMA_LAYANAN_TAMBAHAN)
        etHarga = findViewById(R.id.etHARGA)
        btSimpan = findViewById(R.id.btSIMPAN)
    }

    fun cekValidasi() {
        val nama = etNama.text.toString()
        val harga = etHarga.text.toString()

        if (nama.isEmpty()) {
            etNama.error = this.getString(R.string.validasi_nama_pegawai)
            Toast.makeText(this, this.getString(R.string.validasi_nama_layanan), Toast.LENGTH_SHORT).show()
            etNama.requestFocus()
            return
        }
        if (harga.isEmpty()) {
            etHarga.error = this.getString(R.string.validasi_nohp_pegawai)
            Toast.makeText(this, this.getString(R.string.validasi_harga_layanan), Toast.LENGTH_SHORT).show()
            etHarga.requestFocus()
            return
        }

        // Kalau tambahanId null ➔ tambah data baru
        if (tambahanId == null) {
            simpan()
        } else {
            update()
        }
    }

    fun simpan() {
        val tambahanBaru = myRef.push()
        val tambahanid = tambahanBaru.key
        val data = modelTambahan(
            tambahanid.toString(),
            "", // PERBAIKAN: Set idCabang sebagai string kosong atau null
            etNama.text.toString(),
            etHarga.text.toString()
        )
        tambahanBaru.setValue(data)
            .addOnSuccessListener {
                Toast.makeText(
                    this,
                    "Berhasil menyimpan layanan tambahan", // PERBAIKAN: Pesan yang lebih spesifik
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(
                    this,
                    "Gagal menyimpan layanan tambahan: ${exception.message}", // PERBAIKAN: Tampilkan error detail
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    fun update() {
        val dataUpdate = mapOf(
            "namaLayananTambahan" to etNama.text.toString(),
            "hargaLayananTambahan" to etHarga.text.toString()
        )

        myRef.child(tambahanId ?: "").updateChildren(dataUpdate)
            .addOnSuccessListener {
                Toast.makeText(this, "Berhasil update data layanan tambahan", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Gagal update data layanan tambahan: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }
}