package edu.ucne.farmaciacruz.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton


private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "farmacia_preferences")

@Singleton
class PreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val dataStore = context.dataStore

    // Keys
    private object PreferencesKeys {
        val TOKEN = stringPreferencesKey("token")
        val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
        val USER_ID = intPreferencesKey("user_id")
        val USER_EMAIL = stringPreferencesKey("user_email")
        val USER_NAME = stringPreferencesKey("user_name")
        val USER_ROLE = stringPreferencesKey("user_role")
        val API_URL = stringPreferencesKey("api_url")
        val IS_DARK_THEME = booleanPreferencesKey("is_dark_theme")
    }

    suspend fun saveToken(token: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.TOKEN] = token
        }
    }

    fun getToken(): Flow<String?> {
        return dataStore.data.map { preferences ->
            preferences[PreferencesKeys.TOKEN]
        }
    }

    suspend fun saveRefreshToken(refreshToken: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.REFRESH_TOKEN] = refreshToken
        }
    }

    fun getRefreshToken(): Flow<String?> {
        return dataStore.data.map { preferences ->
            preferences[PreferencesKeys.REFRESH_TOKEN]
        }
    }

    suspend fun saveUserData(
        userId: Int,
        email: String,
        name: String,
        role: String
    ) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.USER_ID] = userId
            preferences[PreferencesKeys.USER_EMAIL] = email
            preferences[PreferencesKeys.USER_NAME] = name
            preferences[PreferencesKeys.USER_ROLE] = role
        }
    }

    fun getUserId(): Flow<Int?> {
        return dataStore.data.map { preferences ->
            preferences[PreferencesKeys.USER_ID]
        }
    }

    fun getUserEmail(): Flow<String?> {
        return dataStore.data.map { preferences ->
            preferences[PreferencesKeys.USER_EMAIL]
        }
    }

    fun getUserName(): Flow<String?> {
        return dataStore.data.map { preferences ->
            preferences[PreferencesKeys.USER_NAME]
        }
    }

    fun getUserRole(): Flow<String?> {
        return dataStore.data.map { preferences ->
            preferences[PreferencesKeys.USER_ROLE]
        }
    }

    suspend fun saveApiUrl(url: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.API_URL] = url
        }
    }

    fun getApiUrl(): Flow<String> {
        return dataStore.data.map { preferences ->
            preferences[PreferencesKeys.API_URL] ?: "https://farmaciacruzapi.azurewebsites.net/"
        }
    }


    suspend fun saveThemePreference(isDark: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_DARK_THEME] = isDark
        }
    }

    fun getThemePreference(): Flow<Boolean> {
        return dataStore.data.map { preferences ->
            preferences[PreferencesKeys.IS_DARK_THEME] ?: false
        }
    }


    suspend fun clearUserData() {
        dataStore.edit { preferences ->
            preferences.remove(PreferencesKeys.TOKEN)
            preferences.remove(PreferencesKeys.REFRESH_TOKEN)
            preferences.remove(PreferencesKeys.USER_ID)
            preferences.remove(PreferencesKeys.USER_EMAIL)
            preferences.remove(PreferencesKeys.USER_NAME)
            preferences.remove(PreferencesKeys.USER_ROLE)
        }
    }

    suspend fun clearAll() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}