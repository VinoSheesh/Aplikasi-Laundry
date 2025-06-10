package com.tugasss.laundryapp.layanan

import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import android.widget.Button
import android.widget.Toast
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.database.FirebaseDatabase
import com.tugasss.laundryapp.R
import com.tugasss.laundryapp.modeldata.modelLayanan

class TambahLayananActivity : AppCompatActivity() {
    val database = FirebaseDatabase.getInstance()
    val myRef = database.getReference("layanan")
    lateinit var tvJudul: TextView
    lateinit var etNama: EditText
    lateinit var etHarga: EditText
    lateinit var etDurasi: EditText
    lateinit var btSimpan: Button
    lateinit var backButton: ImageView

    var layananId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_tambah_layanan)
        init()

        val dataIntent = intent
        if (dataIntent != null) {
            layananId = dataIntent.getStringExtra("id")
            val nama = dataIntent.getStringExtra("nama")
            val harga = dataIntent.getStringExtra("harga")
            val durasi = dataIntent.getStringExtra("durasi")

            if (layananId != null) {
                tvJudul.text = "Edit Data Layanan"
                btSimpan.text = "Update"
                etNama.setText(nama)
                etHarga.setText(harga)
                etDurasi.setText(durasi)
            }
        }

        btSimpan.setOnClickListener{
            cekValidasi()
        }

        backButton.setOnClickListener {
            finish()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    fun init() {
        tvJudul = findViewById(R.id.tvJudulHalaman)
        etNama = findViewById(R.id.etNAMA_LAYANAN)
        etHarga = findViewById(R.id.etHARGA)
        etDurasi = findViewById(R.id.etDURASI)
        btSimpan = findViewById(R.id.btSIMPAN)
        backButton = findViewById(R.id.Back)
        // Hapus baris: etCabang = findViewById(R.id.etBRANCH)
    }

    fun cekValidasi() {
        val nama = etNama.text.toString()
        val harga = etHarga.text.toString()
        val durasi = etDurasi.text.toString()

        if (nama.isEmpty()) {
            etNama.error = "Nama layanan harus diisi"
            Toast.makeText(this, "Nama layanan harus diisi", Toast.LENGTH_SHORT).show()
            etNama.requestFocus()
            return
        }
        if (harga.isEmpty()) {
            etHarga.error = "Harga layanan harus diisi"
            Toast.makeText(this, "Harga layanan harus diisi", Toast.LENGTH_SHORT).show()
            etHarga.requestFocus()
            return
        }
        if (durasi.isEmpty()) {
            etDurasi.error = "Durasi harus diisi"
            Toast.makeText(this, "Durasi harus diisi", Toast.LENGTH_SHORT).show()
            etDurasi.requestFocus()
            return
        }
        // Hapus validasi cabang

        if (layananId == null) {
            simpan()
        } else {
            update()
        }
    }

    fun simpan() {
        val layananBaru = myRef.push()
        val layananid = layananBaru.key
        val data = modelLayanan(
            layananid.toString(),
            etNama.text.toString(),
            etHarga.text.toString(),
            "", // Atau hapus parameter cabang jika tidak digunakan dalam model
            etDurasi.text.toString()
        )
        layananBaru.setValue(data)
            .addOnSuccessListener {
                Toast.makeText(
                    this,
                    "Data layanan berhasil disimpan",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(
                    this,
                    "Gagal menyimpan data layanan",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    fun update() {
        val dataUpdate = mapOf(
            "namaLayanan" to etNama.text.toString(),
            "hargaLayanan" to etHarga.text.toString(),
            "durasi" to etDurasi.text.toString()
            // Hapus: "idCabang" to etCabang.text.toString()
        )

        myRef.child(layananId ?: "").updateChildren(dataUpdate)
            .addOnSuccessListener {
                Toast.makeText(this, "Berhasil update data layanan", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal update data layanan", Toast.LENGTH_SHORT).show()
            }
    }
}