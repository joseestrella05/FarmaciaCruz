package edu.ucne.farmaciacruz.domain.usecase.login


import edu.ucne.farmaciacruz.data.repository.AuthRepositoryImpl
import edu.ucne.farmaciacruz.domain.model.Resource
import edu.ucne.farmaciacruz.domain.model.User
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepositoryImpl
) {
    suspend operator fun invoke(email: String, password: String): Flow<Resource<User>> {
        if (email.isBlank()) {
            return kotlinx.coroutines.flow.flow {
                emit(Resource.Error("El email es requerido"))
            }
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return kotlinx.coroutines.flow.flow {
                emit(Resource.Error("Email inválido"))
            }
        }

        if (password.isBlank()) {
            return kotlinx.coroutines.flow.flow {
                emit(Resource.Error("La contraseña es requerida"))
            }
        }

        if (password.length < 8) {
            return kotlinx.coroutines.flow.flow {
                emit(Resource.Error("La contraseña debe tener al menos 8 caracteres"))
            }
        }

        return authRepository.login(email, password)
    }
}