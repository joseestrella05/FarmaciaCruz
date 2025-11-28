package edu.ucne.farmaciacruz.domain.usecase.payment

import edu.ucne.farmaciacruz.domain.model.CarritoItem
import edu.ucne.farmaciacruz.domain.model.Resource
import edu.ucne.farmaciacruz.domain.repository.PaymentRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CreatePayPalOrderUseCase @Inject constructor(
    private val repository: PaymentRepository
) {
     operator fun invoke(
        usuarioId: Int,
        items: List<CarritoItem>,
        total: Double
    ): Flow<Resource<String>> {
        if (items.isEmpty()) {
            return kotlinx.coroutines.flow.flow {
                emit(Resource.Error("El carrito está vacío"))
            }
        }

        if (total <= 0) {
            return kotlinx.coroutines.flow.flow {
                emit(Resource.Error("El total debe ser mayor a cero"))
            }
        }

        return repository.createPayPalOrder(usuarioId, items, total)
    }
}
