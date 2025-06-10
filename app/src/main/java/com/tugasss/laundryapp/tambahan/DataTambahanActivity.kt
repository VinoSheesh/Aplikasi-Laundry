package com.tugasss.laundryapp.tambahan

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.tugasss.laundryapp.HomeActivity
import com.tugasss.laundryapp.R
import com.tugasss.laundryapp.adapter.AdapterDataTambahan
import com.tugasss.laundryapp.modeldata.modelTambahan

class DataTambahanActivity : AppCompatActivity() {
    val database = FirebaseDatabase.getInstance()
    val myRef = database.getReference("tambahan")
    lateinit var rvDATA_LAYANAN_TAMBAHAN: RecyclerView
    lateinit var fabDATA_PENGGUNA_Tambah: Button
    lateinit var tambahanList: ArrayList<modelTambahan>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_data_tambahan)
        init()
        val layoutManager = LinearLayoutManager(this)
        layoutManager.reverseLayout = true
        layoutManager.stackFromEnd = true
        rvDATA_LAYANAN_TAMBAHAN.layoutManager = layoutManager
        rvDATA_LAYANAN_TAMBAHAN.setHasFixedSize(true)
        tambahanList = arrayListOf<modelTambahan>()
        getData()

        val tambahtambahan = findViewById<Button>(R.id.fabDATA_PENGGUNA_Tambah)
        tambahtambahan.setOnClickListener {
            val intent = Intent(this, TambahTambahanActivity::class.java)
            startActivity(intent)
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val back = findViewById<ImageView>(R.id.Back)
        back.setOnClickListener{
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        }
    }

    fun init(){
        rvDATA_LAYANAN_TAMBAHAN = findViewById(R.id.rvDATA_LAYANAN_TAMBAHAN)
        fabDATA_PENGGUNA_Tambah = findViewById(R.id.fabDATA_PENGGUNA_Tambah)
    }

    fun getData(){
        // PERBAIKAN: Gunakan field yang benar untuk ordering
        val query = myRef.orderByChild("idLayananTambahan").limitToLast(100)
        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    tambahanList.clear()
                    for (dataSnapshot in snapshot.children){
                        val tambahan = dataSnapshot.getValue(modelTambahan::class.java)
                        // PERBAIKAN: Tambahkan null check
                        if (tambahan != null) {
                            tambahanList.add(tambahan)
                        }
                    }
                    val adapter = AdapterDataTambahan(tambahanList)
                    rvDATA_LAYANAN_TAMBAHAN.adapter = adapter
                    adapter.notifyDataSetChanged()
                } else {
                    // PERBAIKAN: Handle ketika tidak ada data
                    tambahanList.clear()
                    val adapter = AdapterDataTambahan(tambahanList)
                    rvDATA_LAYANAN_TAMBAHAN.adapter = adapter
                    Toast.makeText(this@DataTambahanActivity, "Tidak ada data layanan tambahan", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@DataTambahanActivity, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}