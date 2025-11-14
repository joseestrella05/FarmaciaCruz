package edu.ucne.faemaciacruz.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import edu.ucne.faemaciacruz.data.local.PreferencesManager
import edu.ucne.faemaciacruz.data.remote.api.ApiService
import edu.ucne.faemaciacruz.data.repository.AuthRepositoryImpl
import edu.ucne.faemaciacruz.data.repository.ProductRepositoryImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun providePreferencesManager(
        @ApplicationContext context: Context
    ): PreferencesManager {
        return PreferencesManager(context)
    }

    @Provides
    @Singleton
    fun provideAuthRepository(
        apiService: ApiService,
        preferencesManager: PreferencesManager
    ): AuthRepositoryImpl {
        return AuthRepositoryImpl(apiService, preferencesManager)
    }

    @Provides
    @Singleton
    fun provideProductRepository(
        apiService: ApiService
    ): ProductRepositoryImpl {
        return ProductRepositoryImpl(apiService)
    }
}