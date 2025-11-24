package edu.ucne.farmaciacruz.presentation.checkout

sealed class CheckoutUiEvent {
    data class ShowError(val message: String) : CheckoutUiEvent()
    data class ShowSuccess(val message: String) : CheckoutUiEvent()
    data class OpenPayPalWebView(val approvalUrl: String, val orderId: String) : CheckoutUiEvent()
    data object NavigateBack : CheckoutUiEvent()
    data object NavigateToOrders : CheckoutUiEvent()
}