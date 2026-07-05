package com.example.medinear.adapters;



import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.example.medinear.R;
import com.example.medinear.models.Reminder;
import java.util.List;

public class ReminderAdapter extends RecyclerView.Adapter<ReminderAdapter.ViewHolder> {

    public interface OnDeleteListener {
        void onDelete(Reminder reminder);
    }

    public interface OnEditListener {
        void onEdit(Reminder reminder);
    }

    public interface OnToggleActiveListener {
        void onToggle(Reminder reminder, boolean isActive);
    }

    private List<Reminder> reminders;
    private final OnDeleteListener deleteListener;
    private final OnEditListener editListener;
    private final OnToggleActiveListener toggleListener;

    public ReminderAdapter(List<Reminder> reminders,
                           OnDeleteListener deleteListener,
                           OnEditListener editListener,
                           OnToggleActiveListener toggleListener) {
        this.reminders = reminders;
        this.deleteListener = deleteListener;
        this.editListener = editListener;
        this.toggleListener = toggleListener;
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

        holder.tvMedicineName.setText(r.getMedicineName());
        holder.tvDosage.setText(r.getDosage());
        holder.tvTime.setText("⏰ " + r.getTime());

        // IMPORTANT: clear listener before setChecked to avoid firing
        // the toggle callback during RecyclerView view-recycling
        holder.switchActive.setOnCheckedChangeListener(null);
        holder.switchActive.setChecked(r.isActive());
        updateToggleLabel(holder, r.isActive());

        holder.switchActive.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateToggleLabel(holder, isChecked);
            if (toggleListener != null) {
                toggleListener.onToggle(r, isChecked);
            }
        });

        holder.tvEdit.setOnClickListener(v -> {
            if (editListener != null) {
                editListener.onEdit(r);
            }
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onDelete(r);
            }
        });
    }

    private void updateToggleLabel(ViewHolder holder, boolean isActive) {
        if (isActive) {
            holder.tvToggleLabel.setText("Notify: ON");
            holder.tvToggleLabel.setTextColor(0xFF16A34A);
        } else {
            holder.tvToggleLabel.setText("Notify: OFF");
            holder.tvToggleLabel.setTextColor(0xFF94A3B8);
        }
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
        TextView tvMedicineName, tvDosage, tvTime, tvEdit, btnDelete, tvToggleLabel;
        SwitchMaterial switchActive;

        ViewHolder(View itemView) {
            super(itemView);
            tvMedicineName = itemView.findViewById(R.id.tvMedicineName);
            tvDosage = itemView.findViewById(R.id.tvDosage);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvEdit = itemView.findViewById(R.id.tvEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            switchActive = itemView.findViewById(R.id.switchActive);
            tvToggleLabel = itemView.findViewById(R.id.tvToggleLabel);
        }
    }
}