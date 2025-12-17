package com.example.ids.ui.profile;

import static android.content.Context.MODE_PRIVATE;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.ids.constants.Constants;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.ids.databinding.FragmentSettingsBinding;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import java.util.Map;

public class SettingsFragment extends Fragment {

    private FragmentSettingsBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        SettingsViewModel settingsViewModel =
                new ViewModelProvider(this).get(SettingsViewModel.class);

        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // User profile views
        TextView userFullname = binding.userFullname;
        TextView userPhone = binding.userPhone;
        TextView userEmail = binding.userEmail;
        Button btnModificaProfilo = binding.btnModificaProfilo;

        // Caregiver profile views
        TextView caregiverFullname = binding.caregiverFullname;
        TextView caregiverPhone = binding.caregiverPhone;
        TextView caregiverEmail = binding.caregiverEmail;
        Button btnModificaCaregiver = binding.btnModificaCaregiver;

        // Other buttons
        Button btnEliminaProfilo = binding.btnEliminaProfilo;
        Button btnLogout = binding.btnLogout;

        SharedPreferences prefs = requireActivity()
                .getSharedPreferences("app_prefs", MODE_PRIVATE);

        int user_id = 1;
        // Client obj used to make requests
        OkHttpClient client = new OkHttpClient();
        // Get the user information
        Request request = new Request.Builder()
                .url(Constants.BASE_URL + "/users/"+user_id)
                .addHeader("Authorization", "Bearer " + prefs.getString("session_token", null))
                .build();

        // Asynchronous request
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("Error", "Request failed", e);
                // Update the UI to show an error message
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Snackbar.make(view, "Error connecting to server", Snackbar.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                // Check if the response is successful
                if (response.isSuccessful()) {
                    String response_body_str = response.body().string();
                    Log.d("Response str", response_body_str);

                    // Use Gson to parse JSON into a Map
                    Gson gson = new Gson();
                    try {
                        Map<String, Object> response_data = gson.fromJson(response_body_str, new TypeToken<Map<String, Object>>(){}.getType());

                        final String fullname = (String) response_data.get("fullname");
                        final String phone_number = (String) response_data.get("phone_number");
                        final String email = (String) response_data.get("email");

                        // Update UI with user info
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (fullname != null) {
                                    Log.d("User fullname", fullname);
                                    userFullname.append(fullname);
                                } else
                                    Log.e("Error", "Fullname is null or not a valid String");

                                if (phone_number != null) {
                                    Log.d("User phone number", phone_number);
                                    userPhone.append(phone_number);
                                } else
                                    Log.e("Error", "Phone number is null or not a valid String");

                                if (email != null) {
                                    Log.d("User email", email);
                                    userEmail.append(email);
                                } else
                                    Log.e("Error", "Email is null or not a valid String");
                            }
                        });

                    } catch (JsonSyntaxException e) {
                        Log.e("Gson Error", "Failed to parse JSON: " + e.getMessage());
                    }
                } else {
                    // Log an error response code
                    Log.e("Error", "Request failed with code " + response.code());

                    // Update the UI to show an error message
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            userFullname.setText("Error loading user data.");
                            Snackbar.make(view, "Error loading user data.", Snackbar.LENGTH_LONG).show();
                        }
                    });
                }
            }
        });

        // Set up click listeners
        btnModificaProfilo.setOnClickListener(v -> {
        });

        btnModificaCaregiver.setOnClickListener(v -> {
        });

        btnEliminaProfilo.setOnClickListener(v -> {
            Snackbar.make(view, "Profilo eliminato", Snackbar.LENGTH_LONG).show();

            /*
            Request request = new Request.Builder()
            .url("https://jsonplaceholder.typicode.com/posts/1")  // URL for the resource to delete
            .delete()  // DELETE request
            .build();

            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    Log.d("Response", "Delete successful!");
                } else {
                    Log.e("Error", "Delete failed with code " + response.code());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            */
        });

        btnLogout.setOnClickListener(v -> {
            Snackbar.make(view, "Log out", Snackbar.LENGTH_LONG).show();
            // Add your logic for logout here
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
