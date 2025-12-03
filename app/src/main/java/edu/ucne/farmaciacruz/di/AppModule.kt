package edu.ucne.farmaciacruz.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import edu.ucne.farmaciacruz.data.local.PreferencesManager
import edu.ucne.farmaciacruz.data.remote.ApiService
import edu.ucne.farmaciacruz.data.repository.AuthRepositoryImpl
import edu.ucne.farmaciacruz.data.repository.ProductRepositoryImpl
import edu.ucne.farmaciacruz.domain.repository.AuthRepository
import edu.ucne.farmaciacruz.domain.repository.ProductRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides @Singleton
    fun providePreferencesManager(
        @ApplicationContext context: Context
    ): PreferencesManager = PreferencesManager(context)

    @Provides @Singleton
    fun provideAuthRepository(
        apiService: ApiService,
        preferencesManager: PreferencesManager
    ): AuthRepository = AuthRepositoryImpl(apiService, preferencesManager)

    @Provides @Singleton
    fun provideProductRepository(
        apiService: ApiService
    ): ProductRepository = ProductRepositoryImpl(apiService)

}