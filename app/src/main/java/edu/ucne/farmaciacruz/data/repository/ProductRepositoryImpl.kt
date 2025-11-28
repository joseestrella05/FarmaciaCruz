package edu.ucne.farmaciacruz.data.repository

import edu.ucne.farmaciacruz.data.remote.ApiService
import edu.ucne.farmaciacruz.data.remote.dto.ProductoDto
import edu.ucne.farmaciacruz.data.remote.dto.CreateProductoRequest
import edu.ucne.farmaciacruz.domain.model.Producto
import edu.ucne.farmaciacruz.domain.model.Resource
import edu.ucne.farmaciacruz.domain.repository.ProductRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : ProductRepository {

    override fun getProductos(): Flow<Resource<List<Producto>>> = flow {
        try {
            emit(Resource.Loading())

            val response = apiService.getProductos()

            if (response.isSuccessful && response.body() != null) {
                val productos = response.body()!!.map { it.toDomain() }
                emit(Resource.Success(productos))
            } else {
                val errorMessage = when (response.code()) {
                    401 -> "No autorizado. Por favor, inicia sesión nuevamente"
                    403 -> "No tienes permisos para ver los productos"
                    404 -> "No se encontraron productos"
                    500 -> "Error del servidor. Intenta más tarde"
                    else -> "Error al cargar productos: ${response.message()}"
                }
                emit(Resource.Error(errorMessage))
            }
        } catch (e: HttpException) {
            emit(Resource.Error("Error de red: ${e.message()}"))
        } catch (e: IOException) {
            emit(Resource.Error("Error de conexión. Verifica tu internet"))
        } catch (e: Exception) {
            emit(Resource.Error("Error inesperado: ${e.message ?: "Desconocido"}"))
        }
    }

    override fun getProducto(id: Int): Flow<Resource<Producto>> = flow {
        try {
            emit(Resource.Loading())

            val response = apiService.getProducto(id)

            if (response.isSuccessful && response.body() != null) {
                val producto = response.body()!!.toDomain()
                emit(Resource.Success(producto))
            } else {
                val errorMessage = when (response.code()) {
                    404 -> "Producto no encontrado"
                    401 -> "No autorizado"
                    else -> "Error al cargar el producto"
                }
                emit(Resource.Error(errorMessage))
            }
        } catch (e: HttpException) {
            emit(Resource.Error("Error de red: ${e.message()}"))
        } catch (e: IOException) {
            emit(Resource.Error("Error de conexión"))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Error desconocido"))
        }
    }


    override fun getProductosPorCategoria(categoria: String): Flow<Resource<List<Producto>>> = flow {
        try {
            emit(Resource.Loading())

            val response = apiService.getProductosPorCategoria(categoria)

            if (response.isSuccessful && response.body() != null) {
                val productos = response.body()!!.map { it.toDomain() }
                emit(Resource.Success(productos))
            } else {
                emit(Resource.Error("Error al cargar productos de la categoría"))
            }
        } catch (e: HttpException) {
            emit(Resource.Error("Error de red: ${e.message()}"))
        } catch (e: IOException) {
            emit(Resource.Error("Error de conexión"))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Error desconocido"))
        }
    }

    override fun searchProductos(query: String): Flow<Resource<List<Producto>>> = flow {
        try {
            emit(Resource.Loading())

            if (query.isBlank()) {
                emit(Resource.Success(emptyList()))
                return@flow
            }

            val response = apiService.getProductos()

            if (response.isSuccessful && response.body() != null) {
                val allProductos = response.body()!!
                val filteredProductos = allProductos
                    .filter { producto ->
                        producto.nombre.contains(query, ignoreCase = true) ||
                                producto.descripcion.contains(query, ignoreCase = true) ||
                                producto.categoria.contains(query, ignoreCase = true)
                    }
                    .map { it.toDomain() }

                emit(Resource.Success(filteredProductos))
            } else {
                emit(Resource.Error("Error al buscar productos"))
            }
        } catch (e: HttpException) {
            emit(Resource.Error("Error de red: ${e.message()}"))
        } catch (e: IOException) {
            emit(Resource.Error("Error de conexión"))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Error desconocido"))
        }
    }

    override suspend fun getCategorias(): Flow<Resource<List<String>>> = flow {
        try {
            emit(Resource.Loading())

            val response = apiService.getProductos()

            if (response.isSuccessful && response.body() != null) {
                val categorias = response.body()!!
                    .map { it.categoria }
                    .distinct()
                    .sorted()

                emit(Resource.Success(categorias))
            } else {
                emit(Resource.Error("Error al cargar categorías"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Error desconocido"))
        }
    }

    override suspend fun createProducto(
        nombre: String,
        categoria: String,
        descripcion: String,
        precio: Double,
        imagenUrl: String
    ): Flow<Resource<Producto>> = flow {
        try {
            emit(Resource.Loading())

            val request = CreateProductoRequest(
                nombre = nombre,
                categoria = categoria,
                descripcion = descripcion,
                precio = precio,
                imagenUrl = imagenUrl
            )

            val response = apiService.createProducto(request)

            if (response.isSuccessful && response.body() != null) {
                val producto = response.body()!!.data!!.toDomain()
                emit(Resource.Success(producto))
            } else {
                val errorMessage = when (response.code()) {
                    401 -> "No autorizado"
                    403 -> "No tienes permisos para crear productos"
                    409 -> "Ya existe un producto con ese nombre"
                    else -> "Error al crear el producto"
                }
                emit(Resource.Error(errorMessage))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Error desconocido"))
        }
    }


    override suspend fun updateProducto(producto: Producto): Flow<Resource<Unit>> = flow {
        try {
            emit(Resource.Loading())

            val dto = ProductoDto(
                productoId = producto.id,
                nombre = producto.nombre,
                categoria = producto.categoria,
                descripcion = producto.descripcion,
                precio = producto.precio,
                imagenUrl = producto.imagenUrl
            )

            val response = apiService.updateProducto(producto.id, dto)

            if (response.isSuccessful) {
                emit(Resource.Success(Unit))
            } else {
                emit(Resource.Error("Error al actualizar el producto"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Error desconocido"))
        }
    }


    override suspend fun deleteProducto(id: Int): Flow<Resource<Unit>> = flow {
        try {
            emit(Resource.Loading())

            val response = apiService.deleteProducto(id)

            if (response.isSuccessful) {
                emit(Resource.Success(Unit))
            } else {
                val errorMessage = when (response.code()) {
                    401 -> "No autorizado"
                    403 -> "Solo administradores pueden eliminar productos"
                    404 -> "Producto no encontrado"
                    else -> "Error al eliminar el producto"
                }
                emit(Resource.Error(errorMessage))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Error desconocido"))
        }
    }
}


private fun ProductoDto.toDomain(): Producto {
    return Producto(
        id = this.productoId,
        nombre = this.nombre,
        categoria = this.categoria,
        descripcion = this.descripcion,
        precio = this.precio,
        imagenUrl = this.imagenUrl
    )
}