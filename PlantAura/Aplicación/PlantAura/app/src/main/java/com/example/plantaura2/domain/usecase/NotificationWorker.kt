package com.example.plantaura2.domain.usecase

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.plantaura2.MainActivity

class NotificationWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : Worker(appContext, workerParams) {

    override fun doWork(): Result {
        // Crear un Handler en el hilo principal
        val mainHandler = Handler(Looper.getMainLooper())

        // Enviar un mensaje al hilo principal para mostrar la notificación
        mainHandler.post {
            // Llama a la función createSimpleNotification() de MainActivity para mostrar la notificación
            MainActivity().createSimpleNotification(applicationContext)
        }

        return Result.success()
    }
}