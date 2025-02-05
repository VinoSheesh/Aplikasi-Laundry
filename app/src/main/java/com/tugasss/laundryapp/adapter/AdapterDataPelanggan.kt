package com.tugasss.laundryapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.core.view.View


class AdapterDataPelanggan(
    private val listPelanggan: ArrayList<ModelPelanggan>) :
    RecyclerView.Adapter<AdapterDataPelanggan.ViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(parent,context)
            .inflate(R.layout.card_data_pelanggan,parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = listPelanggan(position)
        holder.tvID.text = item.idPelanggan
        holder.tvNama.text = item
    }

    override fun getItemCount(): Int {
        return listPelanggan.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){

    }
}