package edu.ucne.faemaciacruz.data.remote.api

data class ApiResponse<T>(
    val mensaje: String? = null,
    val data: T? = null
)