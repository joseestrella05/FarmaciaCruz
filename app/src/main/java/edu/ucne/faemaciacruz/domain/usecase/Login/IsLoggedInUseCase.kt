package edu.ucne.faemaciacruz.domain.usecase.Login

import edu.ucne.faemaciacruz.data.repository.AuthRepositoryImpl
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class IsLoggedInUseCase @Inject constructor(
    private val authRepository: AuthRepositoryImpl
) {
    operator fun invoke(): Flow<Boolean> {
        return authRepository.isLoggedIn()
    }
}