package com.hirenest.app;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.List;

public class JobAdapter extends RecyclerView.Adapter<JobAdapter.JobViewHolder> {

    Context context;
    List<JobModel> jobList;
    String userRole = "student";
    String searchQuery = "";

    public void setSearchQuery(String query) {
        this.searchQuery = query;
    }

    public JobAdapter(Context context, List<JobModel> jobList) {
        this.context = context;
        this.jobList = jobList;

        // Check user role from Firestore — guard against null user
        com.google.firebase.auth.FirebaseUser currentUser =
                FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(currentUser.getUid())
                    .get()
                    .addOnSuccessListener(doc -> {
                        String role = doc.getString("role");
                        if (role != null) {
                            userRole = role;
                        }
                    });
        }
    }

    @NonNull
    @Override
    public JobViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                            int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_job, parent, false);
        return new JobViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull JobViewHolder holder,
                                 int position) {
        JobModel job = jobList.get(position);

        highlightText(holder.tvTitle,
                job.getTitle(), searchQuery);
        highlightText(holder.tvCompany,
                job.getCompany(), searchQuery);
        holder.tvLocation.setText("📍 " + job.getLocation());
        holder.tvSalary.setText("💰 " + job.getSalary());
        holder.tvType.setText(job.getType());

        // Click on job card
        holder.itemView.setOnClickListener(v -> {
            if (userRole.equals("recruiter")) {
                // Recruiter sees applicants for this job
                Intent intent = new Intent(context,
                        ApplicantsActivity.class);
                intent.putExtra("jobId", job.getJobId());
                intent.putExtra("jobTitle", job.getTitle());
                context.startActivity(intent);
            } else {
                // Student sees job detail
                Intent intent = new Intent(context,
                        JobDetailActivity.class);
                intent.putExtra("title", job.getTitle());
                intent.putExtra("company", job.getCompany());
                intent.putExtra("location", job.getLocation());
                intent.putExtra("salary", job.getSalary());
                intent.putExtra("type", job.getType());
                intent.putExtra("jobId", job.getJobId());
                intent.putExtra("description", job.getDescription());
                intent.putExtra("requirements", job.getRequirements());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return jobList.size();
    }

    public static class JobViewHolder
            extends RecyclerView.ViewHolder {
        TextView tvTitle, tvCompany, tvLocation,
                tvSalary, tvType;

        public JobViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvCompany = itemView.findViewById(R.id.tvCompany);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvSalary = itemView.findViewById(R.id.tvSalary);
            tvType = itemView.findViewById(R.id.tvType);
        }
    }
    private void highlightText(TextView textView,
                               String text, String query) {
        if (query == null || query.isEmpty()) {
            textView.setText(text);
            return;
        }
        String lowerText = text.toLowerCase();
        String lowerQuery = query.toLowerCase();
        int startPos = lowerText.indexOf(lowerQuery);

        if (startPos >= 0) {
            android.text.SpannableString spannable =
                    new android.text.SpannableString(text);
            spannable.setSpan(
                    new android.text.style.ForegroundColorSpan(
                            0xFFF4A836),
                    startPos,
                    startPos + query.length(),
                    android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannable.setSpan(
                    new android.text.style.StyleSpan(
                            android.graphics.Typeface.BOLD),
                    startPos,
                    startPos + query.length(),
                    android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            textView.setText(spannable);
        } else {
            textView.setText(text);
        }
    }
}