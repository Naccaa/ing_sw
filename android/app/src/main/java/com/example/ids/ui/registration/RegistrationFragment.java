package com.example.ids.ui.registration;

import static android.content.Context.MODE_PRIVATE;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;

import com.example.ids.R;
import com.example.ids.constants.Constants;
import com.example.ids.data.network.AuthInterceptor;

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

public class RegistrationFragment extends Fragment {
    @Override
    public View onCreateView(

            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.signup, container, false);

        // Parametri presi dal form
        EditText nameInput = view.findViewById(R.id.nameInput);
        EditText surnameInput = view.findViewById(R.id.surnameInput);
        EditText emailInput = view.findViewById(R.id.emailInput);
        EditText phoneInput = view.findViewById(R.id.phoneInput);
        EditText passwordInput = view.findViewById(R.id.passwordInput);
        EditText confirmPasswordInput = view.findViewById(R.id.confirmPasswordInput);

        CheckBox termsCheckbox = view.findViewById(R.id.termsCheckbox); // Checkbox su termini e condizioni
        Button registerButton = view.findViewById(R.id.registerButton); // Bottone di registrazione

        TextView registerLink = view.findViewById(R.id.loginLink);
        registerLink.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(requireView());
            navController.navigate(R.id.navigation_login);
        });

        registerButton.setOnClickListener(v -> {

            if (!termsCheckbox.isChecked()) {
                // Non è possibile registrarsi se non si sono accettati i termini di utilizzo tramite l'apposita spunta
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), "Devi accettare i termini e le condizioni", Toast.LENGTH_SHORT).show();
                });
            }else{
                // Altrimenti si procede con la registrazione
                registration(nameInput.getText().toString(), surnameInput.getText().toString(), emailInput.getText().toString(), phoneInput.getText().toString(), passwordInput.getText().toString(), confirmPasswordInput.getText().toString());
            }
        });

        return view;
    }

    private void registration(String name, String surname, String email, String phone_number, String password, String confirm_password) {


        // Che le password siano uguali
        if (!password.equals(confirm_password)) {
            requireActivity().runOnUiThread(() -> {
                Toast.makeText(requireContext(), "Le password non coincidono", Toast.LENGTH_SHORT).show();
            });
            return;
        }

        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(new AuthInterceptor(requireContext()))
                .build();

        MediaType JSON = MediaType.get("application/json; charset=utf-8");

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("email", email);
            jsonObject.put("fullname", name + " " + surname);
            jsonObject.put("phone_number", phone_number);
            jsonObject.put("password", password);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        RequestBody body = RequestBody.create(
                jsonObject.toString(),
                JSON
        );

        Request request = new Request.Builder()
                .url(Constants.BASE_URL + "/users") // per testare uso l'IP locale della macchina che hosta il backend
                .post(body)
                .build();

        // Se il login fallisce
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                requireActivity().runOnUiThread(() -> {
                    Log.e("Registration", "Errore di rete", e);
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(),
                                "Registrazione completata. Effettua il login.",
                                Toast.LENGTH_SHORT).show();

                        NavController navController =
                                Navigation.findNavController(requireView());

                        navController.navigate(R.id.navigation_login); // torni al login
                    });
                }
            }

        });
    }

}
