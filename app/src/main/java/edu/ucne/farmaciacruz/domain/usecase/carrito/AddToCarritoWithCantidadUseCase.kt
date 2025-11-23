package edu.ucne.farmaciacruz.domain.usecase.carrito

import edu.ucne.farmaciacruz.domain.model.Producto
import edu.ucne.farmaciacruz.domain.repository.CarritoRepository
import javax.inject.Inject

class AddToCarritoWithCantidadUseCase @Inject constructor(
    private val repo: CarritoRepository
) {
    suspend operator fun invoke(usuarioId: Int, producto: Producto, cantidad: Int) {
        repo.addToCarrito(usuarioId, producto, cantidad)
    }
}