package com.tugasss.laundryapp.transaksi

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.tugasss.laundryapp.HomeActivity
import com.tugasss.laundryapp.R
import com.tugasss.laundryapp.adapter.AdapterPilihLayananTambahan
import com.tugasss.laundryapp.modeldata.modelTambahan
import com.tugasss.laundryapp.modeldata.modelPegawai
import com.tugasss.laundryapp.modeldata.modelCabang
import com.tugasss.laundryapp.modeldata.modelTransaksi
import java.io.Serializable
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class KonfirmasiDataTransaksi : AppCompatActivity() {

    private lateinit var textViewNamaPelanggan: TextView
    private lateinit var textViewNomorHP: TextView
    private lateinit var textViewNamaLayanan: TextView
    private lateinit var textViewHargaLayanan: TextView
    private lateinit var recyclerViewLayananTambahan: RecyclerView
    private lateinit var textViewTotalHarga: TextView
    private lateinit var buttonBatal: Button
    private lateinit var buttonPembayaran: Button

    // Spinner untuk pegawai dan cabang
    private lateinit var spinnerPegawai: Spinner
    private lateinit var spinnerCabang: Spinner

    // Firebase references
    private val database = FirebaseDatabase.getInstance()
    private val pegawaiRef = database.getReference("pegawai")
    private val cabangRef = database.getReference("cabang")

    // Data lists untuk spinner
    private val pegawaiList = arrayListOf<modelPegawai>()
    private val cabangList = arrayListOf<modelCabang>()

    // Adapter untuk spinner
    private lateinit var pegawaiAdapter: ArrayAdapter<String>
    private lateinit var cabangAdapter: ArrayAdapter<String>

    // Selected values
    private var selectedPegawaiId: String = ""
    private var selectedCabangId: String = ""

    // Menggunakan AdapterDataTambahan yang sudah dimodifikasi
    private lateinit var adapterLayananTambahan: AdapterPilihLayananTambahan

    private val listLayananTambahan = mutableListOf<modelTambahan>() // UBAH KE MUTABLELIST

    // Data transaksi yang akan disimpan atau dikirim ke invoice
    private var idPelanggan: String = ""
    private var namaPelanggan: String = ""
    private var noHP: String = ""
    private var idLayanan: String = ""
    private var namaLayanan: String = ""
    private var hargaLayanan: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_konfirmasi_data_transaksi)

        // Inisialisasi views
        initViews()
        setupRecyclerView()
        setupSpinners()
        getDataFromIntent()
        setupButtonListeners()

        // Load data untuk spinner
        loadPegawaiData()
        loadCabangData()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.konfirmasi_transaksi)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val konfirmasiDataTransaksi = findViewById<ImageView>(R.id.Back)
        konfirmasiDataTransaksi.setOnClickListener{
            val intent = Intent(this, transaksi::class.java)
            startActivity(intent)
        }
    }

    private fun initViews() {
        textViewNamaPelanggan = findViewById(R.id.tvNAMA_PELANGGAN)
        textViewNomorHP = findViewById(R.id.tvNO_HP)
        textViewNamaLayanan = findViewById(R.id.tvNAMA_LAYANAN)
        textViewHargaLayanan = findViewById(R.id.tvHARGA_LAYANAN)
        recyclerViewLayananTambahan = findViewById(R.id.rvLAYANAN_TAMBAHAN)
        textViewTotalHarga = findViewById(R.id.textViewTotalHarga)
        buttonBatal = findViewById(R.id.buttonBatal)
        buttonPembayaran = findViewById(R.id.buttonPembayaran)
        spinnerPegawai = findViewById(R.id.spinnerPegawai)
        spinnerCabang = findViewById(R.id.spinnerCabang)
    }

    private fun setupSpinners() {
        // Setup Pegawai Spinner
        pegawaiAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, arrayListOf<String>())
        pegawaiAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerPegawai.adapter = pegawaiAdapter

        spinnerPegawai.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position > 0 && pegawaiList.size > position - 1) { // Skip "Pilih Pegawai"
                    selectedPegawaiId = pegawaiList[position - 1].idPegawai ?: ""
                    Log.d("SpinnerPegawai", "Selected Pegawai ID: $selectedPegawaiId")
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Setup Cabang Spinner
        cabangAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, arrayListOf<String>())
        cabangAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCabang.adapter = cabangAdapter

        spinnerCabang.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position > 0 && cabangList.size > position - 1) { // Skip "Pilih Cabang"
                    selectedCabangId = cabangList[position - 1].idCabang ?: ""
                    Log.d("SpinnerCabang", "Selected Cabang ID: $selectedCabangId")
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun loadPegawaiData() {
        pegawaiRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                pegawaiList.clear()
                val pegawaiNames = arrayListOf<String>()
                pegawaiNames.add("Pilih Pegawai") // Default option

                if (snapshot.exists()) {
                    for (dataSnapshot in snapshot.children) {
                        val pegawai = dataSnapshot.getValue(modelPegawai::class.java)
                        pegawai?.let {
                            pegawaiList.add(it)
                            // Tampilkan nama pegawai di spinner
                            pegawaiNames.add(it.namaPegawai ?: "Nama tidak tersedia")
                        }
                    }
                }

                // Update adapter
                pegawaiAdapter.clear()
                pegawaiAdapter.addAll(pegawaiNames)
                pegawaiAdapter.notifyDataSetChanged()

                Log.d("LoadPegawai", "Loaded ${pegawaiList.size} pegawai")
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("LoadPegawai", "Error loading pegawai: ${error.message}")
                Toast.makeText(this@KonfirmasiDataTransaksi, "Error loading pegawai data", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun loadCabangData() {
        cabangRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                cabangList.clear()
                val cabangNames = arrayListOf<String>()
                cabangNames.add("Pilih Cabang") // Default option

                if (snapshot.exists()) {
                    for (dataSnapshot in snapshot.children) {
                        val cabang = dataSnapshot.getValue(modelCabang::class.java)
                        cabang?.let {
                            cabangList.add(it)
                            // Tampilkan nama cabang di spinner
                            cabangNames.add(it.namaCabang ?: "Cabang tidak tersedia")
                        }
                    }
                }

                // Update adapter
                cabangAdapter.clear()
                cabangAdapter.addAll(cabangNames)
                cabangAdapter.notifyDataSetChanged()

                Log.d("LoadCabang", "Loaded ${cabangList.size} cabang")
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("LoadCabang", "Error loading cabang: ${error.message}")
                Toast.makeText(this@KonfirmasiDataTransaksi, "Error loading cabang data", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setupRecyclerView() {
        // PERBAIKAN: Inisialisasi adapter dengan list kosong dulu
        adapterLayananTambahan = AdapterPilihLayananTambahan(listLayananTambahan)
        recyclerViewLayananTambahan.layoutManager = LinearLayoutManager(this)
        recyclerViewLayananTambahan.adapter = adapterLayananTambahan
    }

    private fun getDataFromIntent() {
        // Mengambil data dari Intent
        idPelanggan = intent.getStringExtra("idPelanggan") ?: ""
        namaPelanggan = intent.getStringExtra("nama_pelanggan") ?: "Tidak ada data"
        noHP = intent.getStringExtra("nomor_hp") ?: "Tidak ada data"
        idLayanan = intent.getStringExtra("idLayanan") ?: ""
        namaLayanan = intent.getStringExtra("nama_layanan") ?: "Tidak ada data"
        hargaLayanan = intent.getStringExtra("harga_layanan") ?: "0"

        // PERBAIKAN: Mengambil data layanan tambahan
        @Suppress("UNCHECKED_CAST")
        val tambahanSerializable = intent.getSerializableExtra("layanan_tambahan")

        // Debug log untuk memeriksa data
        Log.d("KonfirmasiData", "Received tambahan: $tambahanSerializable")

        when (tambahanSerializable) {
            is ArrayList<*> -> {
                try {
                    @Suppress("UNCHECKED_CAST")
                    val layananTambahanList = tambahanSerializable as ArrayList<modelTambahan>
                    Log.d("KonfirmasiData", "Successfully cast to ArrayList<modelTambahan>, size: ${layananTambahanList.size}")

                    // Update tampilan
                    textViewNamaPelanggan.text = namaPelanggan
                    textViewNomorHP.text = noHP
                    textViewNamaLayanan.text = namaLayanan
                    textViewHargaLayanan.text = formatRupiah(extractNumber(hargaLayanan))

                    // PERBAIKAN: Update list dan adapter
                    listLayananTambahan.clear()
                    listLayananTambahan.addAll(layananTambahanList)

                    // Notify adapter bahwa data sudah berubah
                    adapterLayananTambahan.notifyDataSetChanged()

                    Log.d("KonfirmasiData", "Updated listLayananTambahan size: ${listLayananTambahan.size}")

                    // Hitung total harga
                    hitungTotalHarga(hargaLayanan, layananTambahanList)

                } catch (e: ClassCastException) {
                    Log.e("KonfirmasiData", "Failed to cast to ArrayList<modelTambahan>: ${e.message}")
                    // Set data tanpa layanan tambahan
                    setDataTanpaLayananTambahan()
                }
            }
            else -> {
                Log.w("KonfirmasiData", "No layanan tambahan data received or wrong type")
                setDataTanpaLayananTambahan()
            }
        }
    }

    private fun setDataTanpaLayananTambahan() {
        textViewNamaPelanggan.text = namaPelanggan
        textViewNomorHP.text = noHP
        textViewNamaLayanan.text = namaLayanan
        textViewHargaLayanan.text = formatRupiah(extractNumber(hargaLayanan))

        listLayananTambahan.clear()
        adapterLayananTambahan.notifyDataSetChanged()

        hitungTotalHarga(hargaLayanan, ArrayList())
    }

    private fun hitungTotalHarga(hargaLayananString: String, layananTambahan: ArrayList<modelTambahan>) {
        var total = extractNumber(hargaLayananString)

        for (layanan in layananTambahan) {
            total += extractNumber(layanan.hargaLayananTambahan ?: "0")
        }
        textViewTotalHarga.text = formatRupiah(total)
    }

    // Helper function untuk mengekstrak angka dari string harga
    private fun extractNumber(hargaString: String): Double {
        val cleaned = hargaString.replace("Rp", "").replace(".", "").replace(",", ".").trim()
        return cleaned.toDoubleOrNull() ?: 0.0
    }

    // Helper function untuk format angka menjadi Rupiah
    private fun formatRupiah(amount: Double): String {
        val localeID = Locale("in", "ID")
        val formatRupiah = NumberFormat.getCurrencyInstance(localeID)
        return formatRupiah.format(amount)
    }

    private fun generateRandomId(): String {
        return "-INV" + UUID.randomUUID().toString().take(8).uppercase(Locale.getDefault())
    }

    // PERBAIKAN UTAMA: Fungsi untuk menyimpan transaksi dengan status yang konsisten
    private fun saveTransaksiToDatabase(
        idTransaksi: String,
        metodePembayaran: String,
        totalBayar: Double
    ) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            Toast.makeText(this, "User tidak terautentikasi", Toast.LENGTH_SHORT).show()
            return
        }

        val existingTransaksiId = intent.getStringExtra("id_transaksi")
        val tipePembayaran = intent.getStringExtra("tipe_pembayaran")

        val finalTransaksiId = if (!existingTransaksiId.isNullOrEmpty()) {
            existingTransaksiId
        } else {
            idTransaksi
        }

        // PERBAIKAN: Mapping status yang konsisten dengan filter di laporan
        val statusPembayaran = when {
            tipePembayaran == "LUNASI" -> "Sudah Bayar"
            metodePembayaran == "Pembayaran DP" -> "Belum Bayar" // DP masih dianggap belum lunas
            metodePembayaran in listOf(
                "Lunas Cash / Tunai",
                "Link Aja Payment",
                "Dana Payment",
                "GOPAY",
                "OVO Payment"
            ) -> "Sudah Bayar"
            else -> "Belum Bayar"
        }

        val transaksiRef = database.getReference("transaksi")
            .child(currentUser.uid)
            .child(finalTransaksiId)

        if (!existingTransaksiId.isNullOrEmpty() && tipePembayaran == "LUNASI") {
            val updates = hashMapOf<String, Any>(
                "metodePembayaran" to metodePembayaran,
                "statusPembayaran" to "Sudah Bayar",
                "tanggalPelunasan" to getCurrentDateTime(),
                "sisaPembayaran" to 0.0
            )

            transaksiRef.updateChildren(updates)
                .addOnSuccessListener {
                    Log.d("UpdateTransaksi", "Transaksi berhasil dilunasi: $finalTransaksiId")
                    Toast.makeText(this, "Transaksi berhasil dilunasi", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { error ->
                    Log.e("UpdateTransaksi", "Gagal melunasi transaksi: ${error.message}")
                    Toast.makeText(this, "Gagal melunasi transaksi: ${error.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            // PERBAIKAN: Hitung sisa pembayaran dengan benar
            val totalHargaSebenarnya = extractNumber(textViewTotalHarga.text.toString())
            val sisaPembayaran = if (statusPembayaran == "Belum Bayar" && metodePembayaran == "Pembayaran DP") {
                totalHargaSebenarnya - totalBayar // Sisa yang harus dibayar
            } else {
                0.0
            }

            val transaksi = modelTransaksi(
                idTransaksi = finalTransaksiId,
                idPelanggan = idPelanggan,
                namaPelanggan = namaPelanggan,
                noHP = noHP,
                idLayanan = idLayanan,
                namaLayanan = namaLayanan,
                hargaLayanan = hargaLayanan,
                layananTambahan = ArrayList(listLayananTambahan),
                subtotalTambahan = hitungSubtotalTambahan(),
                totalBayar = totalHargaSebenarnya, // Total harga sebenarnya
                jumlahBayar = totalBayar, // Jumlah yang dibayar (bisa DP atau lunas)
                sisaPembayaran = sisaPembayaran,
                metodePembayaran = metodePembayaran,
                statusPembayaran = statusPembayaran,
                statusPesanan = "Pending",
                idPegawai = selectedPegawaiId,
                namaPegawai = if (spinnerPegawai.selectedItemPosition > 0) {
                    pegawaiList[spinnerPegawai.selectedItemPosition - 1].namaPegawai
                } else "",
                idCabang = selectedCabangId,
                namaCabang = if (spinnerCabang.selectedItemPosition > 0) {
                    cabangList[spinnerCabang.selectedItemPosition - 1].namaCabang
                } else "",
                tanggalTransaksi = getCurrentDateTime(),
                tanggalPelunasan = if (statusPembayaran == "Sudah Bayar") getCurrentDateTime() else null,
                tanggalSelesai = null,
                catatan = null
            )

            transaksiRef.setValue(transaksi)
                .addOnSuccessListener {
                    Log.d("SaveTransaksi", "Transaksi berhasil disimpan: $finalTransaksiId")
                    Toast.makeText(this, "Transaksi berhasil disimpan", Toast.LENGTH_SHORT).show()

                    // TAMBAHAN: Log untuk debugging
                    Log.d("SaveTransaksi", "Status: ${transaksi.statusPembayaran}")
                    Log.d("SaveTransaksi", "Method: ${transaksi.metodePembayaran}")
                    Log.d("SaveTransaksi", "Total: ${transaksi.totalBayar}")
                    Log.d("SaveTransaksi", "Bayar: ${transaksi.jumlahBayar}")
                    Log.d("SaveTransaksi", "Sisa: ${transaksi.sisaPembayaran}")
                }
                .addOnFailureListener { error ->
                    Log.e("SaveTransaksi", "Gagal menyimpan transaksi: ${error.message}")
                    Toast.makeText(this, "Gagal menyimpan transaksi: ${error.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun hitungSubtotalTambahan(): Double {
        return listLayananTambahan.sumOf {
            extractNumber(it.hargaLayananTambahan ?: "0")
        }
    }

    private fun getCurrentDateTime(): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return formatter.format(Date())
    }

    private fun setupButtonListeners() {
        buttonBatal.setOnClickListener {
            finish()
        }

        buttonPembayaran.setOnClickListener {
            // Validasi spinner sebelum melanjutkan
            if (validateSpinners()) {
                showPembayaranDialog()
            }
        }
    }

    private fun validateSpinners(): Boolean {
        if (spinnerPegawai.selectedItemPosition == 0) {
            Toast.makeText(this, "Silakan pilih pegawai yang menangani", Toast.LENGTH_SHORT).show()
            return false
        }

        if (spinnerCabang.selectedItemPosition == 0) {
            Toast.makeText(this, "Silakan pilih cabang", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    // FUNGSI DIMODIFIKASI: Dialog pembayaran dengan integrasi penyimpanan ke database
    private fun showPembayaranDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_payment_method)

        val window = dialog.window
        if (window != null) {
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
            window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            window.attributes.windowAnimations = R.style.DialogSlideAnimation
        }

        val btnClose = dialog.findViewById<ImageView>(R.id.btnClose)
        val btnBayarNanti = dialog.findViewById<LinearLayout>(R.id.btnBayarNanti)
        val btnTunai = dialog.findViewById<LinearLayout>(R.id.btnTunai)
        val btnQris = dialog.findViewById<LinearLayout>(R.id.btnQris)
        val btnDana = dialog.findViewById<LinearLayout>(R.id.btnDana)
        val btnGopay = dialog.findViewById<LinearLayout>(R.id.btnGopay)
        val btnOvo = dialog.findViewById<LinearLayout>(R.id.btnOvo)
        val tvBatal = dialog.findViewById<TextView>(R.id.tvBatal)

        btnClose?.setOnClickListener { dialog.dismiss() }

        val metodePembayaranListener = View.OnClickListener { v ->
            val metodePembayaran = when (v.id) {
                R.id.btnBayarNanti -> "Pembayaran DP"
                R.id.btnTunai -> "Lunas Cash / Tunai"
                R.id.btnQris -> "Link Aja Payment"
                R.id.btnDana -> "Dana Payment"
                R.id.btnGopay -> "GOPAY"
                R.id.btnOvo -> "OVO Payment"
                else -> "Tidak Diketahui"
            }

            val idTransaksi = generateRandomId()
            val totalBayar = extractNumber(textViewTotalHarga.text.toString())

            // FITUR BARU: Simpan transaksi ke database
            saveTransaksiToDatabase(idTransaksi, metodePembayaran, totalBayar)

            Toast.makeText(this, "Metode pembayaran: $metodePembayaran", Toast.LENGTH_SHORT).show()

            val intent = Intent(this, InvoiceTransaksiActivity::class.java)
            intent.putExtra("id_pelanggan", idPelanggan)
            intent.putExtra("nama_pelanggan", namaPelanggan)
            intent.putExtra("no_hp", noHP)
            intent.putExtra("id_layanan", idLayanan)
            intent.putExtra("nama_layanan", namaLayanan)
            intent.putExtra("harga_layanan", hargaLayanan)
            intent.putExtra("layanan_tambahan", ArrayList(listLayananTambahan) as Serializable)
            intent.putExtra("total_bayar", totalBayar)
            intent.putExtra("id_transaksi", idTransaksi)
            intent.putExtra("metode_pembayaran", metodePembayaran)
            intent.putExtra("id_pegawai", selectedPegawaiId)
            intent.putExtra("id_cabang", selectedCabangId)

            val selectedPegawaiName = if (spinnerPegawai.selectedItemPosition > 0) {
                pegawaiList[spinnerPegawai.selectedItemPosition - 1].namaPegawai
            } else ""

            val selectedCabangName = if (spinnerCabang.selectedItemPosition > 0) {
                cabangList[spinnerCabang.selectedItemPosition - 1].namaCabang
            } else ""

            intent.putExtra("nama_pegawai", selectedPegawaiName)
            intent.putExtra("nama_cabang", selectedCabangName)

            dialog.dismiss()
            startActivity(intent)
        }

        btnBayarNanti?.setOnClickListener(metodePembayaranListener)
        btnTunai?.setOnClickListener(metodePembayaranListener)
        btnQris?.setOnClickListener(metodePembayaranListener)
        btnDana?.setOnClickListener(metodePembayaranListener)
        btnGopay?.setOnClickListener(metodePembayaranListener)
        btnOvo?.setOnClickListener(metodePembayaranListener)
        tvBatal?.setOnClickListener { dialog.dismiss() }

        dialog.show()
    }
}