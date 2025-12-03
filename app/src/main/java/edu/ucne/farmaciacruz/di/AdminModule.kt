package edu.ucne.farmaciacruz.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import edu.ucne.farmaciacruz.data.repository.AdminRepository
import edu.ucne.farmaciacruz.data.repository.AdminRepositoryImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
fun interface AdminModule {

    @Binds
    @Singleton
    fun bindAdminRepository(
        impl: AdminRepositoryImpl
    ): AdminRepository
}