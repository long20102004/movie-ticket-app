package com.example.myapplication.booking;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.notify.ShowtimeAlarmScheduler;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class BookingActivity extends AppCompatActivity implements SeatsAdapter.Listener {

    private static final String EXTRA_MOVIE_ID = "movieId";
    private static final String EXTRA_MOVIE_TITLE = "movieTitle";
    private static final String EXTRA_SHOWTIME_ID = "showtimeId";

    public static Intent newIntent(@NonNull Context ctx, @NonNull String movieId, @NonNull String movieTitle, @NonNull String showtimeId) {
        Intent i = new Intent(ctx, BookingActivity.class);
        i.putExtra(EXTRA_MOVIE_ID, movieId);
        i.putExtra(EXTRA_MOVIE_TITLE, movieTitle);
        i.putExtra(EXTRA_SHOWTIME_ID, showtimeId);
        return i;
    }

    private String movieId;
    private String movieTitle;
    private String showtimeId;

    private TextView tvHeader;
    private TextView tvShowtimeInfo;
    private TextInputEditText etBuyerName;
    private TextInputEditText etBuyerPhone;
    private TextView tvTotal;
    private TextView tvSelectedSeats;
    private MaterialButton btnBook;
    private ProgressBar progress;
    private RecyclerView rvSeats;
    private SeatsAdapter seatsAdapter;

    private Showtime showtime;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking);

        movieId = getIntent().getStringExtra(EXTRA_MOVIE_ID);
        movieTitle = getIntent().getStringExtra(EXTRA_MOVIE_TITLE);
        showtimeId = getIntent().getStringExtra(EXTRA_SHOWTIME_ID);

        tvHeader = findViewById(R.id.tvHeader);
        tvShowtimeInfo = findViewById(R.id.tvShowtimeInfo);
        etBuyerName = findViewById(R.id.etBuyerName);
        etBuyerPhone = findViewById(R.id.etBuyerPhone);
        tvTotal = findViewById(R.id.tvTotal);
        tvSelectedSeats = findViewById(R.id.tvSelectedSeats);
        btnBook = findViewById(R.id.btnBook);
        progress = findViewById(R.id.progress);
        rvSeats = findViewById(R.id.rvSeats);

        if (movieTitle == null) movieTitle = "Booking";
        tvHeader.setText(getString(R.string.booking_header_fmt, movieTitle));

        seatsAdapter = new SeatsAdapter(this);
        rvSeats.setLayoutManager(new GridLayoutManager(this, SeatHallConfig.COLS));
        rvSeats.setAdapter(seatsAdapter);
        rvSeats.setNestedScrollingEnabled(false);

        updateSelectedLabel();
        recalcTotal();

        btnBook.setOnClickListener(v -> submitBooking());

        loadShowtime();
    }

    @Override
    public void onSelectionChanged() {
        updateSelectedLabel();
        recalcTotal();
    }

    private void updateSelectedLabel() {
        List<String> sel = seatsAdapter.getSelectedSeatIds();
        if (sel.isEmpty()) {
            tvSelectedSeats.setText(R.string.none_selected);
        } else {
            tvSelectedSeats.setText(getString(R.string.selected_seats_prefix, TextUtils.join(", ", sel)));
        }
    }

    private void setLoading(boolean loading) {
        progress.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnBook.setEnabled(!loading);
    }

    private void loadShowtime() {
        setLoading(true);
        FirebaseFirestore.getInstance()
                .collection("showtimes")
                .document(showtimeId)
                .get()
                .addOnCompleteListener(task -> {
                    setLoading(false);
                    if (!task.isSuccessful()) {
                        Toast.makeText(this, "Load showtime failed", Toast.LENGTH_LONG).show();
                        finish();
                        return;
                    }
                    DocumentSnapshot d = task.getResult();
                    Showtime s = d != null ? d.toObject(Showtime.class) : null;
                    if (s == null) {
                        Toast.makeText(this, "Showtime not found", Toast.LENGTH_LONG).show();
                        finish();
                        return;
                    }
                    s.id = showtimeId;
                    if (s.startTime == null || s.price == null || s.price <= 0) {
                        Toast.makeText(this, "Showtime data invalid", Toast.LENGTH_LONG).show();
                        finish();
                        return;
                    }
                    showtime = s;
                    String info = (s.theaterName != null ? s.theaterName : "Theater")
                            + "\n" + ShowtimesActivity.formatTs(s.startTime)
                            + "\n" + String.format("%,.0f đ / ghế", s.price);
                    tvShowtimeInfo.setText(info);

                    List<String> booked = s.bookedSeatIds != null ? s.bookedSeatIds : new ArrayList<>();
                    seatsAdapter.setSeats(SeatHallConfig.allSeatIds(), booked);
                    updateSelectedLabel();
                    recalcTotal();
                });
    }

    private void recalcTotal() {
        if (showtime == null || showtime.price == null) {
            tvTotal.setText(R.string.total_placeholder);
            return;
        }
        int n = seatsAdapter.getSelectedCount();
        if (n <= 0) {
            tvTotal.setText(R.string.total_placeholder);
            return;
        }
        double total = showtime.price * n;
        tvTotal.setText(getString(R.string.total_amount_fmt, String.format(Locale.getDefault(), "%,.0f", total)));
    }

    private String validateName(String name) {
        if (name == null || name.trim().isEmpty()) return "Name is required";
        if (name.trim().length() < 2) return "Name is too short";
        return null;
    }

    private String validatePhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) return "Phone is required";
        if (phone.trim().length() < 9) return "Phone is invalid";
        return null;
    }

    private void submitBooking() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Please login again", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        if (showtime == null) {
            Toast.makeText(this, "Showtime not loaded", Toast.LENGTH_LONG).show();
            return;
        }

        List<String> selected = seatsAdapter.getSelectedSeatIds();
        if (selected.isEmpty()) {
            Toast.makeText(this, R.string.pick_at_least_one_seat, Toast.LENGTH_LONG).show();
            return;
        }

        String buyerName = etBuyerName.getText() != null ? etBuyerName.getText().toString() : "";
        String buyerPhone = etBuyerPhone.getText() != null ? etBuyerPhone.getText().toString() : "";
        String nameErr = validateName(buyerName);
        if (nameErr != null) {
            etBuyerName.setError(nameErr);
            etBuyerName.requestFocus();
            return;
        }
        String phoneErr = validatePhone(buyerPhone);
        if (phoneErr != null) {
            etBuyerPhone.setError(phoneErr);
            etBuyerPhone.requestFocus();
            return;
        }

        if (showtime.availableSeats != null && selected.size() > showtime.availableSeats) {
            Toast.makeText(this, R.string.not_enough_seats_quota, Toast.LENGTH_LONG).show();
            return;
        }

        double price = showtime.price != null ? showtime.price : 0;
        double total = price * selected.size();

        setLoading(true);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference showRef = db.collection("showtimes").document(showtimeId);

        db.runTransaction(transaction -> {
                    DocumentSnapshot snap = transaction.get(showRef);
                    @SuppressWarnings("unchecked")
                    List<String> booked = (List<String>) snap.get("bookedSeatIds");
                    if (booked == null) booked = new ArrayList<>();
                    List<String> newBooked = new ArrayList<>(booked);
                    for (String seat : selected) {
                        if (newBooked.contains(seat)) {
                            throw new FirebaseFirestoreException(
                                    "Ghế " + seat + " đã được đặt",
                                    FirebaseFirestoreException.Code.ABORTED
                            );
                        }
                        newBooked.add(seat);
                    }
                    Long avail = snap.getLong("availableSeats");
                    if (avail != null && selected.size() > avail) {
                        throw new FirebaseFirestoreException(
                                "Không đủ ghế trống",
                                FirebaseFirestoreException.Code.ABORTED
                        );
                    }
                    Map<String, Object> upd = new HashMap<>();
                    upd.put("bookedSeatIds", newBooked);
                    if (avail != null) {
                        upd.put("availableSeats", avail - selected.size());
                    }
                    transaction.update(showRef, upd);
                    return null;
                })
                .addOnSuccessListener(aVoid -> {
                    Map<String, Object> ticket = new HashMap<>();
                    ticket.put("userId", user.getUid());
                    ticket.put("movieId", movieId);
                    ticket.put("movieTitle", movieTitle);
                    ticket.put("showtimeId", showtimeId);
                    ticket.put("theaterId", showtime.theaterId);
                    ticket.put("theaterName", showtime.theaterName);
                    ticket.put("showtimeStartTime", showtime.startTime);
                    ticket.put("quantity", (long) selected.size());
                    ticket.put("seatIds", selected);
                    ticket.put("seatsLabel", TextUtils.join(", ", selected));
                    ticket.put("pricePerTicket", price);
                    ticket.put("totalPrice", total);
                    ticket.put("buyerName", buyerName.trim());
                    ticket.put("buyerPhone", buyerPhone.trim());
                    ticket.put("bookedAt", FieldValue.serverTimestamp());
                    ticket.put("status", "BOOKED");

                    db.collection("tickets")
                            .add(ticket)
                            .addOnCompleteListener(task -> {
                                setLoading(false);
                                if (!task.isSuccessful()) {
                                    String msg = task.getException() != null ? task.getException().getMessage() : "Booking failed";
                                    Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
                                    return;
                                }

                                String ticketId = task.getResult() != null ? task.getResult().getId() : null;

                                if (ticketId != null && showtime.startTime != null) {
                                    long startMs = showtime.startTime.toDate().getTime();
                                    ShowtimeAlarmScheduler.scheduleReminderBeforeShowtime(
                                            this,
                                            ticketId,
                                            startMs,
                                            movieTitle != null ? movieTitle : ""
                                    );
                                }

                                Toast.makeText(this, "Booked successfully!", Toast.LENGTH_LONG).show();
                                finish();
                            });
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    String msg = e.getMessage() != null ? e.getMessage() : "Transaction failed";
                    Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
                });
    }
}
