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
                + "1. Titolare del trattamento\n"
                + "Il presente progetto è gestito da un gruppo di studenti per scopi didattici nell'ambito delle emergenze climatiche. "
                + "Il gruppo di studenti è responsabile del trattamento dei vostri dati personali conformemente alla normativa vigente in materia di protezione dei dati (GDPR).\n\n"
                + "2. Finalità del trattamento\n"
                + "I vostri dati personali vengono raccolti e trattati esclusivamente per le seguenti finalità:\n"
                + "• Visualizzazione delle emergenze climatiche nelle vicinanze\n"
                + "• Gestione della rete di caregiver per il vostro supporto in situazioni di pericolo\n"
                + "• Invio di messaggi SMS ai caregiver designati in caso di emergenza\n\n"
                + "3. Tipologie di dati personali trattati\n"
                + "L'applicazione raccoglie i seguenti dati:\n"
                + "• Dati identificativi (nome, cognome, email, numero di telefono)\n"
                + "• Dati di localizzazione (posizione geografica)\n"
                + "• Dati relativi ai caregiver designati\n\n"
                + "4. Accesso ai dati\n"
                + "L'accesso ai dati personali degli utenti è consentito esclusivamente agli amministratori del sistema, "
                + "che possono accedere alle informazioni personali per finalità di gestione e manutenzione. "
                + "Gli altri utenti non possono accedere ai dati personali altrui, ad eccezione dei dati necessari per il funzionamento "
                + "della rete di caregiver (invio di SMS in caso di emergenza).\n\n"
                + "5. Conservazione dei dati\n"
                + "I dati personali vengono conservati per la durata della vostra partecipazione al progetto. "
                + "Non vengono effettuate analisi, ricerche o studi sui dati raccolti. "
                + "I vostri dati verranno eliminati secondo le modalità descritte nella sezione 6.\n\n"
                + "6. Diritto di cancellazione\n"
                + "Avete il diritto di cancellare in qualsiasi momento il vostro profilo e tutti i dati personali associati. "
                + "La cancellazione del profilo comporterà l'eliminazione definitiva di:\n"
                + "• Dati identificativi personali\n"
                + "• Relazioni di caregiver\n"
                + "La richiesta di cancellazione può essere effettuata direttamente dall'applicazione nelle impostazioni del profilo.\n\n"
                + "7. Aggiornamento dei dati\n"
                + "Potete aggiornare i vostri dati personali (nome, cognome, email, numero di telefono, posizione, caregiver) "
                + "in qualsiasi momento attraverso le impostazioni del profilo all'interno dell'applicazione.\n\n"
                + "8. Diritti dell'utente\n"
                + "Secondo la normativa GDPR, avete diritto a:\n"
                + "• Accedere ai vostri dati personali\n"
                + "• Rettificare dati inesatti\n"
                + "• Cancellare i vostri dati\n"
                + "• Limitare il trattamento\n"
                + "• Portabilità dei dati\n\n"
                + "9. Sicurezza dei dati\n"
                + "Il gruppo di studenti implementa misure di sicurezza appropriate per proteggere i vostri dati da accessi non autorizzati, "
                + "perdite o alterazioni non intenzionali.\n\n"
                + "10. Condivisione dei dati\n"
                + "I vostri dati non vengono condivisi con terze parti. "
                + "I caregiver designati riceveranno messaggi SMS esclusivamente in caso di emergenza.\n\n"
                + "11. Cookie e tecnologie di tracciamento\n"
                + "L'applicazione non utilizza cookie, tecnologie di tracciamento o qualsiasi altra forma di monitoraggio dei dati personali.\n\n"
                + "12. Modifiche all'informativa\n"
                + "La presente informativa sulla privacy può essere aggiornata in qualsiasi momento. "
                + "Le modifiche entreranno in vigore dalla loro pubblicazione all'interno dell'applicazione.\n\n"
                + "Proseguendo nell'utilizzo dell'applicazione, acconsentite al trattamento dei vostri dati personali secondo le modalità descritte in questa informativa."
        );

    }
}
