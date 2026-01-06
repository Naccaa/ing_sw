package com.example.ids.ui.profile;

import static android.content.Context.MODE_PRIVATE;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.ids.R;
import com.example.ids.data.network.AuthInterceptor;
import com.example.ids.databinding.FragmentProfileBinding;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;

import com.example.ids.databinding.FragmentProfileBinding;

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

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;

    private TextView userFullname;
    private TextView userPhone;
    private TextView userEmail;
    private Button btnModificaProfilo;

    // Caregiver profile views
    private LinearLayout caregiverContainer;
    private TextView noCaregiverText;
    private Button btnAggiungiCaregiver;


    // Admin profile view
    private Button btnAggiungiAdmin;

    // Other buttons
    private Button btnEliminaProfilo;
    private Button btnLogout;
    private Button btnOnboarding;

    private String jwt;
    private String user_id;
    private Boolean is_admin;
    private OkHttpClient client;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        SettingsViewModel settingsViewModel =
                new ViewModelProvider(this).get(SettingsViewModel.class);

        binding = FragmentProfileBinding.inflate(inflater, container, false);
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
        caregiverContainer = view.findViewById(R.id.caregiverContainer);
        noCaregiverText = view.findViewById(R.id.noCaregiverText);
        btnAggiungiCaregiver = view.findViewById(R.id.btnAggiungiCaregiver);

        // Admin profile views
        btnAggiungiAdmin = view.findViewById(R.id.btnAggiungiAdmin);

        // Other buttons
        btnEliminaProfilo = binding.btnEliminaProfilo;
        btnLogout = binding.btnLogout;
        btnOnboarding = binding.btnOnboarding;

        // Retrieve authentication token from shared preferences
        jwt = requireActivity().getSharedPreferences("app_prefs", MODE_PRIVATE).getString("session_token", null);
        user_id = requireActivity().getSharedPreferences("app_prefs", MODE_PRIVATE).getString("user_id", null);
        is_admin = requireActivity().getSharedPreferences("app_prefs", MODE_PRIVATE).getBoolean("is_admin", false);



        // Client obj used to make requests
        client = new OkHttpClient.Builder().addInterceptor(new AuthInterceptor(requireContext()))
                .build();

        // Update UI showing user info
        ShowUserInfo(view);
        // Update UI showing caregiver info or admin buttons
        if (is_admin){
            caregiverContainer.setVisibility(View.GONE);
            noCaregiverText.setVisibility(View.GONE);
            btnAggiungiCaregiver.setVisibility(View.GONE);

            btnAggiungiAdmin.setVisibility(View.VISIBLE);
        }
        else
            ShowCaregiversInfo(view);



        // Set up click listeners
        btnModificaProfilo.setOnClickListener(v -> {
            // Redirect user to the update profile page
            getActivity().runOnUiThread(() -> {
                NavController navController = Navigation.findNavController(getView());
                navController.navigate(R.id.action_navigation_profile_to_update_profile);
            });
        });

        btnAggiungiCaregiver.setOnClickListener(v -> {
            // Redirect user to the add caregiver page
            getActivity().runOnUiThread(() -> {
                NavController navController = Navigation.findNavController(getView());
                navController.navigate(R.id.action_navigation_profile_to_add_caregiver);
            });
        });

        btnAggiungiAdmin.setOnClickListener(v -> {
            // Redirect user to the add admin page
            getActivity().runOnUiThread(() -> {
                NavController navController = Navigation.findNavController(getView());
                navController.navigate(R.id.action_navigation_profile_to_add_admin);
            });
        });

        btnEliminaProfilo.setOnClickListener(this::DeleteUserCallback);

        btnLogout.setOnClickListener(this::LogoutCallback);

        btnOnboarding.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(getView());
            navController.navigate(R.id.action_navigation_profile_to_navigation_onboarding);
        });
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
                        JSONObject response_data = (new JSONObject(response_body_str)).getJSONObject("data");

                        final String fullname = response_data.optString("name", "") + " " + response_data.optString("surname", "");
                        final String phone_number = (String) response_data.get("phone_number");
                        final String email = (String) response_data.get("email");

                        // Update UI with user info
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                userFullname.append(fullname);
                                userPhone.append(phone_number);
                                userEmail.append(email);
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
                            Snackbar.make(view, "Error loading user data.", Snackbar.LENGTH_LONG).show();
                        }
                    });
                }
            }
        });
    }

    private void ShowCaregiversInfo(View view){
        // Remove any card from the container before adding the new ones
        caregiverContainer.removeAllViews();

        // Get the user information
        Request request = new Request.Builder()
                .url(Constants.BASE_URL+"/users/"+user_id+"/caregivers")
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
                        JSONObject response_ = new JSONObject(response_body_str);
                        JSONArray response_data = new JSONArray(response_.get("data").toString());
                        // Add the cards to the container
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (response_data.length() == 0) {
                                    noCaregiverText.setVisibility(View.VISIBLE);
                                    caregiverContainer.setVisibility(View.GONE);
                                    Log.d("No caregivers", "No caregivers found");
                                } else {

                                    noCaregiverText.setVisibility(View.GONE);
                                    caregiverContainer.setVisibility(View.VISIBLE);

                                    LayoutInflater inflater = LayoutInflater.from(getContext());

                                    for (int i = 0; i < response_data.length(); i++) {
                                        JSONObject caregiver = null;
                                        String caregiver_id;
                                        String alias;
                                        String phone_number;
                                        String email;
                                        boolean auth;
                                        try {
                                            caregiver = response_data.getJSONObject(i);

                                            caregiver_id = String.valueOf(caregiver.get("caregiver_id")); // caregiver_id is an int, i need to cast it to string for simplicity
                                            alias = (String) caregiver.get("alias");
                                            phone_number = (String) caregiver.get("phone_number");
                                            email = (String) caregiver.get("email");
                                            auth = (boolean) caregiver.get("authenticated");
                                        }catch (JSONException e) {
                                            throw new RuntimeException(e);
                                        }
                                        // Create card
                                        View cardView = inflater.inflate(R.layout.item_caregiver_card, caregiverContainer, false);

                                        TextView name_tv = cardView.findViewById(R.id.caregiverFullname);
                                        TextView phone_tv = cardView.findViewById(R.id.caregiverPhone);
                                        TextView email_tv = cardView.findViewById(R.id.caregiverEmail);
                                        TextView auth_tv = cardView.findViewById(R.id.authenticated);
                                        Button deleteButton = cardView.findViewById(R.id.btnEliminaCaregiver);

                                        name_tv.append(alias);
                                        phone_tv.append(phone_number);
                                        email_tv.append(email);
                                        if (!auth)   // if caregiver is not authenticated, show a message
                                            auth_tv.setVisibility(View.VISIBLE);

                                        // Set up the delete button
                                        deleteButton.setOnClickListener(v -> {
                                            DeleteCaregiver(v, cardView, caregiver_id);
                                        });

                                        // Add the card to the container
                                        caregiverContainer.addView(cardView);
                                    }
                                }
                            }
                        });
                    } catch (JSONException e) {
                        Snackbar.make(view, "Application error, please reopen the application", Snackbar.LENGTH_LONG).show();
                        throw new RuntimeException(e);
                    }
                } else if (response.code() == 404) {
                    // Nessun caregiver trovato
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            caregiverContainer.setVisibility(View.GONE);
                            noCaregiverText.setVisibility(View.VISIBLE);
                            noCaregiverText.setText("Nessun caregiver associato.\nPremi “Aggiungi Caregiver” per aggiungerne uno.\nI dati del caregiver compariranno qui una volta completata la procedura di associazione.");
                            Log.d("Caregivers", "No caregivers found (404)");
                        }
                    });
                } else {
                    // Effettivo errore (che è diverso da semplicemente non avere un caregiver associato)
                    Log.e("Error", "Request failed with code " + response.code());
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Snackbar.make(view, "Error loading caregivers data.", Snackbar.LENGTH_LONG).show();
                        }
                    });
                }
            }
        });
    }

    private void DeleteCaregiver(View view, View caregiverCard, String caregiver_id){
        // Ask for confirmation before deleting the caregiver
        new AlertDialog.Builder(getContext())
                .setTitle("Conferma Eliminazione")
                .setMessage("Sei sicuro di voler eliminare questo caregiver?")
                .setPositiveButton("Elimina", (dialog, which) -> {
                    // make the call to the backend
                    Request request = new Request.Builder()
                            .url(Constants.BASE_URL+"/users/"+user_id+"/caregivers/"+caregiver_id)
                            .delete()
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
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    // Check if the response is successful
                                    if (response.isSuccessful()) {
                                        String response_body_str = response.body().toString();
                                        Log.d("Response str", response_body_str);

                                        // remove the card from the container
                                        caregiverContainer.removeView(caregiverCard);

                                        Snackbar.make(view, "Caregiver removed", Snackbar.LENGTH_LONG).show();
                                    } else {
                                        // Log an error response code
                                        Log.e("Error", "Request failed with code " + response.code());

                                        // Update the UI to show an error message

                                        Snackbar.make(view, "Error deleting caregiver.", Snackbar.LENGTH_LONG).show();
                                    }
                                }
                            });
                        }
                    });

                })
                .setNegativeButton("Annulla", null)
                .show();
    }
    private void DeleteUserCallback(View view){
        // Ask for confirmation before deleting the caregiver
        new AlertDialog.Builder(getContext())
            .setTitle("Conferma Eliminazione")
            .setMessage("Sei sicuro di voler eliminare il tuo profilo?")
            .setPositiveButton("Elimina", (dialog, which) -> {
                Request request = new Request.Builder()
                .url(Constants.BASE_URL+"/users/"+user_id)
                .delete()
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

                            // After deleting the user, logout
                            LogoutLogic(view, "Profilo eliminato");
                        } else {
                            // Log an error response code
                            Log.e("Error", "Request failed with code " + response.code());

                            // Update the UI to show an error message
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Snackbar.make(view, "Error deleting user.", Snackbar.LENGTH_LONG).show();
                                }
                            });
                        }
                    }
                });
            })
            .setNegativeButton("Annulla", null)
            .show();
    }

    // Shows the logout confirmation dialog
    // and then calls the LogoutLogic function
    private void LogoutCallback(View view) {
        // Ask for confirmation
        new AlertDialog.Builder(getContext())
            .setTitle("Conferma Logout")
            .setMessage("Sei sicuro di voler effettuare il logout?")
            .setPositiveButton("Logout", (dialog, which) -> {
                LogoutLogic(view, null);
            })
            .setNegativeButton("Annulla", null)
            .show();
    }

    // Actual logout logic
    private void LogoutLogic(View view, String message) {
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("app_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // Remove session_token, user_id, is_admin
        editor.remove("session_token");
        editor.remove("user_id");
        editor.remove("is_admin");

        // Set local variables to null
        user_id = jwt = null;
        is_admin = false;

        // Apply the changes
        editor.apply();

        // if message is null, set it to "Logout effettuato"
        // otherwise, use the provided message
        // used to print "Profilo eliminato" if this function was called from DeleteUser
        if (message == null)
            message = "Logout effettuato";
        Snackbar.make(view, message, Snackbar.LENGTH_LONG).show();

        // Redirect user to the login page
        getActivity().runOnUiThread(() -> {
            // Redirect user to the login page
            NavController navController = Navigation.findNavController(requireView());
            NavOptions navOptions = new NavOptions.Builder()
                    .setPopUpTo(R.id.mobile_navigation, true)
                    .build();
            navController.navigate(R.id.navigation_login, null, navOptions);
        });
    }
}
