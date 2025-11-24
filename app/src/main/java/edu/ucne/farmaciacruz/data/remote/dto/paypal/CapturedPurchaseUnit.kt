package edu.ucne.farmaciacruz.data.remote.dto.paypal

import com.google.gson.annotations.SerializedName

data class CapturedPurchaseUnit(
    @SerializedName("reference_id")
    val referenceId: String?,
    val payments: Payments?
)