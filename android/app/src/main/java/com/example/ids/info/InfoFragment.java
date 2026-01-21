package com.example.ids.info;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.ids.R;
import com.example.ids.constants.Constants;

public class InfoFragment extends Fragment {

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_info, container, false);

        // Set up button listeners
        setupButtonListeners(root);

        // Set up IP address autocomplete
        setupIPAddressInput(root);

        return root;
    }

    private void setupIPAddressInput(View root) {
        EditText ipInput = root.findViewById(R.id.server_ip_input);
        if (ipInput != null) {
            // Set current IP
            String currentIp = Constants.BASE_URL;
            ipInput.setText(currentIp);

            // Set listener for when user confirms input with Enter key
            ipInput.setOnEditorActionListener((v, actionId, event) -> {
                String ip = ipInput.getText().toString().trim();
                if (!ip.isEmpty() && isValidIP(ip)) {
                    updateServerIp(ip);
                    return true;
                } else if (!ip.isEmpty()) {
                    Toast.makeText(getContext(), "Formato IP non valido", Toast.LENGTH_SHORT).show();
                }
                return false;
            });
        }
    }

    private void updateServerIp(String ip) {
        Constants.BASE_URL = ip;
        Toast.makeText(getContext(), "Indirizzo IP del server aggiornato: " + Constants.BASE_URL, Toast.LENGTH_SHORT).show();
    }
    private boolean isValidIP(String ip) {
        // Basic validation
        return ip.startsWith("http://") && ip.contains(":");
    }

    private void setupButtonListeners(View root) {
        // Terms and Conditions button
        Button termsButton = root.findViewById(R.id.btn_terms);
        if (termsButton != null) {
            termsButton.setOnClickListener(v -> {
                Navigation.findNavController(v).navigate(R.id.termsFragment);
            });
        }

        // License button (Apache 2.0)
        Button licenseButton = root.findViewById(R.id.btn_license);
        if (licenseButton != null) {
            licenseButton.setOnClickListener(v -> {
                String licenseUrl = "https://www.apache.org/licenses/LICENSE-2.0";
                openUrl(licenseUrl);
            });
        }

        // Open Source Licenses button
        Button openSourceButton = root.findViewById(R.id.btn_open_source_licenses);
        if (openSourceButton != null) {
            openSourceButton.setOnClickListener(v -> {
                Navigation.findNavController(v).navigate(R.id.licensesFragment);
            });
        }

        // Restart Onboarding button
        Button restartOnboardingButton = root.findViewById(R.id.btn_restart_onboarding);
        if (restartOnboardingButton != null) {
            restartOnboardingButton.setOnClickListener(v -> {
                Navigation.findNavController(v).navigate(R.id.navigation_onboarding);
            });
        }
    }

    private void openUrl(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        startActivity(intent);
    }
}
