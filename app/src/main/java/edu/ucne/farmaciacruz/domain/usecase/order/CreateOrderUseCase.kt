package edu.ucne.farmaciacruz.domain.usecase.order

import edu.ucne.farmaciacruz.domain.model.CarritoItem
import edu.ucne.farmaciacruz.domain.model.Order
import edu.ucne.farmaciacruz.domain.model.Resource
import edu.ucne.farmaciacruz.domain.repository.OrderRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CreateOrderUseCase @Inject constructor(
    private val repository: OrderRepository
) {
    operator fun invoke(
        usuarioId: Int,
        carrito: List<CarritoItem>,
        total: Double,
        paypalOrderId: String,
        paypalPayerId: String? = null
    ): Flow<Resource<Order>> {
        if (carrito.isEmpty()) {
            return kotlinx.coroutines.flow.flow {
                emit(Resource.Error("El carrito está vacío"))
            }
        }

        if (total <= 0) {
            return kotlinx.coroutines.flow.flow {
                emit(Resource.Error("El total debe ser mayor a cero"))
            }
        }

        return repository.createOrder(
            usuarioId,
            carrito,
            total,
            paypalOrderId,
            paypalPayerId
        )
    }
}