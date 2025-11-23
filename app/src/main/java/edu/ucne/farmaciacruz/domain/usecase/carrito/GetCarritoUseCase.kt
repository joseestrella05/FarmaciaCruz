package edu.ucne.farmaciacruz.domain.usecase.carrito

import edu.ucne.farmaciacruz.domain.model.CarritoItem
import edu.ucne.farmaciacruz.domain.repository.CarritoRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCarritoUseCase @Inject constructor(
    private val repo: CarritoRepository
) {
    operator fun invoke(usuarioId: Int): Flow<List<CarritoItem>> =
        repo.getCarrito(usuarioId)
}