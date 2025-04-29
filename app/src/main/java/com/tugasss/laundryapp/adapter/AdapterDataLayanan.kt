package com.tugasss.laundryapp.adapter

import android.app.AlertDialog
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.tugasss.laundryapp.R
import com.tugasss.laundryapp.modeldata.modelLayanan
import com.tugasss.laundryapp.pelanggan.TambahPelangganActivity
import android.widget.Toast

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
        holder.tvLayanan.text = item.namaLayanan
        holder.tvHargaLayanan.text = item.hargaLayanan
        holder.tvCabang.text = item.idCabang

        holder.cvCARD.setOnClickListener {

        }
        holder.btHubungi.setOnClickListener{

        }
        holder.btLihat.setOnClickListener {
            val dialogView = LayoutInflater.from(holder.itemView.context)
                .inflate(R.layout.dialog_mod_layanan, null)

            val dialogBuilder = AlertDialog.Builder(holder.itemView.context)
                .setView(dialogView)
                .setCancelable(true)

            val alertDialog = dialogBuilder.create()

            val tvIdLayanan = dialogView.findViewById<TextView>(R.id.tvID_LAYANAN)
            val tvNamaLayanan = dialogView.findViewById<TextView>(R.id.tvNAMA_LAYANAN)
            val tvHargaLayanan = dialogView.findViewById<TextView>(R.id.tvHARGA_LAYANAN)
            val tvCabangPelanggan = dialogView.findViewById<TextView>(R.id.tvCABANG)

            val btEdit = dialogView.findViewById<Button>(R.id.btDIALOG_MOD_LAYANAN_Edit)
            val btHapus = dialogView.findViewById<Button>(R.id.btDIALOG_MOD_LAYANAN_Hapus)

            tvIdLayanan.text = item.idLayanan
            tvNamaLayanan.text = item.namaLayanan
            tvHargaLayanan.text = item.hargaLayanan
            tvCabangPelanggan.text = item.idCabang

            btEdit.setOnClickListener {
                val context = holder.itemView.context
                val intent = Intent(context, TambahPelangganActivity::class.java)
                intent.putExtra("id", item.idLayanan)
                intent.putExtra("nama", item.namaLayanan)
                intent.putExtra("harga", item.hargaLayanan)
                intent.putExtra("cabang", item.idCabang)
                context.startActivity(intent)
                alertDialog.dismiss()
            }

            btHapus.setOnClickListener {
                val position = holder.adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val idLayanan = listlayanan[position].idLayanan

                    val database = com.google.firebase.database.FirebaseDatabase.getInstance()
                    val layananRef = database.getReference("Layanan").child(idLayanan ?: "")

                    layananRef.removeValue().addOnSuccessListener {
                        listlayanan.removeAt(position)
                        notifyItemRemoved(position)
                        Toast.makeText(holder.itemView.context, "Data layanan berhasil dihapus", Toast.LENGTH_SHORT).show()
                        alertDialog.dismiss()
                    }.addOnFailureListener {
                        Toast.makeText(holder.itemView.context, "Gagal menghapus data layanan", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            alertDialog.show()
        }
    }

    override fun getItemCount(): Int {
        return listlayanan.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val tvID: TextView = itemView.findViewById(R.id.tvID_LAYANAN)
        val tvLayanan: TextView = itemView.findViewById(R.id.tvNAMA_LAYANAN)
        val tvHargaLayanan: TextView = itemView.findViewById(R.id.tvHARGA_LAYANAN)
        val tvCabang: TextView = itemView.findViewById(R.id.tvCABANG)
        val cvCARD: CardView = itemView.findViewById(R.id.cvCARD_LAYANAN)
        val btHubungi: Button = itemView.findViewById(R.id.btHUBUNGI)
        val btLihat: Button = itemView.findViewById(R.id.btLIHAT)
    }
}