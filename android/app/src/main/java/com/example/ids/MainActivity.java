package com.example.ids;

import android.Manifest;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.ids.databinding.ActivityMainBinding;



public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    // Permission is granted. You can post the notification now.
                    Log.d("Permission", "Notification permission granted.");
                    postNotification(); // Call the function to post the notification
                } else {
                    // Permission denied. Explain why the feature won't work.
                    Log.d("Permission", "Notification permission denied.");
                    // You might show a custom dialog here.
                }
            });
    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is not in the Support Library.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            var CHANNEL_ID = "0";
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "channel_name", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("channel_description");
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this.
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void checkNotificationPermission() {
        // Check if the device is on Android 13 (API 33) or higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                    PackageManager.PERMISSION_GRANTED) {

                // Permission already granted, proceed to post the notification
                postNotification();

            } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {

                // The user previously denied the permission.
                // Show a custom UI explaining *why* you need the permission (the "menu").
                showPermissionRationaleDialog();

            } else {

                // First time requesting, or user checked "Don't ask again".
                // Directly launch the standard permission dialog.
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        } else {
            // Devices running below Android 13 (API < 33) don't need this runtime check.
            postNotification();
        }
    }

    private void showPermissionRationaleDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Notification Permission Needed")
                .setMessage("This app needs permission to send you alerts about potential climate emergencies. Please grant the notification permission.")
                .setPositiveButton("Continue", (dialog, which) -> {
                    // User agrees, launch the system permission request
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
                })
                .setNegativeButton("No Thanks", (dialog, which) -> {
                    dialog.dismiss();
                    Log.d("Permission", "User declined rationale.");
                })
                .show();
    }

    private void postNotification() {
        var CHANNEL_ID = "0";
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("textTitle")
                .setContentText("textContent")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        var NOTIFICATION_ID = 0;

        // You MUST check permission here again, as postNotification() can be called
        // from various places, including the permission result callback.
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // Log or handle the case where we tried to post without permission (shouldn't happen with the flow above)
            return;
        }

        NotificationManagerCompat.from(this).notify(NOTIFICATION_ID, builder.build());
        Log.d("Notification", "Notification posted successfully.");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_alert,R.id.navigation_guide, R.id.navigation_profile)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);

        createNotificationChannel();
        checkNotificationPermission();

        var CHANNEL_ID = "0";
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Title")
                .setContentText("Content")
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setAutoCancel(true)
                .setContentIntent(PendingIntent.getActivity(this, 0,
                        new Intent(this, MainActivity.class), PendingIntent.FLAG_IMMUTABLE));


        // Log.d("test", "This is a test");

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            // ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            // public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                        int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.

            Log.d("myTag", "This is my message");

            return;
        }

        var NOTIFICATION_ID = 0;
        NotificationManagerCompat.from(this).notify(NOTIFICATION_ID, builder.build());
    }

}