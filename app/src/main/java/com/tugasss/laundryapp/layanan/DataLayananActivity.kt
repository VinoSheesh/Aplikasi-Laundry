package com.tugasss.laundryapp.layanan

import android.widget.Toast
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.tugasss.laundryapp.R
import com.tugasss.laundryapp.adapter.AdapterDataLayanan
import com.tugasss.laundryapp.modeldata.modelLayanan
import com.tugasss.laundryapp.layanan.TambahLayananActivity

class DataLayananActivity : AppCompatActivity() {
    val database = FirebaseDatabase.getInstance()
    val myRef = database.getReference("layanan")
    lateinit var rvDATA_LAYANAN: RecyclerView
    lateinit var fabDATA_PENGGUNA_Tambah: FloatingActionButton
    lateinit var layananList: ArrayList<modelLayanan>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_data_layanan)
        init()
        val layoutManager = LinearLayoutManager(this)
        layoutManager.reverseLayout = true
        layoutManager.stackFromEnd = true
        rvDATA_LAYANAN.layoutManager = layoutManager
        rvDATA_LAYANAN.setHasFixedSize(true)
        layananList = arrayListOf<modelLayanan>()
        getData()
        val tambahlayanan = findViewById<FloatingActionButton>(R.id.fabDATA_PENGGUNA_Tambah)
        tambahlayanan.setOnClickListener {
            val intent = Intent(this, TambahLayananActivity::class.java)
            startActivity(intent)
        }
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    fun init(){
        rvDATA_LAYANAN = findViewById(R.id.rvDATA_LAYANAN)
        fabDATA_PENGGUNA_Tambah = findViewById(R.id.fabDATA_PENGGUNA_Tambah)
    }

    fun getData(){
        val query = myRef.orderByChild("idLayanan").limitToLast(100)
        query.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    layananList.clear()
                    for (dataSnapshot in snapshot.children){
                        val layanan = dataSnapshot.getValue(modelLayanan::class.java)
                        layananList.add(layanan!!)
                    }
                    val adapter = AdapterDataLayanan(layananList)
                    rvDATA_LAYANAN.adapter = adapter
                    adapter.notifyDataSetChanged()
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@DataLayananActivity, error.message, Toast.LENGTH_SHORT).show()
            }
        })
    }
}
