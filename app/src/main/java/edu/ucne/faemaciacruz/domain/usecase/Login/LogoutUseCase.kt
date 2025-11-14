package edu.ucne.faemaciacruz.domain.usecase.Login

import edu.ucne.faemaciacruz.data.repository.AuthRepositoryImpl
import javax.inject.Inject

class LogoutUseCase @Inject constructor(
    private val authRepository: AuthRepositoryImpl
) {
    suspend operator fun invoke() {
        authRepository.logout()
    }
}
