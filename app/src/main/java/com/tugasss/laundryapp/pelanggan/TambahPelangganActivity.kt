package com.tugasss.laundryapp.pelanggan

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.ArrayAdapter
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.tugasss.laundryapp.R
import com.tugasss.laundryapp.modeldata.modelPelanggan
import com.tugasss.laundryapp.modeldata.modelCabang
import com.tugasss.laundryapp.tambahan.DataTambahanActivity

class TambahPelangganActivity : AppCompatActivity() {
    val database = FirebaseDatabase.getInstance()
    val myRef = database.getReference("pelanggan")
    val cabangRef = database.getReference("cabang")

    private lateinit var tvJudul: TextView
    private lateinit var etNama: EditText
    private lateinit var etAlamat: EditText
    private lateinit var etNoHP: EditText
    private lateinit var spinnerCabang: Spinner
    private lateinit var btSimpan: Button
    private lateinit var rgJenisKelamin: RadioGroup

    var pelangganId: String? = null
    var cabangList: ArrayList<modelCabang> = arrayListOf()
    var cabangAdapter: ArrayAdapter<String>? = null
    var selectedCabangId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_tambah_pelanggan)

        // Initialize views dengan null check
        if (!initViews()) {
            Toast.makeText(this, "Error: Layout tidak dapat dimuat dengan benar", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        loadCabangData()

        handleIntentData()
        setupClickListeners()
        setupWindowInsets()
    }

    private fun initViews(): Boolean {
        return try {
            tvJudul = findViewById(R.id.tvJudulHalaman) ?: return false
            etNama = findViewById(R.id.etNAMA_LENGKAP) ?: return false
            etAlamat = findViewById(R.id.etALAMAT) ?: return false
            etNoHP = findViewById(R.id.etNO_HP) ?: return false
            spinnerCabang = findViewById(R.id.spinnerCabang) ?: return false
            btSimpan = findViewById(R.id.btSIMPAN) ?: return false
            rgJenisKelamin = findViewById(R.id.rgJENIS_KELAMIN) ?: return false
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun handleIntentData() {
        val dataIntent = intent
        if (dataIntent != null) {
            pelangganId = dataIntent.getStringExtra("id")
            val nama = dataIntent.getStringExtra("nama")
            val alamat = dataIntent.getStringExtra("alamat")
            val noHp = dataIntent.getStringExtra("nohp")
            val cabang = dataIntent.getStringExtra("cabang")
            val jenisKelamin = dataIntent.getStringExtra("jeniskelamin")

            if (pelangganId != null) {
                tvJudul.text = "Edit Data Pelanggan"
                btSimpan.text = "Update"
                etNama.setText(nama)
                etAlamat.setText(alamat)
                etNoHP.setText(noHp)
                selectedCabangId = cabang

                // Set radio button berdasarkan jenis kelamin
                when (jenisKelamin) {
                    "pria" -> rgJenisKelamin.check(R.id.rbPRIA)
                    "wanita" -> rgJenisKelamin.check(R.id.rbWANITA)
                }
            }
        }
    }

    private fun setupClickListeners() {
        btSimpan.setOnClickListener {
            cekValidasi()
        }

        val kembali = findViewById<ImageView>(R.id.Back)
        kembali?.setOnClickListener {
            val intent = Intent(this, DataPelangganActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupWindowInsets() {
        val mainView = findViewById<androidx.constraintlayout.widget.ConstraintLayout>(R.id.main)
        mainView?.let { view ->
            ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        }
    }

    fun loadCabangData() {
        cabangRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    cabangList.clear()
                    val cabangNames = mutableListOf<String>()
                    cabangNames.add("Pilih Cabang") // Item pertama sebagai placeholder

                    for (dataSnapshot in snapshot.children) {
                        val cabang = dataSnapshot.getValue(modelCabang::class.java)
                        if (cabang != null) {
                            cabangList.add(cabang)
                            cabangNames.add(cabang.namaCabang ?: "")
                        }
                    }

                    // Setup adapter untuk spinner
                    cabangAdapter = ArrayAdapter(
                        this@TambahPelangganActivity,
                        android.R.layout.simple_spinner_item,
                        cabangNames
                    )
                    cabangAdapter?.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spinnerCabang.adapter = cabangAdapter

                    // Set selected cabang jika dalam mode edit
                    if (selectedCabangId != null) {
                        for (i in cabangList.indices) {
                            if (cabangList[i].namaCabang == selectedCabangId) {
                                spinnerCabang.setSelection(i + 1) // +1 karena ada "Pilih Cabang" di index 0
                                break
                            }
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@TambahPelangganActivity, "Gagal memuat data cabang: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    fun cekValidasi() {
        val nama = etNama.text.toString()
        val alamat = etAlamat.text.toString()
        val noHp = etNoHP.text.toString()

        if (nama.isEmpty()) {
            etNama.error = this.getString(R.string.validasi_nama_pelanggan)
            Toast.makeText(this, this.getString(R.string.validasi_nama_pelanggan), Toast.LENGTH_SHORT).show()
            etNama.requestFocus()
            return
        }
        if (alamat.isEmpty()) {
            etAlamat.error = this.getString(R.string.validasi_alamat_pelanggan)
            Toast.makeText(this, this.getString(R.string.validasi_alamat_pelanggan), Toast.LENGTH_SHORT).show()
            etAlamat.requestFocus()
            return
        }
        if (noHp.isEmpty()) {
            etNoHP.error = this.getString(R.string.validasi_nohp_pelanggan)
            Toast.makeText(this, this.getString(R.string.validasi_nohp_pelanggan), Toast.LENGTH_SHORT).show()
            etNoHP.requestFocus()
            return
        }

        // Validasi spinner cabang
        if (spinnerCabang.selectedItemPosition == 0) {
            Toast.makeText(this, "Pilih cabang terlebih dahulu", Toast.LENGTH_SHORT).show()
            return
        }

        // Validasi jenis kelamin
        if (rgJenisKelamin.checkedRadioButtonId == -1) {
            Toast.makeText(this, "Pilih jenis kelamin", Toast.LENGTH_SHORT).show()
            return
        }

        // Kalau pelangganId null ➔ tambah data baru
        if (pelangganId == null) {
            simpan()
        } else {
            update()
        }
    }

    fun getSelectedCabangName(): String {
        val selectedPosition = spinnerCabang.selectedItemPosition
        return if (selectedPosition > 0 && selectedPosition <= cabangList.size) {
            cabangList[selectedPosition - 1].namaCabang ?: ""
        } else {
            ""
        }
    }

    fun getJenisKelamin(): String {
        return when (rgJenisKelamin.checkedRadioButtonId) {
            R.id.rbPRIA -> "pria"
            R.id.rbWANITA -> "wanita"
            else -> ""
        }
    }

    fun simpan() {
        val pelangganBaru = myRef.push()
        val pelangganid = pelangganBaru.key
        val data = modelPelanggan(
            pelangganid.toString(),
            etNama.text.toString(),
            etAlamat.text.toString(),
            etNoHP.text.toString(),
            getSelectedCabangName(),
            getJenisKelamin()
        )
        pelangganBaru.setValue(data)
            .addOnSuccessListener {
                Toast.makeText(this, this.getString(R.string.sukses_simpan_pelanggan), Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, this.getString(R.string.gagal_simpan_pelanggan), Toast.LENGTH_SHORT).show()
            }
    }

    fun update() {
        val dataUpdate = mapOf(
            "namaPelanggan" to etNama.text.toString(),
            "alamatPelanggan" to etAlamat.text.toString(),
            "noHPPelanggan" to etNoHP.text.toString(),
            "idCabang" to getSelectedCabangName(),
            "jenisKelamin" to getJenisKelamin()
        )

        myRef.child(pelangganId ?: "").updateChildren(dataUpdate)
            .addOnSuccessListener {
                Toast.makeText(this, "Berhasil update data pelanggan", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal update data pelanggan", Toast.LENGTH_SHORT).show()
            }
    }
}