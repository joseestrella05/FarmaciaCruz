package edu.ucne.farmaciacruz.data.remote.dto

import com.google.gson.annotations.SerializedName

data class CreateOrderDto(
    @SerializedName("usuarioId")
    val usuarioId: Int,

    @SerializedName("total")
    val total: Double,

    @SerializedName("productos")
    val productos: List<OrderProductDto>,

    @SerializedName("paypalOrderId")
    val paypalOrderId: String,

    @SerializedName("paypalPayerId")
    val paypalPayerId: String? = null
)