package com.hirenest.app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class StudentRegisterActivity extends AppCompatActivity {

    EditText etName, etEmail, etPassword,
            etPhone, etUniversity, etDegree;
    Button btnRegister;
    TextView tvLogin;
    FirebaseAuth mAuth;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_register);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etPhone = findViewById(R.id.etPhone);
        etUniversity = findViewById(R.id.etUniversity);
        etDegree = findViewById(R.id.etDegree);
        btnRegister = findViewById(R.id.btnRegister);
        tvLogin = findViewById(R.id.tvLogin);

        findViewById(R.id.tvBack).setOnClickListener(v -> finish());

        btnRegister.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();
            String university = etUniversity.getText().toString().trim();
            String degree = etDegree.getText().toString().trim();

            if (name.isEmpty() || email.isEmpty() ||
                    password.isEmpty() || phone.isEmpty() ||
                    university.isEmpty() || degree.isEmpty()) {
                Toast.makeText(this,
                        "Please fill all fields",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            if (password.length() < 6) {
                Toast.makeText(this,
                        "Password must be at least 6 characters",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            btnRegister.setText("Creating account...");
            btnRegister.setEnabled(false);

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            String uid = mAuth.getCurrentUser().getUid();

                            Map<String, Object> user = new HashMap<>();
                            user.put("name", name);
                            user.put("email", email);
                            user.put("phone", phone);
                            user.put("university", university);
                            user.put("degree", degree);
                            user.put("role", "student");
                            user.put("uid", uid);
                            user.put("cvUrl", "");

                            db.collection("users").document(uid)
                                    .set(user)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(this,
                                                "Account created! Please login.",
                                                Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(this,
                                                LoginActivity.class));
                                        finish();
                                    })
                                    .addOnFailureListener(e -> {
                                        btnRegister.setText("CREATE STUDENT ACCOUNT");
                                        btnRegister.setEnabled(true);
                                        Toast.makeText(this,
                                                "Error: " + e.getMessage(),
                                                Toast.LENGTH_LONG).show();
                                    });
                        } else {
                            btnRegister.setText("CREATE STUDENT ACCOUNT");
                            btnRegister.setEnabled(true);
                            Toast.makeText(this,
                                    "Error: " + task.getException().getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
        });

        tvLogin.setOnClickListener(v ->
                startActivity(new Intent(this, LoginActivity.class)));
    }
}