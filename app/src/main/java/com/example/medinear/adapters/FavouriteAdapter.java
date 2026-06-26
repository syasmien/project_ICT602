package com.example.medinear.adapters;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.medinear.R;
import com.example.medinear.models.Medicine;
import com.example.medinear.utils.LocalStorageHelper;
import java.util.List;

public class FavouriteAdapter extends RecyclerView.Adapter<FavouriteAdapter.ViewHolder> {

    private List<Medicine> medicines;
    private LocalStorageHelper storageHelper;
    private OnItemDeleteListener deleteListener;

    public interface OnItemDeleteListener {
        void onDelete(Medicine medicine, int position);
    }

    public FavouriteAdapter(List<Medicine> medicines) {
        this.medicines = medicines;
    }

    public void setOnItemDeleteListener(OnItemDeleteListener listener) {
        this.deleteListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_favourite, parent, false);
        storageHelper = new LocalStorageHelper(parent.getContext());
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Medicine medicine = medicines.get(position);

        holder.tvName.setText(medicine.getName());
        holder.tvNotes.setText(medicine.getNotes() != null && !medicine.getNotes().isEmpty()
                ? medicine.getNotes() : "No notes");
        holder.tvTime.setText(medicine.getTimestamp() != null ? medicine.getTimestamp() : "");

        if (medicine.getImagePath() != null && !medicine.getImagePath().isEmpty()) {
            Bitmap bitmap = storageHelper.loadImage(medicine.getImagePath());
            if (bitmap != null) {
                holder.ivImage.setImageBitmap(bitmap);
            } else {
                holder.ivImage.setImageResource(R.drawable.ic_launcher_foreground);
            }
        } else {
            holder.ivImage.setImageResource(R.drawable.ic_launcher_foreground);
        }

        holder.btnDelete.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos != RecyclerView.NO_POSITION && deleteListener != null) {
                deleteListener.onDelete(medicine, pos);
            }
        });
    }

    @Override
    public int getItemCount() {
        return medicines != null ? medicines.size() : 0;
    }

    public void removeItem(int position) {
        if (medicines != null && position >= 0 && position < medicines.size()) {
            medicines.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, medicines.size());
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;
        TextView tvName, tvNotes, tvTime;
        ImageButton btnDelete;

        ViewHolder(View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.ivMedicine);
            tvName = itemView.findViewById(R.id.tvMedicineName);
            tvNotes = itemView.findViewById(R.id.tvMedicineNotes);
            tvTime = itemView.findViewById(R.id.tvMedicineTime);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}