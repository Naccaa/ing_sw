package com.example.ids.ui.login;

import static android.content.Context.MODE_PRIVATE;

import com.example.ids.MainActivity;
import com.example.ids.R;
import com.example.ids.constants.Constants;
import com.example.ids.data.network.AuthInterceptor;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.SyncStateContract;
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

import com.auth0.android.jwt.JWT;


public class LoginFragment extends Fragment {

    @Override
    public View onCreateView(

            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_login, container, false);

        SharedPreferences prefs = requireActivity().getSharedPreferences("app_prefs", MODE_PRIVATE);
        String existingToken = prefs.getString("session_token", null);
        if (existingToken != null && !existingToken.isEmpty()) {
            Log.d("LOGIN", "Token di sessione esistente: " + existingToken);
            // Postpone navigation to avoid crash
            view.post(() -> {
                NavController navController = Navigation.findNavController(view);
                navController.navigate(R.id.navigation_alert);
            });
            return view;
        }




        EditText email = view.findViewById(R.id.emailInput);
        EditText password = view.findViewById(R.id.passwordInput);
        Button btnLogin = view.findViewById(R.id.loginButton);
        TextView forgotPassword = view.findViewById(R.id.forgotPassword);
        TextView registerLink = view.findViewById(R.id.registerLink);

        btnLogin.setOnClickListener(v -> {
            String e = email.getText().toString();
            String p = password.getText().toString();
            if (e.isEmpty() || p.isEmpty()) {
                Toast.makeText(requireContext(), "Inserisci email e password", Toast.LENGTH_SHORT).show();
                return;
            }
            login(e, p);
        });

        forgotPassword.setOnClickListener(v -> {
            Log.d("LOGIN", "Password dimenticata cliccata");
            forgotPass();
        });

        registerLink.setOnClickListener(v -> {
            Log.d("LOGIN", "Reindirizzamento verso pagina di registrazione...");
            registerLink();
        });




        return view;
    }

    private void login(String email, String password) {
        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(new AuthInterceptor(requireContext())).build();

        MediaType JSON = MediaType.get("application/json; charset=utf-8");
        String json = "{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}";
        RequestBody body = RequestBody.create(json, JSON);

        Request request = new Request.Builder()
                .url(Constants.BASE_URL + "/sessions") // per testare uso l'IP locale della macchina che hosta il backend
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
                        Log.d("Login", "Login effettuato: " + respBody);

                        try {
                            JSONObject obj = new JSONObject(respBody);

                            // Estrazione del token dalla risposta HTTP
                            JSONObject data = obj.getJSONObject("data");
                            String token = data.getString("session_token");

                            // Salvataggio del token di sessione
                            requireActivity().getSharedPreferences("app_prefs", MODE_PRIVATE)
                                    .edit()
                                    .putString("session_token", token)
                                    .apply();

                            // Salvataggio dell'user_id dell'utente autenticato (evita di doverlo ricavare ogni volta dal JWT)
                            JWT jwt = new JWT(token);  // Decode il JWT
                            String user_id = jwt.getClaim("sub").asString();
                            requireActivity().getSharedPreferences("app_prefs", MODE_PRIVATE)
                                    .edit()
                                    .putString("user_id", user_id)
                                    .apply();

                            MainActivity.send_firebase_token(requireContext(), token, user_id);
                            
                            // Salvataggio del ruolo dell'utente (evita di doverlo ricavare ogni volta dal JWT)
                            boolean is_admin = jwt.getClaim("is_admin").asBoolean();
                            requireActivity().getSharedPreferences("app_prefs", MODE_PRIVATE)
                                    .edit()
                                    .putBoolean("is_admin", is_admin)
                                    .apply();
                            // Reindirizza l'utente alla "home" dell'applicazione
                            requireActivity().runOnUiThread(() -> {
                                NavController navController = Navigation.findNavController(requireView());
                                NavOptions navOptions = new NavOptions.Builder()
                                        .setPopUpTo(R.id.navigation_login, true)
                                        .build();
                                navController.navigate(R.id.navigation_alert, null, navOptions);
                            });

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

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

    private void forgotPass() {
        NavController navController = Navigation.findNavController(requireView());
        navController.navigate(R.id.navigation_forgotPassword);
    }

    private void registerLink() {
        NavController navController = Navigation.findNavController(requireView());
        navController.navigate(R.id.navigation_registration);
    }
}
