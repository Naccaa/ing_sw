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

import com.example.ids.R;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.ids.databinding.FragmentSettingsBinding;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.example.ids.constants.Constants;

public class SettingsFragment extends Fragment {

    private FragmentSettingsBinding binding;

    private TextView userFullname;
    private TextView userPhone;
    private TextView userEmail;
    private Button btnModificaProfilo;

    // Caregiver profile views
    private TextView caregiverFullname;
    private TextView caregiverPhone;
    private TextView caregiverEmail;
    private Button btnModificaCaregiver;

    // Other buttons
    private Button btnEliminaProfilo;
    private Button btnLogout;

    private String jwt;
    private String user_id;

    private OkHttpClient client;

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
        userFullname = binding.userFullname;
        userPhone = binding.userPhone;
        userEmail = binding.userEmail;
        btnModificaProfilo = binding.btnModificaProfilo;

        // Caregiver profile views
        caregiverFullname = binding.caregiverFullname;
        caregiverPhone = binding.caregiverPhone;
        caregiverEmail = binding.caregiverEmail;
        btnModificaCaregiver = binding.btnModificaCaregiver;

        // Other buttons
        btnEliminaProfilo = binding.btnEliminaProfilo;
        btnLogout = binding.btnLogout;

        // Retrieve authentication token from shared preferences
        jwt = requireActivity().getSharedPreferences("app_prefs", MODE_PRIVATE).getString("session_token", null);
        user_id = requireActivity().getSharedPreferences("app_prefs", MODE_PRIVATE).getString("user_id", null);

        // Client obj used to make requests
        client = new OkHttpClient();

        // Update UI showing user info
        ShowUserInfo(view);
        // Update UI showing caregiver info
        ShowCaregiversInfo(view);

        // Set up click listeners
        btnModificaProfilo.setOnClickListener(v -> {
        });

        btnModificaCaregiver.setOnClickListener(v -> {
        });

        btnEliminaProfilo.setOnClickListener(this::DeleteUser);

        btnLogout.setOnClickListener( v -> Logout(v, null));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void ShowUserInfo(View view){

        // Get the user information
        Request request = new Request.Builder()
                .url(Constants.BASE_URL+"/users/"+user_id)
                .header("Authorization", "Bearer " + jwt)
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

                    // Use JSONObject to parse the response string into a JSON
                    try {
                        JSONObject response_data = new JSONObject(response_body_str);
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
                    } catch (JSONException e) {
                        Snackbar.make(view, "Application error, please reopen the application", Snackbar.LENGTH_LONG).show();
                        throw new RuntimeException(e);
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
    }

    private void ShowCaregiversInfo(View view){
        // Get the user information
        Request request = new Request.Builder()
                .url(Constants.BASE_URL+"/users/"+user_id+"/caregivers")
                .header("Authorization", "Bearer " + jwt)
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

                    // Use JSONArray to parse the response string into a JSON
                    try {
                        JSONArray response_data = new JSONArray(response_body_str);
                        if(response_data.length() == 0){
                            Log.d("No caregivers", "No caregivers found");
                        }
                        else {
                            for (int i = 0; i < response_data.length(); i++) {
                                JSONObject caregiver = response_data.getJSONObject(i);
                                final String fullname = (String) caregiver.get("fullname");
                                final String phone_number = (String) caregiver.get("phone_number");
                                final String email = (String) caregiver.get("email");
                                /*
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
                                */
                            }
                        }

                    } catch (JSONException e) {
                        Snackbar.make(view, "Application error, please reopen the application", Snackbar.LENGTH_LONG).show();
                        throw new RuntimeException(e);
                    }
                } else {
                    // Log an error response code
                    Log.e("Error", "Request failed with code " + response.code());

                    // Update the UI to show an error message
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            userFullname.setText("Error loading caregivers data.");
                            Snackbar.make(view, "Error loading caregivers data.", Snackbar.LENGTH_LONG).show();
                        }
                    });
                }
            }
        });
    }
    private void DeleteUser(View view){
        Request request = new Request.Builder()
        .url(Constants.BASE_URL+"/users/"+user_id)
        .delete()
        .header("Authorization", "Bearer " + jwt)
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
                } else {
                    // Log an error response code
                    Log.e("Error", "Request failed with code " + response.code());

                    // Update the UI to show an error message
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            userFullname.setText("Error deleting user.");
                            Snackbar.make(view, "Error deleting user.", Snackbar.LENGTH_LONG).show();
                        }
                    });
                }
            }
        });

        // After deleting the user, logout
        Logout(view, "Profilo eliminato");
    }
    private void Logout(View view, String message){
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("app_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // Remove session_token and user_id
        editor.remove("session_token");
        editor.remove("user_id");

        // Set local variables to null
        user_id = jwt = null;

        // Apply the changes
        editor.apply();

        // Redirect user to the login page
        NavController navController = Navigation.findNavController(getView());
        navController.navigate(R.id.navigation_login);

        // if message is null, set it to "Logout effettuato"
        // otherwise, use the provided message
        // used to print "Profilo eliminato" if this function was called from DeleteUser
        if(message == null)
            message = "Logout effettuato";
        Snackbar.make(view,message, Snackbar.LENGTH_LONG).show();
    }
}
