package com.tugasss.laundryapp.pelanggan

import android.widget.Toast
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SearchView
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
import com.tugasss.laundryapp.HomeActivity
import com.tugasss.laundryapp.R
import com.tugasss.laundryapp.adapter.AdapterDataPelanggan
import com.tugasss.laundryapp.cabang.DataCabangActivity
import com.tugasss.laundryapp.modeldata.modelPelanggan

class DataPelangganActivity : AppCompatActivity() {
    val database = FirebaseDatabase.getInstance()
    val myRef = database.getReference("pelanggan")
    lateinit var rvDATA_PELANGGAN: RecyclerView
    lateinit var fabDATA_PENGGUNA_Tambah: Button
    lateinit var searchView: SearchView
    lateinit var pelangganList: ArrayList<modelPelanggan>
    lateinit var filteredList: ArrayList<modelPelanggan>

    // Ubah adapter menjadi nullable untuk menghindari crash
    private var adapter: AdapterDataPelanggan? = null

    // Simpan search query untuk restore setelah orientation change
    private var currentSearchQuery: String = ""



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_data_pelanggan)

        // Restore search query jika ada
        savedInstanceState?.let {
            currentSearchQuery = it.getString("search_query", "")
        }

        init()
        setupRecyclerView()
        setupClickListeners()

        // Inisialisasi list sebelum getData()
        pelangganList = arrayListOf<modelPelanggan>()
        filteredList = arrayListOf<modelPelanggan>()

        // Setup search setelah inisialisasi
        setupSearch()

        // Load data dari Firebase
        getData()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val pelangganactivity = findViewById<ImageView>(R.id.Back)
        pelangganactivity.setOnClickListener{
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // Simpan search query saat orientation change
        outState.putString("search_query", searchView.query.toString())
    }

    private fun init(){
        rvDATA_PELANGGAN = findViewById(R.id.rvDATA_PELANGGAN)
        fabDATA_PENGGUNA_Tambah = findViewById(R.id.fabDATA_PENGGUNA_Tambah)
        searchView = findViewById(R.id.searchView)
    }

    private fun setupRecyclerView() {
        val layoutManager = LinearLayoutManager(this)
        layoutManager.reverseLayout = true
        layoutManager.stackFromEnd = true
        rvDATA_PELANGGAN.layoutManager = layoutManager
        rvDATA_PELANGGAN.setHasFixedSize(true)
    }

    private fun setupClickListeners() {
        fabDATA_PENGGUNA_Tambah.setOnClickListener {
            val intent = Intent(this, TambahPelangganActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupSearch() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                currentSearchQuery = newText ?: ""
                // Pastikan adapter sudah ada sebelum melakukan filter
                if (adapter != null) {
                    filterData(currentSearchQuery)
                }
                return true
            }
        })

        // Restore search query jika ada setelah orientation change
        if (currentSearchQuery.isNotEmpty()) {
            searchView.setQuery(currentSearchQuery, false)
        }
    }

    private fun filterData(query: String) {
        // Safety check - pastikan adapter tidak null
        adapter?.let {
            filteredList.clear()

            if (query.isEmpty()) {
                filteredList.addAll(pelangganList)
            } else {
                val searchQuery = query.lowercase()
                for (pelanggan in pelangganList) {
                    if (pelanggan.namaPelanggan?.lowercase()?.contains(searchQuery) == true ||
                        pelanggan.alamatPelanggan?.lowercase()?.contains(searchQuery) == true ||
                        pelanggan.noHPPelanggan?.contains(searchQuery) == true ||
                        pelanggan.idCabang?.lowercase()?.contains(searchQuery) == true) {
                        filteredList.add(pelanggan)
                    }
                }
            }

            it.notifyDataSetChanged()
        }
    }

    private fun getData(){
        val query = myRef.orderByChild("idPelanggan").limitToLast(100)
        query.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    pelangganList.clear()

                    for (dataSnapshot in snapshot.children){
                        val pelanggan = dataSnapshot.getValue(modelPelanggan::class.java)
                        pelanggan?.let { pelangganList.add(it) }
                    }

                    // Update filtered list
                    filteredList.clear()
                    filteredList.addAll(pelangganList)

                    // Inisialisasi adapter jika belum ada
                    if (adapter == null) {
                        adapter = AdapterDataPelanggan(filteredList)
                        rvDATA_PELANGGAN.adapter = adapter
                    }

                    // Apply current search filter jika ada
                    if (currentSearchQuery.isNotEmpty()) {
                        filterData(currentSearchQuery)
                    } else {
                        adapter?.notifyDataSetChanged()
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@DataPelangganActivity, error.message, Toast.LENGTH_SHORT).show()
            }
        })
    }

    // Getter untuk compatibility dengan kode lain yang mungkin menggunakan getAdapter()
    fun getAdapter(): AdapterDataPelanggan? {
        return adapter
    }
}