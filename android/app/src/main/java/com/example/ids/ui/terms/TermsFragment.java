package com.example.ids.ui.terms;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.ids.R;

public class TermsFragment extends Fragment {

    public TermsFragment() {
        super(R.layout.terms);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView termsContent = view.findViewById(R.id.termsContent);

        termsContent.setText(
                        "Ultimo aggiornamento: gennaio 2026\n\n" +

                        "L’accesso e l’utilizzo della presente applicazione comportano l’accettazione integrale dei seguenti " +
                        "Termini e Condizioni d’Uso. Qualora l’utente non intenda accettare quanto riportato di seguito, è invitato " +
                        "a non utilizzare l’applicazione.\n\n" +

                        "1. Finalità dell’applicazione\n" +
                        "L’applicazione è fornita a scopo dimostrativo e/o didattico. Le funzionalità offerte hanno l’obiettivo " +
                        "di mostrare il funzionamento generale del sistema e non costituiscono un servizio professionale o " +
                        "commerciale.\n\n" +

                        "2. Utilizzo dell’app\n" +
                        "L’utente si impegna a utilizzare l’applicazione in modo lecito, corretto e conforme alle normative vigenti, " +
                        "evitando qualsiasi comportamento che possa compromettere il corretto funzionamento del sistema o arrecare " +
                        "danni a terzi.\n\n" +

                        "3. Contenuti e dati\n" +
                        "Eventuali dati inseriti dall’utente all’interno dell’applicazione vengono utilizzati esclusivamente per il " +
                        "funzionamento delle funzionalità offerte. Tali dati non vengono ceduti a terze parti e non sono utilizzati per " +
                        "fini commerciali. Tuttavia, non viene garantita la conservazione permanente delle informazioni inserite.\n\n" +

                        "4. Limitazione di responsabilità\n" +
                        "Lo sviluppatore non garantisce che l’applicazione sia priva di errori, interruzioni o malfunzionamenti. " +
                        "L’utilizzo dell’app avviene sotto la piena responsabilità dell’utente. In nessun caso lo sviluppatore potrà " +
                        "essere ritenuto responsabile per danni diretti o indiretti derivanti dall’utilizzo o dall’impossibilità di " +
                        "utilizzo dell’applicazione.\n\n" +

                        "5. Disponibilità del servizio\n" +
                        "L’applicazione può essere modificata, sospesa o interrotta in qualsiasi momento, anche senza preavviso, per " +
                        "esigenze tecniche, di manutenzione o di sviluppo.\n\n" +

                        "6. Modifiche ai Termini\n" +
                        "I presenti Termini e Condizioni possono essere aggiornati o modificati in qualsiasi momento. Le eventuali " +
                        "modifiche entreranno in vigore dal momento della loro pubblicazione all’interno dell’applicazione.\n\n" +

                        "7. Accettazione dei Termini\n" +
                        "Proseguendo nell’utilizzo dell’applicazione, l’utente dichiara di aver letto, compreso e accettato " +
                        "integralmente i presenti Termini e Condizioni d’Uso."
        );

    }
}
