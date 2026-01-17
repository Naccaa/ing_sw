package com.example.ids;


import android.app.Application;
import android.util.Log;

import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.example.ids.data.network.PingServerWorker;

import java.util.concurrent.TimeUnit;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        //Log.d("APP", "MyApplication avviata");
        //schedulePingWorker();
    }

    private void schedulePingWorker() {

        Log.d("APP", "schedulePingWorker avviata");

        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        PeriodicWorkRequest workRequest =
                new PeriodicWorkRequest.Builder(
                        PingServerWorker.class,
                        15,
                        TimeUnit.MINUTES
                )
                        .setConstraints(constraints)
                        .build();

        WorkManager.getInstance(this)
                .enqueueUniquePeriodicWork(
                        "ping_server_worker",
                        ExistingPeriodicWorkPolicy.KEEP,
                        workRequest
                );
    }
}
