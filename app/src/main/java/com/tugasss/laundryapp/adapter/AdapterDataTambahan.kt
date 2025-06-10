package com.tugasss.laundryapp.adapter

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.FirebaseDatabase
import com.tugasss.laundryapp.R
import com.tugasss.laundryapp.modeldata.modelTambahan
import com.tugasss.laundryapp.tambahan.TambahTambahanActivity

class AdapterDataTambahan(
    private var listLayananTambahan: MutableList<modelTambahan>) :
    RecyclerView.Adapter<AdapterDataTambahan.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_data_layanan_tambahan, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = listLayananTambahan[position]
        val context = holder.itemView.context

        // Menggunakan string resources untuk teks yang bisa diterjemahkan
        holder.tvID.text = item.idLayananTambahan ?: context.getString(R.string.id_tidak_tersedia)
        holder.tvLayanan.text = item.namaLayananTambahan ?: context.getString(R.string.nama_layanan_tidak_tersedia)
        holder.tvHargaLayanan.text = item.hargaLayananTambahan ?: context.getString(R.string.harga_tidak_tersedia)

        holder.btOpsi.setOnClickListener {
            showDialogMod(context, item)
        }
    }

    override fun getItemCount(): Int {
        return listLayananTambahan.size
    }

    private fun showDialogMod(context: Context, item: modelTambahan) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_mod_layanan_tambahan, null)
        val dialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .create()

        // PERBAIKAN: Tambahkan null check untuk setiap findViewById
        val tvId = dialogView.findViewById<TextView>(R.id.tvID_LAYANAN_TAMBAHAN)
        val tvNama = dialogView.findViewById<TextView>(R.id.tvNAMA_LAYANAN_TAMBAHAN)
        val tvHarga = dialogView.findViewById<TextView>(R.id.tvHARGA_LAYANAN_TAMBAHAN)
        val btEdit = dialogView.findViewById<Button>(R.id.btDIALOG_MOD_LAYANAN_TAMBAHAN_Edit)
        val btHapus = dialogView.findViewById<Button>(R.id.btDIALOG_MOD_LAYANAN_TAMBAHAN_Hapus)

        // PERBAIKAN: Tambahkan null check sebelum menggunakan view dengan string resources
        tvId?.text = item.idLayananTambahan ?: context.getString(R.string.id_tidak_tersedia)
        tvNama?.text = item.namaLayananTambahan ?: context.getString(R.string.nama_tidak_tersedia)
        tvHarga?.text = item.hargaLayananTambahan ?: context.getString(R.string.harga_tidak_tersedia)

        // Handle tombol Edit
        btEdit?.setOnClickListener {
            val intent = Intent(context, TambahTambahanActivity::class.java)
            intent.putExtra("id", item.idLayananTambahan)
            intent.putExtra("nama", item.namaLayananTambahan)
            intent.putExtra("harga", item.hargaLayananTambahan)
            context.startActivity(intent)
            dialog.dismiss()
        }

        // Handle tombol Hapus
        btHapus?.setOnClickListener {
            AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.konfirmasi_hapus))
                .setMessage(context.getString(R.string.konfirmasi_hapus_message, item.namaLayananTambahan ?: ""))
                .setPositiveButton(context.getString(R.string.ya)) { _, _ ->
                    hapusData(context, item.idLayananTambahan)
                    dialog.dismiss()
                }
                .setNegativeButton(context.getString(R.string.tidak), null)
                .show()
        }

        dialog.show()
    }

    private fun hapusData(context: Context, id: String?) {
        if (id != null) {
            val database = FirebaseDatabase.getInstance()
            val myRef = database.getReference("tambahan")

            myRef.child(id).removeValue()
                .addOnSuccessListener {
                    Toast.makeText(context, context.getString(R.string.berhasil_menghapus_data), Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(context, context.getString(R.string.gagal_menghapus_data, exception.message), Toast.LENGTH_SHORT).show()
                }
        }
    }

    fun updateData(newData: List<modelTambahan>) {
        listLayananTambahan.clear()
        listLayananTambahan.addAll(newData)
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val tvID: TextView = itemView.findViewById(R.id.tvID_LAYANAN_TAMBAHAN)
        val tvLayanan: TextView = itemView.findViewById(R.id.tvNAMA_LAYANAN_TAMBAHAN)
        val tvHargaLayanan: TextView = itemView.findViewById(R.id.tvHARGA_LAYANAN_TAMBAHAN)
        val cvCARD: CardView = itemView.findViewById(R.id.cvCARD_LAYANAN_TAMBAHAN)
        val btOpsi: CardView = itemView.findViewById(R.id.btLIHAT)
    }
}