package com.example.myapplication.booking;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ShowtimesActivity extends AppCompatActivity implements ShowtimesAdapter.Listener {

    private static final String EXTRA_MOVIE_ID = "movieId";
    private static final String EXTRA_MOVIE_TITLE = "movieTitle";

    public static Intent newIntent(@NonNull Context ctx, @NonNull String movieId, @NonNull String movieTitle) {
        Intent i = new Intent(ctx, ShowtimesActivity.class);
        i.putExtra(EXTRA_MOVIE_ID, movieId);
        i.putExtra(EXTRA_MOVIE_TITLE, movieTitle);
        return i;
    }

    private String movieId;
    private String movieTitle;

    private TextView tvHeader;
    private RecyclerView rv;
    private TextView tvEmpty;
    private ProgressBar progress;

    private ShowtimesAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_showtimes);

        movieId = getIntent().getStringExtra(EXTRA_MOVIE_ID);
        movieTitle = getIntent().getStringExtra(EXTRA_MOVIE_TITLE);
        if (movieId == null || movieId.trim().isEmpty()) {
            finish();
            return;
        }
        if (movieTitle == null) movieTitle = "Showtimes";

        tvHeader = findViewById(R.id.tvHeader);
        rv = findViewById(R.id.rvShowtimes);
        tvEmpty = findViewById(R.id.tvEmpty);
        progress = findViewById(R.id.progress);

        tvHeader.setText("Showtimes • " + movieTitle);

        adapter = new ShowtimesAdapter(new ArrayList<>(), this);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        loadShowtimes();
    }

    private void setLoading(boolean loading) {
        progress.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    private void showEmpty(boolean empty) {
        tvEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        rv.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    private void loadShowtimes() {
        setLoading(true);
        showEmpty(false);

        FirebaseFirestore.getInstance()
                .collection("showtimes")
                .whereEqualTo("movieId", movieId)
                .get()
                .addOnCompleteListener(task -> {
                    setLoading(false);
                    if (!task.isSuccessful()) {
                        String msg = task.getException() != null ? task.getException().getMessage() : "Load showtimes failed";
                        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
                        showEmpty(true);
                        return;
                    }

                    List<Showtime> list = new ArrayList<>();
                    for (DocumentSnapshot d : task.getResult().getDocuments()) {
                        Showtime s = d.toObject(Showtime.class);
                        if (s == null) continue;
                        s.id = d.getId();
                        if (s.startTime == null) continue;
                        if (s.theaterName == null || s.theaterName.trim().isEmpty()) continue;
                        if (s.theaterId == null || s.theaterId.trim().isEmpty()) continue;
                        if (s.price == null || s.price <= 0) continue;
                        list.add(s);
                    }

                    adapter.setItems(list);
                    showEmpty(list.isEmpty());
                });
    }

    @Override
    public void onShowtimeClicked(@NonNull Showtime showtime) {
        startActivity(BookingActivity.newIntent(this, movieId, movieTitle, showtime.id));
    }

    public static String formatTs(@NonNull com.google.firebase.Timestamp ts) {
        Date d = ts.toDate();
        return new SimpleDateFormat("EEE, dd/MM • HH:mm", Locale.getDefault()).format(d);
    }
}

