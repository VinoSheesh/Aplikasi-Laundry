package com.tugasss.laundryapp.cabang

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
import com.tugasss.laundryapp.R
import com.tugasss.laundryapp.modeldata.modelCabang

class TambahCabangActivity : AppCompatActivity() {
    val database = FirebaseDatabase.getInstance()
    val myRef = database.getReference("cabang")

    lateinit var tvJudul: TextView
    lateinit var etNamaCabang: EditText
    lateinit var etAlamat: EditText
    lateinit var etNoHP: EditText
    lateinit var btSimpan: Button
    lateinit var backButton: ImageView

    var cabangId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_tambah_cabang)
        init()

        val dataIntent = intent
        if (dataIntent != null) {
            val judul = dataIntent.getStringExtra("Judul")
            cabangId = dataIntent.getStringExtra("idCabang")
            val nama = dataIntent.getStringExtra("namaCabang")
            val alamat = dataIntent.getStringExtra("alamatCabang")
            val kontak = dataIntent.getStringExtra("kontakCabang")

            if (cabangId != null) {
                tvJudul.text = judul ?: "Edit Cabang"
                btSimpan.text = "Update"
                etNamaCabang.setText(nama)
                etAlamat.setText(alamat)
                etNoHP.setText(kontak)
            } else {
                tvJudul.text = "Tambah Cabang"
                btSimpan.text = "Simpan"
            }
        }

        btSimpan.setOnClickListener {
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
        tvJudul = findViewById(R.id.tvJudul)
        etNamaCabang = findViewById(R.id.etNAMA_CABANG)
        etAlamat = findViewById(R.id.etALAMAT)
        etNoHP = findViewById(R.id.etNO_HP)
        btSimpan = findViewById(R.id.btSIMPAN)
        backButton = findViewById(R.id.Back)
    }

    fun cekValidasi() {
        val nama = etNamaCabang.text.toString().trim()
        val alamat = etAlamat.text.toString().trim()
        val kontak = etNoHP.text.toString().trim()

        if (nama.isEmpty()) {
            etNamaCabang.error = "Nama cabang tidak boleh kosong"
            Toast.makeText(this, "Nama cabang tidak boleh kosong", Toast.LENGTH_SHORT).show()
            etNamaCabang.requestFocus()
            return
        }
        if (alamat.isEmpty()) {
            etAlamat.error = "Alamat tidak boleh kosong"
            Toast.makeText(this, "Alamat tidak boleh kosong", Toast.LENGTH_SHORT).show()
            etAlamat.requestFocus()
            return
        }
        if (kontak.isEmpty()) {
            etNoHP.error = "Kontak tidak boleh kosong"
            Toast.makeText(this, "Kontak tidak boleh kosong", Toast.LENGTH_SHORT).show()
            etNoHP.requestFocus()
            return
        }

        // Kalau cabangId null → tambah data baru
        if (cabangId == null) {
            simpan()
        } else {
            update()
        }
    }

    fun simpan() {
        val cabangBaru = myRef.push()
        val cabangid = cabangBaru.key
        val data = modelCabang(
            cabangid.toString(),
            etNamaCabang.text.toString().trim(),
            etAlamat.text.toString().trim(),
            etNoHP.text.toString().trim()
        )
        cabangBaru.setValue(data)
            .addOnSuccessListener {
                Toast.makeText(this, "Berhasil menambah cabang", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal menambah cabang", Toast.LENGTH_SHORT).show()
            }
    }

    fun update() {
        val dataUpdate = mapOf(
            "namaCabang" to etNamaCabang.text.toString().trim(),
            "alamatCabang" to etAlamat.text.toString().trim(),
            "kontakCabang" to etNoHP.text.toString().trim()
        )

        myRef.child(cabangId ?: "").updateChildren(dataUpdate)
            .addOnSuccessListener {
                Toast.makeText(this, "Berhasil update data cabang", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal update data cabang", Toast.LENGTH_SHORT).show()
            }
    }
}