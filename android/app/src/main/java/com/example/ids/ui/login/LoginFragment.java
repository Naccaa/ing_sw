package com.example.ids.ui.login;

import static android.content.Context.MODE_PRIVATE;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;

import com.auth0.android.jwt.JWT;
import com.example.ids.MainActivity;
import com.example.ids.R;
import com.example.ids.constants.Constants;
import com.example.ids.data.network.AuthInterceptor;
import com.example.ids.databinding.FragmentLoginBinding;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LoginFragment extends Fragment {

    private FragmentLoginBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        checkExistingSession();
        setupListeners();
    }

    private void checkExistingSession() {
        SharedPreferences prefs = requireActivity().getSharedPreferences("app_prefs", MODE_PRIVATE);
        String token = prefs.getString("session_token", null);

        if (token != null && !token.isEmpty()) {
            binding.getRoot().post(() -> {
                if (isAdded()) {
                    Navigation.findNavController(binding.getRoot()).navigate(R.id.navigation_alert);
                }
            });
        }
    }

    private void setupListeners() {
        binding.loginButton.setOnClickListener(v -> {
            String email = binding.emailInput.getText().toString().trim();
            String password = binding.passwordInput.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(getContext(), "Compila tutti i campi", Toast.LENGTH_SHORT).show();
                return;
            }
            performLogin(email, password);
        });

        binding.forgotPassword.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.navigation_forgotPassword));

        binding.registerLink.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.navigation_registration));

        binding.onboardingLink.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.navigation_onboarding));
    }

    private void performLogin(String email, String password) {
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new AuthInterceptor(requireContext()))
                .connectTimeout(10, TimeUnit.SECONDS)
                .build();

        JSONObject payload = new JSONObject();
        try {
            payload.put("email", email);
            payload.put("password", password);
        } catch (JSONException e) { e.printStackTrace(); }

        RequestBody body = RequestBody.create(payload.toString(), MediaType.get("application/json; charset=utf-8"));
        Request request = new Request.Builder().url(Constants.BASE_URL + "/sessions").post(body).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                safeUiUpdate(() -> Toast.makeText(getContext(), "Server offline", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                final String respBody = response.body() != null ? response.body().string() : "";

                Log.d("response", respBody);
                safeUiUpdate(() -> {
                    if (response.isSuccessful()) {
                        handleLoginSuccess(respBody);

                    } else {
                        Toast.makeText(getContext(), "Credenziali errate", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void handleLoginSuccess(String responseBody) {
        try {
            JSONObject data = new JSONObject(responseBody).getJSONObject("data");
            String token = data.getString("session_token");

            JWT jwt = new JWT(token);
            String userId = jwt.getClaim("sub").asString();
            boolean isAdmin = jwt.getClaim("is_admin").asBoolean();


            requireActivity().getSharedPreferences("app_prefs", MODE_PRIVATE).edit()
                    .putString("session_token", token)
                    .putString("user_id", userId)
                    .putString("fullname", data.optString("fullname", ""))
                    .putString("email", data.optString("email", ""))
                    .putString("phone_number", data.optString("phone_number", ""))
                    .putBoolean("is_admin", isAdmin)
                    .apply();

            MainActivity.send_firebase_token(requireContext(), token, userId);

            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).startLocationService();
            }

            NavController navController = Navigation.findNavController(binding.getRoot());
            navController.navigate(R.id.navigation_alert, null,
                    new NavOptions.Builder().setPopUpTo(R.id.navigation_login, true).build());

        } catch (JSONException e) {
            Log.e("Login", "Parsing error", e);
        }
    }

    private void safeUiUpdate(Runnable runnable) {
        if (getActivity() != null && isAdded() && binding != null) {
            getActivity().runOnUiThread(runnable);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}