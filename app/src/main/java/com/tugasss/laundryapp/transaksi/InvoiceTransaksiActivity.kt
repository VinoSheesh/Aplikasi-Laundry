package com.tugasss.laundryapp.transaksi

import android.Manifest
import com.tugasss.laundryapp.adapter.AdapterDataTambahan
import com.tugasss.laundryapp.modeldata.modelTambahan

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
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
import java.io.OutputStream
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.UUID

class InvoiceTransaksiActivity : AppCompatActivity() {

    private lateinit var tvTanggal: TextView
    private lateinit var tvIdTransaksi: TextView
    private lateinit var tvNamaPelanggan: TextView
    private lateinit var tvNamaKaryawan: TextView
    private lateinit var tvBRANCH: TextView
    private lateinit var tvNAMA_LAUNDRY: TextView
    private lateinit var tvLayananUtama: TextView
    private lateinit var tvHargaLayanan: TextView
    private lateinit var tvMetodePembayaran: TextView
    private lateinit var rvTambahan: RecyclerView
    private lateinit var tvSubtotalTambahan: TextView
    private lateinit var tvTotalBayar: TextView
    private lateinit var btnCetak: Button
    private lateinit var btnKirimWhatsapp: Button

    private var tvLayananTambahanList: TextView? = null

    private val listTambahan = ArrayList<modelTambahan>()
    private lateinit var adapter: AdapterPilihLayananTambahan

    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private var bluetoothSocket: BluetoothSocket? = null
    private var outputStream: OutputStream? = null

    private val printerMAC = "DC:0D:51:A7:FF:7A"
    private val printerUUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb")

    // Firebase
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    // Data tambahan untuk invoice
    private var metodePembayaran: String = ""
    private var namaKaryawan: String = "Admin"
    private var namaCabang: String = "Cabang Utama"
    private var namaLaundry: String = "Vinn Laundry"

    // Konstanta untuk request permission
    private val BLUETOOTH_PERMISSION_REQUEST_CODE = 1001
    private val ENABLE_BLUETOOTH_REQUEST_CODE = 1002

