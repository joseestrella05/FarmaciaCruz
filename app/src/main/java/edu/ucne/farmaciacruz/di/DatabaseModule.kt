package edu.ucne.farmaciacruz.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import edu.ucne.farmaciacruz.data.local.FarmaciaDatabase
import edu.ucne.farmaciacruz.data.local.dao.CarritoDao
import edu.ucne.farmaciacruz.data.repository.CarritoRepositoryImpl
import edu.ucne.farmaciacruz.domain.repository.CarritoRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideFarmaciaDatabase(
        @ApplicationContext context: Context
    ): FarmaciaDatabase {
        return Room.databaseBuilder(
            context,
            FarmaciaDatabase::class.java,
            "farmacia_database"
        ).build()
    }

    @Provides
    @Singleton
    fun provideCarritoDao(database: FarmaciaDatabase): CarritoDao {
        return database.carritoDao()
    }

}