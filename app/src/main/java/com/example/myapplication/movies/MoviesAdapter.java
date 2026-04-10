package com.example.myapplication.movies;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.example.myapplication.R;
import com.example.myapplication.util.ImageUrlNormalizer;

import java.util.ArrayList;
import java.util.List;

public class MoviesAdapter extends RecyclerView.Adapter<MoviesAdapter.VH> {

    public interface Listener {
        void onMovieClicked(@NonNull Movie movie);
    }

    private final List<Movie> items = new ArrayList<>();
    private final Listener listener;

    public MoviesAdapter(@NonNull List<Movie> initial, @NonNull Listener listener) {
        this.items.addAll(initial);
        this.listener = listener;
    }

    public void setItems(@NonNull List<Movie> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_movie, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Movie m = items.get(position);
        holder.tvTitle.setText(m.title != null ? m.title : "");

        String meta = "";
        if (m.durationMinutes != null && m.durationMinutes > 0) meta += m.durationMinutes + " phút";
        if (m.rating != null && m.rating > 0) meta += (meta.isEmpty() ? "" : " • ") + "⭐ " + m.rating;
        holder.tvMeta.setText(meta);
        holder.tvMeta.setVisibility(meta.isEmpty() ? View.GONE : View.VISIBLE);

        String url = ImageUrlNormalizer.posterUrlForGlide(m.posterUrl);
        int cornerPx = (int) (12 * holder.itemView.getResources().getDisplayMetrics().density + 0.5f);
        RequestOptions opts = new RequestOptions()
                .transform(new RoundedCorners(cornerPx))
                .placeholder(R.drawable.poster_placeholder)
                .error(R.drawable.poster_placeholder);

        if (url.isEmpty()) {
            holder.ivPoster.setImageResource(R.drawable.poster_placeholder);
        } else {
            Glide.with(holder.ivPoster)
                    .load(url)
                    .apply(opts)
                    .into(holder.ivPoster);
        }

        holder.itemView.setOnClickListener(v -> listener.onMovieClicked(m));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView ivPoster;
        TextView tvTitle;
        TextView tvMeta;

        VH(@NonNull View itemView) {
            super(itemView);
            ivPoster = itemView.findViewById(R.id.ivPoster);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvMeta = itemView.findViewById(R.id.tvMeta);
        }
    }
}
