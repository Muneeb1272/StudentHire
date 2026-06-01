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

public class PostJobActivity extends AppCompatActivity {

    EditText etTitle, etSalary, etLocation,
            etDescription, etRequirements, etType;
    Button btnPost;
    TextView tvBack;
    FirebaseFirestore db;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_job);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        etTitle = findViewById(R.id.etTitle);
        etSalary = findViewById(R.id.etSalary);
        etLocation = findViewById(R.id.etLocation);
        etDescription = findViewById(R.id.etDescription);
        etRequirements = findViewById(R.id.etRequirements);
        etType = findViewById(R.id.etType);
        btnPost = findViewById(R.id.btnPost);
        tvBack = findViewById(R.id.tvBack);

        tvBack.setOnClickListener(v -> finish());

        btnPost.setOnClickListener(v -> {
            String title = etTitle.getText().toString().trim();
            String salary = etSalary.getText().toString().trim();
            String location = etLocation.getText().toString().trim();
            String description = etDescription.getText().toString().trim();
            String requirements = etRequirements.getText().toString().trim();
            String type = etType.getText().toString().trim();

            if (title.isEmpty() || salary.isEmpty() ||
                    location.isEmpty() || description.isEmpty() ||
                    requirements.isEmpty() || type.isEmpty()) {
                Toast.makeText(this,
                        "Please fill all fields",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            btnPost.setText("Posting...");
            btnPost.setEnabled(false);

            String uid = mAuth.getCurrentUser().getUid();

            // Get recruiter company name
            db.collection("users").document(uid)
                    .get()
                    .addOnSuccessListener(doc -> {
                        String company = doc.getString("company");
                        String recruiterName = doc.getString("name");

                        Map<String, Object> job = new HashMap<>();
                        job.put("title", title);
                        job.put("company", company);
                        job.put("salary", salary);
                        job.put("location", location);
                        job.put("description", description);
                        job.put("requirements", requirements);
                        job.put("type", type);
                        job.put("recruiterId", uid);
                        job.put("recruiterName", recruiterName);
                        job.put("timestamp",
                                System.currentTimeMillis());
                        job.put("active", true);

                        db.collection("jobs")
                                .add(job)
                                .addOnSuccessListener(ref -> {
                                    Toast.makeText(this,
                                            "Job posted successfully! ✅",
                                            Toast.LENGTH_LONG).show();
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    btnPost.setText("POST JOB NOW");
                                    btnPost.setEnabled(true);
                                    Toast.makeText(this,
                                            "Error: " + e.getMessage(),
                                            Toast.LENGTH_LONG).show();
                                });
                    });
        });
    }
}