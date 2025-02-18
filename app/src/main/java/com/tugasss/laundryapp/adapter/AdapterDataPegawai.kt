package com.tugasss.laundryapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.tugasss.laundryapp.R
import com.tugasss.laundryapp.modeldata.modelPegawai

class AdapterDataPegawai(
    private val listPegawai: ArrayList<modelPegawai>) :
    RecyclerView.Adapter<AdapterDataPegawai.ViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_data_pegawai, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = listPegawai[position]
        holder.tvID.text = item.idPegawai
        holder.tvNama.text = item.namaPegawai
        holder.tvAlamat.text = item.alamatPegawai
        holder.tvNoHP.text = item.noHPPegawai
        holder.tvCabang.text = item.idCabang

        holder.cvCARD.setOnClickListener {

        }
        holder.btHubungi.setOnClickListener{

        }
        holder.btLihat.setOnClickListener{

        }
    }

    override fun getItemCount(): Int {
        return listPegawai.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val tvID: TextView = itemView.findViewById(R.id.tvID_PEGAWAI)
        val tvNama: TextView = itemView.findViewById(R.id.tvNAMA_PEGAWAI)
        val tvAlamat: TextView = itemView.findViewById(R.id.tvALAMAT_PEGAWAI)
        val tvNoHP: TextView = itemView.findViewById(R.id.tvNO_HP)
        val tvCabang: TextView = itemView.findViewById(R.id.tvCABANG)
        val cvCARD: CardView = itemView.findViewById(R.id.cvCARD_PEGAWAI)
        val btHubungi: Button = itemView.findViewById(R.id.btHUBUNGI)
        val btLihat: Button = itemView.findViewById(R.id.btLIHAT)
    }
}