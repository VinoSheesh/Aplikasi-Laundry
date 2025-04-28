package com.tugasss.laundryapp.pegawai

import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.database.FirebaseDatabase
import com.tugasss.laundryapp.R
import com.tugasss.laundryapp.modeldata.modelPegawai
import com.google.firebase.Timestamp

class TambahPegawaiActivity : AppCompatActivity() {
    val database = FirebaseDatabase.getInstance()
    val myRef = database.getReference("pegawai")
    lateinit var tvJudul: TextView
    lateinit var etNama: EditText
    lateinit var etAlamat: EditText
    lateinit var etNoHP: EditText
    lateinit var etCabang: EditText
    lateinit var btSimpan: Button

    var idPegawai: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_tambah_pegawai)
        init()
        getData()
        btSimpan.setOnClickListener {
            cekValidasi()
        }
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    fun init() {
        tvJudul = findViewById(R.id.tvTAMBAH_PEGAWAI)
        etNama = findViewById(R.id.etNAMA_LENGKAP)
        etAlamat = findViewById(R.id.etALAMAT)
        etNoHP = findViewById(R.id.etNO_HP)
        etCabang = findViewById(R.id.etBRANCH)
        btSimpan = findViewById(R.id.btSIMPAN)
    }

    fun getData() {
        idPegawai = intent.getStringExtra("idPegawai").toString()
        val judul = intent.getStringExtra("Judul")
        val nama = intent.getStringExtra("namaPegawai")
        val alamat = intent.getStringExtra("alamatPegawai")
        val hp = intent.getStringExtra("noHPPegawai")
        val cabang = intent.getStringExtra("idCabang")

        tvJudul.text = judul
        etNama.setText(nama)
        etAlamat.setText(alamat)
        etNoHP.setText(hp)
        etCabang.setText(cabang)

        // Memastikan mode tambah atau edit
        if (judul != null && judul != getString(R.string.activity_tambah_pegawai)) {
            if (judul == "Edit Pegawai") {
                btSimpan.text = "Sunting"
                // Aktifkan inputan langsung saat edit
                hidup()
            }
        } else {
            btSimpan.text = "Simpan"
            // Aktifkan inputan saat tambah pegawai
            hidup()
            etNama.requestFocus()  // Fokus ke input nama
        }
    }

    fun hidup() {
        etNama.isEnabled = true
        etAlamat.isEnabled = true
        etNoHP.isEnabled = true
        etCabang.isEnabled = true
    }

    fun mati() {
        etNama.isEnabled = false
        etAlamat.isEnabled = false
        etNoHP.isEnabled = false
        etCabang.isEnabled = false
    }

    fun update() {
        val pegawaiRef = database.getReference("pegawai").child(idPegawai)
        val updateData = mutableMapOf<String, Any>()
        updateData["namaPegawai"] = etNama.text.toString()
        updateData["alamatPegawai"] = etAlamat.text.toString()
        updateData["noHPPegawai"] = etNoHP.text.toString()
        updateData["idCabang"] = etCabang.text.toString()

        pegawaiRef.updateChildren(updateData).addOnSuccessListener {
            Toast.makeText(this@TambahPegawaiActivity, "Data Pegawai Berhasil Diperbarui", Toast.LENGTH_SHORT).show()
            finish()
        }.addOnFailureListener {
            Toast.makeText(this@TambahPegawaiActivity, "Data Pegawai Gagal Diperbarui", Toast.LENGTH_SHORT).show()
        }
    }

    fun cekValidasi() {
        val nama = etNama.text.toString()
        val alamat = etAlamat.text.toString()
        val noHp = etNoHP.text.toString()
        val cabang = etCabang.text.toString()

        if (nama.isEmpty()) {
            etNama.error = this.getString(R.string.validasi_nama_pegawai)
            Toast.makeText(this, this.getString(R.string.validasi_nama_pegawai), Toast.LENGTH_SHORT).show()
            etNama.requestFocus()
            return
        }
        if (alamat.isEmpty()) {
            etAlamat.error = this.getString(R.string.validasi_alamat_pegawai)
            Toast.makeText(this, this.getString(R.string.validasi_alamat_pegawai), Toast.LENGTH_SHORT).show()
            etAlamat.requestFocus()
            return
        }
        if (noHp.isEmpty()) {
            etNoHP.error = this.getString(R.string.validasi_nohp_pegawai)
            Toast.makeText(this, this.getString(R.string.validasi_nohp_pegawai), Toast.LENGTH_SHORT).show()
            etNoHP.requestFocus()
            return
        }
        if (cabang.isEmpty()) {
            etCabang.error = this.getString(R.string.validasi_cabang_pegawai)
            Toast.makeText(this, this.getString(R.string.validasi_cabang_pegawai), Toast.LENGTH_SHORT).show()
            etCabang.requestFocus()
            return
        }

        // Cek tombol jika sedang dalam mode edit
        when (btSimpan.text.toString()) {
            "Sunting" -> {
                btSimpan.text = "Update"
                hidup()  // Mengaktifkan input untuk edit
            }
            "Update" -> {
                update()  // Memperbarui data
            }
            "Simpan" -> {
                simpan()  // Menyimpan data baru
            }
        }
    }

    fun simpan() {
        val pegawaiBaru = myRef.push()
        val pegawaiId = pegawaiBaru.key
        val data = modelPegawai(
            pegawaiId.toString(),
            etNama.text.toString(),
            etAlamat.text.toString(),
            etNoHP.text.toString(),
            etCabang.text.toString()
        )

        pegawaiBaru.setValue(data)
            .addOnSuccessListener {
                Toast.makeText(this, this.getString(R.string.sukes_simpan_pegawai), Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, this.getString(R.string.gagal_simpan_pegawai), Toast.LENGTH_SHORT).show()
            }
    }
}
