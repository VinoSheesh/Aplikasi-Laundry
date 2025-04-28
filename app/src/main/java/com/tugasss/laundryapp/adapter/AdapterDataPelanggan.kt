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
        holder.tvCabang.text = item.idCabang

        holder.cvCARD.setOnClickListener {

        }
            holder.btHubungi.setOnClickListener{

        }
        holder.btLihat.setOnClickListener {
            // Inflate layout pop-up
            val dialogView = LayoutInflater.from(holder.itemView.context)
                .inflate(R.layout.dialog_mod_pelanggan, null)

            // Bikin AlertDialog
            val dialogBuilder = android.app.AlertDialog.Builder(holder.itemView.context)
                .setView(dialogView)
                .setCancelable(true) // Bisa ditutup dengan klik luar dialog

            val alertDialog = dialogBuilder.create()

            // Binding komponen dalam popup
            val tvIdPelanggan = dialogView.findViewById<TextView>(R.id.tvID_PELANGGAN)
            val tvNamaPelanggan = dialogView.findViewById<TextView>(R.id.tvNAMA_PELANGGAN)
            val tvAlamatPelanggan = dialogView.findViewById<TextView>(R.id.tvALAMAT_PELANGGAN)
            val tvNoHPPelanggan = dialogView.findViewById<TextView>(R.id.tvNO_HP)
            val tvCabangPelanggan = dialogView.findViewById<TextView>(R.id.tvCABANG)

            val btEdit = dialogView.findViewById<Button>(R.id.btDIALOG_MOD_PELANGGAN_Edit)
            val btHapus = dialogView.findViewById<Button>(R.id.btDIALOG_MOD_PELANGGAN_Hapus)

            // Set data pelanggan ke popup
            tvIdPelanggan.text = item.idPelanggan
            tvNamaPelanggan.text = item.namaPelanggan
            tvAlamatPelanggan.text = item.alamatPelanggan
            tvNoHPPelanggan.text = item.noHPPelanggan
            tvCabangPelanggan.text = item.idCabang

            // Tombol edit dan hapus tinggal diset onClickListener kalau mau
            btEdit.setOnClickListener {
                // Aksi edit
            }

            btHapus.setOnClickListener {
                // Hapus data dari list
                val position = holder.adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listPelanggan.removeAt(position)
                    notifyItemRemoved(position)

                    // Tutup dialog setelah hapus
                    alertDialog.dismiss()
                }
            }

            // Tampilkan dialog
            alertDialog.show()
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
    val tvCabang: TextView = itemView.findViewById(R.id.tvCABANG)
    val cvCARD: CardView = itemView.findViewById(R.id.cvCARD_PELANGGAN)
    val btHubungi: Button = itemView.findViewById(R.id.btHUBUNGI)
    val btLihat: Button = itemView.findViewById(R.id.btLIHAT)
    }
}