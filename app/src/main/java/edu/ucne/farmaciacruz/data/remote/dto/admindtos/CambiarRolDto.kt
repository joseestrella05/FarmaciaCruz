package edu.ucne.farmaciacruz.data.remote.dto.admindtos

import com.google.gson.annotations.SerializedName

data class CambiarRolDto(
    @SerializedName("nuevoRol")
    val nuevoRol: String
)