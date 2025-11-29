package edu.ucne.farmaciacruz.data.repository

import edu.ucne.farmaciacruz.domain.model.*
import kotlinx.coroutines.flow.Flow


interface AdminRepository {
    fun getEstadisticas(): Flow<Resource<AdminStats>>
    fun getAllUsuarios(): Flow<Resource<List<UsuarioAdmin>>>
    fun cambiarRolUsuario(usuarioId: Int, nuevoRol: String): Flow<Resource<Unit>>
    fun cambiarEstadoUsuario(usuarioId: Int, activo: Boolean): Flow<Resource<Unit>>
    fun deleteUsuario(usuarioId: Int): Flow<Resource<Unit>>
    fun getAllOrders(): Flow<Resource<List<OrderAdmin>>>
    fun updateOrderStatus(orderId: Int, nuevoEstado: String): Flow<Resource<Unit>>
}