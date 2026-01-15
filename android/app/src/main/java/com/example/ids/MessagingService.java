package com.example.ids;

import android.util.Log;
import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;

import android.os.Build;
import androidx.core.app.NotificationCompat;

import android.app.PendingIntent;
import android.content.Intent;

import java.io.*;

import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import java.util.concurrent.TimeUnit;

import androidx.work.OutOfQuotaPolicy;

public class MessagingService extends FirebaseMessagingService {


    private static final String WORK_TAG = "emergency_alert_work";

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        if (remoteMessage.getData().size() > 0) {

            String emergency_type = remoteMessage.getData().get("emergency_type");

            String title = remoteMessage.getData().get("title");
            String body = remoteMessage.getData().get("body");

            Log.d("FCM", "Messaggio ricevuto in background: " + title);

            showNotification(emergency_type, title, body);

            scheduleEmergencyWorker();
        }
    }

    private void scheduleEmergencyWorker() {
        Log.d("MessagingService", "Schedulazione Worker tra 30 secondi...");

        OneTimeWorkRequest emergencyWork = new OneTimeWorkRequest.Builder(EmergencyWorker.class)
                //.setInitialDelay(30, TimeUnit.SECONDS)
                .addTag(WORK_TAG)
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .build();

        WorkManager.getInstance(getApplicationContext()).enqueue(emergencyWork);
    }

    private void showNotification(String emergency_type, String title, String message) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = "emergency_channel_new";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "Notifiche Emergenze", NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Notifiche per emergenze in corso");
            channel.enableVibration(true);
            notificationManager.createNotificationChannel(channel);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (androidx.core.app.ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                Log.e("Notification", "Permesso NOTIFICHE non concesso. Impossibile mostrare.");
                return;
            }
        }

        Intent intent = new Intent(this, NotificationActivity.class);
        intent.putExtra("emergency_type", emergency_type);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                (int) System.currentTimeMillis(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setDefaults(NotificationCompat.DEFAULT_ALL);;

        notificationManager.notify((int) System.currentTimeMillis(), builder.build());
        Log.d("Notification", "Notifica inviata al sistema UI");
    }



    public static void abortEmergency(Context context) {
        WorkManager.getInstance(context).cancelAllWorkByTag(WORK_TAG);
        Log.d("MessagingService", "Emergency Worker CANCELLATO.");
    }

    public static void triggerImmediateEmergency(Context context) {
        // 1. Cancella il timer che sta aspettando (per evitare doppi invii)
        WorkManager.getInstance(context).cancelAllWorkByTag(WORK_TAG);

        Log.d("MessagingService", "Attivazione emergenza IMMEDIATA manuale.");

        // 2. Prepara i dati per dire al worker di saltare il timer
        androidx.work.Data inputData = new androidx.work.Data.Builder()
                .putBoolean("is_immediate", true)
                .build();

        // 3. Crea la richiesta (Expedited + InputData)
        OneTimeWorkRequest immediateWork = new OneTimeWorkRequest.Builder(EmergencyWorker.class)
                .addTag(WORK_TAG) // Usiamo lo stesso tag
                .setInputData(inputData) // Passiamo il flag
                .setExpedited(androidx.work.OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .build();

        // 4. Invia subito
        WorkManager.getInstance(context).enqueue(immediateWork);
    }

}
