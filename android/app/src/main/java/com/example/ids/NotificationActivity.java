package com.example.ids;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class NotificationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        TextView textEmergency = findViewById(R.id.textEmergency);
        Button btnStop = findViewById(R.id.btnStop);
        Button btnHelp = findViewById(R.id.btnHelp);

        String emergency_type = getIntent().getStringExtra("emergency_type");
        if(emergency_type!=null){
            textEmergency.setText("Emergenza di tipo: " + emergency_type + " nella tua zona.");
        } else {
            textEmergency.setText("Emergenza generica rilevata.");
        }

        btnStop.setOnClickListener(v -> {
            MessagingService.abortEmergency(NotificationActivity.this);
            Toast.makeText(NotificationActivity.this, "Allarme annullato. Nessun SMS inviato.", Toast.LENGTH_LONG).show();
            finish();
        });

        btnHelp.setOnClickListener(v -> {
            // Chiama il nuovo metodo che resetta il worker e lo fa partire senza attesa
            MessagingService.triggerImmediateEmergency(this);
            Toast.makeText(this, "Invio richiesta di aiuto in corso...", Toast.LENGTH_LONG).show();
            finish();
        });

    }
}
