package com.hirenest.app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;

public class RoleSelectionActivity extends AppCompatActivity {

    LinearLayout cardStudent, cardRecruiter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_role_selection);

        cardStudent = findViewById(R.id.cardStudent);
        cardRecruiter = findViewById(R.id.cardRecruiter);

        // Already have account — go to login
        findViewById(R.id.tvLogin).setOnClickListener(v ->
                startActivity(new Intent(this, LoginActivity.class)));

        // Student card clicked
        cardStudent.setOnClickListener(v -> {
            Intent intent = new Intent(this, StudentRegisterActivity.class);
            startActivity(intent);
        });

        // Recruiter card clicked
        cardRecruiter.setOnClickListener(v -> {
            Intent intent = new Intent(this, RecruiterRegisterActivity.class);
            startActivity(intent);
        });
    }
}