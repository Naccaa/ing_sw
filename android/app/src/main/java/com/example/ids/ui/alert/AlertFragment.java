package com.example.ids.ui.alert;

import static com.example.ids.constants.Constants.BASE_URL;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.ids.R;
import com.example.ids.data.network.AuthInterceptor;
import com.example.ids.databinding.FragmentAlertBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.card.MaterialCardView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class AlertFragment extends Fragment {

    private FragmentAlertBinding binding;
    private OkHttpClient client;
    private FusedLocationProviderClient fusedLocationClient;

    private static final int LOCATION_PERMISSION_REQUEST = 1001;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentAlertBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        client = new OkHttpClient.Builder()
                .addInterceptor(new AuthInterceptor(requireContext()))
                .connectTimeout(3, TimeUnit.SECONDS)
                .readTimeout(3, TimeUnit.SECONDS)
                .build();

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        // Controlla permessi e ottieni posizione
        checkLocationPermissionAndFetchAlerts();


        return root;
    }

    private void checkLocationPermissionAndFetchAlerts() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Richiesta permesso
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST);
        } else {
            // Permessi già concessi
            getUserLocationAndFetch();
        }
    }

    private void getUserLocationAndFetch() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e("ALERTS", "Permesso posizione negato");
            fetchAlerts(0,0); // fallback
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        double lat = location.getLatitude();
                        double lon = location.getLongitude();
                        Log.d("ALERTS", "Lat: " + lat + " Lon: " + lon);
                        fetchAlerts(lat, lon);
                    } else {
                        Log.w("ALERTS", "Posizione non disponibile, uso valori di default");
                        fetchAlerts(0, 0);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("ALERTS", "Errore nel recupero posizione");
                    fetchAlerts(0, 0);
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getUserLocationAndFetch();
            } else {
                Log.w("ALERTS", "Permesso posizione negato, uso valori di default");
                fetchAlerts(0,0);
            }
        }
    }

    private void fetchAlerts(double lat, double lon) {
        Request request = new Request.Builder()
                .url(BASE_URL + "/emergencies?near=" + lat + "," + lon)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("ALERTS", "Server down, uso cache locale");
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                try {
                    String body = response.body() != null ? response.body().string() : "[]";
                    Log.d("ALERTS", "Alert ricevuti: " + body);


                    // Passa direttamente JSON alla funzione showAlerts
                    showAlerts(body);

                } catch (Exception e) {
                    Log.e("ALERTS", "Parse error");
                }
            }
        });
    }

    private void showAlerts(String json) {
        try {
            JSONArray arr;

            JSONObject root = new JSONObject(json);
            if (root.has("data")) {
                arr = root.getJSONArray("data");
            } else {
                arr = new JSONArray(); // fallback vuoto
            }

            if (isAdded()) {
                JSONArray finalArr = arr;
                requireActivity().runOnUiThread(() -> {
                    for (int i = 0; i < finalArr.length(); i++) {
                        JSONObject obj = null;
                        try {
                            obj = finalArr.getJSONObject(i);
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                        String endTime = null;
                        try {
                            endTime = obj.isNull("end_time") ? null : obj.getString("end_time");
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                        try {
                            createAllertCard(
                                    getString(R.string.title_alert) + " " + obj.getString("emergency_type"),
                                    obj.getString("message"),
                                    obj.getString("start_time"),
                                    endTime
                            );
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
            }

        } catch (JSONException e) {
            Log.e("ALERTS", "Parse error", e);
        }
    }




    private void createAllertCard(String emergencyType, String message, String start_time, String end_time) {
        int density = (int) getResources().getDisplayMetrics().density;
        MaterialCardView card = new MaterialCardView(requireContext());

        card.setRadius(20*density);
        card.setCardElevation(10f);
        int ldp = (int) (370 * getResources().getDisplayMetrics().density);

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                ldp,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(0, 30, 0, 16);
        card.setLayoutParams(cardParams);

        LinearLayout container = new LinearLayout(requireContext());
        container.setOrientation(LinearLayout.VERTICAL);

        int paddingPx = (int) (20 * getResources().getDisplayMetrics().density); // 20dp
        container.setPadding(paddingPx, paddingPx, paddingPx, paddingPx);

        LinearLayout header = new LinearLayout(requireContext());
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);

        ImageView icon = new ImageView(requireContext());
        icon.setImageResource(R.drawable.ic_alert);
        icon.setColorFilter(getResources().getColor(R.color.title_color, null));
        int iconSize = (int) (32 * getResources().getDisplayMetrics().density); // 32dp
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(iconSize, iconSize);
        icon.setLayoutParams(iconParams);

        TextView titleTv = new TextView(requireContext());
        titleTv.setText(emergencyType.toUpperCase());
        titleTv.setTextSize(18f);
        titleTv.setTextColor(getResources().getColor(R.color.title_color, null));
        titleTv.setTypeface(null, android.graphics.Typeface.BOLD);
        titleTv.setPadding((int)(14 * getResources().getDisplayMetrics().density),0,0,0);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        titleTv.setLayoutParams(titleParams);

        header.addView(icon);
        header.addView(titleTv);

        View divider = new View(requireContext());
        LinearLayout.LayoutParams dividerParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                (int)(1 * getResources().getDisplayMetrics().density) // 1dp
        );
        dividerParams.setMargins(0, 10, 0, 10);
        divider.setLayoutParams(dividerParams);
        divider.setBackgroundColor(0x33FFFFFF);
        TextView messageTv = new TextView(requireContext());
        messageTv.setText(message);
        messageTv.setTextSize(14f);
        messageTv.setTextColor(getResources().getColor(R.color.black, null));
        messageTv.setLineSpacing(0f, 1.2f);


        TextView dateTv = new TextView(requireContext());
        dateTv.setText(formatValidity(start_time, end_time));
        dateTv.setTextSize(12f);
        dateTv.setTextColor(getResources().getColor(R.color.black, null));

        LinearLayout.LayoutParams dateParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        dateParams.topMargin = (int) (10 * getResources().getDisplayMetrics().density);
        dateTv.setLayoutParams(dateParams);

        Button detailsBtn = new Button(requireContext());
        detailsBtn.setText("Vedi Dettagli");
        detailsBtn.setTextColor(getResources().getColor(android.R.color.white, null));
        detailsBtn.setBackgroundResource(R.drawable.bg_button);
        detailsBtn.setElevation(4f);

        TypedValue typedValue = new TypedValue();
        requireContext().getTheme().resolveAttribute(
                android.R.attr.selectableItemBackground,
                typedValue,
                true
        );

        detailsBtn.setForeground(requireContext().getDrawable(typedValue.resourceId));

        LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                (int) (48 * getResources().getDisplayMetrics().density)
        );
        btnParams.topMargin = (int) (9 * getResources().getDisplayMetrics().density);
        detailsBtn.setLayoutParams(btnParams);

        detailsBtn.setOnClickListener(v -> {
            Log.d("ALERTS", "Dettagli emergenza: " + emergencyType);
            showAlertDetails(emergencyType, message, start_time, end_time);
        });


        container.addView(header);
        container.addView(divider);
        container.addView(messageTv);
        container.addView(dateTv);
        container.addView(detailsBtn);


        card.addView(container);

        binding.allertContainer.addView(card);
    }


    private void showAlertDetails(String emergencyType, String message, String start_time, String end_time) {
        View dialogView = requireActivity()
                .getLayoutInflater().inflate(R.layout.alert_details, null);

        androidx.appcompat.app.AlertDialog dialog =
                new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                        .setView(dialogView)
                        .create();


        dialog.show();

    }

    private String formatValidity(String start, String end) {
        String formattedStart = formatDate(start);
        String formattedEnd = end == null || end.equals("null") ? "" : formatTime(end);

        if (formattedEnd.isEmpty()) {
            return "Validità: " + formattedStart;
        } else {
            return "Validità: " + formattedStart + " - " + formattedEnd;
        }
    }

    private String formatDate(String isoDate) {
        try {
            SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS", Locale.getDefault());
            Date date = isoFormat.parse(isoDate);

            SimpleDateFormat desiredFormat = new SimpleDateFormat("dd/MM/yyyy, HH:mm", Locale.getDefault());
            return desiredFormat.format(date);

        } catch (ParseException e) {
            e.printStackTrace();
            return isoDate; // fallback
        }
    }

    private String formatTime(String isoDate) {
        try {
            SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS", Locale.getDefault());
            Date date = isoFormat.parse(isoDate);

            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            return timeFormat.format(date);

        } catch (ParseException e) {
            e.printStackTrace();
            return isoDate; // fallback
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
