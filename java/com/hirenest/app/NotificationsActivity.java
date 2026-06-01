package com.hirenest.app;

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
import java.util.Map;

public class NotificationsActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    TextView tvBack, tvEmpty;
    FirebaseFirestore db;
    FirebaseAuth mAuth;
    List<Map<String, Object>> notifList;
    NotificationsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        notifList = new ArrayList<>();

        tvBack = findViewById(R.id.tvBack);
        tvEmpty = findViewById(R.id.tvEmpty);
        recyclerView = findViewById(R.id.recyclerView);

        tvBack.setOnClickListener(v -> finish());

        adapter = new NotificationsAdapter(notifList);
        recyclerView.setLayoutManager(
                new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        loadNotifications();
    }

    private void loadNotifications() {
        if (mAuth.getCurrentUser() == null) return;
        String uid = mAuth.getCurrentUser().getUid();

        db.collection("notifications")
                .whereEqualTo("studentId", uid)
                .orderBy("timestamp",
                        Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(snap -> {
                    notifList.clear();
                    for (var doc : snap.getDocuments()) {
                        notifList.add(doc.getData());
                    }
                    adapter.notifyDataSetChanged();

                    if (notifList.isEmpty()) {
                        tvEmpty.setVisibility(
                                android.view.View.VISIBLE);
                    } else {
                        tvEmpty.setVisibility(
                                android.view.View.GONE);
                    }
                })
                .addOnFailureListener(e -> {
                    // Firestore index may be missing — show empty state
                    tvEmpty.setVisibility(android.view.View.VISIBLE);
                    tvEmpty.setText("Could not load notifications.");
                });
    }
}