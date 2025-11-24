package edu.ucne.farmaciacruz.data.remote.request.paypal

import com.google.gson.annotations.SerializedName
import edu.ucne.farmaciacruz.data.remote.dto.paypal.ApplicationContext
import edu.ucne.farmaciacruz.data.remote.dto.paypal.PurchaseUnit


data class PayPalOrderRequest(
    val intent: String = "CAPTURE",
    @SerializedName("purchase_units")
    val purchaseUnits: List<PurchaseUnit>,
    @SerializedName("application_context")
    val applicationContext: ApplicationContext? = null
)