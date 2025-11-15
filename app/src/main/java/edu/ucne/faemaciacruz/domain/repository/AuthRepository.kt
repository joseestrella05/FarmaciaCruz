package edu.ucne.faemaciacruz.domain.repository

import edu.ucne.faemaciacruz.domain.model.Resource
import edu.ucne.faemaciacruz.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun login(email: String, password: String): Flow<Resource<User>>
    suspend fun logout()
    fun isLoggedIn(): Flow<Boolean>
    fun getUserData(): Flow<User?>
    suspend fun register(
        email: String,
        password: String,
        nombre: String,
        apellido: String,
        telefono: String?
    ): Flow<Resource<User>>
}
