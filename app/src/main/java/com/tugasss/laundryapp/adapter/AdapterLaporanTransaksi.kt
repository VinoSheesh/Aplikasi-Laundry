package com.tugasss.laundryapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.tugasss.laundryapp.R
import com.tugasss.laundryapp.modeldata.modelTransaksi
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class AdapterLaporanTransaksi(
    private val listTransaksi: List<modelTransaksi>,
    private val onItemClick: (modelTransaksi) -> Unit,
    private val onActionClick: (modelTransaksi, String) -> Unit
) : RecyclerView.Adapter<AdapterLaporanTransaksi.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNamaPelanggan: TextView = itemView.findViewById(R.id.tvCARD_PELANGGAN_nama)
        val tvTanggal: TextView = itemView.findViewById(R.id.tvTanggal)
        val tvPaymentStatus: TextView = itemView.findViewById(R.id.tvPaymentStatus)
        val tvLayanan: TextView = itemView.findViewById(R.id.tvLayanan)
        val tvLayananTambahan: TextView = itemView.findViewById(R.id.tvLayananTambahan)
        val tvDetailLayananTambahan: TextView = itemView.findViewById(R.id.tvDetailLayananTambahan)
        val tvTotalBayar: TextView = itemView.findViewById(R.id.tvTotalBayar)
        val btnAction: MaterialButton = itemView.findViewById(R.id.btnPickup)
        val tvTanggalAksi: TextView = itemView.findViewById(R.id.tvTanggalAksi)
        val mesincuci: ImageView = itemView.findViewById(R.id.mesincuci)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_laporan_transaksi, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val transaksi = listTransaksi[position]
        val context = holder.itemView.context

        // Set basic data
        holder.tvNamaPelanggan.text = transaksi.namaPelanggan ?: context.getString(R.string.default_nama_tidak_tersedia)
        holder.tvTanggal.text = formatTanggalCompact(transaksi.tanggalTransaksi, context)
        holder.tvLayanan.text = transaksi.namaLayanan ?: context.getString(R.string.default_layanan_tidak_tersedia)
        holder.tvTotalBayar.text = formatRupiahCompact(transaksi.totalBayar ?: 0.0)

        // Setup layanan tambahan dengan detail
        setupLayananTambahanWithDetail(holder, transaksi)

        // Setup status dan button berdasarkan status pembayaran
        when (transaksi.statusPembayaran) {
            "Belum Bayar" -> {
                setupBelumBayar(holder, context)
                holder.btnAction.setOnClickListener {
                    onActionClick(transaksi, "BAYAR")
                }
            }
            "Sudah Bayar" -> {
                setupSudahBayar(holder, context, transaksi)
                holder.btnAction.setOnClickListener {
                    onActionClick(transaksi, "AMBIL")
                }
            }
            "Selesai" -> {
                setupSelesai(holder, context, transaksi)
                holder.btnAction.setOnClickListener {
                    // Tidak ada aksi, hanya menampilkan informasi
                }
            }
            else -> {
                setupBelumBayar(holder, context)
                holder.btnAction.setOnClickListener {
                    onActionClick(transaksi, "BAYAR")
                }
            }
        }

        // Set click listener untuk item
        holder.itemView.setOnClickListener {
            onItemClick(transaksi)
        }
    }

    private fun setupLayananTambahanWithDetail(holder: ViewHolder, transaksi: modelTransaksi) {
        val context = holder.itemView.context
        val layananTambahan = transaksi.layananTambahan
        if (!layananTambahan.isNullOrEmpty()) {
            val jumlahLayananTambahan = layananTambahan.size

            // Tampilkan jumlah layanan tambahan
            holder.tvLayananTambahan.visibility = View.VISIBLE
            holder.tvLayananTambahan.text = "+$jumlahLayananTambahan ${context.getString(R.string.tambahan)}"

            // Tampilkan detail layanan tambahan
            holder.tvDetailLayananTambahan.visibility = View.VISIBLE

            // Membuat string detail layanan tambahan
            val detailBuilder = StringBuilder()
            layananTambahan.forEachIndexed { index, layanan ->
                if (index > 0) detailBuilder.append(" • ")
                detailBuilder.append(layanan.namaLayananTambahan ?: context.getString(R.string.layanan))

                // Tambahkan harga jika tersedia
                val harga = layanan.hargaLayananTambahan
                if (!harga.isNullOrEmpty() && harga != "0") {
                    detailBuilder.append(" (${formatRupiahCompact(extractNumber(harga))})")
                }
            }

            holder.tvDetailLayananTambahan.text = detailBuilder.toString()

            // Styling untuk detail layanan tambahan
            holder.tvDetailLayananTambahan.setTextColor(
                ContextCompat.getColor(holder.itemView.context, R.color.text_secondary)
            )
            holder.tvDetailLayananTambahan.textSize = 12f
        } else {
            holder.tvLayananTambahan.visibility = View.GONE
            holder.tvDetailLayananTambahan.visibility = View.GONE
        }
    }

    private fun setupBelumBayar(holder: ViewHolder, context: android.content.Context) {
        // Status
        holder.tvPaymentStatus.apply {
            text = context.getString(R.string.belumbayar)
            setTextColor(ContextCompat.getColor(context, R.color.red_600))
            setBackgroundResource(R.drawable.bg_payment_status_belumbayar)
        }

        // Button untuk bayar
        holder.btnAction.apply {
            text = context.getString(R.string.btn_bayar)
            backgroundTintList = ContextCompat.getColorStateList(context, R.color.red_500)
            setTextColor(ContextCompat.getColor(context, android.R.color.white))
            icon = ContextCompat.getDrawable(context, R.drawable.baseline_attach_money_24)
            isEnabled = true
            visibility = View.VISIBLE
            isClickable = true
        }

        // Hide tanggal aksi
        holder.tvTanggalAksi.visibility = View.GONE
    }

    private fun setupSudahBayar(holder: ViewHolder, context: android.content.Context, transaksi: modelTransaksi) {
        // Status
        holder.tvPaymentStatus.apply {
            text = context.getString(R.string.sudahbayar)
            setTextColor(ContextCompat.getColor(context, R.color.blue_600))
            setBackgroundResource(R.drawable.bg_payment_status_sudahbayar)
        }

        // Button untuk ambil cucian
        holder.btnAction.apply {
            text = context.getString(R.string.btn_ambil)
            backgroundTintList = ContextCompat.getColorStateList(context, R.color.blue_500)
            setTextColor(ContextCompat.getColor(context, android.R.color.white))
            icon = ContextCompat.getDrawable(context, R.drawable.baseline_add_shopping_cart_24)
            isEnabled = true
            visibility = View.VISIBLE
            isClickable = true
        }

        // Show tanggal pelunasan
        holder.tvTanggalAksi.apply {
            visibility = View.VISIBLE
            text = "${context.getString(R.string.sudahbayar)} ${formatTanggalAksiCompact(transaksi.tanggalPelunasan, context)}"
        }
    }

    private fun setupSelesai(holder: ViewHolder, context: android.content.Context, transaksi: modelTransaksi) {
        // Status
        holder.tvPaymentStatus.apply {
            text = context.getString(R.string.selesai)
            setTextColor(ContextCompat.getColor(context, R.color.green_600))
            setBackgroundResource(R.drawable.bg_payment_status_selesai)
        }

        // Button menampilkan "Diambil" saja
        holder.btnAction.apply {
            text = context.getString(R.string.selesai)
            backgroundTintList = ContextCompat.getColorStateList(context, R.color.green_100)
            setTextColor(ContextCompat.getColor(context, R.color.green_700))
            icon = null
            isEnabled = false
            visibility = View.VISIBLE
            isClickable = false
        }

        // Show tanggal diambil
        holder.tvTanggalAksi.apply {
            visibility = View.VISIBLE
            text = "${context.getString(R.string.selesai)} ${formatTanggalAksiCompact(transaksi.tanggalSelesai, context)}"
        }
    }

    override fun getItemCount(): Int = listTransaksi.size

    // Helper function untuk mengekstrak angka dari string harga
    private fun extractNumber(hargaString: String): Double {
        val cleaned = hargaString.replace("Rp", "").replace(".", "").replace(",", ".").trim()
        return cleaned.toDoubleOrNull() ?: 0.0
    }

    // Format rupiah yang lebih compact untuk card
    private fun formatRupiahCompact(amount: Double): String {
        val localeID = Locale("in", "ID")
        val formatRupiah = NumberFormat.getCurrencyInstance(localeID)
        val formatted = formatRupiah.format(amount)
        // Hapus ",00" jika ada untuk tampilan yang lebih clean
        return formatted.replace(",00", "")
    }

    // Format tanggal yang lebih compact untuk header card
    private fun formatTanggalCompact(tanggalString: String?, context: android.content.Context): String {
        return try {
            if (tanggalString.isNullOrBlank()) {
                context.getString(R.string.default_tanggal_tidak_tersedia)
            } else {
                val inputFormats = arrayOf(
                    SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()),
                    SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                )

                val outputFormat = SimpleDateFormat("dd MMM yyyy • HH:mm", Locale("id", "ID"))

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
            context.getString(R.string.default_tanggal_tidak_valid)
        }
    }

    // Format tanggal untuk aksi (pembayaran/pengambilan) yang lebih compact
    private fun formatTanggalAksiCompact(tanggalString: String?, context: android.content.Context): String {
        return try {
            if (tanggalString.isNullOrBlank()) {
                ""
            } else {
                val inputFormats = arrayOf(
                    SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()),
                    SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                )

                val outputFormat = SimpleDateFormat("dd/MM HH:mm", Locale("id", "ID"))

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
            ""
        }
    }
}