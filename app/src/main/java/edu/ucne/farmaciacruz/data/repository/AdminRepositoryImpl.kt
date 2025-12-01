package edu.ucne.farmaciacruz.data.repository

import edu.ucne.farmaciacruz.core.common.ErrorMessages
import edu.ucne.farmaciacruz.data.remote.ApiService
import edu.ucne.farmaciacruz.data.remote.dto.*
import edu.ucne.farmaciacruz.domain.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdminRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : AdminRepository {

    override fun getEstadisticas(): Flow<Resource<AdminStats>> = flow {
        emit(Resource.Loading())

        try {
            val response = apiService.getEstadisticas()

            if (response.isSuccessful && response.body() != null) {
                val data = response.body()!!.data!!

                // Extraer datos de usuarios de forma segura
                val usuariosMap = data["usuarios"] as? Map<*, *>
                val totalUsuarios = (usuariosMap?.get("total") as? Number)?.toInt() ?: 0
                val usuariosActivos = (usuariosMap?.get("activos") as? Number)?.toInt() ?: 0

                // Extraer datos de productos de forma segura
                val productosMap = data["productos"] as? Map<*, *>
                val totalProductos = (productosMap?.get("total") as? Number)?.toInt() ?: 0

                val stats = AdminStats(
                    totalUsuarios = totalUsuarios,
                    usuariosActivos = usuariosActivos,
                    totalProductos = totalProductos,
                    totalOrdenes = 0,
                    ventasDelMes = 0.0,
                    usuariosPorRol = extractRolMap(data),
                    productosPorCategoria = extractCategoriaMap(data)
                )
                emit(Resource.Success(stats))
            } else {
                emit(Resource.Error(ErrorMessages.ERROR_DESCONOCIDO))
            }

        } catch (e: HttpException) {
            emit(Resource.Error("${ErrorMessages.ERROR_RED}: ${e.message()}"))
        } catch (e: IOException) {
            emit(Resource.Error(ErrorMessages.ERROR_CONEXION))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: ErrorMessages.ERROR_DESCONOCIDO))
        }
    }

    override fun getAllUsuarios(): Flow<Resource<List<UsuarioAdmin>>> = flow {
        emit(Resource.Loading())

        try {
            val response = apiService.getAllUsuarios()

            if (response.isSuccessful && response.body() != null) {
                val usuarios = response.body()!!.data!!.map { dto ->
                    UsuarioAdmin(
                        usuarioId = dto.usuarioId,
                        email = dto.email,
                        nombre = dto.nombre,
                        apellido = dto.apellido,
                        telefono = dto.telefono,
                        rol = dto.rol,
                        activo = dto.activo,
                        emailConfirmado = dto.emailConfirmado,
                        fechaCreacion = dto.fechaCreacion,
                        ultimoAcceso = null
                    )
                }
                emit(Resource.Success(usuarios))
            } else {
                val msg = when (response.code()) {
                    401 -> ErrorMessages.NO_AUTORIZADO
                    403 -> "No tienes permisos de administrador"
                    else -> ErrorMessages.ERROR_DESCONOCIDO
                }
                emit(Resource.Error(msg))
            }

        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: ErrorMessages.ERROR_DESCONOCIDO))
        }
    }

    override fun cambiarRolUsuario(usuarioId: Int, nuevoRol: String): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())

        try {
            val response = apiService.cambiarRolUsuario(
                usuarioId,
                CambiarRolDto(nuevoRol)
            )

            if (response.isSuccessful) {
                emit(Resource.Success(Unit))
            } else {
                emit(Resource.Error("Error al cambiar rol"))
            }

        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: ErrorMessages.ERROR_DESCONOCIDO))
        }
    }

    override fun cambiarEstadoUsuario(usuarioId: Int, activo: Boolean): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())

        try {
            val response = apiService.cambiarEstadoUsuario(
                usuarioId,
                CambiarEstadoDto(activo)
            )

            if (response.isSuccessful) {
                emit(Resource.Success(Unit))
            } else {
                emit(Resource.Error("Error al cambiar estado"))
            }

        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: ErrorMessages.ERROR_DESCONOCIDO))
        }
    }

    override fun deleteUsuario(usuarioId: Int): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())

        try {
            val response = apiService.deleteUsuario(usuarioId)

            if (response.isSuccessful) {
                emit(Resource.Success(Unit))
            } else {
                val msg = when (response.code()) {
                    400 -> "No puedes eliminar tu propia cuenta"
                    404 -> "Usuario no encontrado"
                    else -> "Error al eliminar usuario"
                }
                emit(Resource.Error(msg))
            }

        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: ErrorMessages.ERROR_DESCONOCIDO))
        }
    }

    override fun getAllOrders(): Flow<Resource<List<OrderAdmin>>> = flow {
        emit(Resource.Loading())

        try {
            val response = apiService.getAllOrders()

            if (response.isSuccessful && response.body() != null) {
                val ordenes = response.body()!!.map { dto ->
                    OrderAdmin(
                        orderId = dto.orderId,
                        usuarioId = dto.usuarioId,
                        usuarioNombre = "Usuario #${dto.usuarioId}",
                        total = dto.total,
                        estado = parseOrderStatus(dto.estado),
                        metodoPago = "PayPal",
                        cantidadProductos = dto.productos.size,
                        fechaCreacion = dto.fechaCreacion,
                        fechaActualizacion = dto.fechaActualizacion
                    )
                }
                emit(Resource.Success(ordenes))
            } else {
                val msg = when (response.code()) {
                    401 -> ErrorMessages.NO_AUTORIZADO
                    403 -> "No tienes permisos de administrador"
                    else -> ErrorMessages.ERROR_DESCONOCIDO
                }
                emit(Resource.Error(msg))
            }

        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: ErrorMessages.ERROR_DESCONOCIDO))
        }
    }

    override fun updateOrderStatus(orderId: Int, nuevoEstado: String): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())

        try {
            val response = apiService.updateOrderStatus(
                orderId,
                UpdateOrderStatusRequest(nuevoEstado)
            )

            if (response.isSuccessful) {
                emit(Resource.Success(Unit))
            } else {
                emit(Resource.Error("Error al actualizar estado"))
            }

        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: ErrorMessages.ERROR_DESCONOCIDO))
        }
    }

    private fun extractRolMap(data: Map<String, Any>): Map<String, Int> {
        return try {
            val usuarios = data["usuarios"] as? Map<*, *>
            val porRol = usuarios?.get("porRol") as? List<*>
            porRol?.associate { item ->
                val map = item as? Map<*, *>
                val rol = map?.get("rol") as? String ?: ""
                val cantidad = (map?.get("cantidad") as? Number)?.toInt() ?: 0
                rol to cantidad
            } ?: emptyMap()
        } catch (e: Exception) {
            emptyMap()
        }
    }

    private fun extractCategoriaMap(data: Map<String, Any>): Map<String, Int> {
        return try {
            val productos = data["productos"] as? Map<*, *>
            val porCategoria = productos?.get("porCategoria") as? List<*>
            porCategoria?.associate { item ->
                val map = item as? Map<*, *>
                val categoria = map?.get("categoria") as? String ?: ""
                val cantidad = (map?.get("cantidad") as? Number)?.toInt() ?: 0
                categoria to cantidad
            } ?: emptyMap()
        } catch (e: Exception) {
            emptyMap()
        }
    }

    private fun parseOrderStatus(status: String): OrderStatus {
        return try {
            OrderStatus.valueOf(status.uppercase())
        } catch (e: Exception) {
            OrderStatus.PENDIENTE
        }
    }
}