package com.tugasss.laundryapp.modeldata

import java.io.Serializable

data class modelTambahan(
    val idLayananTambahan: String? = null,
    val idCabang: String? = null,
    val namaLayananTambahan: String? = null,
    val hargaLayananTambahan: String? = null
) : Serializable // PENTING: Harus ada ini