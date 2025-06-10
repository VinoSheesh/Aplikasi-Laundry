package com.tugasss.laundryapp.adapter

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.tugasss.laundryapp.R
import com.tugasss.laundryapp.modeldata.modelCabang
import com.tugasss.laundryapp.cabang.TambahCabangActivity

class AdapterDataCabang(
    private val listCabang: ArrayList<modelCabang>
) : RecyclerView.Adapter<AdapterDataCabang.ViewHolder>() {

    lateinit var appContext: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_data_cabang, parent, false)
        appContext = parent.context
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = listCabang[position]
        holder.tvNamaCabang.text = item.namaCabang
        holder.tvAlamat.text = item.alamatCabang
        holder.tvNoHP.text = item.kontakCabang

        // Tombol Hubungi - Ke WhatsApp
        holder.btHubungi.setOnClickListener {
            val phoneNumber = item.kontakCabang?.replace("+", "")?.replace("-", "")?.replace(" ", "")
            if (!phoneNumber.isNullOrEmpty()) {
                val message = appContext.getString(R.string.whatsapp_message_template, item.namaCabang)
                val url = "https://api.whatsapp.com/send?phone=$phoneNumber&text=${Uri.encode(message)}"
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                try {
                    appContext.startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(
                        appContext,
                        appContext.getString(R.string.whatsapp_not_installed),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                Toast.makeText(
                    appContext,
                    appContext.getString(R.string.invalid_contact_number),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        // Tombol Lihat/Option - Tampilkan Dialog
        holder.btLihat.setOnClickListener {
            showDetailDialog(holder, item, position)
        }

        // Click pada card untuk edit
        holder.cvCabang.setOnClickListener {
            val intent = Intent(appContext, TambahCabangActivity::class.java)
            intent.putExtra("Judul", appContext.getString(R.string.edit_branch))
            intent.putExtra("idCabang", item.idCabang)
            intent.putExtra("namaCabang", item.namaCabang)
            intent.putExtra("alamatCabang", item.alamatCabang)
            intent.putExtra("kontakCabang", item.kontakCabang)
            appContext.startActivity(intent)
        }
    }

    private fun showDetailDialog(holder: ViewHolder, item: modelCabang, position: Int) {
        val dialogView = LayoutInflater.from(holder.itemView.context)
            .inflate(R.layout.dialog_mod_cabang, null)

        val dialogBuilder = AlertDialog.Builder(holder.itemView.context)
            .setView(dialogView)
            .setCancelable(true)

        val alertDialog = dialogBuilder.create()

        // Set data ke dialog
        val tvIdCabang = dialogView.findViewById<TextView>(R.id.tvID_CABANG)
        val tvNamaCabang = dialogView.findViewById<TextView>(R.id.tvNAMA_CABANG)
        val tvAlamatCabang = dialogView.findViewById<TextView>(R.id.tvALAMAT_CABANG)
        val tvNoHP = dialogView.findViewById<TextView>(R.id.tvNO_HP)

        val btEdit = dialogView.findViewById<Button>(R.id.btDIALOG_MOD_CABANG_Edit)
        val btHapus = dialogView.findViewById<Button>(R.id.btDIALOG_MOD_CABANG_Hapus)

        tvIdCabang.text = item.idCabang
        tvNamaCabang.text = item.namaCabang
        tvAlamatCabang.text = item.alamatCabang
        tvNoHP.text = item.kontakCabang

        // Set text untuk button dari string resource
        btEdit.text = appContext.getString(R.string.edit_button)
        btHapus.text = appContext.getString(R.string.delete_button)

        // Tombol Edit
        btEdit.setOnClickListener {
            val intent = Intent(appContext, TambahCabangActivity::class.java)
            intent.putExtra("Judul", appContext.getString(R.string.edit_branch))
            intent.putExtra("idCabang", item.idCabang)
            intent.putExtra("namaCabang", item.namaCabang)
            intent.putExtra("alamatCabang", item.alamatCabang)
            intent.putExtra("kontakCabang", item.kontakCabang)
            appContext.startActivity(intent)
            alertDialog.dismiss()
        }

        // Tombol Hapus
        btHapus.setOnClickListener {
            showDeleteConfirmation(item, position, alertDialog)
        }

        alertDialog.show()
    }

    private fun showDeleteConfirmation(item: modelCabang, position: Int, parentDialog: AlertDialog) {
        AlertDialog.Builder(appContext)
            .setTitle(appContext.getString(R.string.delete_confirmation_title))
            .setMessage(appContext.getString(R.string.delete_confirmation_message, item.namaCabang))
            .setPositiveButton(appContext.getString(R.string.yes_button)) { _, _ ->
                deleteItem(item, position, parentDialog)
            }
            .setNegativeButton(appContext.getString(R.string.no_button), null)
            .show()
    }

    private fun deleteItem(item: modelCabang, position: Int, parentDialog: AlertDialog) {
        val database = com.google.firebase.database.FirebaseDatabase.getInstance()
        val cabangRef = database.getReference("cabang").child(item.idCabang ?: "")

        cabangRef.removeValue().addOnSuccessListener {
            listCabang.removeAt(position)
            notifyItemRemoved(position)
            Toast.makeText(
                appContext,
                appContext.getString(R.string.branch_deleted_success),
                Toast.LENGTH_SHORT
            ).show()
            parentDialog.dismiss()
        }.addOnFailureListener {
            Toast.makeText(
                appContext,
                appContext.getString(R.string.branch_delete_failed),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun getItemCount(): Int {
        return listCabang.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNamaCabang: TextView = itemView.findViewById(R.id.tvNAMA_CABANG)
        val tvAlamat: TextView = itemView.findViewById(R.id.tvALAMAT)
        val tvNoHP: TextView = itemView.findViewById(R.id.tvNO_HP)
        val cvCabang: CardView = itemView.findViewById(R.id.cvCARD_PELANGGAN)
        val btHubungi: CardView = itemView.findViewById(R.id.btHUBUNGI)
        val btLihat: CardView = itemView.findViewById(R.id.btLIHAT)
    }
}