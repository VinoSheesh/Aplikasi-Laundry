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
import com.tugasss.laundryapp.adapter.AdapterPilihLayananTambahan
import com.tugasss.laundryapp.modeldata.modelLayanan
import com.tugasss.laundryapp.modeldata.modelTambahan

class Activity_pilih_layanan_tambahan : AppCompatActivity() {
    private val TAG = "PilihlayananTambahan"
    val database = FirebaseDatabase.getInstance()
    val myRef = database.getReference("tambahan")
    private lateinit var rvPilihLayananTambahan: RecyclerView
    private lateinit var searchView: SearchView
    private var idCabang: String? = null // Ubah menjadi nullable String
    private lateinit var tvKosong: TextView
    lateinit var listTambahan: ArrayList<modelTambahan>
    private lateinit var adapter: AdapterPilihLayananTambahan
    private lateinit var filteredList: ArrayList<modelTambahan>

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_pilih_layanan_tambahan)

        Log.d(TAG, "Activity created")

        tvKosong = findViewById(R.id.tvKosong)
        rvPilihLayananTambahan = findViewById(R.id.rvPILIH_LAYANAN_TAMBAHAN)
        searchView = findViewById(R.id.searchView)

        listTambahan = ArrayList()
        filteredList = ArrayList()

        rvPilihLayananTambahan.layoutManager = LinearLayoutManager(this)

        // Terima idCabang sebagai String?
        idCabang = intent.getStringExtra("idCabang")
        Log.d(TAG, "Received idCabang: $idCabang") // Pastikan ini menampilkan nilai yang benar

        setupSearchView()
        getData() // Panggil getData setelah idCabang dipastikan terisi

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
            filteredList.addAll(listTambahan)
        } else {
            val searchText = query.toLowerCase().trim()
            for (tambahan in listTambahan) {
                if (tambahan.namaLayananTambahan?.toLowerCase()?.contains(searchText) == true ||
                    tambahan.hargaLayananTambahan?.toLowerCase()?.contains(searchText) == true) {
                    filteredList.add(tambahan)
                }
            }
        }

        updateRecyclerView()
    }

    private fun updateRecyclerView() {
        Log.d(TAG, "Updating RecyclerView with ${filteredList.size} items")

        if (filteredList.isEmpty()) {
            tvKosong.visibility = View.VISIBLE
            tvKosong.text = "Tidak ada Layanan Tambahan yang cocok"
        } else {
            tvKosong.visibility = View.GONE
        }

        adapter = AdapterPilihLayananTambahan(filteredList)
        rvPilihLayananTambahan.adapter = adapter
    }


    fun getData() {
        Log.d(TAG, "getData() called")

        val query = if (idCabang.isNullOrEmpty()) { // Cek null atau kosong
            // Jika idCabang null atau kosong, ambil semua data (atau log peringatan)
            // Tergantung kebutuhan: apakah ingin menampilkan semua jika idCabang tidak ada,
            // atau hanya data yang tidak punya idCabang (yang tidak disarankan).
            // Untuk saat ini, kita akan ambil semua jika idCabang tidak valid.
            myRef.limitToLast(100) // Ambil 100 terakhir
        } else {
            myRef.orderByChild("idCabang").equalTo(idCabang).limitToLast(100)
        }


        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d(TAG, "onDataChange: data exists: ${snapshot.exists()}, count: ${snapshot.childrenCount}")

                if (snapshot.exists()) {
                    tvKosong.visibility = View.GONE
                    listTambahan.clear()

                    for (snap in snapshot.children) {
                        try {
                            val tambahan = snap.getValue(modelTambahan::class.java)
                            if (tambahan != null) {
                                listTambahan.add(tambahan)
                                Log.d(TAG, "Added layanan: ${tambahan.namaLayananTambahan}")
                            } else {
                                Log.e(TAG, "layanan Tambahan is null for key: ${snap.key}")
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Error parsing layanan tambahan: ${e.message}")
                        }
                    }

                    Log.d(TAG, "Total layanan tambahan loaded: ${listTambahan.size}")

                    // Initialize the filtered list with all data
                    filteredList.clear()
                    filteredList.addAll(listTambahan)

                    // Set adapter
                    if (listTambahan.isNotEmpty()) {
                        adapter = AdapterPilihLayananTambahan(filteredList)
                        rvPilihLayananTambahan.adapter = adapter
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
                Toast.makeText(this@Activity_pilih_layanan_tambahan, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}