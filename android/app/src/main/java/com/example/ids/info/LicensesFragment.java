package com.example.ids.info;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.ids.R;

public class LicensesFragment extends Fragment {

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_licenses, container, false);

        LinearLayout licensesContainer = root.findViewById(R.id.licenses_container);
        addLicense(licensesContainer, "Android Jetpack", "Apache License 2.0");
        addLicense(licensesContainer, "Material Components", "Apache License 2.0");
        addLicense(licensesContainer, "Firebase", "Apache License 2.0");
        addLicense(licensesContainer, "OkHttp", "Apache License 2.0");
        addLicense(licensesContainer, "Gson", "Apache License 2.0");
        addLicense(licensesContainer, "JWT Decode", "MIT License");
        addLicense(licensesContainer, "Commons Validator", "Apache License 2.0");

        return root;
    }

    private void addLicense(LinearLayout container, String libraryName, String licenseName) {
        LinearLayout licenseItem = new LinearLayout(getContext());
        licenseItem.setOrientation(LinearLayout.VERTICAL);
        licenseItem.setPadding(16, 16, 16, 16);

        TextView nameView = new TextView(getContext());
        nameView.setText(libraryName);
        nameView.setTextSize(16);
        nameView.setTextColor(getResources().getColor(R.color.title_color, null));

        TextView licenseView = new TextView(getContext());
        licenseView.setText(licenseName);
        licenseView.setTextSize(14);
        licenseView.setTextColor(getResources().getColor(R.color.black, null));

        licenseItem.addView(nameView);
        licenseItem.addView(licenseView);

        container.addView(licenseItem);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}
