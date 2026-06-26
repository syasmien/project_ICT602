package com.example.medinear.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.medinear.R;
import com.example.medinear.models.Reminder;
import java.util.List;

public class ReminderAdapter extends RecyclerView.Adapter<ReminderAdapter.ViewHolder> {

    public interface OnDeleteListener {
        void onDelete(Reminder reminder);
    }

    private List<Reminder> reminders;
    private final OnDeleteListener deleteListener;

    public ReminderAdapter(List<Reminder> reminders, OnDeleteListener deleteListener) {
        this.reminders = reminders;
        this.deleteListener = deleteListener;
        setHasStableIds(true);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_reminder, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (position >= reminders.size()) return;

        Reminder r = reminders.get(position);

        holder.tvMedicineName.setText("💊 " + r.getMedicineName());
        holder.tvDosage.setText("Dosage: " + r.getDosage());
        holder.tvTime.setText("⏰ " + r.getTime());

        // Show active status
        holder.tvStatus.setText(r.isActive() ? "🟢 Active" : "🔴 Inactive");

        holder.btnDelete.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onDelete(r);
            }
        });
    }

    @Override
    public int getItemCount() {
        return reminders != null ? reminders.size() : 0;
    }

    @Override
    public long getItemId(int position) {
        if (position >= 0 && position < reminders.size()) {
            Reminder reminder = reminders.get(position);
            if (reminder != null && reminder.getId() != null) {
                return reminder.getId().hashCode();
            }
        }
        return position;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvMedicineName, tvDosage, tvTime, tvStatus;
        Button btnDelete;

        ViewHolder(View itemView) {
            super(itemView);
            tvMedicineName = itemView.findViewById(R.id.tvMedicineName);
            tvDosage = itemView.findViewById(R.id.tvDosage);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}