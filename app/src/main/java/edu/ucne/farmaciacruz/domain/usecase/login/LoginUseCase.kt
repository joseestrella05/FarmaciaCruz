package edu.ucne.farmaciacruz.domain.usecase.login

import android.util.Patterns

import edu.ucne.farmaciacruz.domain.model.Resource
import edu.ucne.farmaciacruz.domain.model.User
import edu.ucne.farmaciacruz.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
     operator fun invoke(email: String, password: String): Flow<Resource<User>> {
        if (email.isBlank()) {
            return flow {
                emit(Resource.Error("El email es requerido"))
            }
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return flow {
                emit(Resource.Error("Email inválido"))
            }
        }

        if (password.isBlank()) {
            return flow {
                emit(Resource.Error("La contraseña es requerida"))
            }
        }

        if (password.length < 8) {
            return flow {
                emit(Resource.Error("La contraseña debe tener al menos 8 caracteres"))
            }
        }

        return authRepository.login(email, password)
    }
}