package edu.ucne.farmaciacruz.domain.repository

import edu.ucne.farmaciacruz.domain.model.Producto
import edu.ucne.farmaciacruz.domain.model.Resource
import kotlinx.coroutines.flow.Flow

interface ProductRepository {
    fun getProductos(): Flow<Resource<List<Producto>>>
    fun getProducto(id: Int): Flow<Resource<Producto>>
    fun getProductosPorCategoria(categoria: String): Flow<Resource<List<Producto>>>
    fun searchProductos(query: String): Flow<Resource<List<Producto>>>
    suspend fun getCategorias(): Flow<Resource<List<String>>>
    suspend fun createProducto(
        nombre: String,
        categoria: String,
        descripcion: String,
        precio: Double,
        imagenUrl: String
    ): Flow<Resource<Producto>>
    suspend fun updateProducto(producto: Producto): Flow<Resource<Unit>>
    suspend fun deleteProducto(id: Int): Flow<Resource<Unit>>
}