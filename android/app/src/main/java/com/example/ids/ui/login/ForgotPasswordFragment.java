package com.example.ids.ui.login;

import static android.content.Context.MODE_PRIVATE;

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
import androidx.navigation.Navigation;

import com.example.ids.R;
import com.example.ids.constants.Constants;
import com.example.ids.data.network.AuthInterceptor;
import com.example.ids.databinding.FragmentForgotPasswordBinding;

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

public class ForgotPasswordFragment extends Fragment {

    private FragmentForgotPasswordBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentForgotPasswordBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.recoverButton.setOnClickListener(v -> {
            String email = binding.emailInput.getText().toString().trim();
            if (email.isEmpty()) {
                Toast.makeText(getContext(), "Inserisci un indirizzo email", Toast.LENGTH_SHORT).show();
                return;
            }
            passwordRecover(email);
        });

        binding.loginLink.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.navigation_login));
    }

    private void passwordRecover(String email) {
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new AuthInterceptor(requireContext()))
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .build();

        JSONObject jsonPayload = new JSONObject();
        try {
            jsonPayload.put("email", email);
        } catch (JSONException e) { e.printStackTrace(); }

        RequestBody body = RequestBody.create(jsonPayload.toString(), MediaType.get("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url(Constants.BASE_URL + "/forgot_password")
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                safeUiUpdate(() ->
                        Toast.makeText(getContext(), "Errore di rete: server non raggiungibile", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                safeUiUpdate(() -> {
                    if (response.isSuccessful()) {
                        Toast.makeText(getContext(), "Mail per il reset inviata correttamente", Toast.LENGTH_LONG).show();
                        if (isAdded()) {
                            Navigation.findNavController(binding.getRoot()).navigate(R.id.navigation_login);
                        }
                    } else {
                        Toast.makeText(getContext(), "Errore: utente non trovato", Toast.LENGTH_SHORT).show();
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