package edu.ucne.farmaciacruz.data.remote.dto.admindtos

import com.google.gson.annotations.SerializedName

data class CambiarEstadoDto(
    @SerializedName("activo")
    val activo: Boolean
)