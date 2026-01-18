package com.example.ids;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.app.AlertDialog;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.util.Log;
import android.widget.Toast;

import com.example.ids.constants.Constants;
import com.example.ids.data.network.AuthInterceptor;

import com.example.ids.data.session.SessionEventBus;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;


import com.example.ids.databinding.ActivityMainBinding;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private Snackbar currentSnackbar;
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {});

    private String mToken;

    public void requestNotificationPermission() {
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //schedulePingWorker();


        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        BottomNavigationView navView = findViewById(R.id.nav_view);

        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_alert,
                R.id.navigation_guide,
                R.id.navigation_profile,
                R.id.navigation_login,
                R.id.navigation_forgotPassword,
                R.id.navigation_registration,
                R.id.infoFragment
        ).build();

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);

        // Handle bottom nav clicks to pop info fragment if needed
        binding.navView.setOnItemSelectedListener(item -> {
            if (navController.getCurrentDestination().getId() == R.id.infoFragment) {
                navController.popBackStack();
            }
            return NavigationUI.onNavDestinationSelected(item, navController) || super.onSupportNavigateUp();
        });

        // Nascondi BottomNavigationView sul LoginFragment
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            if (destination.getId() == R.id.navigation_login || destination.getId() == R.id.navigation_forgotPassword || destination.getId() == R.id.navigation_registration || destination.getId() == R.id.navigation_onboarding || destination.getId() == R.id.termsFragment) {
                binding.navView.setVisibility(View.GONE);
            } else {
                binding.navView.setVisibility(View.VISIBLE);
            }
        });

        // Check onboarding
        final var onboardingCompleted = getSharedPreferences("app_prefs", MODE_PRIVATE)
                .getBoolean("onboarding_completed", false);

        if (!onboardingCompleted) {
            navController.navigate(R.id.navigation_onboarding);
        }

        final var sessionToken = getSharedPreferences("app_prefs", MODE_PRIVATE)
                .getString("session_token", null);

        final var userId = getSharedPreferences("app_prefs", MODE_PRIVATE)
                .getString("user_id", "");

        if (sessionToken != null && !userId.isEmpty()) {
            send_firebase_token(this, sessionToken, userId);
        }

        // Observe for session expiration
        SessionEventBus.sessionExpired.observe(this, expired -> {
            if (Boolean.TRUE.equals(expired)) {
                Log.d("Session", "Session expired, redirect to login page");
                // Redirect to login")
                runOnUiThread(()-> {
                    try {
                        // Dismiss any currently showing Snackbar
                        if (currentSnackbar != null && currentSnackbar.isShown()) {
                            currentSnackbar.dismiss();
                        }
                        // Delay slightly to ensure it appears on top of any fragment Snackbars
                        new Handler(Looper.getMainLooper()).postDelayed(() -> {
                            currentSnackbar = Snackbar.make(
                                    findViewById(android.R.id.content), // attach to activity root
                                    "La sessione è scaduta, effettua il login di nuovo",
                                    Snackbar.LENGTH_LONG
                            );
                            currentSnackbar.show();
                        }, 200); // 200ms delay is enough

                        NavController navController_ =
                                Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);

                        navController_.navigate(R.id.navigation_login, null,
                                new NavOptions.Builder()
                                        .setPopUpTo(R.id.mobile_navigation, true) // clears back stack
                                        .build()
                        );
                    } catch (IllegalArgumentException | IllegalStateException e) {
                        Log.e("SessionLogout", "Navigation failed: " + e.getMessage());
                    }
                });

                SessionEventBus.sessionExpired.setValue(false);
            }
        });

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    100);
        }

        // per le notifiche
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                mToken = task.getResult();
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    startLocationService();
                }
            }
        });
    }

    private void startLocationService() {
        Intent serviceIntent = new Intent(this, LocationService.class);
        serviceIntent.putExtra("firebase_token", mToken);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }
    }
/*
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
    }*/

    public static void send_firebase_token(Context context, String sessionToken, String userId) {
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

                    OkHttpClient client = new OkHttpClient.Builder().addInterceptor(new AuthInterceptor(context)).build();

                    MediaType JSON = MediaType.get("application/json; charset=utf-8");

                    JSONObject jsonObject = new JSONObject();
                    try {
                        jsonObject.put("firebase_token", token);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        return;
                    }

                    RequestBody body = RequestBody.create(
                            jsonObject.toString(),
                            JSON
                    );

                    Request request = new Request.Builder()
                            .url(Constants.BASE_URL + "/users/" + userId) // per testare uso l'IP locale della macchina che hosta il backend
                            .patch(body)
                            .build();

                    client.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                                Log.e("Registration", "Errore di rete", e);
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            if (response.isSuccessful()) {
                                Log.d(TAG, "Firebase token inviato.");

                            }

                            Log.d(TAG, response.toString());

                        }
                    });
                }
            });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        
        if (item.getItemId() == R.id.action_info) {
            // Check if we're not already on the info screen
            if (navController.getCurrentDestination().getId() != R.id.infoFragment) {
                navController.navigate(R.id.infoFragment);
                return true;
            } else {
                // If already on info, pop back to the previous destination
                navController.popBackStack();
                return true;
            }
        }
        
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        return navController.navigateUp() || super.onSupportNavigateUp();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 100) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (mToken != null) {
                    startLocationService();
                }
            }
        }
    }
}
