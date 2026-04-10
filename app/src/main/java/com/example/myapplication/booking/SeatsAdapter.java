package com.example.myapplication.booking;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;

import java.util.ArrayList;
import java.util.List;

public class SeatsAdapter extends RecyclerView.Adapter<SeatsAdapter.VH> {

    public enum SeatState {
        AVAILABLE,
        SELECTED,
        BOOKED
    }

    public static class SeatCell {
        @NonNull public final String id;
        @NonNull public SeatState state;

        public SeatCell(@NonNull String id, @NonNull SeatState state) {
            this.id = id;
            this.state = state;
        }
    }

    public interface Listener {
        void onSelectionChanged();
    }

    private final List<SeatCell> cells = new ArrayList<>();
    private final Listener listener;

    public SeatsAdapter(@NonNull Listener listener) {
        this.listener = listener;
    }

    public void setSeats(@NonNull List<String> allIds, @NonNull List<String> bookedIds) {
        cells.clear();
        for (String id : allIds) {
            boolean booked = bookedIds != null && bookedIds.contains(id);
            cells.add(new SeatCell(id, booked ? SeatState.BOOKED : SeatState.AVAILABLE));
        }
        notifyDataSetChanged();
    }

    public void clearSelection() {
        boolean changed = false;
        for (SeatCell c : cells) {
            if (c.state == SeatState.SELECTED) {
                c.state = SeatState.AVAILABLE;
                changed = true;
            }
        }
        if (changed) notifyDataSetChanged();
    }

    @NonNull
    public List<String> getSelectedSeatIds() {
        List<String> out = new ArrayList<>();
        for (SeatCell c : cells) {
            if (c.state == SeatState.SELECTED) out.add(c.id);
        }
        return SeatHallConfig.sortSeatIds(out);
    }

    public int getSelectedCount() {
        int n = 0;
        for (SeatCell c : cells) {
            if (c.state == SeatState.SELECTED) n++;
        }
        return n;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_seat, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        SeatCell cell = cells.get(position);
        holder.tv.setText(cell.id);

        switch (cell.state) {
            case BOOKED:
                holder.tv.setBackgroundResource(R.drawable.seat_booked);
                holder.tv.setTextColor(Color.parseColor("#888899"));
                holder.tv.setAlpha(0.85f);
                break;
            case SELECTED:
                holder.tv.setBackgroundResource(R.drawable.seat_selected);
                holder.tv.setTextColor(Color.parseColor("#1A1208"));
                holder.tv.setAlpha(1f);
                break;
            case AVAILABLE:
            default:
                holder.tv.setBackgroundResource(R.drawable.seat_available);
                holder.tv.setTextColor(Color.parseColor("#E8E8F0"));
                holder.tv.setAlpha(1f);
                break;
        }

        holder.itemView.setOnClickListener(v -> {
            int pos = holder.getBindingAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return;
            SeatCell c = cells.get(pos);
            if (c.state == SeatState.BOOKED) return;
            if (c.state == SeatState.AVAILABLE) {
                c.state = SeatState.SELECTED;
            } else {
                c.state = SeatState.AVAILABLE;
            }
            notifyItemChanged(pos);
            listener.onSelectionChanged();
        });
    }

    @Override
    public int getItemCount() {
        return cells.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final TextView tv;

        VH(@NonNull View itemView) {
            super(itemView);
            tv = itemView.findViewById(R.id.tvSeat);
        }
    }
}
