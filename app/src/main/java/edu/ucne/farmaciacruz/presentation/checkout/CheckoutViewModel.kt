package edu.ucne.farmaciacruz.presentation.checkout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.ucne.farmaciacruz.domain.model.PaymentResult
import edu.ucne.farmaciacruz.domain.model.Resource
import edu.ucne.farmaciacruz.domain.usecase.carrito.GetCarritoUseCase
import edu.ucne.farmaciacruz.domain.usecase.payment.CapturePayPalPaymentUseCase
import edu.ucne.farmaciacruz.domain.usecase.payment.CreatePayPalOrderUseCase
import edu.ucne.farmaciacruz.domain.usecase.preference.GetUserIdUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CheckoutViewModel @Inject constructor(
    private val getCarritoUseCase: GetCarritoUseCase,
    private val createPayPalOrderUseCase: CreatePayPalOrderUseCase,
    private val capturePayPalPaymentUseCase: CapturePayPalPaymentUseCase,
    private val getUserIdUseCase: GetUserIdUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(CheckoutState())
    val state = _state.asStateFlow()

    private val _uiEvent = MutableSharedFlow<CheckoutUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    init {
        loadCart()
    }

    fun onEvent(event: CheckoutEvent) {
        when (event) {
            CheckoutEvent.LoadCart -> loadCart()
            CheckoutEvent.CreatePayPalOrder -> createPayPalOrder()
            is CheckoutEvent.PaymentCompleted -> capturePayment(event.paypalOrderId)
            CheckoutEvent.PaymentCancelled -> handlePaymentCancelled()
            CheckoutEvent.NavigateBack -> viewModelScope.launch {
                _uiEvent.emit(CheckoutUiEvent.NavigateBack)
            }
        }
    }

    private fun loadCart() = viewModelScope.launch {
        val usuarioId = getUserIdUseCase().first() ?: return@launch

        getCarritoUseCase(usuarioId).collect { items ->
            val total = items.sumOf { it.producto.precio * it.cantidad }
            _state.update {
                it.copy(
                    carrito = items,
                    total = total
                )
            }
        }
    }

    private fun createPayPalOrder() = viewModelScope.launch {
        try {
            val usuarioId = getUserIdUseCase().first() ?: run {
                _uiEvent.emit(CheckoutUiEvent.ShowError("Usuario no identificado"))
                return@launch
            }

            val currentState = _state.value

            if (currentState.carrito.isEmpty()) {
                _uiEvent.emit(CheckoutUiEvent.ShowError("El carrito está vacío"))
                return@launch
            }

            createPayPalOrderUseCase(
                usuarioId = usuarioId,
                items = currentState.carrito,
                total = currentState.total
            ).collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _state.update { it.copy(isLoading = true, error = null) }
                    }

                    is Resource.Success -> {
                        val orderId = result.data!!
                        _state.update {
                            it.copy(
                                isLoading = false,
                                paypalOrderId = orderId,
                                error = null
                            )
                        }

                        // Generate approval URL (this would come from PayPal API response in real implementation)
                        val approvalUrl = generatePayPalApprovalUrl(orderId)
                        _uiEvent.emit(
                            CheckoutUiEvent.OpenPayPalWebView(approvalUrl, orderId)
                        )
                    }

                    is Resource.Error -> {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                error = result.message
                            )
                        }
                        _uiEvent.emit(
                            CheckoutUiEvent.ShowError(result.message ?: "Error creando orden")
                        )
                    }
                }
            }

        } catch (e: Exception) {
            _state.update { it.copy(isLoading = false, error = e.message) }
            _uiEvent.emit(CheckoutUiEvent.ShowError(e.message ?: "Error inesperado"))
        }
    }

    private fun capturePayment(paypalOrderId: String) = viewModelScope.launch {
        try {
            val localOrderId = _state.value.paypalOrderId ?: paypalOrderId

            capturePayPalPaymentUseCase(paypalOrderId, localOrderId).collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _state.update { it.copy(isLoading = true) }
                    }

                    is Resource.Success -> {
                        when (val paymentResult = result.data) {
                            is PaymentResult.Success -> {
                                _state.update {
                                    it.copy(
                                        isLoading = false,
                                        paymentCompleted = true,
                                        error = null
                                    )
                                }
                                _uiEvent.emit(
                                    CheckoutUiEvent.ShowSuccess(
                                        "¡Pago completado! Total: $${String.format("%.2f", paymentResult.amount)}"
                                    )
                                )
                                _uiEvent.emit(CheckoutUiEvent.NavigateToOrders)
                            }

                            is PaymentResult.Cancelled -> {
                                _state.update {
                                    it.copy(isLoading = false, error = "Pago cancelado")
                                }
                                _uiEvent.emit(CheckoutUiEvent.ShowError("Pago cancelado por el usuario"))
                            }

                            is PaymentResult.Error -> {
                                _state.update {
                                    it.copy(isLoading = false, error = paymentResult.message)
                                }
                                _uiEvent.emit(CheckoutUiEvent.ShowError(paymentResult.message))
                            }

                            null -> {
                                _state.update {
                                    it.copy(isLoading = false, error = "Error desconocido")
                                }
                            }
                        }
                    }

                    is Resource.Error -> {
                        _state.update {
                            it.copy(isLoading = false, error = result.message)
                        }
                        _uiEvent.emit(
                            CheckoutUiEvent.ShowError(result.message ?: "Error capturando pago")
                        )
                    }
                }
            }

        } catch (e: Exception) {
            _state.update { it.copy(isLoading = false, error = e.message) }
            _uiEvent.emit(CheckoutUiEvent.ShowError(e.message ?: "Error inesperado"))
        }
    }

    private fun handlePaymentCancelled() {
        _state.update {
            it.copy(
                isLoading = false,
                paypalOrderId = null,
                error = "Pago cancelado"
            )
        }
        viewModelScope.launch {
            _uiEvent.emit(CheckoutUiEvent.ShowError("Has cancelado el pago"))
        }
    }

    private fun generatePayPalApprovalUrl(orderId: String): String {
        val environment = "sandbox" // or "production"
        val baseUrl = if (environment == "sandbox") {
            "https://www.sandbox.paypal.com"
        } else {
            "https://www.paypal.com"
        }
        return "$baseUrl/checkoutnow?token=$orderId"
    }
}