package com.tugasss.laundryapp.adapter

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.firebase.database.FirebaseDatabase
import com.tugasss.laundryapp.R
import com.tugasss.laundryapp.modeldata.modelLayanan
import com.tugasss.laundryapp.layanan.TambahLayananActivity

class AdapterDataLayanan(private var layananList: ArrayList<modelLayanan>) :
    RecyclerView.Adapter<AdapterDataLayanan.ViewHolder>() {

    private val database = FirebaseDatabase.getInstance()
    private val myRef = database.getReference("layanan")

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_data_layanan, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val layanan = layananList[position]
        val context = holder.itemView.context

        // Bind data ke views menggunakan string resources
        holder.tvIdLayanan.text = layanan.idLayanan ?: ""
        holder.tvNamaLayanan.text = layanan.namaLayanan ?: context.getString(R.string.nama_tidak_tersedia)
        holder.tvHargaLayanan.text = context.getString(R.string.harga_format, layanan.hargaLayanan ?: "0")
        holder.tvDurasi.text = context.getString(R.string.durasi_format, layanan.durasi ?: "0")

        // Set click listener untuk tombol opsi - menampilkan dialog detail
        holder.btLihat.setOnClickListener {
            showDetailDialog(context, layanan, position)
        }
    }

    private fun showDetailDialog(context: Context, layanan: modelLayanan, position: Int) {
        try {
            // Inflate layout dialog detail
            val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_mod_layanan, null)

            // Inisialisasi views dari dialog
            val tvIdLayanan = dialogView.findViewById<TextView>(R.id.tvID_LAYANAN)
            val tvNamaLayanan = dialogView.findViewById<TextView>(R.id.tvNAMA_LAYANAN)
            val tvHargaLayanan = dialogView.findViewById<TextView>(R.id.tvHARGA_LAYANAN)
            val tvDurasi = dialogView.findViewById<TextView>(R.id.tvDURASI)
            val btEdit = dialogView.findViewById<MaterialButton>(R.id.btDIALOG_MOD_LAYANAN_Edit)
            val btHapus = dialogView.findViewById<Button>(R.id.btDIALOG_MOD_LAYANAN_Hapus)

            // Set data to dialog views menggunakan string resources
            tvIdLayanan.text = layanan.idLayanan ?: ""
            tvNamaLayanan.text = layanan.namaLayanan ?: context.getString(R.string.nama_tidak_tersedia)
            tvHargaLayanan.text = context.getString(R.string.harga_format, layanan.hargaLayanan ?: "0")
            tvDurasi.text = context.getString(R.string.durasi_format, layanan.durasi ?: "0")

            // Create dialog
            val dialog = Dialog(context)
            dialog.setContentView(dialogView)
            dialog.window?.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            dialog.setCancelable(true) // Allow dismiss by back button or outside touch

            // Set click listeners untuk tombol di dialog
            btEdit.setOnClickListener {
                dialog.dismiss()
                editLayanan(context, layanan)
            }

            btHapus.setOnClickListener {
                dialog.dismiss()
                showDeleteDialog(context, layanan, position)
            }

            dialog.show()

        } catch (e: Exception) {
            Toast.makeText(
                context,
                context.getString(R.string.error_dialog, e.message),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun editLayanan(context: Context, layanan: modelLayanan) {
        val intent = Intent(context, TambahLayananActivity::class.java).apply {
            putExtra(context.getString(R.string.extra_id), layanan.idLayanan)
            putExtra(context.getString(R.string.extra_nama), layanan.namaLayanan)
            putExtra(context.getString(R.string.extra_harga), layanan.hargaLayanan)
            putExtra(context.getString(R.string.extra_durasi), layanan.durasi)
            putExtra(context.getString(R.string.extra_mode), context.getString(R.string.mode_edit))
        }
        context.startActivity(intent)
    }

    private fun showDeleteDialog(context: Context, layanan: modelLayanan, position: Int) {
        AlertDialog.Builder(context)
            .setTitle(context.getString(R.string.hapus_layanan))
            .setMessage(context.getString(R.string.konfirmasi_hapus_layanan, layanan.namaLayanan))
            .setPositiveButton(context.getString(R.string.ya)) { _, _ ->
                deleteLayanan(context, layanan, position)
            }
            .setNegativeButton(context.getString(R.string.tidak), null)
            .setCancelable(true)
            .show()
    }

    private fun deleteLayanan(context: Context, layanan: modelLayanan, position: Int) {
        layanan.idLayanan?.let { id ->
            myRef.child(id).removeValue()
                .addOnSuccessListener {
                    // Update list dan notify adapter
                    if (position < layananList.size) {
                        layananList.removeAt(position)
                        notifyItemRemoved(position)
                        notifyItemRangeChanged(position, layananList.size)
                    }
                    Toast.makeText(
                        context,
                        context.getString(R.string.layanan_berhasil_dihapus),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(
                        context,
                        context.getString(R.string.gagal_menghapus_layanan, exception.message),
                        Toast.LENGTH_SHORT
                    ).show()
                }
        } ?: run {
            Toast.makeText(
                context,
                context.getString(R.string.id_layanan_tidak_valid),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun getItemCount(): Int = layananList.size

    fun updateData(newList: ArrayList<modelLayanan>) {
        layananList.clear()
        layananList.addAll(newList)
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvIdLayanan: TextView = itemView.findViewById(R.id.tvID_LAYANAN)
        val tvNamaLayanan: TextView = itemView.findViewById(R.id.tvNAMA_LAYANAN)
        val tvHargaLayanan: TextView = itemView.findViewById(R.id.tvHARGA_LAYANAN)
        val tvDurasi: TextView = itemView.findViewById(R.id.tvDURASI)
        val btLihat: androidx.cardview.widget.CardView = itemView.findViewById(R.id.btLIHAT)
    }
}