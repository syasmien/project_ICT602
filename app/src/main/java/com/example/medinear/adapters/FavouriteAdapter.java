package com.example.medinear.adapters;

import android.content.Context;
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
import com.example.medinear.utils.ImageStorageHelper;
import com.example.medinear.utils.LocalStorageHelper;

import java.util.List;

public class FavouriteAdapter extends RecyclerView.Adapter<FavouriteAdapter.ViewHolder> {

    private Context context;
    private List<Medicine> medicineList;
    private LocalStorageHelper storageHelper;
    private ImageStorageHelper imageStorageHelper;
    private OnItemClickListener onItemClickListener;
    private OnDeleteClickListener onDeleteClickListener;

    public interface OnItemClickListener {
        void onItemClick(Medicine medicine);
    }

    public interface OnDeleteClickListener {
        void onDeleteClick(Medicine medicine);
    }

    public FavouriteAdapter(Context context, List<Medicine> medicineList) {
        this.context = context;
        this.medicineList = medicineList;
        this.storageHelper = new LocalStorageHelper(context);
        this.imageStorageHelper = new ImageStorageHelper(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_favourite, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Medicine medicine = medicineList.get(position);

        holder.tvName.setText(medicine.getName());
        holder.tvTimestamp.setText(medicine.getTimestamp());

        if (medicine.getNotes() != null && !medicine.getNotes().isEmpty()) {
            holder.tvNotes.setText(medicine.getNotes());
            holder.tvNotes.setVisibility(View.VISIBLE);
        } else {
            holder.tvNotes.setVisibility(View.GONE);
        }

        // Load image
        if (medicine.getImagePath() != null && !medicine.getImagePath().isEmpty()) {
            Bitmap bitmap = imageStorageHelper.loadImage(medicine.getImagePath());
            if (bitmap != null) {
                holder.ivImage.setImageBitmap(bitmap);
            } else {
                holder.ivImage.setImageResource(android.R.drawable.ic_menu_gallery);
            }
        } else {
            holder.ivImage.setImageResource(android.R.drawable.ic_menu_gallery);
        }

        // Item click
        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(medicine);
            }
        });

        // Delete button click
        holder.btnDelete.setOnClickListener(v -> {
            if (onDeleteClickListener != null) {
                onDeleteClickListener.onDeleteClick(medicine);
            }
        });
    }

    @Override
    public int getItemCount() {
        return medicineList != null ? medicineList.size() : 0;
    }

    public void updateData(List<Medicine> newList) {
        this.medicineList = newList;
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    public void setOnDeleteClickListener(OnDeleteClickListener listener) {
        this.onDeleteClickListener = listener;
    }

    // ===================== VIEWHOLDER =====================
    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;
        TextView tvName, tvTimestamp, tvNotes;
        ImageButton btnDelete;  // ← ImageButton (matches your layout)

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // Using YOUR IDs from item_favourite.xml
            ivImage = itemView.findViewById(R.id.ivMedicine);
            tvName = itemView.findViewById(R.id.tvMedicineName);
            tvNotes = itemView.findViewById(R.id.tvMedicineNotes);
            tvTimestamp = itemView.findViewById(R.id.tvMedicineTime);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}