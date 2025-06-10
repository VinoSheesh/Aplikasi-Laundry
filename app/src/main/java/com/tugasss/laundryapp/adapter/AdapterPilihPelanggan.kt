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
import com.tugasss.laundryapp.modeldata.modelPelanggan
import com.tugasss.laundryapp.transaksi.transaksi

class AdapterPilihPelanggan(private val listPelanggan: ArrayList<modelPelanggan>) :
    RecyclerView.Adapter<AdapterPilihPelanggan.ViewHolder>() {

    private val TAG = "AdapterPilihPelanggan"
    lateinit var appContext: Context
    lateinit var databaseReference: DatabaseReference

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): ViewHolder {
        Log.d(TAG, "onCreateViewHolder called")
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_pilih_pelanggan, parent, false)
        appContext = parent.context
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Log.d(TAG, "onBindViewHolder for position: $position")
        val nomor = position + 1
        val item = listPelanggan[position]

        try {
            holder.tvID.text = "[$nomor]"
            holder.tvNama.text = item.namaPelanggan ?: appContext.getString(R.string.nama_tidak_tersedia)
            holder.tvAlamat.text = item.alamatPelanggan ?: appContext.getString(R.string.alamat_tidak_tersedia)
            holder.tvNoHP.text = item.noHPPelanggan ?: appContext.getString(R.string.nohp_tidak_tersedia)

            Log.d(TAG, "Binding data: ${item.namaPelanggan} at position $position")

            holder.cvCARD.setOnClickListener {
                try {
                    val intent = Intent(appContext, transaksi::class.java)
                    intent.putExtra("idPelanggan", item.idPelanggan)
                    intent.putExtra("nama", item.namaPelanggan)
                    intent.putExtra("noHP", item.noHPPelanggan)
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
        val count = listPelanggan.size
        Log.d(TAG, "getItemCount: $count")
        return count
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvID: TextView = itemView.findViewById(R.id.tvID_PELANGGAN)
        val tvNama: TextView = itemView.findViewById(R.id.tvNAMA_PELANGGAN)
        val tvAlamat: TextView = itemView.findViewById(R.id.tvALAMAT_PELANGGAN)
        val tvNoHP: TextView = itemView.findViewById(R.id.tvNO_HP)
        val cvCARD: CardView = itemView.findViewById(R.id.cvCARD_PILIH_PELANGGAN)
    }
}