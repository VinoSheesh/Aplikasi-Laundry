package com.tugasss.laundryapp.layanan

import android.widget.Toast
import android.content.Intent
import android.os.Bundle
import android.widget.SearchView
import android.widget.ImageView
import android.widget.Button
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
import com.tugasss.laundryapp.adapter.AdapterDataLayanan
import com.tugasss.laundryapp.modeldata.modelLayanan
import com.tugasss.laundryapp.layanan.TambahLayananActivity
import com.tugasss.laundryapp.HomeActivity

class DataLayananActivity : AppCompatActivity() {
    val database = FirebaseDatabase.getInstance()
    val myRef = database.getReference("layanan")
    lateinit var rvDATA_LAYANAN: RecyclerView
    lateinit var fabDATA_PENGGUNA_Tambah: Button
    lateinit var searchView: SearchView
    lateinit var backButton: ImageView
    lateinit var layananList: ArrayList<modelLayanan>
    lateinit var filteredList: ArrayList<modelLayanan>
    lateinit var adapter: AdapterDataLayanan

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
        filteredList = arrayListOf<modelLayanan>()

        getData()
        setupSearch()

        fabDATA_PENGGUNA_Tambah.setOnClickListener {
            val intent = Intent(this, TambahLayananActivity::class.java)
            startActivity(intent)
        }

        backButton.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
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
        searchView = findViewById(R.id.searchView)
        backButton = findViewById(R.id.Back)
    }

    fun setupSearch() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterData(newText ?: "")
                return true
            }
        })
    }

    fun filterData(query: String) {
        filteredList.clear()
        if (query.isEmpty()) {
            filteredList.addAll(layananList)
        } else {
            for (layanan in layananList) {
                if (layanan.namaLayanan?.lowercase()?.contains(query.lowercase()) == true ||
                    layanan.hargaLayanan?.contains(query) == true ||
                    layanan.idCabang?.lowercase()?.contains(query.lowercase()) == true) {
                    filteredList.add(layanan)
                }
            }
        }
        adapter.updateData(filteredList)
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
                    filteredList.clear()
                    filteredList.addAll(layananList)
                    adapter = AdapterDataLayanan(filteredList)
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