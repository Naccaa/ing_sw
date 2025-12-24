package com.example.ids.ui.guide;

import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.ids.R;
import com.example.ids.databinding.FragmentGuideBinding;
import com.google.android.material.card.MaterialCardView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class GuideFragment extends Fragment {

    private FragmentGuideBinding binding;
    private OkHttpClient client;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentGuideBinding.inflate(inflater, container, false);
        client = new OkHttpClient();

        fetchGuidelines();

        return binding.getRoot();
    }


    private void fetchGuidelines() {

        Request request = new Request.Builder()
                .url("http://10.0.2.2:5000/guidelines")
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("GUIDES", "Server down", e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response)
                    throws IOException {

                if (!response.isSuccessful()) return;


                String resp = response.body() != null ? response.body().string() : "[]";
                Log.d("response", resp);

                try {
                    JSONArray arr = new JSONArray(resp);

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
        });
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

        // Aggiungi container alla card
        card.addView(container);

        // Aggiungi card al layout principale
        binding.guidelinesContainer.addView(card);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
