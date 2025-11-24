package edu.ucne.farmaciacruz.domain.repository

import edu.ucne.farmaciacruz.domain.model.CarritoItem
import edu.ucne.farmaciacruz.domain.model.PaymentOrder
import edu.ucne.farmaciacruz.domain.model.PaymentResult
import edu.ucne.farmaciacruz.domain.model.Resource
import kotlinx.coroutines.flow.Flow

interface PaymentRepository {
    suspend fun createPayPalOrder(
        usuarioId: Int,
        items: List<CarritoItem>,
        total: Double
    ): Flow<Resource<String>>

    suspend fun capturePayPalPayment(
        paypalOrderId: String,
        localOrderId: String
    ): Flow<Resource<PaymentResult>>

    suspend fun createLocalOrder(
        usuarioId: Int,
        items: List<CarritoItem>,
        total: Double,
        paypalOrderId: String? = null
    ): Flow<Resource<PaymentOrder>>

    fun getOrdersByUser(usuarioId: Int): Flow<List<PaymentOrder>>

    suspend fun getOrderById(orderId: Int): PaymentOrder?

    suspend fun updateOrderStatus(
        orderId: Int,
        status: String,
        paypalPayerId: String? = null
    )

    suspend fun syncOrders(): Flow<Resource<Unit>>
}