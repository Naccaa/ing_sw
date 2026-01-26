package com.example.ids.ui.registration;

import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.ids.R;
import com.example.ids.constants.Constants;
import com.example.ids.data.network.AuthInterceptor;
import com.example.ids.databinding.SignupBinding;

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

public class RegistrationFragment extends Fragment {

    private SignupBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = SignupBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupTermsSpannable();

        binding.registerButton.setOnClickListener(v -> validateAndRegister());

        binding.loginLink.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.navigation_login));
    }

    private void setupTermsSpannable() {
        String fullText = "Accetto termini e condizioni d'uso";
        SpannableString spannable = new SpannableString(fullText);
        int start = fullText.indexOf("termini");

        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                Navigation.findNavController(binding.getRoot()).navigate(R.id.termsFragment);
            }
        };

        spannable.setSpan(clickableSpan, start, fullText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        binding.termsText.setText(spannable);
        binding.termsText.setMovementMethod(LinkMovementMethod.getInstance());
        binding.termsText.setHighlightColor(Color.TRANSPARENT);
    }

    private void validateAndRegister() {
        String name = binding.nameInput.getText().toString().trim();
        String surname = binding.surnameInput.getText().toString().trim();
        String email = binding.emailInput.getText().toString().trim();
        String phone = binding.phoneInput.getText().toString().trim();
        String pass = binding.passwordInput.getText().toString();
        String confirmPass = binding.confirmPasswordInput.getText().toString();

        if (name.isEmpty() || email.isEmpty() || pass.isEmpty()) {
            Toast.makeText(getContext(), "Compila i campi obbligatori", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!pass.equals(confirmPass)) {
            Toast.makeText(getContext(), "Le password non coincidono", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!binding.termsCheckbox.isChecked()) {
            Toast.makeText(getContext(), "Accetta i termini e le condizioni", Toast.LENGTH_SHORT).show();
            return;
        }

        performRegistration(name, surname, email, phone, pass);
    }

    private void performRegistration(String name, String surname, String email, String phone, String password) {
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new AuthInterceptor(requireContext()))
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .build();

        JSONObject json = new JSONObject();
        try {
            json.put("email", email);
            json.put("fullname", name + " " + surname);
            json.put("phone_number", phone);
            json.put("password", password);
        } catch (JSONException e) { e.printStackTrace(); }

        RequestBody body = RequestBody.create(json.toString(), MediaType.get("application/json; charset=utf-8"));
        Request request = new Request.Builder().url(Constants.BASE_URL + "/users").post(body).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                safeUiUpdate(() -> Toast.makeText(getContext(), "Server non raggiungibile", Toast.LENGTH_LONG).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                safeUiUpdate(() -> {
                    if (response.isSuccessful()) {
                        Toast.makeText(getContext(), "Registrazione completata!", Toast.LENGTH_SHORT).show();
                        Navigation.findNavController(binding.getRoot()).navigate(R.id.navigation_login);
                    } else {
                        Toast.makeText(getContext(), "Errore server: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
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