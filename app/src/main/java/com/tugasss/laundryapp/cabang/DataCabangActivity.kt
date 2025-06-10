package com.tugasss.laundryapp.cabang

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
import com.tugasss.laundryapp.R
import com.tugasss.laundryapp.adapter.AdapterDataCabang
import com.tugasss.laundryapp.modeldata.modelCabang

class DataCabangActivity : AppCompatActivity() {
    val database = FirebaseDatabase.getInstance()
    val myRef = database.getReference("cabang")

    lateinit var rvDataCabang: RecyclerView
    lateinit var fabTambahCabang: Button
    lateinit var backButton: ImageView
    lateinit var cabangList: ArrayList<modelCabang>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_data_cabang)
        init()

        val layoutManager = LinearLayoutManager(this)
        layoutManager.reverseLayout = true
        layoutManager.stackFromEnd = true
        rvDataCabang.layoutManager = layoutManager
        rvDataCabang.setHasFixedSize(true)

        cabangList = arrayListOf<modelCabang>()
        getData()

        fabTambahCabang.setOnClickListener {
            val intent = Intent(this, TambahCabangActivity::class.java)
            startActivity(intent)
        }

        backButton.setOnClickListener {
            finish()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    fun init(){
        rvDataCabang = findViewById(R.id.rvDATA_CABANG)
        fabTambahCabang = findViewById(R.id.fabDATA_PENGGUNA_Tambah)
        backButton = findViewById(R.id.Back)
    }

    fun getData(){
        val query = myRef.orderByChild("namaCabang").limitToLast(100)
        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    cabangList.clear()
                    for (dataSnapshot in snapshot.children){
                        val cabang = dataSnapshot.getValue(modelCabang::class.java)
                        if (cabang != null) {
                            cabangList.add(cabang)
                        }
                    }
                    val adapter = AdapterDataCabang(cabangList)
                    rvDataCabang.adapter = adapter
                    adapter.notifyDataSetChanged()
                } else {
                    // Jika tidak ada data
                    cabangList.clear()
                    val adapter = AdapterDataCabang(cabangList)
                    rvDataCabang.adapter = adapter
                    adapter.notifyDataSetChanged()
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@DataCabangActivity, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onResume() {
        super.onResume()
        // Refresh data ketika kembali ke activity ini
        getData()
    }
}