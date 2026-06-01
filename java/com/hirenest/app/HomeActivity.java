package com.hirenest.app;

import android.os.Bundle;
import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    TextView tvWelcome;
    EditText etSearch;
    List<JobModel> jobList;
    List<JobModel> filteredList;
    JobAdapter jobAdapter;
    FirebaseFirestore db;

    // Store the base welcome string so filterJobs can restore it
    String baseWelcome = "Welcome, Student!";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        db = FirebaseFirestore.getInstance();

        tvWelcome = findViewById(R.id.tvWelcome);
        recyclerView = findViewById(R.id.recyclerView);
        etSearch = findViewById(R.id.etSearch);

        // Notification bell opens Notifications
        findViewById(R.id.tvNotifications)
                .setOnClickListener(v ->
                        startActivity(new Intent(this,
                                NotificationsActivity.class)));

        // Logout button signs out and returns to Login
        findViewById(R.id.tvLogout).setOnClickListener(v -> {
            com.google.firebase.auth.FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        // See All resets search
        findViewById(R.id.tvSeeAll).setOnClickListener(v -> {
            etSearch.setText("");
            filteredList.clear();
            filteredList.addAll(jobList);
            jobAdapter.setSearchQuery("");
            jobAdapter.notifyDataSetChanged();
            tvWelcome.setText(baseWelcome);
        });

        // Load user name from Firestore for a personal welcome
        com.google.firebase.auth.FirebaseAuth auth =
                com.google.firebase.auth.FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            db.collection("users").document(auth.getCurrentUser().getUid())
                    .get()
                    .addOnSuccessListener(doc -> {
                        String name = doc.getString("name");
                        if (name != null && !name.isEmpty()) {
                            baseWelcome = "Welcome, " + name.split(" ")[0] + "!";
                        } else {
                            baseWelcome = "Welcome, Student!";
                        }
                        tvWelcome.setText(baseWelcome);
                    });
        } else {
            tvWelcome.setText(baseWelcome);
        }

        jobList = new ArrayList<>();
        filteredList = new ArrayList<>();
        jobAdapter = new JobAdapter(this, filteredList);
        recyclerView.setLayoutManager(
                new LinearLayoutManager(this));
        recyclerView.setAdapter(jobAdapter);

        loadJobs();

        // Search functionality
        etSearch.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s,
                                          int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s,
                                      int start, int before, int count) {
                filterJobs(s.toString());
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });
    }

    private void filterJobs(String query) {
        filteredList.clear();
        if (query.isEmpty()) {
            filteredList.addAll(jobList);
            tvWelcome.setText(baseWelcome);
        } else {
            String lower = query.toLowerCase();
            for (JobModel job : jobList) {
                if (job.getTitle().toLowerCase()
                        .contains(lower) ||
                        job.getCompany().toLowerCase()
                                .contains(lower) ||
                        job.getLocation().toLowerCase()
                                .contains(lower) ||
                        job.getType().toLowerCase()
                                .contains(lower)) {
                    filteredList.add(job);
                }
            }
            if (filteredList.isEmpty()) {
                tvWelcome.setText(
                        "No jobs found for: " + query);
            } else {
                tvWelcome.setText(
                        filteredList.size() + " result(s) found");
            }
        }
        jobAdapter.setSearchQuery(query);
        jobAdapter.notifyDataSetChanged();
    }

    private void loadJobs() {
        db.collection("jobs")
                .orderBy("timestamp",
                        Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    jobList.clear();
                    for (var doc :
                            querySnapshot.getDocuments()) {
                        JobModel job = new JobModel(
                                doc.getString("title"),
                                doc.getString("company"),
                                doc.getString("location"),
                                doc.getString("salary"),
                                doc.getString("type")
                        );
                        job.setJobId(doc.getId());
                        job.setDescription(doc.getString("description"));
                        job.setRequirements(doc.getString("requirements"));
                        jobList.add(job);
                    }
                    filteredList.clear();
                    filteredList.addAll(jobList);
                    jobAdapter.notifyDataSetChanged();

                    if (jobList.isEmpty()) {
                        tvWelcome.setText(
                                "No jobs posted yet!");
                    }
                });
    }
}