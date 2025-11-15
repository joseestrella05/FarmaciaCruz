package edu.ucne.faemaciacruz.data.remote.api

import edu.ucne.faemaciacruz.data.local.PreferencesManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val preferencesManager: PreferencesManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val token = runBlocking {
            preferencesManager.getToken().first()
        }

        val request = chain.request().newBuilder()

        if (!token.isNullOrEmpty()) {
            request.addHeader("Authorization", "Bearer $token")
        }

        request.addHeader("Content-Type", "application/json")
        request.addHeader("Accept", "application/json")

        return chain.proceed(request.build())
    }
}