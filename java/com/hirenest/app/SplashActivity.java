package com.hirenest.app;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser == null) {
                // Not logged in — go to role selection
                startActivity(new Intent(SplashActivity.this, RoleSelectionActivity.class));
                finish();
            } else {
                // Already logged in — look up role in Firestore and route correctly
                FirebaseFirestore.getInstance()
                        .collection("users")
                        .document(currentUser.getUid())
                        .get()
                        .addOnSuccessListener(doc -> {
                            String role = doc.getString("role");
                            Intent intent;
                            if ("recruiter".equals(role)) {
                                intent = new Intent(SplashActivity.this, RecruiterActivity.class);
                            } else {
                                intent = new Intent(SplashActivity.this, HomeActivity.class);
                            }
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            // Can't read role → go to login
                            startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                            finish();
                        });
            }
        }, 2500);
    }
}