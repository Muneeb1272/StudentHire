package com.hirenest.app;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
public class JobDetailActivity extends AppCompatActivity {

    TextView tvTitle, tvCompany, tvLocation, tvSalary,
            tvType, tvDescription, tvRequirements, tvBack;
    Button btnApply;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_job_detail);

        // Get job data passed from home screen
        String title = getIntent().getStringExtra("title");
        String company = getIntent().getStringExtra("company");
        String location = getIntent().getStringExtra("location");
        String salary = getIntent().getStringExtra("salary");
        String type = getIntent().getStringExtra("type");
        String description = getIntent().getStringExtra("description");
        String requirements = getIntent().getStringExtra("requirements");

        // Connect to XML views
        tvTitle = findViewById(R.id.tvTitle);
        tvCompany = findViewById(R.id.tvCompany);
        tvLocation = findViewById(R.id.tvLocation);
        tvSalary = findViewById(R.id.tvSalary);
        tvType = findViewById(R.id.tvType);
        tvDescription = findViewById(R.id.tvDescription);
        tvRequirements = findViewById(R.id.tvRequirements);
        btnApply = findViewById(R.id.btnApply);
        tvBack = findViewById(R.id.tvBack);
        tvBack.setOnClickListener(v -> finish());

        // Set data to views
        tvTitle.setText(title);
        tvCompany.setText(company);
        tvLocation.setText("📍 " + location);
        tvSalary.setText("💰 " + salary);
        tvType.setText(type);

        // Set description
        tvDescription.setText(description != null ?
                description : "No description available.");

        // Set requirements
        tvRequirements.setText(requirements != null ?
                requirements : "No requirements listed.");

        // Apply button
        btnApply.setOnClickListener(v -> {
            Intent intent = new Intent(JobDetailActivity.this,
                    ApplyActivity.class);
            intent.putExtra("title", title);
            intent.putExtra("company", company);
            intent.putExtra("jobId", getIntent().getStringExtra("jobId"));
            intent.putExtra("description", description);
            intent.putExtra("requirements", requirements);
            startActivity(intent);
        });
    }
}