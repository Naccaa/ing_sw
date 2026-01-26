package com.example.ids;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.ids.constants.Constants;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import android.content.pm.ServiceInfo;


public class LocationService extends Service {
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private String mToken;

    @Override
    public void onCreate() {
        super.onCreate();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);


        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    sendLocationToServer(location);
                }
            }
        };
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            mToken = intent.getStringExtra("firebase_token");
        }

        createNotificationChannel();
        Notification notification = new NotificationCompat.Builder(this, "LocationChannel")
                .setContentTitle("Monitoraggio Emergenze")
                .setContentText("Stiamo monitorando la tua posizione per la tua sicurezza")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(12345, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION);
            } else {
                startForeground(12345, notification);
            }
        } catch (Exception e) {
            Log.e("LocationService", "ERRORE: Permessi mancanti all'avvio del servizio.");
            stopSelf();
            return START_NOT_STICKY;
        }

        requestLocationUpdates();

        return START_STICKY;
    }

    private void requestLocationUpdates() {
        LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 30*60*1000) // 30 minuti
                .setMinUpdateIntervalMillis(15*60*1000) // da cambiare
                .build();

        if (androidx.core.app.ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != android.content.pm.PackageManager.PERMISSION_GRANTED
                && androidx.core.app.ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != android.content.pm.PackageManager.PERMISSION_GRANTED) {

            Log.e("LocationService", "Permessi posizione mancanti.");
            stopSelf();
            return;
        }

        try {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
        } catch (SecurityException e) {
            Log.e("LocationService", "Permessi mancanti: " + e.getMessage());
        }
    }
    private void sendLocationToServer(Location location) {

        getSharedPreferences("location_prefs", MODE_PRIVATE)
                .edit()
                .putString("last_lat", String.valueOf(location.getLatitude()))
                .putString("last_lon", String.valueOf(location.getLongitude()))
                .putLong("last_update_time", System.currentTimeMillis()) // Utile per sapere quanto è vecchia
                .apply();

        if (mToken == null) return;

        String position = "(" + location.getLongitude() + "," + location.getLatitude() + ")";
        String url = Constants.BASE_URL + "/emergencies?near=" + position + "&firebase_token=" + mToken;

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).get().build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("LocationService", "Errore invio posizione");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if(response.isSuccessful()){
                    Log.d("LocationService", "Posizione inviata: " + position);
                }
                response.close();
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    "LocationChannel",
                    "Canale Posizione Background",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(serviceChannel);
            }
        }
    }
}
