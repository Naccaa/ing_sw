package com.example.ids.ui.guide;

import static android.content.Context.MODE_PRIVATE;

import static com.example.ids.constants.Constants.BASE_URL;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.ids.R;
import com.example.ids.data.network.AuthInterceptor;
import com.example.ids.databinding.FragmentGuideBinding;
import com.google.android.material.card.MaterialCardView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;




public class GuideFragment extends Fragment {

    private FragmentGuideBinding binding;
    private OkHttpClient client;
    private String jwtToken = "";


    private boolean isAdmin = false;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentGuideBinding.inflate(inflater, container, false);

        SharedPreferences prefs = requireActivity().getSharedPreferences("app_prefs", MODE_PRIVATE);
        isAdmin = prefs.getBoolean("is_admin", false);

        if (isAdmin) {
            binding.addGuideButton.setVisibility(View.VISIBLE);
            binding.addGuideButton.setOnClickListener(v -> {
                showGuideDialog(null, null, null, null);
            });
        }
        jwtToken = prefs.getString("session_token", "");
        client = new OkHttpClient.Builder().addInterceptor(new AuthInterceptor(requireContext()))
                .connectTimeout(300, TimeUnit.MILLISECONDS)
                .readTimeout(300, TimeUnit.MILLISECONDS)
                .build();

