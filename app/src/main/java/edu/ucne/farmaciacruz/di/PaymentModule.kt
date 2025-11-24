package edu.ucne.farmaciacruz.di

import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import edu.ucne.farmaciacruz.data.local.dao.PaymentOrderDao
import edu.ucne.farmaciacruz.data.remote.api.PayPalApiService
import edu.ucne.farmaciacruz.data.repository.PaymentRepositoryImpl
import edu.ucne.farmaciacruz.domain.repository.PaymentRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PaymentModule {

    @Provides
    @Singleton
    fun providePaymentRepository(
        payPalApi: PayPalApiService,
        paymentOrderDao: PaymentOrderDao,
        gson: Gson
    ): PaymentRepository {
        return PaymentRepositoryImpl(
            payPalApi = payPalApi,
            paymentOrderDao = paymentOrderDao,
            gson = gson
        )
    }
}