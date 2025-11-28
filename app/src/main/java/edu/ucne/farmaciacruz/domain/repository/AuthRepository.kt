package edu.ucne.farmaciacruz.domain.repository

import edu.ucne.farmaciacruz.domain.model.Resource
import edu.ucne.farmaciacruz.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun login(email: String, password: String): Flow<Resource<User>>
    suspend fun logout()
    fun isLoggedIn(): Flow<Boolean>
    fun getUserData(): Flow<User?>
    fun register(
        email: String,
        password: String,
        nombre: String,
        apellido: String,
        telefono: String?
    ): Flow<Resource<User>>

    fun recoveryPassword(email: String): Flow<Resource<Unit>>
}