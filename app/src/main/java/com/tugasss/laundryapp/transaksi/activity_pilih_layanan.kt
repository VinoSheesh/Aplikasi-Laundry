package com.tugasss.laundryapp.transaksi

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.tugasss.laundryapp.R
import com.tugasss.laundryapp.adapter.AdapterPilihLayanan
import com.tugasss.laundryapp.adapter.AdapterPilihPelanggan
import com.tugasss.laundryapp.modeldata.modelLayanan
import com.tugasss.laundryapp.modeldata.modelPelanggan
import com.tugasss.laundryapp.tambahan.DataTambahanActivity

class activity_pilih_layanan : AppCompatActivity() {
    private val TAG = "Pilihlayanan"
    val database = FirebaseDatabase.getInstance()
    val myRef = database.getReference("layanan")
    private lateinit var rvPilihLayanan: RecyclerView
    private lateinit var searchView: SearchView
    private var idCabang: String = ""
    private lateinit var tvKosong: TextView
    lateinit var listLayanan: ArrayList<modelLayanan>
    private lateinit var adapter: AdapterPilihLayanan
    private lateinit var filteredList: ArrayList<modelLayanan>

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_pilih_layanan)

        Log.d(TAG, "Activity created")

        tvKosong = findViewById(R.id.tvKosong)
        rvPilihLayanan = findViewById(R.id.rvPILIH_LAYANAN)
        searchView = findViewById(R.id.searchView)

        listLayanan = ArrayList()
        filteredList = ArrayList()

        rvPilihLayanan.layoutManager = LinearLayoutManager(this)

        idCabang = intent.getStringExtra("idCabang") ?: ""
        Log.d(TAG, "idCabang: $idCabang")

        setupSearchView()
        getData()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val back = findViewById<ImageView>(R.id.Back)
        back.setOnClickListener{
            val intent = Intent(this, transaksi::class.java)
            startActivity(intent)
        }
    }

    private fun setupSearchView() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterList(newText)
                return true
            }
        })
    }

    private fun filterList(query: String?) {
        Log.d(TAG, "Filtering list with query: $query")
        filteredList.clear()

        if (query.isNullOrEmpty()) {
            filteredList.addAll(listLayanan)
        } else {
            val searchText = query.toLowerCase().trim()
            for (layanan in listLayanan) {
                if (layanan.namaLayanan?.toLowerCase()?.contains(searchText) == true ||
                    layanan.hargaLayanan?.toLowerCase()?.contains(searchText) == true) {
                    filteredList.add(layanan)
                }
            }
        }

        updateRecyclerView()
    }

    private fun updateRecyclerView() {
        Log.d(TAG, "Updating RecyclerView with ${filteredList.size} items")

        if (filteredList.isEmpty()) {
            tvKosong.visibility = View.VISIBLE
            tvKosong.text = "Tidak ada layanan yang cocok"
        } else {
            tvKosong.visibility = View.GONE
        }

        adapter = AdapterPilihLayanan(filteredList)
        rvPilihLayanan.adapter = adapter
    }

    fun getData() {
        Log.d(TAG, "getData() called")

        val query = if (idCabang.isEmpty()) {
            myRef.limitToLast(100)
        } else {
            myRef.orderByChild("idCabang").equalTo(idCabang).limitToLast(100)
        }

        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d(TAG, "onDataChange: data exists: ${snapshot.exists()}, count: ${snapshot.childrenCount}")

                if (snapshot.exists()) {
                    tvKosong.visibility = View.GONE
                    listLayanan.clear()

                    for (snap in snapshot.children) {
                        try {
                            val layanan = snap.getValue(modelLayanan::class.java)
                            if (layanan != null) {
                                listLayanan.add(layanan)
                                Log.d(TAG, "Added layanan: ${layanan.namaLayanan}")
                            } else {
                                Log.e(TAG, "layanan is null for key: ${snap.key}")
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Error parsing layanan: ${e.message}")
                        }
                    }

                    Log.d(TAG, "Total layanan loaded: ${listLayanan.size}")

                    // Initialize the filtered list with all data
                    filteredList.clear()
                    filteredList.addAll(listLayanan)

                    // Set adapter
                    if (listLayanan.isNotEmpty()) {
                        adapter = AdapterPilihLayanan(filteredList)
                        rvPilihLayanan.adapter = adapter
                    } else {
                        tvKosong.visibility = View.VISIBLE
                    }
                } else {
                    Log.d(TAG, "No data found")
                    tvKosong.visibility = View.VISIBLE
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Database error: ${error.message}")
                tvKosong.visibility = View.VISIBLE
                tvKosong.text = "Error: ${error.message}"
                Toast.makeText(this@activity_pilih_layanan, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
