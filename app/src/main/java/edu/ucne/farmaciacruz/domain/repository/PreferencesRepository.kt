package edu.ucne.farmaciacruz.domain.repository

import kotlinx.coroutines.flow.Flow

interface PreferencesRepository {
    fun getUserId(): Flow<Int?>
    fun getApiUrl(): Flow<String>
    suspend fun saveApiUrl(url: String)
    fun getDarkTheme(): Flow<Boolean>
    suspend fun saveDarkTheme(isDark: Boolean)
}