    // Status izin dan pending operations
    private var permissionRequestInProgress = false
    private var pendingPrintMessage: String = ""

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_invoice_transaksi)

        // Inisialisasi Firebase
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        initializeViews()
        setupRecyclerView()
        loadUserInfo()
        loadDataFromIntent()
        setupButtons()

        // Request permissions sekali saja saat onCreate
        checkAndRequestBluetoothPermissions()

        val invoiceactivity = findViewById<ImageView>(R.id.Back)
        invoiceactivity.setOnClickListener{
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        }
    }

    private fun initializeViews() {
        tvTanggal = findViewById(R.id.tvTanggal)
        tvIdTransaksi = findViewById(R.id.tvIdTransaksi)
        tvNamaPelanggan = findViewById(R.id.tvNamaPelanggan)
        tvNamaKaryawan = findViewById(R.id.tvNamaKaryawan)
        tvBRANCH = findViewById(R.id.tvBRANCH)
        tvNAMA_LAUNDRY = findViewById(R.id.tvNAMA_LAUNDRY)
        tvLayananUtama = findViewById(R.id.tvLayananUtama)
        tvHargaLayanan = findViewById(R.id.tvHargaLayanan)
        tvMetodePembayaran = findViewById(R.id.tvMetodePembayaran)
        rvTambahan = findViewById(R.id.rvRincianTambahan)
        tvSubtotalTambahan = findViewById(R.id.tvSubtotalTambahan)
        tvTotalBayar = findViewById(R.id.tvTotalBayar)
        btnCetak = findViewById(R.id.btnCetak)
        btnKirimWhatsapp = findViewById(R.id.btnKirimWhatsapp)

        try {
            tvLayananTambahanList = findViewById(R.id.tvLayananTambahanList)
        } catch (e: Exception) {
            tvLayananTambahanList = null
        }
    }

    private fun loadUserInfo() {
        val user = auth.currentUser
        user?.let { currentUser ->
            val userId = currentUser.uid
            val userRef = database.getReference("users").child(userId).child("username")

            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val username = snapshot.getValue(String::class.java)
                    namaLaundry = username ?: "Vinn Laundry"
                    tvNAMA_LAUNDRY.text = namaLaundry
                }

                override fun onCancelled(error: DatabaseError) {
                    namaLaundry = "Vinn Laundry"
                    tvNAMA_LAUNDRY.text = namaLaundry
                }
            })
        } ?: run {
            namaLaundry = "Vinn Laundry"
            tvNAMA_LAUNDRY.text = namaLaundry
        }
    }

    // Fungsi utama untuk cek dan request permissions
    private fun checkAndRequestBluetoothPermissions() {
        if (hasAllBluetoothPermissions()) {
            // Semua izin sudah ada, tidak perlu request lagi
            return
        }

        // Hanya request jika belum pernah request atau user belum memberikan jawaban
        if (!permissionRequestInProgress) {
            requestBluetoothPermissions()
        }
    }

    // Fungsi untuk cek apakah semua izin sudah diberikan
    private fun hasAllBluetoothPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Android 12+
            ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED
        } else {
            // Android < 12
            ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) == PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        }
    }

    // Request permissions hanya jika belum ada
    private fun requestBluetoothPermissions() {
        permissionRequestInProgress = true

        val permissionsToRequest = mutableListOf<String>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Android 12+
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.BLUETOOTH_CONNECT)
            }
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.BLUETOOTH_SCAN)
            }
        } else {
            // Android < 12
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.BLUETOOTH)
            }
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.BLUETOOTH_ADMIN)
            }
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }

        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsToRequest.toTypedArray(), BLUETOOTH_PERMISSION_REQUEST_CODE)
        } else {
            permissionRequestInProgress = false
        }
    }

    private fun setupRecyclerView() {
        adapter = AdapterPilihLayananTambahan(listTambahan.toMutableList())
        rvTambahan.layoutManager = LinearLayoutManager(this)
        rvTambahan.adapter = adapter
    }

    private fun loadDataFromIntent() {
        val namaPelanggan = intent.getStringExtra("nama_pelanggan") ?: "-"
        val namaLayanan = intent.getStringExtra("nama_layanan") ?: "-"
        val hargaLayananString = intent.getStringExtra("harga_layanan") ?: "0"
        val hargaLayananDouble = extractHargaFromString(hargaLayananString)
        val totalBayar = intent.getDoubleExtra("total_bayar", 0.0)
        val idTransaksi = intent.getStringExtra("id_transaksi") ?: generateTransactionId()

        namaKaryawan = intent.getStringExtra("nama_pegawai") ?: "Admin"
        namaCabang = intent.getStringExtra("nama_cabang") ?: "Cabang Utama"
        metodePembayaran = intent.getStringExtra("metode_pembayaran") ?: "Tunai"

        @Suppress("UNCHECKED_CAST")
        val tambahan = intent.getSerializableExtra("layanan_tambahan") as? ArrayList<modelTambahan> ?: arrayListOf()

        val formatter = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        val tanggal = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())

        tvTanggal.text = tanggal
        tvIdTransaksi.text = idTransaksi
        tvNamaPelanggan.text = namaPelanggan
        tvNamaKaryawan.text = namaKaryawan
        tvBRANCH.text = namaCabang
        tvLayananUtama.text = namaLayanan
        tvHargaLayanan.text = formatter.format(hargaLayananDouble)
        tvMetodePembayaran.text = metodePembayaran

        listTambahan.clear()
        listTambahan.addAll(tambahan)
        adapter.notifyDataSetChanged()

        displayLayananTambahanSimple(tambahan)

        val subtotalTambahan = tambahan.sumOf { extractHargaFromString(it.hargaLayananTambahan ?: "0") }
        tvSubtotalTambahan.text = formatter.format(subtotalTambahan)
        tvTotalBayar.text = formatter.format(totalBayar)
    }

    private fun displayLayananTambahanSimple(tambahan: ArrayList<modelTambahan>) {
        tvLayananTambahanList?.let { textView ->
            if (tambahan.isNotEmpty()) {
                val layananText = tambahan.joinToString("\n") { layanan ->
                    "• ${layanan.namaLayananTambahan} - ${formatRupiah(extractHargaFromString(layanan.hargaLayananTambahan ?: "0"))}"
                }
                textView.text = layananText
                textView.visibility = android.view.View.VISIBLE
                rvTambahan.visibility = android.view.View.GONE
            } else {
                textView.visibility = android.view.View.GONE
                rvTambahan.visibility = android.view.View.VISIBLE
            }
        } ?: run {
            rvTambahan.visibility = android.view.View.VISIBLE
        }
    }

    private fun formatRupiah(amount: Double): String {
        val formatter = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        return formatter.format(amount)
    }

    private fun generateTransactionId(): String {
        val timestamp = SimpleDateFormat("yyMMddHHmm", Locale.getDefault()).format(Date())
        return "VL$timestamp"
    }

    private fun extractHargaFromString(harga: String): Double {
        val cleaned = harga.replace("Rp", "")
            .replace(".", "")
            .replace(",", ".")
            .trim()
        return cleaned.toDoubleOrNull() ?: 0.0
    }

    private fun setupButtons() {
        setupPrintButton()
        setupWhatsappButton()
    }

    // Setup tombol cetak yang sudah diperbaiki
    private fun setupPrintButton() {
        btnCetak.setOnClickListener {
            val message = generatePrintMessage()

            // Langsung coba cetak jika semua kondisi terpenuhi
            if (canPrintNow()) {
                printToBluetooth(message)
            } else {
                // Simpan pesan untuk dicetak nanti
                pendingPrintMessage = message
                handlePrintRequirements()
            }
        }
    }

    // Fungsi untuk cek apakah bisa langsung cetak
    private fun canPrintNow(): Boolean {
        // Cek izin
        if (!hasAllBluetoothPermissions()) {
            return false
        }

        // Cek bluetooth adapter
        if (bluetoothAdapter == null) {
            Toast.makeText(this, getString(R.string.toast_bluetooth_not_supported), Toast.LENGTH_LONG).show()
            return false
        }

        // Cek bluetooth aktif
        if (!bluetoothAdapter.isEnabled) {
            return false
        }

        return true
    }

    // Handle requirements untuk printing
    private fun handlePrintRequirements() {
        when {
            !hasAllBluetoothPermissions() -> {
                if (!permissionRequestInProgress) {
                    Toast.makeText(this, getString(R.string.toast_requesting_bluetooth), Toast.LENGTH_SHORT).show()
                    requestBluetoothPermissions()
                }
            }

            bluetoothAdapter == null -> {
                Toast.makeText(this, getString(R.string.toast_bluetooth_not_supported), Toast.LENGTH_LONG).show()
            }

            !bluetoothAdapter.isEnabled -> {
                Toast.makeText(this, getString(R.string.toast_enabling_bluetooth), Toast.LENGTH_SHORT).show()
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(enableBtIntent, ENABLE_BLUETOOTH_REQUEST_CODE)
            }
        }
    }

    private fun generatePrintMessage(): String {
        val formatter = NumberFormat.getCurrencyInstance(Locale("in", "ID"))

        return buildString {
            val width = 32 // Lebar kertas thermal 57mm ≈ 32 karakter

            // Header dengan border
            append(getString(R.string.receipt_header_border) + "\n")
            val headerText = namaLaundry.uppercase()
            val headerPadding = (width - headerText.length) / 2
            append("${" ".repeat(maxOf(0, headerPadding))}$headerText\n")

            val cabangPadding = (width - namaCabang.length) / 2
            append("${" ".repeat(maxOf(0, cabangPadding))}$namaCabang\n")
            append(getString(R.string.receipt_header_border) + "\n\n")

            // Info transaksi
            append(getString(R.string.receipt_invoice, tvIdTransaksi.text.toString()) + "\n")
            append(getString(R.string.receipt_date, tvTanggal.text.toString()) + "\n")
            append(getString(R.string.receipt_cashier, namaKaryawan) + "\n")
            append(getString(R.string.receipt_branch, namaCabang) + "\n")
            append(getString(R.string.receipt_section_border) + "\n")

            // Info pelanggan
            append(getString(R.string.receipt_customer, tvNamaPelanggan.text.toString()) + "\n")
            append(getString(R.string.receipt_payment, metodePembayaran) + "\n")
            append(getString(R.string.receipt_section_border) + "\n")

            // Layanan Utama
            append(getString(R.string.receipt_main_service) + "\n")
            val namaUtama = tvLayananUtama.text.toString()
            val hargaUtama = extractHargaFromString(tvHargaLayanan.text.toString())
            val hargaUtamaStr = formatter.format(hargaUtama)

            // Truncate nama layanan jika terlalu panjang
            val maxNamaLength = width - hargaUtamaStr.length - 1
            val namaUtamaTrimmed = if (namaUtama.length > maxNamaLength) {
                namaUtama.substring(0, maxNamaLength - 3) + "..."
            } else namaUtama

            // Format: Nama layanan di kiri, harga di kanan
            val spacesNeeded = width - namaUtamaTrimmed.length - hargaUtamaStr.length
            append("$namaUtamaTrimmed${" ".repeat(maxOf(1, spacesNeeded))}$hargaUtamaStr\n\n")

            // Layanan Tambahan
            if (listTambahan.isNotEmpty()) {
                append(getString(R.string.receipt_additional_service) + "\n")
                listTambahan.forEachIndexed { index, item ->
                    val nama = "${index + 1}. ${item.namaLayananTambahan}"
                    val harga = extractHargaFromString(item.hargaLayananTambahan ?: "0")
                    val hargaStr = formatter.format(harga)

                    val maxNamaLen = width - hargaStr.length - 1
                    val namaTrimmed = if (nama.length > maxNamaLen) {
                        nama.substring(0, maxNamaLen - 3) + "..."
                    } else nama

                    val spaces = width - namaTrimmed.length - hargaStr.length
                    append("$namaTrimmed${" ".repeat(maxOf(1, spaces))}$hargaStr\n")
                }
                append("\n")
            }

            // Total Section
            append(getString(R.string.receipt_total_border) + "\n")
            val hargaLayananUtama = extractHargaFromString(tvHargaLayanan.text.toString())
            val subtotalTambahan = extractHargaFromString(tvSubtotalTambahan.text.toString())
            val totalBayar = extractHargaFromString(tvTotalBayar.text.toString())

            // Subtotal Layanan Utama
            val utamaLabel = getString(R.string.receipt_main_service_total)
            val utamaValue = formatter.format(hargaLayananUtama)
            val utamaSpaces = width - utamaLabel.length - utamaValue.length
            append("$utamaLabel${" ".repeat(maxOf(1, utamaSpaces))}$utamaValue\n")

            // Subtotal Layanan Tambahan (jika ada)
            if (subtotalTambahan > 0) {
                val tambahanLabel = getString(R.string.receipt_additional_service_total)
                val tambahanValue = formatter.format(subtotalTambahan)
                val tambahanSpaces = width - tambahanLabel.length - tambahanValue.length
                append("$tambahanLabel${" ".repeat(maxOf(1, tambahanSpaces))}$tambahanValue\n")
            }

            append(getString(R.string.receipt_section_border) + "\n")

            // Total Bayar
            val totalLabel = getString(R.string.receipt_total_payment)
            val totalValue = formatter.format(totalBayar)
            val totalSpaces = width - totalLabel.length - totalValue.length
            append("$totalLabel${" ".repeat(maxOf(1, totalSpaces))}$totalValue\n")
            append(getString(R.string.receipt_total_border) + "\n\n")

            // Footer
            append(getString(R.string.receipt_thank_you, namaLaundry) + "\n\n")
            append(getString(R.string.receipt_quality_service) + "\n\n")
            append(getString(R.string.receipt_contact) + "\n\n")
            append(getString(R.string.receipt_section_border) + "\n")
            append(getString(R.string.receipt_valid_proof) + "\n\n\n")
        }
    }

    private fun setupWhatsappButton() {
        btnKirimWhatsapp.setOnClickListener {
            val message = generateWhatsAppMessage()
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("https://wa.me/?text=" + Uri.encode(message))
            startActivity(intent)
        }
    }

    private fun generateWhatsAppMessage(): String {
        val formatter = NumberFormat.getCurrencyInstance(Locale("in", "ID"))

        return buildString {
            append("🧺 *${namaLaundry.uppercase()}* 🧺\n")
            append("═══════════════════════\n\n")

            append(getString(R.string.wa_greeting, tvNamaPelanggan.text.toString()) + "\n\n")
            append(getString(R.string.wa_order_detail) + "\n\n")

            append(getString(R.string.wa_transaction_detail) + "\n")
            append(getString(R.string.wa_invoice, tvIdTransaksi.text.toString()) + "\n")
            append(getString(R.string.wa_date, tvTanggal.text.toString()) + "\n")
            append(getString(R.string.wa_officer, namaKaryawan) + "\n")
            append(getString(R.string.wa_branch, namaCabang) + "\n")
            append(getString(R.string.wa_payment, metodePembayaran) + "\n\n")

            append(getString(R.string.wa_selected_service) + "\n")
            append(getString(R.string.wa_main_service, tvLayananUtama.text.toString()) + "\n")
            append(getString(R.string.wa_price, tvHargaLayanan.text.toString()) + "\n\n")

            if (listTambahan.isNotEmpty()) {
                append(getString(R.string.wa_additional_service) + "\n")
                listTambahan.forEachIndexed { index, item ->
                    val harga = extractHargaFromString(item.hargaLayananTambahan ?: "0")
                    append("${index + 1}. ${item.namaLayananTambahan}\n")
                    append(getString(R.string.wa_price_label, formatter.format(harga)) + "\n")
                }
                append("\n")
                append(getString(R.string.wa_additional_subtotal, tvSubtotalTambahan.text.toString()) + "\n\n")
            }

            append(getString(R.string.wa_total_payment) + "\n")
            append("${tvTotalBayar.text}\n\n")

            append(getString(R.string.wa_store_address) + "\n")
            append("$namaLaundry\n$namaCabang\n\n")

            append(getString(R.string.wa_contact) + "\n")
            append(getString(R.string.wa_whatsapp) + "\n\n")

            append(getString(R.string.wa_thank_you) + "\n")
            append(getString(R.string.wa_commitment) + "\n\n")

            append(getString(R.string.wa_important_info) + "\n")
            append(getString(R.string.wa_keep_receipt) + "\n")
            append(getString(R.string.wa_warranty_claim) + "\n")
            append(getString(R.string.wa_contact_us) + "\n\n")

            append(getString(R.string.wa_warm_regards) + "\n")
            append(getString(R.string.wa_team, namaLaundry) + " 🧺💙")
        }
    }

    @SuppressLint("MissingPermission")
    private fun printToBluetooth(message: String) {
        try {
            val device: BluetoothDevice? = bluetoothAdapter?.bondedDevices?.find { it.address == printerMAC }

            if (device == null) {
                Toast.makeText(this, getString(R.string.error_printer_not_found, printerMAC), Toast.LENGTH_LONG).show()
                return
            }

            Toast.makeText(this, getString(R.string.toast_connecting_printer), Toast.LENGTH_SHORT).show()

            Thread {
                try {
                    bluetoothSocket = device.createRfcommSocketToServiceRecord(printerUUID)
                    bluetoothSocket?.connect()

                    outputStream = bluetoothSocket?.outputStream
                    outputStream?.write(message.toByteArray())
                    outputStream?.flush()

                    runOnUiThread {
                        Toast.makeText(this, getString(R.string.toast_print_success), Toast.LENGTH_SHORT).show()
                    }

                } catch (e: Exception) {
                    runOnUiThread {
                        handlePrintError(e)
                    }
                } finally {
                    Handler(Looper.getMainLooper()).postDelayed({
                        try {
                            outputStream?.close()
                            bluetoothSocket?.close()
                        } catch (e: Exception) {
                            // Ignore close errors
                        }
                    }, 2000)
                }
            }.start()

        } catch (e: Exception) {
            handlePrintError(e)
        }
    }

    private fun handlePrintError(e: Exception) {
        val errorMessage = when {
            e.message?.contains("Connection refused") == true ->
                getString(R.string.error_connection_refused)

            e.message?.contains("Device not found") == true ->
                getString(R.string.error_device_not_found)

            e.message?.contains("Permission denied") == true ->
                getString(R.string.error_permission_denied)

            e.message?.contains("timeout") == true ->
                getString(R.string.error_connection_timeout)

            else -> getString(R.string.error_print_failed, e.message ?: getString(R.string.error_unknown))
        }

        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            outputStream?.close()
            bluetoothSocket?.close()
        } catch (e: Exception) {
            // Ignore close errors
        }
    }

    // Handle hasil request permissions
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == BLUETOOTH_PERMISSION_REQUEST_CODE) {
            permissionRequestInProgress = false

            val allGranted = grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }

            if (allGranted) {
                Toast.makeText(this, getString(R.string.toast_bluetooth_permission_granted), Toast.LENGTH_SHORT).show()

                // Jika ada pending print, langsung eksekusi
                if (pendingPrintMessage.isNotEmpty()) {
                    if (canPrintNow()) {
                        printToBluetooth(pendingPrintMessage)
                        pendingPrintMessage = ""
                    } else {
                        handlePrintRequirements()
                    }
                }
            } else {
                Toast.makeText(this, getString(R.string.toast_bluetooth_permission_needed), Toast.LENGTH_LONG).show()
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == ENABLE_BLUETOOTH_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "✅ Bluetooth berhasil diaktifkan", Toast.LENGTH_SHORT).show()

                // Jika ada pending print, langsung eksekusi
                if (pendingPrintMessage.isNotEmpty()) {
                    if (canPrintNow()) {
                        printToBluetooth(pendingPrintMessage)
                        pendingPrintMessage = ""
                    }
                }
            } else {
                Toast.makeText(this, "❌ Bluetooth perlu diaktifkan untuk dapat mencetak", Toast.LENGTH_LONG).show()
            }
        }
    }
}
