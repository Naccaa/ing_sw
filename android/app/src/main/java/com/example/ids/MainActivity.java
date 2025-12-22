package com.example.ids;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
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
import com.google.firebase.messaging.FirebaseMessaging;
import com.example.ids.ui.login.LoginFragment;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {});

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU
                || ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        ) {
            return;
        }

        if( shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
            new AlertDialog.Builder(this)
                .setTitle("Autorizzazione Notifiche Necessaria")
                .setMessage("Quest'app necessita il permesso di inviarti notifiche riguardanti le emergenze climatiche in corso.")
                .setNegativeButton("NO GRAZIE", (dialog, which) -> dialog.dismiss())
                .setPositiveButton("OK", (dialog, which) -> {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
                    dialog.dismiss();
                })
                .show();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
        }
    }

    private void createNotificationChannel(String channelId) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            getSystemService(NotificationManager.class).createNotificationChannel(new NotificationChannel(channelId, "Emergenze", NotificationManager.IMPORTANCE_HIGH));
        }
    }

    private void postNotification(String channelId, int notificationId) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            NotificationManagerCompat.from(this).notify(notificationId, new NotificationCompat.Builder(this, channelId)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle("Titolo")
                    .setContentText("Testo")
                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    .setAutoCancel(true)
                    .setContentIntent(PendingIntent.getActivity(this, 0,
                            new Intent(this, MainActivity.class), PendingIntent.FLAG_IMMUTABLE))
                    .build());

            Log.d("Notification", "Notification posted successfully.");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        BottomNavigationView navView = findViewById(R.id.nav_view);

        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_alert,
                R.id.navigation_guide,
                R.id.navigation_settings,
                R.id.navigation_login,
                R.id.navigation_forgotPassword,
                R.id.navigation_registration
                ).build();

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);

        // Nascondi BottomNavigationView sul LoginFragment
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            if (destination.getId() == R.id.navigation_login || destination.getId() == R.id.navigation_forgotPassword || destination.getId() == R.id.navigation_registration) {
                binding.navView.setVisibility(View.GONE);
            } else {
                binding.navView.setVisibility(View.VISIBLE);
            }
        });
        

        FirebaseMessaging.getInstance().getToken()
            .addOnCompleteListener(new OnCompleteListener<String>() {
                @Override
                public void onComplete(@NonNull Task<String> task) {
                  if (!task.isSuccessful()) {
                    Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                    return;
                  }

                  String token = task.getResult();

                  Log.d(TAG, token);
                  Toast.makeText(MainActivity.this, token, Toast.LENGTH_SHORT).show();
                  // TODO: PATCH /users/:userId con token
                }
            });
    
        requestNotificationPermission();
        var channelId = "0";
        createNotificationChannel(channelId);
        postNotification(channelId, 0);
    }
}
