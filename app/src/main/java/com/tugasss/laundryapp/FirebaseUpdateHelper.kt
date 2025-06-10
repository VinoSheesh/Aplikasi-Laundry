package com.tugasss.laundryapp

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.*

class FirebaseUpdateHelper {

    companion object {
        private val database = FirebaseDatabase.getInstance()
        private val auth = FirebaseAuth.getInstance()

        fun updateStatusToLunas(idTransaksi: String, callback: (Boolean, String) -> Unit) {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                callback(false, "User tidak terautentikasi")
                return
            }

            val transaksiRef = database.getReference("transaksi")
                .child(currentUser.uid)
                .child(idTransaksi)

            val currentDateTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                .format(Date())

            val updates = mapOf(
                "statusPembayaran" to "Lunas",
                "tanggalPelunasan" to currentDateTime,
                "sisaPembayaran" to 0.0
            )

            transaksiRef.updateChildren(updates)
                .addOnSuccessListener {
                    callback(true, "Status berhasil diupdate menjadi Lunas")
                }
                .addOnFailureListener { exception ->
                    callback(false, "Gagal update status: ${exception.message}")
                }
        }

        // Fungsi untuk update status pembayaran menjadi DP
        fun updateStatusToDP(idTransaksi: String, jumlahDP: Double, totalBayar: Double, callback: (Boolean, String) -> Unit) {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                callback(false, "User tidak terautentikasi")
                return
            }

            val transaksiRef = database.getReference("transaksi")
                .child(currentUser.uid)
                .child(idTransaksi)

            val sisaPembayaran = totalBayar - jumlahDP

            val updates = mapOf(
                "statusPembayaran" to "DP",
                "jumlahDP" to jumlahDP,
                "sisaPembayaran" to sisaPembayaran
            )

            transaksiRef.updateChildren(updates)
                .addOnSuccessListener {
                    callback(true, "Status berhasil diupdate menjadi DP")
                }
                .addOnFailureListener { exception ->
                    callback(false, "Gagal update status: ${exception.message}")
                }
        }
    }
}