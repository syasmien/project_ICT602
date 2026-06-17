package com.example.medinear.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.medinear.R;
import com.example.medinear.models.Favourite;
import java.util.List;

public class FavouriteAdapter extends RecyclerView.Adapter<FavouriteAdapter.ViewHolder> {

    public interface OnActionListener {
        void onAction(Favourite favourite);
    }

    private final List<Favourite> favourites;
    private final OnActionListener navigateListener;
    private final OnActionListener deleteListener;

    public FavouriteAdapter(List<Favourite> favourites,
                             OnActionListener navigateListener,
                             OnActionListener deleteListener) {
        this.favourites       = favourites;
        this.navigateListener = navigateListener;
        this.deleteListener   = deleteListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_favourite, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Favourite f = favourites.get(position);
        holder.tvName.setText("🏥 " + f.getPharmacyName());
        holder.tvAddress.setText("📍 " + f.getAddress());
        holder.tvPhone.setText("📞 " + f.getPhone());
        holder.btnNavigate.setOnClickListener(v -> navigateListener.onAction(f));
        holder.btnDelete.setOnClickListener(v -> deleteListener.onAction(f));
    }

    @Override
    public int getItemCount() { return favourites.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvAddress, tvPhone;
        Button btnNavigate, btnDelete;

        ViewHolder(View itemView) {
            super(itemView);
            tvName      = itemView.findViewById(R.id.tvName);
            tvAddress   = itemView.findViewById(R.id.tvAddress);
            tvPhone     = itemView.findViewById(R.id.tvPhone);
            btnNavigate = itemView.findViewById(R.id.btnNavigate);
            btnDelete   = itemView.findViewById(R.id.btnDelete);
        }
    }
}
