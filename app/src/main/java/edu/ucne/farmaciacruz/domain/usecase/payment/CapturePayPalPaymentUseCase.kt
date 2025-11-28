package edu.ucne.farmaciacruz.domain.usecase.payment

import edu.ucne.farmaciacruz.domain.model.PaymentResult
import edu.ucne.farmaciacruz.domain.model.Resource
import edu.ucne.farmaciacruz.domain.repository.PaymentRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CapturePayPalPaymentUseCase @Inject constructor(
    private val repository: PaymentRepository
) {
    operator fun invoke(
        paypalOrderId: String,
        localOrderId: String
    ): Flow<Resource<PaymentResult>> {
        return repository.capturePayPalPayment(paypalOrderId, localOrderId)
    }
}