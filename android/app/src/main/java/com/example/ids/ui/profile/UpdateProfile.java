package com.example.ids.ui.profile;

import static android.content.Context.MODE_PRIVATE;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.ids.R;
import com.example.ids.constants.Constants;
import com.example.ids.databinding.UpdateProfileBinding;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Constructor;

import org.apache.commons.validator.routines.EmailValidator;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.BufferedSink;

public class UpdateProfile extends Fragment {

    private UpdateProfileBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        SettingsViewModel settingsViewModel =
                new ViewModelProvider(this).get(SettingsViewModel.class);

        binding = UpdateProfileBinding.inflate(inflater, container, false);
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
        String surname = binding.surnameInput.getText().toString().trim();
        String email = binding.emailInput.getText().toString().trim();
        String phone = binding.phoneInput.getText().toString();
        String password = binding.passwordInput.getText().toString();
        String confirmPassword = binding.confirmPasswordInput.getText().toString();

        String errorMessage = null;

        JSONObject body = new JSONObject();

        if (name.isEmpty() && surname.isEmpty() && email.isEmpty() && phone.isEmpty() && password.isEmpty() && confirmPassword.isEmpty())
            errorMessage = "Non hai inserito alcun dato.";


        if (name.isEmpty() && !surname.isEmpty() || !name.isEmpty() && surname.isEmpty())
            errorMessage = "Devi inserire sia il tuo nome che il tuo cognome.";
        else if(!name.isEmpty())
            body.put("fullname", name + " " + surname);


        EmailValidator emailValidator = EmailValidator.getInstance();
        if (!email.isEmpty() && !emailValidator.isValid(email))
            errorMessage = "Email non valida.";
        else if(!email.isEmpty())
            body.put("email", email);


        /* Phone Validator not working
        if (!phone.isEmpty() && phone.matches("^\\+?\\d(?:[\\d\\s]*\\d)?$"))
            errorMessage = "Un numero può contenere solo numeri, spazi e un solo + per il prefisso.";
        else*/ if(!phone.isEmpty())
            body.put("phone_number", phone);

        if (!password.equals(confirmPassword))
            errorMessage = "Le password non coincidono.";
        else if (!password.isEmpty())
            body.put("password", password);

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
                    .url(Constants.BASE_URL + "/users/" + user_id)
                    .patch(RequestBody.create(body_str, MediaType.get("application/json; charset=utf-8")))
                    .header("Authorization", "Bearer " + jwt)
                    .build();

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
                                Snackbar.make(view, "Modifica effettuata con successo.", Snackbar.LENGTH_LONG).show();
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
                                Snackbar.make(view, "Error updating user.", Snackbar.LENGTH_LONG).show();
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
            navController.navigate(R.id.action_update_profile_to_navigation_profile);
        });
    }
}
