package com.example.ids.ui.alert;

import static android.content.Context.MODE_PRIVATE;
import static com.example.ids.constants.Constants.BASE_URL;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.ids.R;
import com.example.ids.constants.Constants;
import com.example.ids.data.network.AuthInterceptor;
import com.example.ids.databinding.FragmentAlertBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.card.MaterialCardView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AlertFragment extends Fragment {

    private FragmentAlertBinding binding;
    private OkHttpClient client;
    private FusedLocationProviderClient fusedLocationClient;
    private boolean isAdmin = false;
    private String jwtToken = "";
    private static boolean showActive = false;


    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    getUserLocationAndFetch();
                } else {
                    Log.w("ALERTS", "Permesso posizione negato, uso valori di default");
                    fetchAlerts(0, 0);
                }
            });

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentAlertBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        client = new OkHttpClient.Builder()
                .addInterceptor(new AuthInterceptor(requireContext()))
                .connectTimeout(300, TimeUnit.MILLISECONDS)
                .readTimeout(300, TimeUnit.MILLISECONDS)
                .build();

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        SharedPreferences prefs = requireActivity().getSharedPreferences("app_prefs", MODE_PRIVATE);
        isAdmin = prefs.getBoolean("is_admin", false);
        jwtToken = prefs.getString("session_token", "");


        if (isAdmin) {
            // Mostriamo la card che contiene lo switch
            binding.switchCard.setVisibility(View.VISIBLE);

            binding.manageAlertsSwitch.setChecked(showActive);
            binding.manageAlertsSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                showActive = isChecked;

                // Feedback aptico (vibrazione leggera) se vuoi un tocco di classe
                buttonView.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY);

                binding.alertContainer.removeAllViews();
                getUserLocationAndFetch();
            });
        }

        binding.swipeRefreshLayout.setOnRefreshListener(() -> {
            binding.alertContainer.removeAllViews();
            getUserLocationAndFetch();
        });

        checkLocationPermissionAndFetchAlerts();

        return root;
    }

    private void checkLocationPermissionAndFetchAlerts() {
        if (ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getUserLocationAndFetch();
        } else {

            if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                new AlertDialog.Builder(requireContext())
                        .setTitle("Permesso Posizione")
                        .setMessage("La posizione è necessaria per mostrare gli avvisi nella tua area")
                        .setPositiveButton("OK", (dialog, which) ->
                                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION))
                        .setNegativeButton("Annulla", (dialog, which) -> fetchAlerts(0, 0))
                        .show();
            } else {
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
            }
        }
    }

    private void getUserLocationAndFetch() {
        if(isAdmin && showActive){
            Log.d("ALERTS", "Fetching all active alerts");
            fetchAllActiveAlerts();
            return;
        }
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e("ALERTS", "Permesso posizione non presente");
            fetchAlerts(0, 0);
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        double lon = location.getLongitude();
                        double lat = location.getLatitude();
                        Log.d("ALERTS", "Lon: " + lon + " Lat: " + lat);
                        fetchAlerts(lon, lat);
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

    private void fetchAllActiveAlerts() {
        Request request = new Request.Builder()
                .url(BASE_URL + "/emergencies")
                .get()
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> {
                        binding.swipeRefreshLayout.setRefreshing(false);
                        binding.alertContainer.removeAllViews();
                        binding.emptyAlertsText.setText("Impossibile collegarsi al server.\nControlla la connessione e riprova.");
                        binding.emptyAlertsText.setVisibility(View.VISIBLE);
                        Toast.makeText(requireContext(), "Errore di rete", Toast.LENGTH_SHORT).show();
                    });
                }

            }
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> binding.swipeRefreshLayout.setRefreshing(false));
                }
                try {
                    String body = response.body() != null ? response.body().string() : "[]";
                    showAlerts(body,showActive);
                } catch (Exception e) {
                    Log.e("All active ALERTS", "Parse error");
                }
            }
        });
    }

    private void fetchAlerts(double lon, double lat) {
        Request request = new Request.Builder()
                .url(BASE_URL + "/emergencies?near=" + lon + "," + lat)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> {
                        binding.swipeRefreshLayout.setRefreshing(false);
                        binding.alertContainer.removeAllViews();
                        binding.emptyAlertsText.setText("Impossibile collegarsi al server.\nControlla la connessione e riprova.");
                        binding.emptyAlertsText.setVisibility(View.VISIBLE);
                        Toast.makeText(requireContext(), "Errore di rete", Toast.LENGTH_SHORT).show();
                    });
                }
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> binding.swipeRefreshLayout.setRefreshing(false));
                }
                try {
                    String body = response.body() != null ? response.body().string() : "[]";
                    showAlerts(body, showActive);
                } catch (Exception e) {
                    Log.e("ALERTS", "Parse error");
                }
            }
        });
    }

    private void showAlerts(String json, boolean showActive) {
        try {
            JSONObject root = new JSONObject(json);
            JSONArray arr = root.optJSONArray("data");
            if (arr == null) arr = new JSONArray();

            if (isAdded()) {
                JSONArray finalArr = arr;
                requireActivity().runOnUiThread(() -> {
                    binding.alertContainer.removeAllViews();
                    int displayedCount = 0;

                    // Formato ISO (tronchiamo mentalmente i microsecondi durante il parse)
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                    Date adesso = new Date();

                    for (int i = 0; i < finalArr.length(); i++) {
                        try {
                            JSONObject obj = finalArr.getJSONObject(i);
                            String endTimeStr = obj.isNull("end_time") || obj.getString("end_time").equals("null") ? null : obj.getString("end_time");

                            boolean shouldShow = false;

                            if (endTimeStr == null) {
                                // Se non c'è end_time, l'allerta è sempre attiva
                                shouldShow = true;
                            } else if (!showActive) {
                                // Se showActive è false, mostriamo anche quelle terminate di recente (max 15 min)
                                try {
                                    Date dataFine = sdf.parse(endTimeStr);
                                    if (dataFine != null) {
                                        long diffInMs = adesso.getTime() - dataFine.getTime();
                                        long diffInMinutes = TimeUnit.MILLISECONDS.toMinutes(diffInMs);

                                        // Mostra se terminata da meno di 15 minuti
                                        if (diffInMinutes <= 15) {
                                            shouldShow = true;
                                        }
                                    }
                                } catch (Exception e) {
                                    Log.e("ALERTS", "Errore data");
                                }
                            }

                            if (shouldShow) {
                                displayedCount++;
                                createAllertCard(
                                        getString(R.string.title_alert) + " " + obj.getString("emergency_type"),
                                        obj.getString("message"),
                                        obj.getString("start_time"),
                                        endTimeStr,
                                        obj.optString("location", null),
                                        obj.optString("guideline_message", null),
                                        obj.optDouble("radius", 0),
                                        obj.optInt("id", -1)
                                );
                            }
                        } catch (JSONException e) {
                            Log.e("ALERTS", "Errore JSON");
                        }
                    }
                    binding.emptyAlertsText.setVisibility(displayedCount == 0 ? View.VISIBLE : View.GONE);
                });
            }
        } catch (JSONException e) {
            Log.e("ALERTS", "Parse error");
        }
    }

    private void createAllertCard(String emergencyType, String message, String start_time, String end_time,
                                  @Nullable String location, @Nullable String guideline, double radius, int alertId) {

        int density = (int) getResources().getDisplayMetrics().density;
        MaterialCardView card = new MaterialCardView(requireContext());
        card.setRadius(20 * density);
        card.setCardElevation(10f);
        int ldp = (int) (370 * getResources().getDisplayMetrics().density);

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(ldp, ViewGroup.LayoutParams.WRAP_CONTENT);
        cardParams.setMargins(0, 30, 0, 16);
        card.setLayoutParams(cardParams);

        LinearLayout container = new LinearLayout(requireContext());
        container.setOrientation(LinearLayout.VERTICAL);
        int paddingPx = (int) (20 * getResources().getDisplayMetrics().density);
        container.setPadding(paddingPx, paddingPx, paddingPx, paddingPx);

        LinearLayout header = new LinearLayout(requireContext());
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);

        ImageView icon = new ImageView(requireContext());
        icon.setImageResource(R.drawable.ic_alert);
        icon.setColorFilter(ContextCompat.getColor(requireContext(), R.color.title_color));
        int iconSize = (int) (32 * density);
        header.addView(icon, new LinearLayout.LayoutParams(iconSize, iconSize));

        TextView titleTv = new TextView(requireContext());
        titleTv.setText(emergencyType.toUpperCase());
        titleTv.setTextSize(18f);
        titleTv.setTextColor(ContextCompat.getColor(requireContext(), R.color.title_color));
        titleTv.setTypeface(null, android.graphics.Typeface.BOLD);
        titleTv.setPadding((int) (14 * density), 0, 0, 0);
        header.addView(titleTv, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        container.addView(header);

        View divider = new View(requireContext());
        LinearLayout.LayoutParams dividerParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) density);
        dividerParams.setMargins(0, 10, 0, 10);
        divider.setBackgroundColor(0x33FFFFFF);
        container.addView(divider, dividerParams);

        TextView messageTv = new TextView(requireContext());
        messageTv.setText(message);
        messageTv.setTextSize(14f);
        messageTv.setTextColor(ContextCompat.getColor(requireContext(), R.color.black));

        messageTv.setMaxLines(3);
        messageTv.setEllipsize(TextUtils.TruncateAt.END);

        container.addView(messageTv);

        TextView dateTv = new TextView(requireContext());
        dateTv.setText(formatValidity(start_time, end_time));
        dateTv.setTextSize(12f);
        dateTv.setTextColor(ContextCompat.getColor(requireContext(), R.color.black));
        LinearLayout.LayoutParams dateParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dateParams.topMargin = (int) (10 * density);
        container.addView(dateTv, dateParams);

        Button detailsBtn = new Button(requireContext());
        detailsBtn.setText(R.string.alert_details_button_str);
        detailsBtn.setBackgroundResource(R.drawable.bg_button);
        detailsBtn.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white));
        LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) (55 * density));
        btnParams.topMargin = (int) (15 * density);
        detailsBtn.setOnClickListener(v -> showAlertDetails(emergencyType, message, start_time, end_time, location, guideline, radius));
        container.addView(detailsBtn, btnParams);

        if (isAdmin && end_time == null && showActive) {
            Button endBtn = new Button(requireContext());
            endBtn.setText("Termina");
            endBtn.setBackgroundResource(R.drawable.bg_button);
            endBtn.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white));
            LinearLayout.LayoutParams endParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) (55 * density));
            endParams.topMargin = (int) (9 * density);
            endBtn.setOnClickListener(v -> {
                endBtn.setEnabled(false);
                String currentTime = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS", Locale.getDefault()).format(new Date());
                updateAlertEndTimeOnServer(alertId, currentTime, start_time, emergencyType, message, location, radius,
                        () -> {
                            dateTv.setText(formatValidity(start_time, currentTime));
                            endBtn.setVisibility(View.GONE);
                            Toast.makeText(requireContext(), "Allerta terminata", Toast.LENGTH_SHORT).show();
                        },
                        () -> endBtn.setEnabled(true));
            });
            container.addView(endBtn, btnParams);
        }

        card.addView(container);
        binding.alertContainer.addView(card);
    }

    private void showAlertDetails(String emergencyType, String message, String startTime, String endTime, String location, String guidelineMessage, double radius) {
        View view = getLayoutInflater().inflate(R.layout.alert_details, null);
        ((TextView) view.findViewById(R.id.tvTitle)).setText(emergencyType.toUpperCase());
        ((TextView) view.findViewById(R.id.alertDate)).setText(formatValidity(startTime, endTime));
        ((TextView) view.findViewById(R.id.alertMessage)).setText(message);
        ((TextView) view.findViewById(R.id.guidelineMessage)).setText(!Objects.equals(guidelineMessage, "null") ? "• " + guidelineMessage : "Nessuna raccomandazione.");

        List<String> cities = getInvolvedCities(location, radius);
        ((TextView) view.findViewById(R.id.alertZones)).setText(cities.isEmpty() ? "Nessuna città coinvolta" : "• " + TextUtils.join("\n• ", cities));

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(view)
                .setPositiveButton("Chiudi", null)
                .create();

        dialog.show();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(R.drawable.bg_card);
        }

    }

    private String formatValidity(String start, String end) {
        String formattedStart = formatDate(start);
        String formattedEnd = end == null || end.equals("null") ? "in corso" : formatTime(end);
        return formattedEnd.isEmpty() ? "Validità: " + formattedStart : "Validità: " + formattedStart + " - " + formattedEnd;
    }

    private List<String> getInvolvedCities(String location, double radiusKm) {
        List<String> result = new ArrayList<>();
        if (location == null || location.isEmpty()) return result;
        try {
            String[] parts = location.split(",");
            double alertLon = Double.parseDouble(parts[0].trim());
            double alertLat = Double.parseDouble(parts[1].trim());
            double radiusDegrees = radiusKm * 0.009;
            for (Map.Entry<String, double[]> cityEntry : Constants.CITIES.entrySet()) {
                double dist = Math.sqrt(Math.pow(alertLat - cityEntry.getValue()[1], 2) + Math.pow(alertLon - cityEntry.getValue()[0], 2));
                if (dist <= radiusDegrees) result.add(cityEntry.getKey());
            }
        } catch (Exception e) { Log.e("ALERTS", "Errore calcolo città", e); }
        return result;
    }

    private String formatDate(String isoDate) {
        try {
            Date date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS", Locale.getDefault()).parse(isoDate);
            assert date != null;
            return new SimpleDateFormat("dd/MM/yyyy, HH:mm", Locale.getDefault()).format(date);
        } catch (Exception e) { return isoDate; }
    }

    private String formatTime(String isoDate) {
        try {
            Date date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS", Locale.getDefault()).parse(isoDate);

            assert date != null;
            return new SimpleDateFormat("HH:mm", Locale.getDefault()).format(date);
        } catch (Exception e) { return isoDate; }
    }

    private void updateAlertEndTimeOnServer(int alertId, String endTime, String start_time, String type, String message, String location, double radius, Runnable onSuccess, Runnable onError) {
        try {
            String[] parts = location.split(",");
            double lon = Double.parseDouble(parts[0].trim());
            double lat = Double.parseDouble(parts[1].trim());
            JSONObject json = new JSONObject();
            json.put("emergency_type", type.toLowerCase());
            json.put("message", message);
            json.put("location", new JSONArray().put(lon).put(lat));
            json.put("radius", radius);
            json.put("start_time", start_time);
            json.put("end_time", endTime);

            RequestBody body = RequestBody.create(json.toString(), MediaType.get("application/json; charset=utf-8"));
            Request request = new Request.Builder()
                    .url(BASE_URL + "/emergencies/" + alertId)
                    .addHeader("Authorization", "Bearer" + jwtToken)
                    .patch(body).build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    if (isAdded()) requireActivity().runOnUiThread(onError);
                }
                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) {
                    if (isAdded()) requireActivity().runOnUiThread(() -> {
                        if (response.isSuccessful()) onSuccess.run();
                        else onError.run();
                    });
                }
            });
        } catch (Exception e) { onError.run(); }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}