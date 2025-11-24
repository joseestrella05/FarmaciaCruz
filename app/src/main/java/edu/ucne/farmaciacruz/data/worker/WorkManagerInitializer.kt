package edu.ucne.farmaciacruz.data.worker

import android.content.Context
import androidx.startup.Initializer
import androidx.work.Configuration
import androidx.work.WorkManager
import javax.inject.Inject

class WorkManagerInitializer : Initializer<WorkManager> {

    override fun create(context: Context): WorkManager {
        val configuration = Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build()

        WorkManager.initialize(context, configuration)

        PaymentSyncWorker.schedulePeriodicSync(context)

        return WorkManager.getInstance(context)
    }

    override fun dependencies(): List<Class<out Initializer<*>>> {
        return emptyList()
    }
}