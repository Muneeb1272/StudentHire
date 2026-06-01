package com.hirenest.app;

import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ApplicantsAdapter extends
        RecyclerView.Adapter<ApplicantsAdapter.ViewHolder> {

    List<Map<String, Object>> list;
    boolean showScore;

    public ApplicantsAdapter(List<Map<String, Object>> list,
                             boolean showScore) {
        this.list = list;
        this.showScore = showScore;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_applicant, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder,
                                 int position) {
        Map<String, Object> app = list.get(position);

        holder.tvName.setText(
                app.get("studentName") != null ?
                        app.get("studentName").toString() : "Unknown");
        holder.tvUniversity.setText("🎓 " +
                (app.get("university") != null ?
                        app.get("university").toString() : "N/A"));
        holder.tvDegree.setText("📚 " +
                (app.get("degree") != null ?
                        app.get("degree").toString() : "N/A"));
        holder.tvEmail.setText("✉️ " +
                (app.get("email") != null ?
                        app.get("email").toString() : "N/A"));

        if (showScore && app.get("score") != null) {
            int score = Integer.parseInt(
                    app.get("score").toString());
            holder.tvScore.setVisibility(View.VISIBLE);
            holder.tvScore.setText("AI Score: " +
                    score + "/100");
            holder.tvScore.setTextColor(
                    score >= 70 ? 0xFF4CAF50 :
                            score >= 50 ? 0xFFF4A836 : 0xFFE53935);

            if (app.get("reason") != null) {
                holder.tvReason.setVisibility(View.VISIBLE);
                holder.tvReason.setText(
                        app.get("reason").toString());
            }
        } else {
            holder.tvScore.setVisibility(View.GONE);
            holder.tvReason.setVisibility(View.GONE);
        }

        // View CV
        holder.btnViewCv.setOnClickListener(v -> {
            String cvUrl = app.get("cvUrl") != null ?
                    app.get("cvUrl").toString() : "";
            if (!cvUrl.isEmpty()) {
                // Convert PDF to viewable image
                String viewUrl = cvUrl
                        .replace("/raw/upload/", "/image/upload/")
                        .replace(".pdf", ".jpg");
                Intent intent = new Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(viewUrl));
                v.getContext().startActivity(intent);
            } else {
                Toast.makeText(v.getContext(),
                        "No CV uploaded",
                        Toast.LENGTH_SHORT).show();
            }
        });

        // Shortlist
        holder.btnShortlist.setOnClickListener(v -> {
            Object appIdObj    = app.get("applicationId");
            Object studentIdObj = app.get("studentId");
            if (appIdObj == null || studentIdObj == null) {
                Toast.makeText(v.getContext(),
                        "Cannot shortlist: missing data",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            String appId    = appIdObj.toString();
            String studentId = studentIdObj.toString();
            String jobTitle = app.get("jobTitle") != null ? app.get("jobTitle").toString() : "";
            String company  = app.get("company")  != null ? app.get("company").toString()  : "";

            FirebaseFirestore db =
                    FirebaseFirestore.getInstance();
            db.collection("applications").document(appId)
                    .update("status", "Shortlisted")
                    .addOnSuccessListener(aVoid -> {
                        holder.btnShortlist.setText(
                                "✅ Shortlisted");
                        holder.btnShortlist.setEnabled(false);

                        Map<String, Object> notif =
                                new HashMap<>();
                        notif.put("studentId", studentId);
                        notif.put("message",
                                "Congratulations! You have been " +
                                        "shortlisted for " + jobTitle +
                                        " at " + company + "!");
                        notif.put("jobTitle", jobTitle);
                        notif.put("company", company);
                        notif.put("read", false);
                        notif.put("timestamp",
                                System.currentTimeMillis());

                        db.collection("notifications")
                                .add(notif);

                        Toast.makeText(v.getContext(),
                                "Student shortlisted! ✅",
                                Toast.LENGTH_SHORT).show();
                    });
        });
    }

    @Override
    public int getItemCount() { return list.size(); }

    public static class ViewHolder
            extends RecyclerView.ViewHolder {
        TextView tvName, tvUniversity, tvDegree,
                tvEmail, tvScore, tvReason;
        Button btnViewCv, btnShortlist;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvUniversity = itemView.findViewById(
                    R.id.tvUniversity);
            tvDegree = itemView.findViewById(R.id.tvDegree);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            tvScore = itemView.findViewById(R.id.tvScore);
            tvReason = itemView.findViewById(R.id.tvReason);
            btnViewCv = itemView.findViewById(
                    R.id.btnViewCv);
            btnShortlist = itemView.findViewById(
                    R.id.btnShortlist);
        }
    }
}