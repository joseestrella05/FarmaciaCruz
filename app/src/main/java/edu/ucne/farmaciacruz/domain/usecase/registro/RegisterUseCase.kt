package edu.ucne.farmaciacruz.domain.usecase.registro

import edu.ucne.farmaciacruz.data.repository.AuthRepositoryImpl
import edu.ucne.farmaciacruz.domain.model.Resource
import edu.ucne.farmaciacruz.domain.model.User
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class RegisterUseCase @Inject constructor(
    private val authRepository: AuthRepositoryImpl
) {
    suspend operator fun invoke(
        email: String,
        password: String,
        nombre: String,
        apellido: String,
        telefono: String?
    ): Flow<Resource<User>> {
        return authRepository.register(
            email = email,
            password = password,
            nombre = nombre,
            apellido = apellido,
            telefono = telefono
        )
    }
}