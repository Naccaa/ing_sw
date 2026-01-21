package com.example.ids;

import android.os.Bundle;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ImageView;
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
        ImageView icAlert = findViewById(R.id.icAlert);


        String emergency_type = getIntent().getStringExtra("emergency_type");
        if(emergency_type != null && !emergency_type.isEmpty()){
            textEmergency.setText("Emergenza: " + emergency_type.toUpperCase());
        } else {
            textEmergency.setText("Emergenza Rilevata");
        }


        if (icAlert != null) {
            Animation anim = new AlphaAnimation(0.4f, 1.0f);
            anim.setDuration(1000);
            anim.setRepeatMode(Animation.REVERSE);
            anim.setRepeatCount(Animation.INFINITE);
            icAlert.startAnimation(anim);
        }


        btnStop.setOnClickListener(v -> {
            MessagingService.abortEmergency(NotificationActivity.this);
            Toast.makeText(NotificationActivity.this, "Allarme annullato.", Toast.LENGTH_SHORT).show();
            finish();
        });


        btnHelp.setOnClickListener(v -> {
            MessagingService.triggerImmediateEmergency(this);
            Toast.makeText(this, "Richiesta di aiuto inviata!", Toast.LENGTH_LONG).show();
            finish();
        });
    }
}
