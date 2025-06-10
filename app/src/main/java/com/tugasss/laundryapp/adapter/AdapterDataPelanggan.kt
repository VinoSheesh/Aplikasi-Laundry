package com.tugasss.laundryapp.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.ImageView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.FirebaseDatabase
import com.tugasss.laundryapp.R
import com.tugasss.laundryapp.modeldata.modelPelanggan
import com.tugasss.laundryapp.pelanggan.TambahPelangganActivity
import android.widget.Toast

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
        val context = holder.itemView.context

        // Tidak menampilkan ID di card, sesuai permintaan
        holder.tvNama.text = item.namaPelanggan
        holder.tvAlamat.text = item.alamatPelanggan
        holder.tvNoHP.text = item.noHPPelanggan
        holder.tvCabang.text = item.idCabang

        when (item.jenisKelamin) {
            "pria" -> holder.ivProfil.setImageResource(R.drawable.profilpria)
            "wanita" -> holder.ivProfil.setImageResource(R.drawable.profilwanita)
            else -> holder.ivProfil.setImageResource(R.drawable.profilpria) // default
        }

        holder.cvCARD.setOnClickListener {
            // Kosong sesuai kode asli
        }

        holder.btHubungi.setOnClickListener{
            // Kosong sesuai kode asli
        }

        holder.btLihat.setOnClickListener {
            val dialogView = LayoutInflater.from(context)
                .inflate(R.layout.dialog_mod_pelanggan, null)

            val dialogBuilder = android.app.AlertDialog.Builder(context)
                .setView(dialogView)
                .setCancelable(true)

            val alertDialog = dialogBuilder.create()

            val tvIdPelanggan = dialogView.findViewById<TextView>(R.id.tvID_PELANGGAN)
            val tvNamaPelanggan = dialogView.findViewById<TextView>(R.id.tvNAMA_PELANGGAN)
            val tvAlamatPelanggan = dialogView.findViewById<TextView>(R.id.tvALAMAT_PELANGGAN)
            val tvNoHPPelanggan = dialogView.findViewById<TextView>(R.id.tvNO_HP)
            val tvCabangPelanggan = dialogView.findViewById<TextView>(R.id.tvCABANG)

            val btEdit = dialogView.findViewById<Button>(R.id.btDIALOG_MOD_PELANGGAN_Edit)
            val btHapus = dialogView.findViewById<Button>(R.id.btDIALOG_MOD_PELANGGAN_Hapus)

            // ID tetap ditampilkan di dialog sesuai permintaan
            tvIdPelanggan.text = item.idPelanggan
            tvNamaPelanggan.text = item.namaPelanggan
            tvAlamatPelanggan.text = item.alamatPelanggan
            tvNoHPPelanggan.text = item.noHPPelanggan
            tvCabangPelanggan.text = item.idCabang

            // Set text button menggunakan string resources
            btEdit.text = context.getString(R.string.button_edit)
            btHapus.text = context.getString(R.string.button_delete)

            btEdit.setOnClickListener {
                val intent = Intent(context, TambahPelangganActivity::class.java)
                intent.putExtra("id", item.idPelanggan)
                intent.putExtra("nama", item.namaPelanggan)
                intent.putExtra("alamat", item.alamatPelanggan)
                intent.putExtra("nohp", item.noHPPelanggan)
                intent.putExtra("cabang", item.idCabang)
                intent.putExtra("jeniskelamin", item.jenisKelamin)
                context.startActivity(intent)
                alertDialog.dismiss()
            }

            btHapus.setOnClickListener {
                val position = holder.adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val idPelanggan = listPelanggan[position].idPelanggan
                    if (!idPelanggan.isNullOrEmpty()) {
                        val database = FirebaseDatabase.getInstance()
                        val pelangganRef = database.getReference("Pelanggan").child(idPelanggan)

                        // Tampilkan dialog konfirmasi dengan string resources
                        android.app.AlertDialog.Builder(context)
                            .setTitle(context.getString(R.string.dialog_confirm_delete_title))
                            .setMessage(context.getString(R.string.dialog_confirm_delete_message))
                            .setPositiveButton(context.getString(R.string.dialog_yes)) { _, _ ->
                                pelangganRef.removeValue().addOnSuccessListener {
                                    // Hapus dari list lokal
                                    listPelanggan.removeAt(position)
                                    notifyItemRemoved(position)
                                    notifyItemRangeChanged(position, listPelanggan.size)

                                    Toast.makeText(
                                        context,
                                        context.getString(R.string.toast_delete_success),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    alertDialog.dismiss()
                                }.addOnFailureListener { exception ->
                                    Toast.makeText(
                                        context,
                                        context.getString(R.string.toast_delete_failed, exception.message),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                            .setNegativeButton(context.getString(R.string.dialog_no)) { dialog, _ ->
                                dialog.dismiss()
                            }
                            .show()
                    }
                }
            }

            alertDialog.show()
        }
    }

    override fun getItemCount(): Int {
        return listPelanggan.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val tvNama: TextView = itemView.findViewById(R.id.tvNAMA_PELANGGAN)
        val tvAlamat: TextView = itemView.findViewById(R.id.tvALAMAT_PELANGGAN)
        val tvNoHP: TextView = itemView.findViewById(R.id.tvNO_HP)
        val tvCabang: TextView = itemView.findViewById(R.id.tvCABANG)
        val cvCARD: CardView = itemView.findViewById(R.id.cvCARD_PELANGGAN)
        val btHubungi: CardView = itemView.findViewById(R.id.btHUBUNGI)
        val btLihat: CardView = itemView.findViewById(R.id.btLIHAT)
        val ivProfil: ImageView = itemView.findViewById(R.id.ivProfilPelanggan)
    }
}