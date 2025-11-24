package edu.ucne.farmaciacruz.data.remote.dto.paypal

import com.google.gson.annotations.SerializedName

data class PayPalCaptureResponse(
    val id: String,
    val status: String,
    @SerializedName("purchase_units")
    val purchaseUnits: List<CapturedPurchaseUnit>?,
    val payer: Payer?
)