package com.example.ids;

import android.content.Context;
import android.telephony.SmsManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.ids.constants.Constants;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class EmergencyWorker extends Worker {

    public EmergencyWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {

        boolean isImmediate = getInputData().getBoolean("is_immediate", false);

        if (!isImmediate) {
            try {
                // quanti secondi l'utente ha per confermare che sta bene: 60 minuti
                for (int i = 0; i < 60*30; i++) {
                    if (isStopped()) {
                        Log.d("EmergencyWorker", "Lavoro annullato durante il conto alla rovescia.");
                        return Result.success();
                    }
                    Thread.sleep(1000); // Dorme 1 secondo alla volta
                }
            } catch (InterruptedException e) {
                Log.d("EmergencyWorker", "Timer interrotto.");
                return Result.success();
            }
        } else {
            Log.d("EmergencyWorker", "Worker in modalità immediata.");
        }

        if (isStopped()) return Result.success();

        Context context = getApplicationContext();
        String userId = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE).getString("user_id", "");
        String token = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE).getString("session_token", "");

        if (userId.isEmpty() || token.isEmpty()) {
            return Result.failure();
        }

        ArrayList<String> numbers = new ArrayList<>();
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(Constants.BASE_URL + "/users/" + userId + "/caregivers")
                .addHeader("Authorization", "Bearer " + token)
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                Log.e("EmergencyWorker", "Errore server: " + response.code());
                return Result.retry();
            }

            String body = response.body().string();
            JSONObject mainObject = new JSONObject(body);
            JSONArray jsonArray = mainObject.getJSONArray("data");

            for (int i = 0; i < jsonArray.length(); i++) {
                numbers.add(jsonArray.getJSONObject(i).getString("phone_number"));
            }
        } catch (Exception e) {
            Log.e("EmergencyWorker", "Errore: "+e.getMessage());
            return Result.failure();
        }

        if (isStopped()) {
            Log.d("EmergencyWorker", "Worker cancellato");
            return Result.success();
        }

        sendSMS(numbers);
        Log.d("EmergencyWorker", "SMS inviati a: " + numbers.toString());

        return Result.success();
    }

    private void sendSMS(ArrayList<String> numbers) {
        if (androidx.core.content.ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.SEND_SMS)
                != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            Log.e("EmergencyWorker", "Permesso SMS mancante.");
            return;
        }

        Context context = getApplicationContext();
        android.content.SharedPreferences prefs = context.getSharedPreferences("location_prefs", Context.MODE_PRIVATE);

        String lat = prefs.getString("last_lat", null);
        String lon = prefs.getString("last_lon", null);

        String link = "https://maps.google.com/?q="+lat+","+lon;

        try {
            SmsManager smsManager = SmsManager.getDefault();
            String message = "Please, I need help.";
            if(lat != null && lon != null) {
                message = "Please, I need help. I am here: " + link + ".";
            }

            for (String number : numbers) {
                smsManager.sendTextMessage(number, null, message, null, null);
                Log.d("EmergencyWorker", "SMS: "+message+" - Inviato a: " + number);
            }
        } catch (Exception e) {
            Log.e("EmergencyWorker", "Errore invio SMS: " + e.getMessage());
        }
    }
}