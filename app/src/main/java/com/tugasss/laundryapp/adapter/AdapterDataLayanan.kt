package com.tugasss.laundryapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.tugasss.laundryapp.R
import com.tugasss.laundryapp.modeldata.modelLayanan

class AdapterDataLayanan(
    private val listlayanan: ArrayList<modelLayanan>) :
    RecyclerView.Adapter<AdapterDataLayanan.ViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_data_layanan, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = listlayanan[position]
        holder.tvID.text = item.idLayanan
        holder.tvLayanan.text = item.idLayanan
        holder.tvHarga.text = item.Harga
        holder.tvCabang.text = item.idCabang

        holder.cvCARD.setOnClickListener {

        }
        holder.btHubungi.setOnClickListener{

        }
        holder.btLihat.setOnClickListener{

        }
    }

    override fun getItemCount(): Int {
        return listlayanan.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val tvID: TextView = itemView.findViewById(R.id.tvID_LAYANAN)
        val tvLayanan: TextView = itemView.findViewById(R.id.tvNAMA_LAYANAN)
        val tvHarga: TextView = itemView.findViewById(R.id.tvHARGA_LAYANAN)
        val tvCabang: TextView = itemView.findViewById(R.id.tvCABANG)
        val cvCARD: CardView = itemView.findViewById(R.id.cvCARD_LAYANAN)
        val btHubungi: Button = itemView.findViewById(R.id.btHUBUNGI)
        val btLihat: Button = itemView.findViewById(R.id.btLIHAT)
    }
}