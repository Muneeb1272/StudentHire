package com.hirenest.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    EditText etEmail, etPassword;
    RadioGroup rgRole;
    RadioButton rbStudent, rbRecruiter;
    Button btnLogin;
    TextView tvRegister;
    ProgressBar progressBar;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        etEmail    = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        rgRole     = findViewById(R.id.rgRole);
        rbStudent  = findViewById(R.id.rbStudent);
        rbRecruiter= findViewById(R.id.rbRecruiter);
        btnLogin   = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.tvRegister);
        progressBar = findViewById(R.id.progressBar);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email    = etEmail.getText().toString().trim();
                String password = etPassword.getText().toString().trim();

                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(LoginActivity.this,
                            "Please fill all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (rgRole.getCheckedRadioButtonId() == -1) {
                    Toast.makeText(LoginActivity.this,
                            "Please select Student or Recruiter",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                // Determine which role the user CHOSE on this screen
                boolean choseRecruiter = (rgRole.getCheckedRadioButtonId() == R.id.rbRecruiter);

                setLoading(true);

                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(task -> {
                            if (!task.isSuccessful()) {
                                setLoading(false);
                                String msg = task.getException() != null
                                        ? task.getException().getMessage()
                                        : "Login failed";
                                Toast.makeText(LoginActivity.this,
                                        "Login failed: " + msg,
                                        Toast.LENGTH_LONG).show();
                                return;
                            }

                            String uid = mAuth.getCurrentUser().getUid();

                            // Verify the real role in Firestore to prevent mixup
                            FirebaseFirestore.getInstance()
                                    .collection("users")
                                    .document(uid)
                                    .get()
                                    .addOnSuccessListener(doc -> {
                                        String realRole = doc.getString("role");

                                        if (realRole == null) {
                                            // No role in DB — sign out and show error
                                            mAuth.signOut();
                                            setLoading(false);
                                            Toast.makeText(LoginActivity.this,
                                                    "Account data not found. Please register again.",
                                                    Toast.LENGTH_LONG).show();
                                            return;
                                        }

                                        boolean isRecruiter = "recruiter".equals(realRole);

                                        // Cross-check: did they pick the correct role?
                                        if (choseRecruiter != isRecruiter) {
                                            mAuth.signOut();
                                            setLoading(false);
                                            String expected = isRecruiter ? "Recruiter" : "Student";
                                            Toast.makeText(LoginActivity.this,
                                                    "Wrong role selected. Your account is registered as: " + expected,
                                                    Toast.LENGTH_LONG).show();
                                            return;
                                        }

                                        Toast.makeText(LoginActivity.this,
                                                "Welcome back!", Toast.LENGTH_SHORT).show();

                                        Intent navIntent = isRecruiter
                                                ? new Intent(LoginActivity.this, RecruiterActivity.class)
                                                : new Intent(LoginActivity.this, HomeActivity.class);
                                        navIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                                                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(navIntent);
                                    })
                                    .addOnFailureListener(e -> {
                                        // Firestore read failed — sign out and surface the error
                                        mAuth.signOut();
                                        setLoading(false);
                                        Toast.makeText(LoginActivity.this,
                                                "Could not verify account. Check your internet connection.",
                                                Toast.LENGTH_LONG).show();
                                    });
                        });
            }
        });

        tvRegister.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, RoleSelectionActivity.class)));
    }

    private void setLoading(boolean loading) {
        btnLogin.setEnabled(!loading);
        btnLogin.setText(loading ? "Logging in..." : "LOGIN");
        if (progressBar != null) {
            progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        etEmail.setText("");
        etPassword.setText("");
        rgRole.clearCheck();
        setLoading(false);
    }
}