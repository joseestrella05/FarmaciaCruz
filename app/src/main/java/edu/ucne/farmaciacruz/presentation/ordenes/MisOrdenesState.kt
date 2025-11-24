package edu.ucne.farmaciacruz.presentation.ordenes

import edu.ucne.farmaciacruz.domain.model.PaymentOrder

data class MisOrdenesState(
    val ordenes: List<PaymentOrder> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
