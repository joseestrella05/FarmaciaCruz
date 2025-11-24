package edu.ucne.farmaciacruz.domain.model

data class PayPalConfig(
    val clientId: String,
    val secret: String,
    val environment: PayPalEnvironment,
    val currency: String = "USD",
    val locale: String = "es_DO"
)

enum class PayPalEnvironment {
    SANDBOX,
    PRODUCTION
}