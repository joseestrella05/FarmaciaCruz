package edu.ucne.farmaciacruz.presentation.ordenes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.ucne.farmaciacruz.domain.model.PaymentOrder
import edu.ucne.farmaciacruz.domain.usecase.payment.GetUserOrdersUseCase
import edu.ucne.farmaciacruz.domain.usecase.preference.GetUserIdUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MisOrdenesViewModel @Inject constructor(
    private val getUserOrdersUseCase: GetUserOrdersUseCase,
    private val getUserIdUseCase: GetUserIdUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(MisOrdenesState())
    val state = _state.asStateFlow()

    init {
        loadOrders()
    }

    fun loadOrders() = viewModelScope.launch {
        try {
            _state.update { it.copy(isLoading = true, error = null) }

            val usuarioId = getUserIdUseCase().first() ?: return@launch

            getUserOrdersUseCase(usuarioId).collect { orders ->
                _state.update {
                    it.copy(
                        ordenes = orders,
                        isLoading = false
                    )
                }
            }

        } catch (e: Exception) {
            _state.update {
                it.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }
}
