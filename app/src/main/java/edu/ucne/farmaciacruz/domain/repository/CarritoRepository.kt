package edu.ucne.farmaciacruz.domain.repository

import edu.ucne.farmaciacruz.domain.model.CarritoItem
import edu.ucne.farmaciacruz.domain.model.Producto
import kotlinx.coroutines.flow.Flow

interface CarritoRepository {

    fun getCarrito(usuarioId: Int): Flow<List<CarritoItem>>

    suspend fun addToCarrito(usuarioId: Int, producto: Producto)

    suspend fun addToCarrito(usuarioId: Int, producto: Producto, cantidad: Int)

    suspend fun removeFromCarrito(usuarioId: Int, productoId: Int)

    suspend fun updateCantidad(usuarioId: Int, productoId: Int, cantidad: Int)

    fun getTotalItems(usuarioId: Int): Flow<Int>
}