package com.example.medinear.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.medinear.R;
import com.example.medinear.models.Pharmacy;
import java.util.List;

public class PharmacyAdapter extends RecyclerView.Adapter<PharmacyAdapter.ViewHolder> {

    public enum Mode { NEARBY, FAVOURITES }

    private List<Pharmacy> pharmacies;
    private final Mode mode;
    private OnItemClickListener listener;
    private OnAddFavouriteClickListener addFavouriteListener;
    private OnDeleteClickListener deleteListener;

    public interface OnItemClickListener {
        void onItemClick(Pharmacy pharmacy);
    }

    public interface OnAddFavouriteClickListener {
        void onAddFavouriteClick(Pharmacy pharmacy);
    }

    public interface OnDeleteClickListener {
        void onDeleteClick(Pharmacy pharmacy);
    }

    public PharmacyAdapter(List<Pharmacy> pharmacies, Mode mode) {
        this.pharmacies = pharmacies;
        this.mode = mode;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setOnAddFavouriteClickListener(OnAddFavouriteClickListener listener) {
        this.addFavouriteListener = listener;
    }

    public void setOnDeleteClickListener(OnDeleteClickListener listener) {
        this.deleteListener = listener;
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

        if (mode == Mode.NEARBY) {
            bindNearbyButton(holder, pharmacy);
        } else {
            bindFavouritesButton(holder, pharmacy);
        }
    }

    private void bindNearbyButton(ViewHolder holder, Pharmacy pharmacy) {
        int primaryColor = holder.itemView.getContext().getColor(R.color.primary_color);
        int grayColor = holder.itemView.getContext().getColor(android.R.color.darker_gray);

        if (pharmacy.isFavourite()) {
            holder.btnFavouriteAction.setText("✓ Added to Favourites");
            holder.btnFavouriteAction.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(grayColor));
            holder.btnFavouriteAction.setEnabled(false);
        } else {
            holder.btnFavouriteAction.setText("Add to My Favourite");
            holder.btnFavouriteAction.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(primaryColor));
            holder.btnFavouriteAction.setEnabled(true);
        }

        holder.btnFavouriteAction.setOnClickListener(v -> {
            pharmacy.setFavourite(true);
            holder.btnFavouriteAction.setText("✓ Added to Favourites");
            holder.btnFavouriteAction.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(grayColor));
            holder.btnFavouriteAction.setEnabled(false);

            if (addFavouriteListener != null) {
                addFavouriteListener.onAddFavouriteClick(pharmacy);
            }
        });
    }

    private void bindFavouritesButton(ViewHolder holder, Pharmacy pharmacy) {
        int redColor = holder.itemView.getContext().getColor(R.color.red);

        holder.btnFavouriteAction.setText("Delete");
        holder.btnFavouriteAction.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(redColor));
        holder.btnFavouriteAction.setEnabled(true);

        holder.btnFavouriteAction.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onDeleteClick(pharmacy);
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
        Button btnFavouriteAction;

        ViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvPharmacyName);
            tvAddress = itemView.findViewById(R.id.tvPharmacyAddress);
            tvDistance = itemView.findViewById(R.id.tvPharmacyDistance);
            tvStatus = itemView.findViewById(R.id.tvPharmacyStatus);
            ratingBar = itemView.findViewById(R.id.ratingBar);
            btnFavouriteAction = itemView.findViewById(R.id.btnFavouriteAction);
        }
    }
}