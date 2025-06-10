package com.tugasss.laundryapp.modeldata

import java.io.Serializable

// Tambahkan field ini ke modelTransaksi class jika belum ada
data class modelTransaksi(
    val idTransaksi: String? = null,
    val idPelanggan: String? = null,
    val namaPelanggan: String? = null,
    val noHP: String? = null,
    val idLayanan: String? = null,
    val namaLayanan: String? = null,
    val hargaLayanan: String? = null,
    val layananTambahan: ArrayList<modelTambahan>? = null,
    val subtotalTambahan: Double? = null,
    val totalBayar: Double? = null,
    val sisaPembayaran: Double? = null, // Field baru untuk sisa pembayaran DP
    val metodePembayaran: String? = null,
    val statusPembayaran: String? = null,
    val statusPesanan: String? = null,
    val idPegawai: String? = null,
    val namaPegawai: String? = null,
    val idCabang: String? = null,
    val namaCabang: String? = null,
    val tanggalTransaksi: String? = null,
    val tanggalPelunasan: String? = null, // Field baru untuk tanggal pelunasan
    val tanggalSelesai: String? = null,
    val catatan: String? = null,
    val jumlahBayar: Double? = null
) : Serializable