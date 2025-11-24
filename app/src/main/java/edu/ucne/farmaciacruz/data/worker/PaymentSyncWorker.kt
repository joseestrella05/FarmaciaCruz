package edu.ucne.farmaciacruz.data.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.*
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import edu.ucne.farmaciacruz.domain.model.Resource
import edu.ucne.farmaciacruz.domain.usecase.payment.SyncPaymentOrdersUseCase
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

@HiltWorker
class PaymentSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val syncPaymentOrdersUseCase: SyncPaymentOrdersUseCase
) : CoroutineWorker(context, params) {

    companion object {
        private const val TAG = "PaymentSyncWorker"
        const val WORK_NAME = "payment_sync_work"

        fun schedulePeriodicSync(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
                .build()

            val syncRequest = PeriodicWorkRequestBuilder<PaymentSyncWorker>(
                repeatInterval = 1,
                repeatIntervalTimeUnit = TimeUnit.HOURS,
                flexTimeInterval = 15,
                flexTimeIntervalUnit = TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    WorkRequest.MIN_BACKOFF_MILLIS,
                    TimeUnit.MILLISECONDS
                )
                .build()

            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                    WORK_NAME,
                    ExistingPeriodicWorkPolicy.KEEP,
                    syncRequest
                )
        }

        fun scheduleSingleSync(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val syncRequest = OneTimeWorkRequestBuilder<PaymentSyncWorker>()
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context)
                .enqueueUniqueWork(
                    "${WORK_NAME}_single",
                    ExistingWorkPolicy.REPLACE,
                    syncRequest
                )
        }
    }

    override suspend fun doWork(): Result {
        return try {
            Log.d(TAG, "Starting payment orders sync...")

            val result = syncPaymentOrdersUseCase().first()

            when (result) {
                is Resource.Success -> {
                    Log.d(TAG, "Payment sync completed successfully")
                    Result.success()
                }
                is Resource.Error -> {
                    Log.e(TAG, "Payment sync error: ${result.message}")
                    if (runAttemptCount < 3) {
                        Result.retry()
                    } else {
                        Result.failure()
                    }
                }
                is Resource.Loading -> {
                    Result.retry()
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "Exception during payment sync", e)
            if (runAttemptCount < 3) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }
}