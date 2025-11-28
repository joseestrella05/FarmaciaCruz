package edu.ucne.farmaciacruz.presentation.order

import edu.ucne.farmaciacruz.domain.model.Order

data class MisOrdenesState(
    val ordenes: List<Order> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)