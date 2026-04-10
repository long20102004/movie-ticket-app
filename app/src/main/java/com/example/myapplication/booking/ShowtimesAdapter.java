package com.example.myapplication.booking;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;

import java.util.ArrayList;
import java.util.List;

public class ShowtimesAdapter extends RecyclerView.Adapter<ShowtimesAdapter.VH> {

    public interface Listener {
        void onShowtimeClicked(@NonNull Showtime showtime);
    }

    private final List<Showtime> items = new ArrayList<>();
    private final Listener listener;

    public ShowtimesAdapter(@NonNull List<Showtime> initial, @NonNull Listener listener) {
        items.addAll(initial);
        this.listener = listener;
    }

    public void setItems(@NonNull List<Showtime> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_showtime, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Showtime s = items.get(position);

        String theaterLine = (s.theaterName != null ? s.theaterName : "Theater");
        if (s.theaterAddress != null && !s.theaterAddress.trim().isEmpty()) {
            theaterLine += " • " + s.theaterAddress.trim();
        }
        holder.tvTheater.setText(theaterLine);

        holder.tvTime.setText(s.startTime != null ? ShowtimesActivity.formatTs(s.startTime) : "");
        holder.tvPrice.setText(s.price != null ? String.format("%,.0f đ", s.price) : "");

        holder.itemView.setOnClickListener(v -> listener.onShowtimeClicked(s));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvTheater;
        TextView tvTime;
        TextView tvPrice;

        VH(@NonNull View itemView) {
            super(itemView);
            tvTheater = itemView.findViewById(R.id.tvTheater);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvPrice = itemView.findViewById(R.id.tvPrice);
        }
    }
}

