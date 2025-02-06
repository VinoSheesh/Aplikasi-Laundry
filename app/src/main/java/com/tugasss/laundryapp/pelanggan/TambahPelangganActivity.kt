    package com.tugasss.laundryapp.pelanggan
    
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
    import com.tugasss.laundryapp.modeldata.modelPelanggan
    
    
    
    class TambahPelangganActivity : AppCompatActivity() {
        val database = FirebaseDatabase.getInstance()
        val myRef = database.getReference("pelanggan")
        lateinit var tvJudul: TextView
        lateinit var etNama: EditText
        lateinit var etAlamat: EditText
        lateinit var etNoHP: EditText
        lateinit var etCabang: EditText
        lateinit var btSimpan: Button
    
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            enableEdgeToEdge()
            setContentView(R.layout.activity_tambah_pelanggan)
            init()
            btSimpan.setOnClickListener{
                cekValidasi()
            }
            ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        }
    
        fun init() {
            tvJudul = findViewById(R.id.tvTAMBAH_PELANGGAN)
            etNama = findViewById(R.id.etNAMA_LENGKAP)
            etAlamat = findViewById(R.id.etALAMAT)
            etNoHP = findViewById(R.id.etNO_HP)
            etCabang = findViewById(R.id.etBRANCH)
            btSimpan = findViewById(R.id.btSIMPAN)
        }
    
        fun cekValidasi() {
            val nama = etNama.text.toString()
            val alamat = etAlamat.text.toString()
            val noHp = etNoHP.text.toString()
            val cabang = etCabang.text.toString()
    
            if (nama.isEmpty()) {
                etNama.error = this.getString(R.string.validasi_nama_pelanggan)
                Toast.makeText(this, this.getString(R.string.validasi_nama_pelanggan),Toast.LENGTH_SHORT).show()
                etNama.requestFocus()
                return
            }
            if (alamat.isEmpty()) {
                etAlamat.error = this.getString(R.string.validasi_alamat_pelanggan)
                Toast.makeText(this, this.getString(R.string.validasi_alamat_pelanggan),Toast.LENGTH_SHORT).show()
                etAlamat.requestFocus()
                return
            }
            if (noHp.isEmpty()) {
                etNoHP.error = this.getString(R.string.validasi_nohp_pelanggan)
                Toast.makeText(this, this.getString(R.string.validasi_alamat_pelanggan),Toast.LENGTH_SHORT).show()
                etNoHP.requestFocus()
                return
            }
            if (cabang.isEmpty()) {
                etCabang.error = this.getString(R.string.validasi_cabang_pelanggan)
                Toast.makeText(this, this.getString(R.string.validasi_alamat_pelanggan),Toast.LENGTH_SHORT).show()
                etCabang.requestFocus()
                return
            }
            simpan()
        }
    
        fun simpan() {
            val pelangganBaru = myRef.push()
            val pelangganid = pelangganBaru.key
            val data = modelPelanggan(
                pelangganid.toString(),
                etNama.text.toString(),
                etAlamat.text.toString(),
                etNoHP.text.toString(),
                etCabang.text.toString(),
            )
            pelangganBaru.setValue(data)
                .addOnSuccessListener {
                    Toast.makeText(
                        this,
                        this.getString(R.string.sukses_simpan_pelanggan),
                        Toast.LENGTH_SHORT
                    )
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(
                        this,
                        this.getString(R.string.gagal_simpan_pelanggan),
                        Toast.LENGTH_SHORT
                    )
                }
        }
    }