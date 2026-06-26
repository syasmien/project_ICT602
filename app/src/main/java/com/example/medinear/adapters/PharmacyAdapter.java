package com.example.medinear.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.medinear.R;
import com.example.medinear.models.Pharmacy;
import java.util.List;

public class PharmacyAdapter extends RecyclerView.Adapter<PharmacyAdapter.ViewHolder> {

    private List<Pharmacy> pharmacies;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Pharmacy pharmacy);
    }

    public PharmacyAdapter(List<Pharmacy> pharmacies) {
        this.pharmacies = pharmacies;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pharmacy, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Pharmacy pharmacy = pharmacies.get(position);

        holder.tvName.setText(pharmacy.getName());
        holder.tvAddress.setText(pharmacy.getAddress());
        holder.tvDistance.setText(String.format("%.1f km away", pharmacy.getDistance()));
        holder.tvStatus.setText(pharmacy.isOpenNow() ? "🟢 Open Now" : "🔴 Closed");
        holder.tvStatus.setTextColor(pharmacy.isOpenNow() ?
                holder.itemView.getContext().getColor(R.color.green) :
                holder.itemView.getContext().getColor(R.color.red));

        if (pharmacy.getRating() > 0) {
            holder.ratingBar.setVisibility(View.VISIBLE);
            holder.ratingBar.setRating(pharmacy.getRating());
        } else {
            holder.ratingBar.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(pharmacy);
            }
        });
    }

    @Override
    public int getItemCount() {
        return pharmacies != null ? pharmacies.size() : 0;
    }

    public void updateData(List<Pharmacy> newPharmacies) {
        this.pharmacies = newPharmacies;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvAddress, tvDistance, tvStatus;
        RatingBar ratingBar;

        ViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvPharmacyName);
            tvAddress = itemView.findViewById(R.id.tvPharmacyAddress);
            tvDistance = itemView.findViewById(R.id.tvPharmacyDistance);
            tvStatus = itemView.findViewById(R.id.tvPharmacyStatus);
            ratingBar = itemView.findViewById(R.id.ratingBar);
        }
    }
}