package com.example.calculadoradeimc.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.calculadoradeimc.MainActivity

/**
 * Background worker that triggers a notification to the user.
 */
class BMIReminderWorker(
    context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    override fun doWork(): Result {
        triggerNotification()
        return Result.success()
    }

    private fun triggerNotification() {
        val channelId = "bmi_reminder_channel"
        val channelName = "Lembrete Semanal IMC"
        val notificationId = 101

        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // 1. Create Notification Channel (Required for Android 8.0+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notificações semanais para atualização de peso."
            }
            notificationManager.createNotificationChannel(channel)
        }

        // 2. Create Intent to open App when clicked
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        // 3. Build the Notification
        // Note: We use a system icon (ic_dialog_info) to ensure it works without custom assets
        val builder = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Hora de checar seu IMC!")
            .setContentText("Faz uma semana desde sua última medição. Atualize seu peso hoje.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        // 4. Show it
        notificationManager.notify(notificationId, builder.build())
    }
}