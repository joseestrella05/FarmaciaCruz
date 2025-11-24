package edu.ucne.farmaciacruz.presentation.checkout

sealed class CheckoutEvent {
    data object LoadCart : CheckoutEvent()
    data object CreatePayPalOrder : CheckoutEvent()
    data class PaymentCompleted(val paypalOrderId: String) : CheckoutEvent()
    data object PaymentCancelled : CheckoutEvent()
    data object NavigateBack : CheckoutEvent()
}