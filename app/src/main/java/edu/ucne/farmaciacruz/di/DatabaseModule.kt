package edu.ucne.farmaciacruz.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import edu.ucne.farmaciacruz.data.local.FarmaciaDatabase
import edu.ucne.farmaciacruz.data.local.dao.CarritoDao
import edu.ucne.farmaciacruz.data.local.dao.PaymentOrderDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(
                """
                CREATE TABLE IF NOT EXISTS payment_orders (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    localId TEXT NOT NULL,
                    usuarioId INTEGER NOT NULL,
                    total REAL NOT NULL,
                    productosJson TEXT NOT NULL,
                    estado TEXT NOT NULL,
                    metodoPago TEXT NOT NULL,
                    paypalOrderId TEXT,
                    paypalPayerId TEXT,
                    fechaCreacion INTEGER NOT NULL,
                    fechaActualizacion INTEGER NOT NULL,
                    sincronizado INTEGER NOT NULL DEFAULT 0,
                    errorMessage TEXT
                )
                """.trimIndent()
            )
            database.execSQL(
                "CREATE INDEX IF NOT EXISTS index_payment_orders_usuarioId ON payment_orders(usuarioId)"
            )
            database.execSQL(
                "CREATE INDEX IF NOT EXISTS index_payment_orders_paypalOrderId ON payment_orders(paypalOrderId)"
            )
            database.execSQL(
                "CREATE INDEX IF NOT EXISTS index_payment_orders_sincronizado ON payment_orders(sincronizado)"
            )
        }
    }


    @Provides
    @Singleton
    fun provideFarmaciaDatabase(
        @ApplicationContext context: Context
    ): FarmaciaDatabase {
        return Room.databaseBuilder(
            context,
            FarmaciaDatabase::class.java,
            "farmacia_database"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideCarritoDao(database: FarmaciaDatabase): CarritoDao {
        return database.carritoDao()
    }

    @Provides
    @Singleton
    fun providePaymentOrderDao(database: FarmaciaDatabase): PaymentOrderDao {
        return database.paymentOrderDao()
    }


}