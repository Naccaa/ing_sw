package com.example.ids.info;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.ids.R;

public class PrivacyFragment extends Fragment {

    public PrivacyFragment() {
        super(R.layout.privacy);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView privacyContent = view.findViewById(R.id.privacyContent);

        privacyContent.setText(
                "Ultimo aggiornamento: gennaio 2026\n\n"
        );

    }
}
