package edu.ucne.farmaciacruz.domain.usecase.carrito

import edu.ucne.farmaciacruz.domain.repository.CarritoRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTotalItemsUseCase @Inject constructor(
    private val repo: CarritoRepository
) {
    operator fun invoke(usuarioId: Int): Flow<Int> =
        repo.getTotalItems(usuarioId)
}
