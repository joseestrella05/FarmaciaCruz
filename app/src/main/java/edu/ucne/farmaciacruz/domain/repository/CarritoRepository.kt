package edu.ucne.farmaciacruz.domain.repository

import edu.ucne.farmaciacruz.domain.model.CarritoItem
import edu.ucne.farmaciacruz.domain.model.Producto
import kotlinx.coroutines.flow.Flow

interface CarritoRepository {
    fun getCarrito(usuarioId: Int): Flow<List<CarritoItem>>
    suspend fun addToCarrito(usuarioId: Int, producto: Producto)
    suspend fun updateQuantity(usuarioId: Int, productoId: Int, cantidad: Int)
    suspend fun removeFromCarrito(usuarioId: Int, productoId: Int)
    suspend fun clearCarrito(usuarioId: Int)
    fun getTotalItems(usuarioId: Int): Flow<Int>
}