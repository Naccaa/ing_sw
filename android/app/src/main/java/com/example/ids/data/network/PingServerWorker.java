package com.example.ids.data.network;

import static com.example.ids.constants.Constants.BASE_URL;

import android.Manifest;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresPermission;
import androidx.core.content.ContextCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class PingServerWorker extends Worker {

    private static final String TAG = "PingWorker";

    private final FusedLocationProviderClient fusedLocationClient;
    private final OkHttpClient client;

    public PingServerWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);

        Log.d(TAG, "Worker COSTRUITO");

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);

        client = new OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(5, TimeUnit.SECONDS)
                .build();
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "Worker AVVIATO");

        try {
            // Recupera posizione
            double[] location = getLocation();
            double lat = location[0];
            double lon = location[1];

            // Chiama server
            fetchAlerts(lat, lon);

            return Result.success();
        } catch (Exception e) {
            Log.e(TAG, "Errore worker", e);
            return Result.retry();
        }
    }

    /**
     * Recupera la posizione dell'utente.
     * ATTENZIONE: in Worker, la posizione potrebbe essere null se permessi negati
     */
    @RequiresPermission(allOf = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION})
    private double[] getLocation() {
        try {
            // fallback valori statici
            final double[] loc = {0.0, 0.0};

            fusedLocationClient.getCurrentLocation(
                    Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                    null
            ).addOnSuccessListener(location -> {
                if (location != null) {
                    loc[0] = location.getLatitude();
                    loc[1] = location.getLongitude();
                    Log.d(TAG, "Lat: " + loc[0] + " Lon: " + loc[1]);
                } else {
                    Log.w(TAG, "Posizione non disponibile, uso default");
                }
            }).addOnFailureListener(e -> {
                Log.e(TAG, "Errore recupero posizione");
            });

            // Nota: getCurrentLocation è asincrono, quindi qui ritorniamo i valori di default
            return loc;

        } catch (Exception e) {
            Log.e(TAG, "Errore getLocation", e);
            return new double[]{0.0, 0.0};
        }
    }

    /**
     * Chiamata al server per recuperare le emergenze vicine
     */
    private void fetchAlerts(double lat, double lon) {

        JSONObject json = new JSONObject();
        try {
            json.put("lat", lat);
            json.put("lon", lon);
        } catch (JSONException e) {
            Log.e(TAG, "Errore JSON", e);
        }

        Request request = new Request.Builder()
                .url(BASE_URL + "/emergencies?near=" + lat + "," + lon)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "Server down o errore rete", e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String body = response.body() != null ? response.body().string() : "[]";
                    Log.d(TAG, "Alert ricevuti: " + body);
                } else {
                    Log.w(TAG, "Errore server: " + response.code());
                }
            }
        });
    }
}
