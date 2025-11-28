package edu.ucne.farmaciacruz.data.repository

import android.util.Base64
import android.util.Log
import edu.ucne.farmaciacruz.BuildConfig
import edu.ucne.farmaciacruz.data.local.dao.PaymentOrderDao
import edu.ucne.farmaciacruz.data.local.entity.PaymentOrderEntity
import edu.ucne.farmaciacruz.data.remote.PayPalApiService
import edu.ucne.farmaciacruz.domain.model.*
import edu.ucne.farmaciacruz.domain.repository.PaymentRepository
import com.google.gson.Gson
import edu.ucne.farmaciacruz.data.remote.dto.paypal.Amount
import edu.ucne.farmaciacruz.data.remote.dto.paypal.ApplicationContext
import edu.ucne.farmaciacruz.data.remote.dto.paypal.PurchaseUnit
import edu.ucne.farmaciacruz.data.remote.dto.paypal.PayPalOrderRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PaymentRepositoryImpl @Inject constructor(
    private val payPalApi: PayPalApiService,
    private val paymentOrderDao: PaymentOrderDao,
    private val gson: Gson
) : PaymentRepository {

    private var cachedAccessToken: String? = null
    private var tokenExpiry: Long = 0L

    companion object {
        private const val TAG = "PaymentRepository"
    }

    override fun createPayPalOrder(
        usuarioId: Int,
        items: List<CarritoItem>,
        total: Double
    ): Flow<Resource<String>> = flow {
        try {
            emit(Resource.Loading())

            val token = getAccessToken()

            val orderRequest = PayPalOrderRequest(
                intent = "CAPTURE",
                purchaseUnits = listOf(
                    PurchaseUnit(
                        amount = Amount(
                            currencyCode = "USD",
                            value = String.format("%.2f", total)
                        ),
                        description = "Compra en Farmacia Cruz - ${items.size} productos",
                        referenceId = UUID.randomUUID().toString()
                    )
                ),
                applicationContext = ApplicationContext(
                    brandName = "Farmacia Cruz",
                    landingPage = "BILLING",
                    shippingPreference = "NO_SHIPPING",
                    userAction = "PAY_NOW"
                )
            )

            val response = payPalApi.createOrder(
                token = "Bearer $token",
                order = orderRequest
            )

            if (response.isSuccessful && response.body() != null) {
                val orderResponse = response.body()!!

                val localId = UUID.randomUUID().toString()
                val orderEntity = PaymentOrderEntity(
                    localId = localId,
                    usuarioId = usuarioId,
                    total = total,
                    productosJson = gson.toJson(items),
                    estado = PaymentStatus.PROCESANDO.name,
                    metodoPago = "PayPal",
                    paypalOrderId = orderResponse.id,
                    paypalPayerId = null,
                    fechaCreacion = System.currentTimeMillis(),
                    fechaActualizacion = System.currentTimeMillis(),
                    sincronizado = false
                )

                paymentOrderDao.insertOrder(orderEntity)

                emit(Resource.Success(orderResponse.id))
            } else {
                val errorMsg = "Error creando orden: ${response.code()} - ${response.message()}"
                Log.e(TAG, errorMsg)
                emit(Resource.Error(errorMsg))
            }

        } catch (e: Exception) {
            Log.e(TAG, "Exception creating PayPal order", e)
            emit(Resource.Error("Error de conexión: ${e.localizedMessage}"))
        }
    }

    override fun capturePayPalPayment(
        paypalOrderId: String,
        localOrderId: String
    ): Flow<Resource<PaymentResult>> = flow {
        try {
            emit(Resource.Loading())

            val token = getAccessToken()

            val response = payPalApi.captureOrder(
                token = "Bearer $token",
                orderId = paypalOrderId
            )

            if (response.isSuccessful && response.body() != null) {
                val captureResponse = response.body()!!

                when (captureResponse.status) {
                    "COMPLETED" -> {
                        val payerId = captureResponse.payer?.payerId
                        val amount = captureResponse.purchaseUnits
                            ?.firstOrNull()
                            ?.payments
                            ?.captures
                            ?.firstOrNull()
                            ?.amount
                            ?.value
                            ?.toDoubleOrNull() ?: 0.0

                        // Update local order
                        val localOrder = paymentOrderDao.getOrderByPayPalId(paypalOrderId)
                        if (localOrder != null) {
                            paymentOrderDao.updateOrder(
                                localOrder.copy(
                                    estado = PaymentStatus.COMPLETADO.name,
                                    paypalPayerId = payerId,
                                    fechaActualizacion = System.currentTimeMillis()
                                )
                            )
                        }

                        emit(Resource.Success(
                            PaymentResult.Success(
                                orderId = paypalOrderId,
                                payerId = payerId ?: "",
                                amount = amount
                            )
                        ))
                    }
                    else -> {
                        updateLocalOrderStatus(paypalOrderId, PaymentStatus.FALLIDO.name)
                        emit(Resource.Error("Estado de pago: ${captureResponse.status}"))
                    }
                }
            } else {
                updateLocalOrderStatus(paypalOrderId, PaymentStatus.FALLIDO.name)
                emit(Resource.Error("Error capturando pago: ${response.code()}"))
            }

        } catch (e: Exception) {
            Log.e(TAG, "Exception capturing payment", e)
            updateLocalOrderStatus(paypalOrderId, PaymentStatus.FALLIDO.name)
            emit(Resource.Error("Error: ${e.localizedMessage}"))
        }
    }

    override suspend fun createLocalOrder(
        usuarioId: Int,
        items: List<CarritoItem>,
        total: Double,
        paypalOrderId: String?
    ): Flow<Resource<PaymentOrder>> = flow {
        try {
            emit(Resource.Loading())

            val orderEntity = PaymentOrderEntity(
                localId = UUID.randomUUID().toString(),
                usuarioId = usuarioId,
                total = total,
                productosJson = gson.toJson(items),
                estado = PaymentStatus.PENDIENTE.name,
                metodoPago = "PayPal",
                paypalOrderId = paypalOrderId,
                paypalPayerId = null,
                fechaCreacion = System.currentTimeMillis(),
                fechaActualizacion = System.currentTimeMillis(),
                sincronizado = false
            )

            val id = paymentOrderDao.insertOrder(orderEntity)
            val savedOrder = paymentOrderDao.getOrderById(id.toInt())

            if (savedOrder != null) {
                emit(Resource.Success(savedOrder.toDomain()))
            } else {
                emit(Resource.Error("Error guardando orden"))
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error creating local order", e)
            emit(Resource.Error(e.localizedMessage ?: "Error desconocido"))
        }
    }

    override fun getOrdersByUser(usuarioId: Int): Flow<List<PaymentOrder>> {
        return paymentOrderDao.getOrdersByUsuario(usuarioId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getOrderById(orderId: Int): PaymentOrder? {
        return paymentOrderDao.getOrderById(orderId)?.toDomain()
    }

    override suspend fun updateOrderStatus(
        orderId: Int,
        status: String,
        paypalPayerId: String?
    ) {
        val order = paymentOrderDao.getOrderById(orderId)
        if (order != null) {
            paymentOrderDao.updateOrder(
                order.copy(
                    estado = status,
                    paypalPayerId = paypalPayerId,
                    fechaActualizacion = System.currentTimeMillis()
                )
            )
        }
    }

    override fun syncOrders(): Flow<Resource<Unit>> = flow {
        try {
            emit(Resource.Loading())

            val unsyncedOrders = paymentOrderDao.getUnsyncedOrders()

            unsyncedOrders.forEach { order ->
                if (order.estado == PaymentStatus.COMPLETADO.name) {
                    paymentOrderDao.markAsSynced(
                        order.id,
                        System.currentTimeMillis()
                    )
                }
            }

            emit(Resource.Success(Unit))

        } catch (e: Exception) {
            Log.e(TAG, "Error syncing orders", e)
            emit(Resource.Error(e.localizedMessage ?: "Error de sincronización"))
        }
    }
    private suspend fun getAccessToken(): String {
        if (cachedAccessToken != null && System.currentTimeMillis() < tokenExpiry) {
            return cachedAccessToken!!
        }

        val credentials = "${BuildConfig.PAYPAL_CLIENT_ID}:${BuildConfig.PAYPAL_SECRET}"
        val basicAuth = "Basic ${Base64.encodeToString(credentials.toByteArray(), Base64.NO_WRAP)}"

        val response = payPalApi.getAccessToken(basicAuth)

        if (response.isSuccessful && response.body() != null) {
            val tokenResponse = response.body()!!
            cachedAccessToken = tokenResponse.accessToken
            tokenExpiry = System.currentTimeMillis() + (tokenResponse.expiresIn * 1000) - 60000
            return tokenResponse.accessToken
        } else {
            throw Exception("Error obteniendo token de acceso: ${response.code()}")
        }
    }

    private suspend fun updateLocalOrderStatus(paypalOrderId: String, status: String) {
        try {
            val order = paymentOrderDao.getOrderByPayPalId(paypalOrderId)
            if (order != null) {
                paymentOrderDao.updateOrder(
                    order.copy(
                        estado = status,
                        fechaActualizacion = System.currentTimeMillis()
                    )
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating order status", e)
        }
    }

    private fun PaymentOrderEntity.toDomain(): PaymentOrder {
        val itemsType = object : com.google.gson.reflect.TypeToken<List<CarritoItem>>() {}.type
        val items: List<CarritoItem> = try {
            gson.fromJson(productosJson, itemsType)
        } catch (e: Exception) {
            emptyList()
        }

        return PaymentOrder(
            id = localId,
            usuarioId = usuarioId,
            total = total,
            productos = items,
            estado = PaymentStatus.valueOf(estado),
            metodoPago = metodoPago,
            paypalOrderId = paypalOrderId,
            paypalPayerId = paypalPayerId,
            fechaCreacion = fechaCreacion,
            fechaActualizacion = fechaActualizacion,
            sincronizado = sincronizado,
            errorMessage = errorMessage
        )
    }
}