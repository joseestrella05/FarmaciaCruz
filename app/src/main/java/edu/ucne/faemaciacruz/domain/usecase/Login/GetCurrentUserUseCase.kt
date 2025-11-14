package edu.ucne.faemaciacruz.domain.usecase.Login

import edu.ucne.faemaciacruz.data.repository.AuthRepositoryImpl
import edu.ucne.faemaciacruz.domain.model.User
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCurrentUserUseCase @Inject constructor(
    private val authRepository: AuthRepositoryImpl
) {
    operator fun invoke(): Flow<User?> {
        return authRepository.getUserData()
    }
}