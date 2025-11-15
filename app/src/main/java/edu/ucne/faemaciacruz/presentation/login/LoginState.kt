package edu.ucne.faemaciacruz.presentation.login

import edu.ucne.faemaciacruz.domain.model.User

data class LoginState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val user: User? = null,
    val error: String? = null
)