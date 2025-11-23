package edu.ucne.farmaciacruz.domain.usecase.carrito

import edu.ucne.farmaciacruz.domain.repository.CarritoRepository
import javax.inject.Inject

class RemoveFromCarritoUseCase @Inject constructor(
    private val repo: CarritoRepository
) {
    suspend operator fun invoke(usuarioId: Int, productoId: Int) {
        repo.removeFromCarrito(usuarioId, productoId)
    }
}