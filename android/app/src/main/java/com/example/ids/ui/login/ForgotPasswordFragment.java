package com.example.ids.ui.login;

import static android.content.Context.MODE_PRIVATE;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;

import com.example.ids.R;
import com.example.ids.constants.Constants;

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

public class ForgotPasswordFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Pagina per il recupero della password
        View view = inflater.inflate(R.layout.fragment_forgot_password, container, false);

        EditText email = view.findViewById(R.id.emailInput);
        Button btnRecover = view.findViewById(R.id.recoverButton);
        TextView loginLink = view.findViewById(R.id.loginLink);

        btnRecover.setOnClickListener(v -> {
            String e = email.getText().toString();

            passwordRecover(e);
        });

        loginLink.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(requireView());
            navController.navigate(R.id.navigation_login);
        });

        return view;
    }

    private void passwordRecover(String email) {
        OkHttpClient client = new OkHttpClient();

        MediaType JSON = MediaType.get("application/json; charset=utf-8");
        String json = "{\"email\":\"" + email + "\"}";
        RequestBody body = RequestBody.create(json, JSON);

        Request request = new Request.Builder()
                .url(Constants.BASE_URL + "/forgot_password") // per testare uso l'IP locale della macchina che hosta il backend
                .post(body)
                .build();

        // Se il login fallisce
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                requireActivity().runOnUiThread(() -> {
                    Log.e("Login", "Errore di rete", e);
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String respBody = response.body().string();

                requireActivity().runOnUiThread(() -> {
                    if (response.isSuccessful()) {
                        // Riporta l'utente alla schermata di login
                        requireActivity().runOnUiThread(() -> {
                            Toast.makeText(requireContext(), "Mail per il reset password inviata all'indirizzo fornito", Toast.LENGTH_LONG).show();
                        });
                        NavController navController = Navigation.findNavController(requireView());
                        navController.navigate(R.id.navigation_login);
                    } else {
                        Log.e("Login", "Errore login: " + respBody);
                        requireActivity().runOnUiThread(() -> {
                            Toast.makeText(requireContext(), "Login fallito", Toast.LENGTH_SHORT).show();
                        });
                    }
                });
            }
        });
    }
}

