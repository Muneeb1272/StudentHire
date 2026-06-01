package com.hirenest.app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.ArrayList;
import java.util.List;

public class RecruiterActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    TextView tvTotalJobs, tvTotalApplicants, tvActiveJobs;
    List<JobModel> jobList;
    JobAdapter jobAdapter;
    FirebaseFirestore db;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recruiter);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        tvTotalJobs = findViewById(R.id.tvTotalJobs);
        tvTotalApplicants = findViewById(R.id.tvTotalApplicants);
        tvActiveJobs = findViewById(R.id.tvActiveJobs);
        recyclerView = findViewById(R.id.recyclerView);

        TextView tvBack = findViewById(R.id.tvBack);
        tvBack.setOnClickListener(v -> {
            // Sign out before returning to login
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        findViewById(R.id.btnPostJob).setOnClickListener(v ->
                startActivity(new Intent(this, PostJobActivity.class)));

        jobList = new ArrayList<>();
        jobAdapter = new JobAdapter(this, jobList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(jobAdapter);

        loadRecruiterJobs();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadRecruiterJobs();
    }

    private void loadRecruiterJobs() {
        String uid = mAuth.getCurrentUser().getUid();

        // Load total applicants count (separate collection)
        db.collection("applications")
                .get()
                .addOnSuccessListener(snap ->
                        tvTotalApplicants.setText(
                                String.valueOf(snap.size())));

        // Load recruiter's jobs — use ONE query for both count and list
        db.collection("jobs")
                .whereEqualTo("recruiterId", uid)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    jobList.clear();
                    for (var doc : querySnapshot.getDocuments()) {
                        JobModel job = new JobModel(
                                doc.getString("title"),
                                doc.getString("company"),
                                doc.getString("location"),
                                doc.getString("salary"),
                                doc.getString("type")
                        );
                        job.setJobId(doc.getId());
                        jobList.add(job);
                    }
                    jobAdapter.notifyDataSetChanged();

                    int count = jobList.size();
                    tvTotalJobs.setText(String.valueOf(count));
                    tvActiveJobs.setText(String.valueOf(count));
                });
    }
}