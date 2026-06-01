package com.hirenest.app;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class NotificationsAdapter extends
        RecyclerView.Adapter<NotificationsAdapter.ViewHolder> {

    List<Map<String, Object>> list;

    public NotificationsAdapter(
            List<Map<String, Object>> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification,
                        parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder, int position) {
        Map<String, Object> notif = list.get(position);

        holder.tvMessage.setText(
                notif.get("message") != null ?
                        notif.get("message").toString() : "");

        holder.tvJob.setText(
                "💼 " + (notif.get("jobTitle") != null ?
                        notif.get("jobTitle").toString() : ""));

        holder.tvCompany.setText(
                "🏢 " + (notif.get("company") != null ?
                        notif.get("company").toString() : ""));

        // Format timestamp
        if (notif.get("timestamp") != null) {
            long timestamp = Long.parseLong(
                    notif.get("timestamp").toString());
            SimpleDateFormat sdf = new SimpleDateFormat(
                    "dd MMM yyyy hh:mm a", Locale.getDefault());
            holder.tvTime.setText(
                    sdf.format(new Date(timestamp)));
        }
        // Make notification tappable
        holder.itemView.setOnClickListener(v -> {
            // Mark as read
            android.widget.Toast.makeText(
                    v.getContext(),
                    "Application Status: Shortlisted ✅",
                    android.widget.Toast.LENGTH_LONG).show();
        });
    }

    @Override
    public int getItemCount() { return list.size(); }

    public static class ViewHolder
            extends RecyclerView.ViewHolder {
        TextView tvMessage, tvJob, tvCompany, tvTime;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(
                    R.id.tvMessage);
            tvJob = itemView.findViewById(R.id.tvJob);
            tvCompany = itemView.findViewById(
                    R.id.tvCompany);
            tvTime = itemView.findViewById(R.id.tvTime);
        }
    }
}