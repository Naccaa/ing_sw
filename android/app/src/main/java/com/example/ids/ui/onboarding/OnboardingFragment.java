package com.example.ids.ui.onboarding;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.ids.MainActivity;
import com.example.ids.R;

public class OnboardingFragment extends Fragment {

    private int currentPage = 0;
    private TextView title, description;
    private ImageView image;
    private Button btnPrev, btnNext;

    private String[] titles = {
        "Benvenuto in IdS!",
        "Permessi Necessari",
        "Cosa sono i Caregiver?",
        "Sezioni dell'App"
    };

    private String[] descriptions = {
        "Per accedere a tutte le funzionalità dell'app, come ricevere avvisi di emergenza e gestire il tuo profilo, è necessario registrarsi. La registrazione garantisce che solo gli utenti autorizzati possano accedere alle informazioni sensibili e personalizzate.",
        "L'app richiede il permesso per la posizione per fornirti avvisi locali sulle emergenze climatiche. Le notifiche sono essenziali per ricevere aggiornamenti in tempo reale su eventi critici. Questi permessi aiutano a proteggere te e la tua comunità.",
        "I caregiver sono persone di fiducia che possono accedere al tuo profilo in caso di emergenza. Puoi designare familiari o amici come caregiver per garantire che ricevano notifiche e possano agire per tuo conto se necessario.",
        "• Avvisi: Ricevi e visualizza le emergenze climatiche in corso.\n• Linee Guida: Leggi consigli e procedure per affrontare situazioni di emergenza.\n• Profilo: Gestisci le tue informazioni personali, caregiver e impostazioni."
    };

    private int[] images = {
        R.drawable.ic_profile,
        R.drawable.ic_alert,
        R.drawable.ic_setting,
        R.drawable.ic_guide
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_onboarding, container, false);

        title = view.findViewById(R.id.title);
        description = view.findViewById(R.id.description);
        image = view.findViewById(R.id.image);
        btnPrev = view.findViewById(R.id.btnPrev);
        btnNext = view.findViewById(R.id.btnNext);

        updatePage();

        btnPrev.setOnClickListener(v -> {
            if (currentPage > 0) {
                currentPage--;
                updatePage();
            }
        });

        btnNext.setOnClickListener(v -> {
            if (currentPage < titles.length - 1) {
                currentPage++;
                updatePage();
            } else {
                finishOnboarding();
            }
        });

        return view;
    }

    private void updatePage() {
        title.setText(titles[currentPage]);
        description.setText(descriptions[currentPage]);
        image.setImageResource(images[currentPage]);

        btnPrev.setVisibility(currentPage == 0 ? View.GONE : View.VISIBLE);
        btnNext.setText(currentPage == titles.length - 1 ? "Fine" : "Avanti");
    }

    private void finishOnboarding() {
        SharedPreferences prefs = requireActivity().getSharedPreferences("app_prefs", requireActivity().MODE_PRIVATE);
        boolean wasCompleted = prefs.getBoolean("onboarding_completed", false);
        if (!wasCompleted) {
            prefs.edit().putBoolean("onboarding_completed", true).apply();
            // Request notification permission after onboarding
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).requestNotificationPermission();
            }
            Navigation.findNavController(requireView()).navigate(R.id.navigation_login);
        } else {
            Navigation.findNavController(requireView()).popBackStack();
        }
    }
}