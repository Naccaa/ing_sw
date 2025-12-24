package com.example.ids.ui.profile;

import static android.content.Context.MODE_PRIVATE;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.ids.R;
import com.example.ids.constants.Constants;
import com.example.ids.databinding.AddCaregiverBinding;
import com.google.android.material.snackbar.Snackbar;

import org.apache.commons.validator.routines.EmailValidator;
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

public class AddCaregiver extends Fragment {

    private AddCaregiverBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        SettingsViewModel settingsViewModel =
                new ViewModelProvider(this).get(SettingsViewModel.class);

        binding = AddCaregiverBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Back Button Logic
        binding.backButton.setOnClickListener(this::redirectUserToProfile);

        // Confirm Button Logic
        binding.confirmButton.setOnClickListener(v -> {
            try {
                updateProfile(v);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void updateProfile(View view) throws JSONException {
        // Retrieve values from Input fields
        String name = binding.nameInput.getText().toString().trim();
        String email = binding.emailInput.getText().toString().trim();
        String phone = binding.phoneInput.getText().toString();

        String errorMessage = null;

        JSONObject body = new JSONObject();

        if (name.isEmpty() || email.isEmpty() || phone.isEmpty())
            errorMessage = "Non hai inserito tutti i dati richiesti.";
        else {
            body.put("alias", name);

            EmailValidator emailValidator = EmailValidator.getInstance();
            if (!emailValidator.isValid(email))
                errorMessage = "Email non valida.";
            else
                body.put("email", email);

            /* Phone Validator not working
            if (!phone.isEmpty() && phone.matches("^\\+?\\d(?:[\\d\\s]*\\d)?$"))
                errorMessage = "Un numero può contenere solo numeri, spazi e un solo + per il prefisso.";
            else*/
            body.put("phone_number", phone);

        }

        if (errorMessage != null) {
            String finalErrorMessage = errorMessage;
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Snackbar.make(view, finalErrorMessage, Snackbar.LENGTH_LONG).show();
                }
            });
        }
        else {
            String jwt = requireActivity().getSharedPreferences("app_prefs", MODE_PRIVATE).getString("session_token", null);
            String user_id = requireActivity().getSharedPreferences("app_prefs", MODE_PRIVATE).getString("user_id", null);

            String body_str = body.toString();
            Log.d("Patch body", body_str);

            Request request = new Request.Builder()
                    .url(Constants.BASE_URL + "/users/" + user_id + "/caregivers")
                    .post(RequestBody.create(body_str, MediaType.get("application/json; charset=utf-8")))
                    .header("Authorization", "Bearer " + jwt)
                    .build();

            Log.d("Request", request.toString());

            // Asynchronous request
            OkHttpClient client = new OkHttpClient();

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

                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Snackbar.make(view, "Caregiver aggiunto con successo. Una mail di conferma verrà inviata al caregiver.", Snackbar.LENGTH_LONG).show();
                            }
                        });

                        redirectUserToProfile(view);
                    } else {
                        // Log an error response code
                        Log.e("Error", "Request failed with code " + response.code());

                        // Update the UI to show an error message
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Snackbar.make(view, "Error adding caregiver.", Snackbar.LENGTH_LONG).show();
                            }
                        });
                    }
                }
            });
        }
    }

    // Redirect user to the profile page
    private void redirectUserToProfile(View v){
        getActivity().runOnUiThread(() -> {
            NavController navController = Navigation.findNavController(getView());
            navController.navigate(R.id.action_add_caregiver_to_navigation_profile);
        });
    }
}
