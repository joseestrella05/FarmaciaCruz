package edu.ucne.farmaciacruz.domain.model

sealed class PaymentResult {
    data class Success(
        val orderId: String,
        val payerId: String,
        val amount: Double
    ) : PaymentResult()

    data class Cancelled(val reason: String? = null) : PaymentResult()

    data class Error(val message: String, val code: String? = null) : PaymentResult()
}