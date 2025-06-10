package com.tugasss.laundryapp.adapter

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.ImageView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.tugasss.laundryapp.R
import com.tugasss.laundryapp.modeldata.modelPegawai
import com.tugasss.laundryapp.pegawai.TambahPegawaiActivity
import android.widget.Toast

class AdapterDataPegawai(
    private val listPegawai: ArrayList<modelPegawai>
) : RecyclerView.Adapter<AdapterDataPegawai.ViewHolder>() {

    lateinit var appContext: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_data_pegawai, parent, false)
        appContext = parent.context
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = listPegawai[position]
        holder.tvID.text = item.idPegawai
        holder.tvNama.text = item.namaPegawai
        holder.tvAlamat.text = item.alamatPegawai
        holder.tvNoHP.text = item.noHPPegawai
        holder.tvCabang.text = item.idCabang

        // Set gambar profil berdasarkan jenis kelamin
        when (item.jenisKelamin?.lowercase()) {
            "wanita", "female" -> holder.ivProfil.setImageResource(R.drawable.profilwanita)
            "pria", "male" -> holder.ivProfil.setImageResource(R.drawable.profilpria)
            else -> holder.ivProfil.setImageResource(R.drawable.profilpria) // default pria
        }

        holder.btHubungi.setOnClickListener {
            // Implementasi hubungi bisa ditambahkan di sini
        }

        holder.btLihat.setOnClickListener {
            val dialogView = LayoutInflater.from(holder.itemView.context)
                .inflate(R.layout.dialog_mod_pegawai, null)

            val dialogBuilder = AlertDialog.Builder(holder.itemView.context)
                .setView(dialogView)
                .setCancelable(true)

            val alertDialog = dialogBuilder.create()

            val tvIdPegawai = dialogView.findViewById<TextView>(R.id.tvID_PEGAWAI)
            val tvNamaPegawai = dialogView.findViewById<TextView>(R.id.tvNAMA_PEGAWAI)
            val tvAlamatPegawai = dialogView.findViewById<TextView>(R.id.tvALAMAT_PEGAWAI)
            val tvNoHPPegawai = dialogView.findViewById<TextView>(R.id.tvNO_HP)
            val tvCabangPegawai = dialogView.findViewById<TextView>(R.id.tvCABANG)

            val btEdit = dialogView.findViewById<Button>(R.id.btDIALOG_MOD_PEGAWAI_Edit)
            val btHapus = dialogView.findViewById<Button>(R.id.btDIALOG_MOD_PEGAWAI_Hapus)

            // Set text dengan data
            tvIdPegawai.text = item.idPegawai
            tvNamaPegawai.text = item.namaPegawai
            tvAlamatPegawai.text = item.alamatPegawai
            tvNoHPPegawai.text = item.noHPPegawai
            tvCabangPegawai.text = item.idCabang

            // Set text button dengan string resource
            btEdit.text = appContext.getString(R.string.edit)
            btHapus.text = appContext.getString(R.string.hapus)

            btEdit.setOnClickListener {
                val intent = Intent(appContext, TambahPegawaiActivity::class.java)
                intent.putExtra("Judul", appContext.getString(R.string.edit_pegawai))
                intent.putExtra("idPegawai", item.idPegawai)
                intent.putExtra("namaPegawai", item.namaPegawai)
                intent.putExtra("noHPPegawai", item.noHPPegawai)
                intent.putExtra("alamatPegawai", item.alamatPegawai)
                intent.putExtra("idCabang", item.idCabang)
                intent.putExtra("jeniskelamin", item.jenisKelamin)
                appContext.startActivity(intent)
                alertDialog.dismiss()
            }

            btHapus.setOnClickListener {
                // Tambah konfirmasi dialog sebelum menghapus
                val confirmDialog = AlertDialog.Builder(holder.itemView.context)
                    .setTitle(appContext.getString(R.string.konfirmasi_hapus))
                    .setMessage(appContext.getString(R.string.yakin_hapus_pegawai))
                    .setPositiveButton(appContext.getString(R.string.ya)) { _, _ ->
                        val position = holder.adapterPosition
                        if (position != RecyclerView.NO_POSITION) {
                            val idPegawai = listPegawai[position].idPegawai

                            val database = com.google.firebase.database.FirebaseDatabase.getInstance()
                            val pegawaiRef = database.getReference("pegawai").child(idPegawai ?: "")

                            pegawaiRef.removeValue().addOnSuccessListener {
                                listPegawai.removeAt(position)
                                notifyItemRemoved(position)
                                Toast.makeText(
                                    holder.itemView.context,
                                    appContext.getString(R.string.data_pegawai_berhasil_dihapus),
                                    Toast.LENGTH_SHORT
                                ).show()
                                alertDialog.dismiss()
                            }.addOnFailureListener {
                                Toast.makeText(
                                    holder.itemView.context,
                                    appContext.getString(R.string.gagal_menghapus_data_pegawai),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                    .setNegativeButton(appContext.getString(R.string.tidak)) { dialog, _ ->
                        dialog.dismiss()
                    }
                    .create()

                confirmDialog.show()
            }

            alertDialog.show()
        }

        holder.cvCARD.setOnClickListener {
            val intent = Intent(appContext, TambahPegawaiActivity::class.java)
            intent.putExtra("Judul", appContext.getString(R.string.edit_pegawai))
            intent.putExtra("idPegawai", item.idPegawai)
            intent.putExtra("namaPegawai", item.namaPegawai)
            intent.putExtra("noHPPegawai", item.noHPPegawai)
            intent.putExtra("alamatPegawai", item.alamatPegawai)
            intent.putExtra("idCabang", item.idCabang)
            intent.putExtra("jeniskelamin", item.jenisKelamin)
            appContext.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return listPegawai.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvID: TextView = itemView.findViewById(R.id.tvID_PEGAWAI)
        val tvNama: TextView = itemView.findViewById(R.id.tvNAMA_PEGAWAI)
        val tvAlamat: TextView = itemView.findViewById(R.id.tvALAMAT_PEGAWAI)
        val tvNoHP: TextView = itemView.findViewById(R.id.tvNO_HP)
        val tvCabang: TextView = itemView.findViewById(R.id.tvCABANG)
        val cvCARD: CardView = itemView.findViewById(R.id.cvCARD_PEGAWAI)
        val btHubungi: CardView = itemView.findViewById(R.id.btHUBUNGI)
        val btLihat: CardView = itemView.findViewById(R.id.btLIHAT)
        val ivProfil: ImageView = itemView.findViewById(R.id.ivProfilPegawai)
    }
}