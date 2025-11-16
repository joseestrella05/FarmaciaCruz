package edu.ucne.farmaciacruz.presentation.login

import edu.ucne.farmaciacruz.domain.model.User

data class LoginState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val user: User? = null,
    val error: String? = null
)