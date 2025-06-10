package com.tugasss.laundryapp.adapter

import android.app.Activity
import android.content.Context
import android.util.Log
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.tugasss.laundryapp.R
import com.tugasss.laundryapp.adapter.AdapterPilihPelanggan.ViewHolder
import com.tugasss.laundryapp.modeldata.modelLayanan
import com.tugasss.laundryapp.modeldata.modelPelanggan
import com.tugasss.laundryapp.transaksi.transaksi

class AdapterPilihLayanan (private val listLayanan: ArrayList<modelLayanan>) :
    RecyclerView.Adapter<AdapterPilihLayanan.ViewHolder>() {

    private val TAG = "AdapterPilihLayanan"
    lateinit var appContext: Context
    lateinit var databaseReference: DatabaseReference

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): ViewHolder {
        Log.d(TAG, "onCreateViewHolder called")
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_pilih_layanan, parent, false)
        appContext = parent.context
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Log.d(TAG, "onBindViewHolder for position: $position")
        val nomor = position + 1
        val item = listLayanan[position]

        try {
            holder.tvID.text = "[$nomor]"

            // Menggunakan string resource untuk internationalization
            holder.tvNama.text = item.namaLayanan ?: appContext.getString(R.string.layanan_tidak_tersedia)

            val hargaText = if (item.hargaLayanan != null) {
                appContext.getString(R.string.harga_label, item.hargaLayanan)
            } else {
                appContext.getString(R.string.harga_label, appContext.getString(R.string.harga_tidak_tersedia))
            }
            holder.tvharga.text = hargaText

            Log.d(TAG, "Binding data: ${item.namaLayanan} at position $position")

            holder.cvCARD.setOnClickListener {
                try {
                    val intent = Intent(appContext, transaksi::class.java)
                    intent.putExtra("idLayanan", item.idLayanan)
                    intent.putExtra("nama", item.namaLayanan)
                    intent.putExtra("harga", item.hargaLayanan)
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
        val count = listLayanan.size
        Log.d(TAG, "getItemCount: $count")
        return count
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvID: TextView = itemView.findViewById(R.id.tvID_LAYANAN)
        val tvNama: TextView = itemView.findViewById(R.id.tvNAMA_LAYANAN)
        val tvharga: TextView = itemView.findViewById(R.id.tvHARGA_LAYANAN)
        val cvCARD: CardView = itemView.findViewById(R.id.cvCARD_PILIH_LAYANAN)
    }
}