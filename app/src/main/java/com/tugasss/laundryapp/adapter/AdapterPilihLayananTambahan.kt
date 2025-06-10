
package com.tugasss.laundryapp.adapter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DatabaseReference
import com.tugasss.laundryapp.R
import com.tugasss.laundryapp.modeldata.modelTambahan // Pastikan ini diimpor

class AdapterPilihLayananTambahan(private val listTambahan: MutableList<modelTambahan>) :
    RecyclerView.Adapter<AdapterPilihLayananTambahan.ViewHolder>() {

    private val TAG = "AdapterPilihLayananTambahan"
    lateinit var appContext: Context
    lateinit var databaseReference: DatabaseReference

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): ViewHolder {
        Log.d(TAG, "onCreateViewHolder called")
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_pilih_layanan_tambahan, parent, false)
        appContext = parent.context
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Log.d(TAG, "onBindViewHolder for position: $position")
        val nomor = position + 1
        val item = listTambahan[position]

        try {
            holder.tvID.text = "[$nomor]"
            holder.tvNama.text = item.namaLayananTambahan
                ?: appContext.getString(R.string.layanan_tidak_tersedia)

            // Menggunakan string resource dengan format
            val hargaText = item.hargaLayananTambahan
                ?: appContext.getString(R.string.harga_tidak_tersedia)
            holder.tvharga.text = appContext.getString(R.string.harga_format, hargaText)

            Log.d(TAG, "Binding data: ${item.namaLayananTambahan} at position $position")

            holder.cvCARD.setOnClickListener {
                try {
                    val intent = Intent()
                    // Mengirim seluruh objek modelTambahan
                    intent.putExtra("selectedTambahan", item) // Gunakan key yang jelas
                    (appContext as Activity).setResult(Activity.RESULT_OK, intent)
                    (appContext as Activity).finish()
                } catch (e: Exception) {
                    Log.e(TAG, "Error in click listener: ${e.message}")
                    Toast.makeText(
                        appContext,
                        appContext.getString(R.string.terjadi_kesalahan),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error binding view holder: ${e.message}")
        }
    }

    override fun getItemCount(): Int {
        val count = listTambahan.size
        Log.d(TAG, "getItemCount: $count")
        return count
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvID: TextView = itemView.findViewById(R.id.tvID_LAYANAN_TAMBAHAN)
        val tvNama: TextView = itemView.findViewById(R.id.tvNAMA_LAYANAN_TAMBAHAN)
        val tvharga: TextView = itemView.findViewById(R.id.tvHARGA_LAYANAN_TAMBAHAN)
        val cvCARD: CardView = itemView.findViewById(R.id.cvCARD_PILIH_LAYANAN_TAMBAHAN)
    }
}