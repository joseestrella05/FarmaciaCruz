package edu.ucne.farmaciacruz.data.repository

import edu.ucne.farmaciacruz.data.local.PreferencesManager
import edu.ucne.farmaciacruz.domain.repository.PreferencesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class PreferencesRepositoryImpl @Inject constructor(
    private val prefs: PreferencesManager
) : PreferencesRepository {

    override fun getUserId(): Flow<Int?> = prefs.getUserId()
    override fun getApiUrl(): Flow<String> = prefs.getApiUrl()
    override suspend fun saveApiUrl(url: String) = prefs.saveApiUrl(url)
    override fun getDarkTheme(): Flow<Boolean> = prefs.getThemePreference()
    override suspend fun saveDarkTheme(isDark: Boolean) = prefs.saveThemePreference(isDark)
}