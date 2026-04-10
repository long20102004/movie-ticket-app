package com.example.myapplication.movies;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.booking.ShowtimesActivity;
import com.example.myapplication.data.SeedDataHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.List;

public class MoviesActivity extends AppCompatActivity implements MoviesAdapter.Listener {

    private static final int REQ_NOTIF = 1001;

    public static Intent newIntent(Context ctx) {
        return new Intent(ctx, MoviesActivity.class);
    }

    private RecyclerView rv;
    private ProgressBar progress;
    private TextView tvEmpty;

    private MoviesAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movies);

        rv = findViewById(R.id.rvMovies);
        progress = findViewById(R.id.progress);
        tvEmpty = findViewById(R.id.tvEmpty);

        adapter = new MoviesAdapter(new ArrayList<>(), this);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        requestNotifPermissionIfNeeded();
        syncFcmTokenIfLoggedIn();

        setLoading(true);
        SeedDataHelper.maybeSeedSampleData(this, this::loadMovies);
    }

    private void requestNotifPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < 33) return;
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            return;
        }
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQ_NOTIF);
    }

    private void syncFcmTokenIfLoggedIn() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        FirebaseMessaging.getInstance().getToken()
                .addOnSuccessListener(token -> {
                    if (token == null || token.trim().isEmpty()) return;
                    FirebaseFirestore.getInstance()
                            .collection("users")
                            .document(user.getUid())
                            .update("fcmToken", token, "updatedAt", com.google.firebase.firestore.FieldValue.serverTimestamp());
                });
    }

    private void setLoading(boolean loading) {
        progress.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    private void showEmpty(boolean empty) {
        tvEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        rv.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    private void loadMovies() {
        setLoading(true);
        showEmpty(false);

        FirebaseFirestore.getInstance()
                .collection("movies")
                .whereEqualTo("isActive", true)
                .get()
                .addOnCompleteListener(task -> {
                    setLoading(false);
                    if (!task.isSuccessful()) {
                        String msg = task.getException() != null ? task.getException().getMessage() : "Load movies failed";
                        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
                        showEmpty(true);
                        return;
                    }

                    List<Movie> movies = new ArrayList<>();
                    for (DocumentSnapshot d : task.getResult().getDocuments()) {
                        Movie m = d.toObject(Movie.class);
                        if (m == null) continue;
                        m.id = d.getId();
                        if (m.title == null || m.title.trim().isEmpty()) continue;
                        movies.add(m);
                    }

                    adapter.setItems(movies);
                    showEmpty(movies.isEmpty());
                });
    }

    @Override
    public void onMovieClicked(@NonNull Movie movie) {
        startActivity(ShowtimesActivity.newIntent(this, movie.id, movie.title));
    }
}

