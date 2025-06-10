package com.tugasss.laundryapp

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
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.tugasss.laundryapp.adapter.AdapterLaporanTransaksi
import com.tugasss.laundryapp.modeldata.modelTransaksi
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class Activity_Data_laporan : AppCompatActivity() {

    private lateinit var recyclerViewLaporan: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvEmptyState: TextView
    private lateinit var btnRefresh: ImageButton

    // Filter buttons
    private lateinit var btnFilterBelumBayar: MaterialButton
    private lateinit var btnFilterSudahBayar: MaterialButton
    private lateinit var btnFilterSelesai: MaterialButton

    private lateinit var adapterLaporan: AdapterLaporanTransaksi
    private val listTransaksi = mutableListOf<modelTransaksi>()
    private val listTransaksiFiltered = mutableListOf<modelTransaksi>()

    // Firebase
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var transaksiRef: DatabaseReference
    private var valueEventListener: ValueEventListener? = null

    // Current filter state - PERBAIKAN: Gunakan var bukan val
    private var currentFilter = "Belum Bayar"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_data_laporan)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        // Get current user's UID for filtering transactions
        val currentUser = auth.currentUser
        if (currentUser != null) {
            transaksiRef = database.getReference("transaksi").child(currentUser.uid)
        } else {
            Toast.makeText(this, "User tidak terautentikasi", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        initViews()
        setupRecyclerView()
        setupFilterButtons()
        loadTransaksiData()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val back = findViewById<ImageView>(R.id.Back)
        back.setOnClickListener{
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Remove listener untuk menghindari memory leak
        valueEventListener?.let { transaksiRef.removeEventListener(it) }
    }

    private fun initViews() {
        recyclerViewLaporan = findViewById(R.id.rvLaporanTransaksi)
        progressBar = findViewById(R.id.progressBar)
        tvEmptyState = findViewById(R.id.tvEmptyState)
        btnRefresh = findViewById(R.id.btnRefresh)

        // Filter buttons
        btnFilterBelumBayar = findViewById(R.id.btnFilterBelumBayar)
        btnFilterSudahBayar = findViewById(R.id.btnFilterSudahBayar)
        btnFilterSelesai = findViewById(R.id.btnFilterSelesai)

        btnRefresh.setOnClickListener {
            loadTransaksiData()
        }
    }

    private fun setupRecyclerView() {
        adapterLaporan = AdapterLaporanTransaksi(
            listTransaksiFiltered,
            onItemClick = { transaksi ->
                showDetailTransaksiDialog(transaksi)
            },
            onActionClick = { transaksi, action ->
                handleActionClick(transaksi, action)
            }
        )
        recyclerViewLaporan.layoutManager = LinearLayoutManager(this)
        recyclerViewLaporan.adapter = adapterLaporan
    }

    private fun setupFilterButtons() {
        // Set initial state
        updateFilterButtonState("Belum Bayar")

        btnFilterBelumBayar.setOnClickListener {
            currentFilter = "Belum Bayar"
            updateFilterButtonState(currentFilter)
            filterTransaksi(currentFilter)
        }

        btnFilterSudahBayar.setOnClickListener {
            currentFilter = "Sudah Bayar"
            updateFilterButtonState(currentFilter)
            filterTransaksi(currentFilter)
        }

        btnFilterSelesai.setOnClickListener {
            currentFilter = "Selesai"
            updateFilterButtonState(currentFilter)
            filterTransaksi(currentFilter)
        }
    }

    private fun updateFilterButtonState(activeFilter: String) {
        // Reset all buttons to inactive state
        resetFilterButton(btnFilterBelumBayar, R.color.red_100, R.color.red_700, R.color.red_300)
        resetFilterButton(btnFilterSudahBayar, R.color.blue_100, R.color.blue_700, R.color.blue_300)
        resetFilterButton(btnFilterSelesai, R.color.green_100, R.color.green_700, R.color.green_300)

        // Set active button
        when (activeFilter) {
            "Belum Bayar" -> setActiveFilterButton(btnFilterBelumBayar, R.color.red_500, android.R.color.white)
            "Sudah Bayar" -> setActiveFilterButton(btnFilterSudahBayar, R.color.blue_500, android.R.color.white)
            "Selesai" -> setActiveFilterButton(btnFilterSelesai, R.color.green_500, android.R.color.white)
        }
    }

    // PERBAIKAN: Fungsi resetFilterButton dengan tipe parameter yang tepat
    private fun resetFilterButton(button: MaterialButton, bgColorRes: Int, textColorRes: Int, strokeColorRes: Int) {
        button.apply {
            backgroundTintList = ContextCompat.getColorStateList(this@Activity_Data_laporan, bgColorRes)
            setTextColor(ContextCompat.getColor(this@Activity_Data_laporan, textColorRes))
            strokeColor = ContextCompat.getColorStateList(this@Activity_Data_laporan, strokeColorRes)
            strokeWidth = 2
        }
    }

    // PERBAIKAN: Fungsi setActiveFilterButton dengan tipe parameter yang tepat
    private fun setActiveFilterButton(button: MaterialButton, bgColorRes: Int, textColorRes: Int) {
        button.apply {
            backgroundTintList = ContextCompat.getColorStateList(this@Activity_Data_laporan, bgColorRes)
            setTextColor(ContextCompat.getColor(this@Activity_Data_laporan, textColorRes))
            strokeWidth = 0
        }
    }

    private fun loadTransaksiData() {
        showLoading(true)

        // Remove existing listener jika ada
        valueEventListener?.let { transaksiRef.removeEventListener(it) }

        valueEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listTransaksi.clear()

                if (snapshot.exists()) {
                    for (dataSnapshot in snapshot.children) {
                        try {
                            val transaksi = dataSnapshot.getValue(modelTransaksi::class.java)
                            transaksi?.let {
                                listTransaksi.add(it)
                            }
                        } catch (e: Exception) {
                            Log.e("LoadTransaksi", "Error parsing transaksi: ${e.message}")
                        }
                    }

                    // Sort by date (newest first)
                    listTransaksi.sortByDescending { parseDate(it.tanggalTransaksi ?: "") }
                }

                // Apply current filter
                filterTransaksi(currentFilter)

                showLoading(false)
                updateEmptyState()

                Log.d("LoadTransaksi", "Loaded ${listTransaksi.size} transaksi")
            }

            override fun onCancelled(error: DatabaseError) {
                showLoading(false)
                Log.e("LoadTransaksi", "Error loading transaksi: ${error.message}")
                Toast.makeText(this@Activity_Data_laporan,
                    "Error memuat data: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        }

        transaksiRef.addValueEventListener(valueEventListener!!)
    }

    // PERBAIKAN 1: Filter transaksi yang konsisten dengan status pembayaran
    private fun filterTransaksi(filter: String) {
        listTransaksiFiltered.clear()
        when (filter) {
            "Belum Bayar" -> {
                // Termasuk transaksi dengan status "Belum Bayar" dan yang masih memiliki sisa pembayaran
                listTransaksiFiltered.addAll(
                    listTransaksi.filter {
                        it.statusPembayaran == "Belum Bayar" ||
                                (it.statusPembayaran == "DP" && (it.sisaPembayaran ?: 0.0) > 0)
                    }
                )
            }
            "Sudah Bayar" -> {
                // Transaksi yang sudah lunas tapi belum diambil/selesai
                listTransaksiFiltered.addAll(
                    listTransaksi.filter {
                        it.statusPembayaran == "Sudah Bayar" ||
                                (it.statusPembayaran == "Lunas" && it.statusPesanan != "Selesai")
                    }
                )
            }
            "Selesai" -> {
                // Transaksi yang sudah selesai/diambil
                listTransaksiFiltered.addAll(
                    listTransaksi.filter {
                        it.statusPembayaran == "Selesai" ||
                                it.statusPesanan == "Selesai"
                    }
                )
            }
        }
        adapterLaporan.notifyDataSetChanged()
        updateEmptyState()

        // TAMBAHAN: Log untuk debugging
        Log.d("FilterTransaksi", "Filter: $filter, Total: ${listTransaksi.size}, Filtered: ${listTransaksiFiltered.size}")

        // Log detail transaksi yang difilter untuk debugging
        listTransaksiFiltered.forEachIndexed { index, transaksi ->
            Log.d("FilterTransaksi", "$index: ${transaksi.namaPelanggan} - Status: ${transaksi.statusPembayaran}, Pesanan: ${transaksi.statusPesanan}")
        }
    }

    private fun handleActionClick(transaksi: modelTransaksi, action: String) {
        when (action) {
            "BAYAR" -> {
                showPaymentConfirmationDialog(transaksi)
            }
            "AMBIL" -> {
                showPickupConfirmationDialog(transaksi)
            }
            // Tidak perlu action untuk status selesai
        }
    }

    private fun showPaymentConfirmationDialog(transaksi: modelTransaksi) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_payment_transaksi)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )

        val tvTitle = dialog.findViewById<TextView>(R.id.tvDialogTitle)
        val tvMessage = dialog.findViewById<TextView>(R.id.tvDialogMessage)
        val tvCustomerName = dialog.findViewById<TextView>(R.id.tvDialogCustomerName)
        val tvAmount = dialog.findViewById<TextView>(R.id.tvDialogAmount)
        val btnCancel = dialog.findViewById<MaterialButton>(R.id.btnDialogCancel)
        val btnConfirm = dialog.findViewById<MaterialButton>(R.id.btnDialogConfirm)

        tvTitle.text = "Konfirmasi Pembayaran"
        tvMessage.text = "Apakah Anda yakin pelanggan telah melakukan pembayaran?"
        tvCustomerName.text = transaksi.namaPelanggan ?: "Nama tidak tersedia"
        tvAmount.text = formatRupiah(transaksi.totalBayar ?: 0.0)

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        btnConfirm.setOnClickListener {
            // Update status ke "Sudah Bayar" dan tambahkan tanggal pelunasan
            updateTransaksiStatus(transaksi, "Sudah Bayar", getCurrentDateTime())
            dialog.dismiss()
            Toast.makeText(this, "Status pembayaran berhasil diubah ke Sudah Bayar", Toast.LENGTH_SHORT).show()
        }

        dialog.show()
    }

    private fun showPickupConfirmationDialog(transaksi: modelTransaksi) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_pickup_transaksi)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )

        val tvTitle = dialog.findViewById<TextView>(R.id.tvDialogTitle)
        val tvMessage = dialog.findViewById<TextView>(R.id.tvDialogMessage)
        val tvCustomerName = dialog.findViewById<TextView>(R.id.tvDialogCustomerName)
        val tvService = dialog.findViewById<TextView>(R.id.tvDialogService)
        val btnCancel = dialog.findViewById<MaterialButton>(R.id.btnDialogCancel)
        val btnConfirm = dialog.findViewById<MaterialButton>(R.id.btnDialogConfirm)

        tvTitle.text = "Konfirmasi Pengambilan"
        tvMessage.text = "Apakah pelanggan telah mengambil cuciannya?"
        tvCustomerName.text = transaksi.namaPelanggan ?: "Nama tidak tersedia"
        tvService.text = transaksi.namaLayanan ?: "Layanan tidak tersedia"

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        btnConfirm.setOnClickListener {
            // Update status ke "Selesai" dan tambahkan tanggal selesai
            updateTransaksiStatus(transaksi, "Selesai", getCurrentDateTime())
            dialog.dismiss()
            Toast.makeText(this, "Status transaksi berhasil diubah ke Selesai", Toast.LENGTH_SHORT).show()
        }

        dialog.show()
    }

    // PERBAIKAN 2: Update status transaksi dengan logic yang benar
    private fun updateTransaksiStatus(transaksi: modelTransaksi, newStatus: String, timestamp: String) {
        val updates = mutableMapOf<String, Any>()
        updates["statusPembayaran"] = newStatus

        when (newStatus) {
            "Sudah Bayar" -> {
                updates["tanggalPelunasan"] = timestamp
                // Jika ini adalah pelunasan DP, set sisaPembayaran ke 0
                if ((transaksi.sisaPembayaran ?: 0.0) > 0) {
                    updates["sisaPembayaran"] = 0.0
                    updates["jumlahBayar"] = transaksi.totalBayar ?: 0.0
                }
                Log.d("UpdateStatus", "Updating to Sudah Bayar with timestamp: $timestamp")
            }
            "Selesai" -> {
                updates["tanggalSelesai"] = timestamp
                updates["statusPesanan"] = "Selesai"
                Log.d("UpdateStatus", "Updating to Selesai with timestamp: $timestamp")
            }
        }

        transaksiRef.child(transaksi.idTransaksi ?: "").updateChildren(updates)
            .addOnSuccessListener {
                Log.d("UpdateStatus", "Status berhasil diupdate ke: $newStatus")
                Toast.makeText(this, "Status berhasil diupdate ke $newStatus", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Log.e("UpdateStatus", "Gagal update status: ${e.message}")
                Toast.makeText(this, "Gagal update status: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showDetailTransaksiDialog(transaksi: modelTransaksi) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_detail_transaksi)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )

        // Setup dialog content
        setupDialogContent(dialog, transaksi)

        dialog.show()
    }

    private fun setupDialogContent(dialog: Dialog, transaksi: modelTransaksi) {
        // Header
        val tvNamaPelanggan = dialog.findViewById<TextView>(R.id.tvDialogNamaPelanggan)
        val tvTanggalTransaksi = dialog.findViewById<TextView>(R.id.tvDialogTanggalTransaksi)
        val tvStatusPembayaran = dialog.findViewById<TextView>(R.id.tvDialogStatusPembayaran)
        val ivDialogClose = dialog.findViewById<ImageView>(R.id.ivDialogClose)

        // Detail Layanan
        val tvLayanan = dialog.findViewById<TextView>(R.id.tvDialogLayanan)
        val tvHargaLayanan = dialog.findViewById<TextView>(R.id.tvDialogHargaLayanan)
        val rvLayananTambahan = dialog.findViewById<RecyclerView>(R.id.rvDialogLayananTambahan)
        val tvSubtotalTambahan = dialog.findViewById<TextView>(R.id.tvDialogSubtotalTambahan)

        // Total dan Pembayaran
        val tvTotalBayar = dialog.findViewById<TextView>(R.id.tvDialogTotalBayar)
        val tvMetodePembayaran = dialog.findViewById<TextView>(R.id.tvDialogMetodePembayaran)

        // Timeline
        val llTimelinePelunasan = dialog.findViewById<LinearLayout>(R.id.llTimelinePelunasan)
        val llTimelineSelesai = dialog.findViewById<LinearLayout>(R.id.llTimelineSelesai)
        val tvTanggalPelunasan = dialog.findViewById<TextView>(R.id.tvTanggalPelunasan)
        val tvTanggalSelesai = dialog.findViewById<TextView>(R.id.tvTanggalSelesai)

        // Catatan
        val tvCatatan = dialog.findViewById<TextView>(R.id.tvDialogCatatan)

        // Set data
        tvNamaPelanggan.text = transaksi.namaPelanggan ?: "Nama tidak tersedia"
        tvTanggalTransaksi.text = formatTanggal(transaksi.tanggalTransaksi, "EEEE, dd MMMM yyyy HH:mm")
        tvLayanan.text = transaksi.namaLayanan ?: "Layanan tidak tersedia"
        tvHargaLayanan.text = formatRupiah(transaksi.hargaLayanan?.toDoubleOrNull() ?: 0.0)
        tvTotalBayar.text = formatRupiah(transaksi.totalBayar ?: 0.0)
        tvMetodePembayaran.text = transaksi.metodePembayaran ?: "Belum ditentukan"

        // Setup status dengan styling
        setupStatusDialog(tvStatusPembayaran, transaksi.statusPembayaran ?: "Belum Bayar")

        // Setup layanan tambahan
        val layananTambahan = transaksi.layananTambahan
        if (!layananTambahan.isNullOrEmpty()) {
            rvLayananTambahan.visibility = View.VISIBLE
            tvSubtotalTambahan?.visibility = View.VISIBLE
            tvSubtotalTambahan?.text = formatRupiah(transaksi.subtotalTambahan ?: 0.0)
            // Setup adapter untuk layanan tambahan
        } else {
            rvLayananTambahan?.visibility = View.GONE
            tvSubtotalTambahan?.visibility = View.GONE
        }

        // Setup timeline
        when (transaksi.statusPembayaran) {
            "Sudah Bayar" -> {
                llTimelinePelunasan?.visibility = View.VISIBLE
                llTimelineSelesai?.visibility = View.GONE
                tvTanggalPelunasan?.text = formatTanggal(transaksi.tanggalPelunasan, "dd MMMM yyyy HH:mm")
            }
            "Selesai" -> {
                llTimelinePelunasan?.visibility = View.VISIBLE
                llTimelineSelesai?.visibility = View.VISIBLE
                tvTanggalPelunasan?.text = formatTanggal(transaksi.tanggalPelunasan, "dd MMMM yyyy HH:mm")
                tvTanggalSelesai?.text = formatTanggal(transaksi.tanggalSelesai, "dd MMMM yyyy HH:mm")
            }
            else -> {
                llTimelinePelunasan?.visibility = View.GONE
                llTimelineSelesai?.visibility = View.GONE
            }
        }

        // Setup catatan
        if (!transaksi.catatan.isNullOrBlank()) {
            tvCatatan?.text = transaksi.catatan
            tvCatatan?.visibility = View.VISIBLE
        } else {
            tvCatatan?.visibility = View.GONE
        }

        ivDialogClose.setOnClickListener {
            dialog.dismiss()
        }
    }

    private fun setupStatusDialog(textView: TextView, status: String) {
        when (status) {
            "Belum Bayar" -> {
                textView.text = getString(R.string.belumbayar)
                textView.setTextColor(ContextCompat.getColor(this, R.color.red_600))
                textView.setBackgroundResource(R.drawable.bg_payment_status_belumbayar)
            }
            "Sudah Bayar" -> {
                textView.text = getString(R.string.sudahbayar)
                textView.setTextColor(ContextCompat.getColor(this, R.color.blue_600))
                textView.setBackgroundResource(R.drawable.bg_payment_status_sudahbayar)
            }
            "Selesai" -> {
                textView.text = getString(R.string.selesai)
                textView.setTextColor(ContextCompat.getColor(this, R.color.green_600))
                textView.setBackgroundResource(R.drawable.bg_payment_status_selesai)
            }
        }
    }

    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
        recyclerViewLaporan.visibility = if (show) View.GONE else View.VISIBLE
    }

    private fun updateEmptyState() {
        if (listTransaksiFiltered.isEmpty()) {
            tvEmptyState.visibility = View.VISIBLE
            tvEmptyState.text = when (currentFilter) {
                "Belum Bayar" -> "Belum ada transaksi yang belum dibayar"
                "Sudah Bayar" -> "Belum ada transaksi yang sudah dibayar"
                "Selesai" -> "Belum ada transaksi yang selesai"
                else -> "Belum ada transaksi"
            }
            recyclerViewLaporan.visibility = View.GONE
        } else {
            tvEmptyState.visibility = View.GONE
            recyclerViewLaporan.visibility = View.VISIBLE
        }
    }

    private fun parseDate(dateString: String): Date {
        return try {
            val formats = arrayOf(
                SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()),
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            )

            for (format in formats) {
                try {
                    return format.parse(dateString) ?: Date()
                } catch (e: Exception) {
                    continue
                }
            }
            Date()
        } catch (e: Exception) {
            Date()
        }
    }

    private fun getCurrentDateTime(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return sdf.format(Date())
    }

    private fun formatRupiah(amount: Double): String {
        val localeID = Locale("in", "ID")
        val formatRupiah = NumberFormat.getCurrencyInstance(localeID)
        return formatRupiah.format(amount)
    }

    private fun formatTanggal(tanggalString: String?, pattern: String = "dd MMM yyyy HH:mm"): String {
        return try {
            if (tanggalString.isNullOrBlank()) {
                "Tanggal tidak tersedia"
            } else {
                val inputFormats = arrayOf(
                    SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()),
                    SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                )

                val outputFormat = SimpleDateFormat(pattern, Locale("id", "ID"))

                for (format in inputFormats) {
                    try {
                        val date = format.parse(tanggalString)
                        date?.let { return outputFormat.format(it) }
                    } catch (e: Exception) {
                        continue
                    }
                }

                tanggalString
            }
        } catch (e: Exception) {
            "Tanggal tidak valid"
        }
    }
}