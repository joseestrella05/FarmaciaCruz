package edu.ucne.farmaciacruz.data.repository

import edu.ucne.farmaciacruz.data.local.dao.CarritoDao
import edu.ucne.farmaciacruz.data.mapper.toCarritoEntity
import edu.ucne.farmaciacruz.data.mapper.toDomain
import edu.ucne.farmaciacruz.domain.model.CarritoItem
import edu.ucne.farmaciacruz.domain.model.Producto
import edu.ucne.farmaciacruz.domain.repository.CarritoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CarritoRepositoryImpl @Inject constructor(
    private val dao: CarritoDao
) : CarritoRepository {

    override fun getCarrito(usuarioId: Int): Flow<List<CarritoItem>> {
        return dao.getCarritoByUsuario(usuarioId).map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun addToCarrito(usuarioId: Int, producto: Producto) {
        val item = dao.getCarritoItem(usuarioId, producto.id)

        if (item == null) {
            dao.insertCarritoItem(producto.toCarritoEntity(usuarioId))
        } else {
            dao.updateCarritoItem(
                item.copy(cantidad = item.cantidad + 1)
            )
        }
    }

    override suspend fun addToCarrito(usuarioId: Int, producto: Producto, cantidad: Int) {
        val item = dao.getCarritoItem(usuarioId, producto.id)

        if (item == null) {
            dao.insertCarritoItem(producto.toCarritoEntity(usuarioId, cantidad))
        } else {
            dao.updateCarritoItem(
                item.copy(cantidad = item.cantidad + cantidad)
            )
        }
    }

    override suspend fun removeFromCarrito(usuarioId: Int, productoId: Int) {
        dao.deleteByProductoId(usuarioId, productoId)
    }

    override suspend fun updateCantidad(usuarioId: Int, productoId: Int, cantidad: Int) {
        val item = dao.getCarritoItem(usuarioId, productoId)
        if (item != null) {
            dao.updateCarritoItem(item.copy(cantidad = cantidad))
        }
    }

    override fun getTotalItems(usuarioId: Int): Flow<Int> {
        return dao.getTotalItems(usuarioId).map { it ?: 0 }
    }
}