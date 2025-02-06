package com.tugasss.laundryapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.tugasss.laundryapp.R
import com.tugasss.laundryapp.modeldata.modelPelanggan

class AdapterDataPelanggan(
    private val listPelanggan: ArrayList<modelPelanggan>) :
    RecyclerView.Adapter<AdapterDataPelanggan.ViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_data_pelanggan, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = listPelanggan[position]
        holder.tvID.text = item.idPelanggan
        holder.tvNama.text = item.namaPelanggan
        holder.tvAlamat.text = item.alamatPelanggan
        holder.tvNoHP.text = item.noHPPelanggan
        holder.tvTerdaftar.text = item.terdaftar

        holder.cvCARD.setOnClickListener {

        }
            holder.btHubungi.setOnClickListener{

        }
            holder.btLihat.setOnClickListener{

        }
    }

    override fun getItemCount(): Int {
        return listPelanggan.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
    val tvID: TextView = itemView.findViewById(R.id.tvID_PELANGGAN)
    val tvNama: TextView = itemView.findViewById(R.id.tvNAMA_PELANGGAN)
    val tvAlamat: TextView = itemView.findViewById(R.id.tvALAMAT_PELANGGAN)
    val tvNoHP: TextView = itemView.findViewById(R.id.tvNO_HP)
    val tvTerdaftar: TextView = itemView.findViewById(R.id.tvTERDAFTAR)
    val cvCARD: CardView = itemView.findViewById(R.id.cvCARD_PELANGGAN)
    val btHubungi: Button = itemView.findViewById(R.id.btHUBUNGI)
    val btLihat: Button = itemView.findViewById(R.id.btLIHAT)
    }
}