        fetchGuidelines();
        return binding.getRoot();
    }

    private void showGuideDialog(
            String initialType,
            String initialMessage,
            TextView titleTv,
            TextView contentTv
    ) {
        View dialogView = requireActivity()
                .getLayoutInflater()
                .inflate(R.layout.dialog_add_guide, null);

        androidx.appcompat.app.AlertDialog dialog =
                new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                        .setView(dialogView)
                        .create();

        dialog.show();
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.bg_card);

        android.widget.EditText inputType = dialogView.findViewById(R.id.inputType);
        android.widget.EditText inputMessage = dialogView.findViewById(R.id.inputMessage);
        android.widget.Button btnCreate = dialogView.findViewById(R.id.btnCreateGuide);
        android.widget.Button btnCancel = dialogView.findViewById(R.id.btnCancelGuide);

        if (initialType != null) inputType.setText(initialType);
        if (initialMessage != null) inputMessage.setText(initialMessage);

        btnCreate.setText(initialType == null ? "Crea" : "Salva");

        btnCreate.setOnClickListener(v -> {
            String type = inputType.getText().toString().trim();
            String message = inputMessage.getText().toString().trim();

            if (type.isEmpty() || message.isEmpty()) {
                Toast.makeText(requireContext(),
                        "Compila tutti i campi",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            // Modifica guida
            if (titleTv != null && contentTv != null) {
                titleTv.setText(type.toUpperCase());
                contentTv.setText(message);
                updateGuide(type.toLowerCase(), message);
            }
            // Crea guida
            else {
                createGuideCard(type, message);
                postNewGuide(type, message);
            }

            dialog.dismiss();
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
    }


    // Ottieni tutte le guide dal server
    private void fetchGuidelines() {

        Request request = new Request.Builder()
                .url(BASE_URL+"/guidelines")
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("GUIDES", "Server down, uso cache locale");
                String localJson = readGuidelinesLocally();
                Log.d("localJson", localJson);
                showGuidelines(localJson);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response)
                    throws IOException {

                if (!response.isSuccessful()) return;

                try {
                    String body = response.body() != null ? response.body().string() : "[]";

                    JSONArray data;
                    if (body.startsWith("[")) {
                        // Caso: [] diretto
                        data = new JSONArray(body);
                    } else {
                        // Caso: { data: [...] }
                        JSONObject root = new JSONObject(body);
                        data = root.optJSONArray("data");
                    }

                    if (data == null || data.length() == 0) {
                        // Nessuna guida, mostro del testo per comunicarlo all'utente
                        if (isAdded()) {
                            requireActivity().runOnUiThread(() -> {
                                binding.guidelinesContainer.removeAllViews();
                                binding.emptyGuidelinesText.setVisibility(View.VISIBLE);
                            });
                        }
                        return;
                    }

                    String resp;
                    if (body.trim().startsWith("[")) {
                        resp = new JSONArray(body).toString();
                    } else {
                        JSONObject obj = new JSONObject(body);
                        resp = obj.getJSONArray("data").toString();
                    }
                    // String resp = response.body() != null ? response.body().string() : "[]";
                    Log.d("response", resp);
                    Log.d("is_admin", String.valueOf(isAdmin));
                    saveGuidelinesLocally(resp);
                    showGuidelines(resp);

                    if (isAdded()) {
                        requireActivity().runOnUiThread(() -> {
                            binding.guidelinesContainer.removeAllViews();
                            showGuidelines(resp);
                        });
                    }
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    private void showGuidelines(String json) {
        try {
            JSONArray arr = new JSONArray(json);

            if (isAdded()) {
                requireActivity().runOnUiThread(() -> {
                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject obj = null;
                        try {
                            obj = arr.getJSONObject(i);
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                        try {
                            createGuideCard(
                                    obj.getString("emergency_type"),
                                    obj.getString("message")
                            );
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
            }

        } catch (Exception e) {
            Log.e("GUIDES", "Parse error", e);
        }
    }

    private void saveGuidelinesLocally(String json) {
        try (FileOutputStream fos = requireContext().openFileOutput("guidelines.json", Context.MODE_PRIVATE)) {
            fos.write(json.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("GUIDES", "Errore nel salvare le guide localmente", e);
        }
    }
    private String readGuidelinesLocally() {
        try (FileInputStream fis = requireContext().openFileInput("guidelines.json")) {
            int size = fis.available();
            byte[] buffer = new byte[size];
            fis.read(buffer);
            return new String(buffer);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("GUIDES", "Errore nel leggere le guide localmente", e);
            return "[]"; // ritorna array vuoto se non esiste il file
        }
    }

    // Crea una card per la guida segue un layout dinamico
    private void createGuideCard(String emergencyType, String message) {
        // Card
        int density = (int) getResources().getDisplayMetrics().density;
        MaterialCardView card = new MaterialCardView(requireContext());

        card.setRadius(20*density);
        card.setCardElevation(8f);
        int ldp = (int) (370 * getResources().getDisplayMetrics().density);

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                ldp,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(0, 30, 0, 16);
        card.setLayoutParams(cardParams);
        card.setClickable(true);
        card.setFocusable(true);


        // Container verticale interno con padding
        LinearLayout container = new LinearLayout(requireContext());
        container.setOrientation(LinearLayout.VERTICAL);
        int paddingPx = (int) (20 * getResources().getDisplayMetrics().density); // 20dp
        container.setPadding(paddingPx, paddingPx, paddingPx, paddingPx);

        // Header
        LinearLayout header = new LinearLayout(requireContext());
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);

        // Icona guida
        ImageView icon = new ImageView(requireContext());
        icon.setImageResource(R.drawable.ic_guide);
        icon.setColorFilter(getResources().getColor(R.color.title_color, null));
        int iconSize = (int) (32 * getResources().getDisplayMetrics().density); // 32dp
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(iconSize, iconSize);
        icon.setLayoutParams(iconParams);

        // Titolo (emergency type)
        TextView titleTv = new TextView(requireContext());
        titleTv.setText(emergencyType.toUpperCase());
        titleTv.setTextSize(18f);
        titleTv.setTextColor(getResources().getColor(R.color.title_color, null));
        titleTv.setTypeface(null, android.graphics.Typeface.BOLD);
        titleTv.setPadding((int)(14 * getResources().getDisplayMetrics().density),0,0,0);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f
        );
        titleTv.setLayoutParams(titleParams);

        // Freccia espandi
        ImageView arrow = new ImageView(requireContext());
        arrow.setImageResource(R.drawable.down_arrow);
        arrow.setColorFilter(getResources().getColor(R.color.title_color, null));
        int arrowSize = (int) (20 * getResources().getDisplayMetrics().density);
        LinearLayout.LayoutParams arrowParams = new LinearLayout.LayoutParams(arrowSize, arrowSize);
        arrow.setLayoutParams(arrowParams);

        // Aggiungi elementi all'header
        header.addView(icon);
        header.addView(titleTv);
        header.addView(arrow);

        // Divisore
        View divider = new View(requireContext());
        LinearLayout.LayoutParams dividerParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                (int)(1 * getResources().getDisplayMetrics().density) // 1dp
        );
        dividerParams.setMargins(0, 10, 0, 10);
        divider.setLayoutParams(dividerParams);
        divider.setBackgroundColor(0x33FFFFFF);

        // Contenuto espandibile
        TextView content = new TextView(requireContext());
        content.setText(message);
        content.setTextSize(14f);
        content.setTextColor(getResources().getColor(R.color.black, null));
        content.setLineSpacing(2f, 1f);
        content.setVisibility(View.GONE);

        // Click listener per espandere
        final boolean[] expanded = {false};
        header.setOnClickListener(v -> {
            expanded[0] = !expanded[0];
            content.setVisibility(expanded[0] ? View.VISIBLE : View.GONE);
            arrow.setRotation(expanded[0] ? 180f : 0f);
        });

        // Aggiungi elementi al container
        container.addView(header);
        container.addView(divider);
        container.addView(content);


        if(isAdmin) {
            LinearLayout adminButtons = new LinearLayout(requireContext());
            adminButtons.setOrientation(LinearLayout.HORIZONTAL);
            adminButtons.setGravity(Gravity.END);
            adminButtons.setPadding(0,10,0,0);

            TextView editBtn = new TextView(requireContext());
            editBtn.setText("Modifica");
            editBtn.setTextColor(getResources().getColor(R.color.title_color, null));
            editBtn.setPadding(20,0,20,0);
            editBtn.setOnClickListener(v ->
                    showGuideDialog(
                            titleTv.getText().toString(),
                            content.getText().toString(),
                            titleTv,
                            content
                    )
            );

            TextView deleteBtn = new TextView(requireContext());
            deleteBtn.setText("Elimina");
            deleteBtn.setTextColor(getResources().getColor(R.color.title_color, null));
            deleteBtn.setPadding(20,0,20,0);
            deleteBtn.setOnClickListener(v -> {
                new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                        .setTitle("Conferma")
                        .setMessage("Vuoi eliminare questa guida?")
                        .setPositiveButton("Sì", (dialog, which) -> deleteGuide(emergencyType))
                        .setNegativeButton("No", null)
                        .show();
            });

            adminButtons.addView(editBtn);
            adminButtons.addView(deleteBtn);

            container.addView(adminButtons);
        }

        // Aggiungi container alla card
        card.addView(container);

        // Aggiungi card al layout principale
        binding.guidelinesContainer.addView(card);
    }

    private void postNewGuide(String type, String message) {
        Log.d("JWT Token", jwtToken);
        JSONObject json = new JSONObject();
        try {
            json.put("emergency_type", type);
            json.put("message", message);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        RequestBody body = RequestBody.create(
                json.toString(),
                okhttp3.MediaType.parse("application/json; charset=utf-8")
        );

        Request request = new Request.Builder()
                .url(BASE_URL+"/guidelines")
                .addHeader("Authorization", "Bearer " + jwtToken)
                .post(body)
                .build();


        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("GUIDES", "POST failed", e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    Log.d("GUIDES", "Guideline created");
                    fetchGuidelines(); // aggiorna UI
                } else {
                    Log.e("GUIDES", "POST error: " + response.code());
                }
            }
        });
    }


    private void updateGuide(String type, String message) {
        JSONObject json = new JSONObject();
        try {
            json.put("message", message); // non cambiare type
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        RequestBody body = RequestBody.create(
                json.toString(),
                okhttp3.MediaType.parse("application/json; charset=utf-8")
        );
        Log.d("GUIDES", "PUT body: " + json.toString());

        Request request = new Request.Builder()
                .url(BASE_URL+"/guidelines/" + type)
                .addHeader("Authorization", "Bearer " + jwtToken)
                .put(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("GUIDES", "PUT failed", e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    Log.d("GUIDES", "Guideline updated");
                    requireActivity().runOnUiThread(() -> {
                        // Aggiorna UI live
                        fetchGuidelines();
                    });
                } else {
                    Log.e("GUIDES", "PUT error: " + response.code());
                }
            }
        });
    }

    private void deleteGuide(String type) {
        Request request = new Request.Builder()
                .url(BASE_URL+"/guidelines/" + type)
                .addHeader("Authorization", "Bearer " + jwtToken)
                .delete()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("GUIDES", "DELETE failed", e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    Log.d("GUIDES", "Guideline deleted");
                    requireActivity().runOnUiThread(() -> fetchGuidelines());
                } else {
                    Log.e("GUIDES", "DELETE error: " + response.code());
                }
            }
        });
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
