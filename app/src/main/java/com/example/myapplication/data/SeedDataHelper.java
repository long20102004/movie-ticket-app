package com.example.myapplication.data;

import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * Tự ghi dữ liệu mẫu (theaters, movies, showtimes) khi collection {@code movies} đang trống.
 * Chỉ chạy một lần (sau đó đã có phim → bỏ qua). Cần user đã đăng nhập và Firestore Rules cho phép ghi.
 */
public final class SeedDataHelper {

    /** Ảnh mẫu (Unsplash, HTTPS) — ổn định cho demo. */
    private static final String POSTER_DUNE =
            "https://images.unsplash.com/photo-1536440136628-849c177e76a1?w=400&q=85&auto=format&fit=crop";
    private static final String POSTER_OPPEN =
            "https://images.unsplash.com/photo-1485846234645-a62644f84728?w=400&q=85&auto=format&fit=crop";
    private static final String POSTER_SPIDER =
            "https://images.unsplash.com/photo-1517604931442-7e0c8ed2963c?w=400&q=85&auto=format&fit=crop";

    private SeedDataHelper() {}

    public interface Callback {
        void onDone();
    }

    /**
     * Nếu không có document nào trong {@code movies} → batch set theaters + movies + showtimes (ID cố định).
     * Luôn gọi {@code callback.onDone()} sau khi xong (kể cả lỗi hoặc không cần seed).
     */
    public static void maybeSeedSampleData(@NonNull Context context, @NonNull Callback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("movies")
                .limit(1)
                .get()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Toast.makeText(context,
                                "Seed check failed: " + (task.getException() != null ? task.getException().getMessage() : ""),
                                Toast.LENGTH_LONG).show();
                        callback.onDone();
                        return;
                    }
                    if (task.getResult() != null && !task.getResult().isEmpty()) {
                        callback.onDone();
                        return;
                    }

                    WriteBatch batch = db.batch();

                    DocumentReference th1 = db.collection("theaters").document("th_cgv_badinh");
                    batch.set(th1, theater("CGV Ba Đình", "123 Đường ABC, Ba Đình, Hà Nội"));

                    DocumentReference th2 = db.collection("theaters").document("th_lotte_hanoi");
                    batch.set(th2, theater("Lotte Cinema Hà Nội", "Tầng 5 TTTM Lotte, Cầu Giấy, Hà Nội"));

                    DocumentReference mv1 = db.collection("movies").document("mv_dune2");
                    batch.set(mv1, movie(
                            "Dune: Part Two",
                            "Paul Atreides hợp nhất với Fremen để chống lại kẻ thù.",
                            166L,
                            8.5,
                            POSTER_DUNE
                    ));

                    DocumentReference mv2 = db.collection("movies").document("mv_oppenheimer");
                    batch.set(mv2, movie(
                            "Oppenheimer",
                            "Tiểu sử J. Robert Oppenheimer và bom nguyên tử.",
                            180L,
                            8.4,
                            POSTER_OPPEN
                    ));

                    DocumentReference mv3 = db.collection("movies").document("mv_spider");
                    batch.set(mv3, movie(
                            "Spider-Man: Across the Spider-Verse",
                            "Miles Morales du hành đa vũ trụ.",
                            140L,
                            8.7,
                            POSTER_SPIDER
                    ));

                    // Test: cùng khung 18:15 — hôm nay nếu chưa qua, không thì ngày mai (để luôn còn trong tương lai)
                    Timestamp slot1815 = nextOccurrenceAt(18, 15);

                    DocumentReference st1 = db.collection("showtimes").document("st_dune_cgv_1");
                    batch.set(st1, showtime(
                            "mv_dune2",
                            "th_cgv_badinh",
                            "CGV Ba Đình",
                            "123 Đường ABC, Ba Đình, Hà Nội",
                            slot1815,
                            90000.0,
                            56L
                    ));

                    DocumentReference st2 = db.collection("showtimes").document("st_dune_lotte_1");
                    batch.set(st2, showtime(
                            "mv_dune2",
                            "th_lotte_hanoi",
                            "Lotte Cinema Hà Nội",
                            "Tầng 5 TTTM Lotte, Cầu Giấy, Hà Nội",
                            slot1815,
                            95000.0,
                            56L
                    ));

                    DocumentReference st3 = db.collection("showtimes").document("st_opp_cgv_1");
                    batch.set(st3, showtime(
                            "mv_oppenheimer",
                            "th_cgv_badinh",
                            "CGV Ba Đình",
                            "123 Đường ABC, Ba Đình, Hà Nội",
                            slot1815,
                            85000.0,
                            56L
                    ));

                    // Thêm suất cố định: 10/4/2026 — 18:15 (đổi năm ở atFixedDateTime nếu cần)
                    Timestamp apr10_2026_1815 = atFixedDateTime(2026, Calendar.APRIL, 10, 18, 15);

                    batch.set(db.collection("showtimes").document("st_2026apr10_dune_cgv"), showtime(
                            "mv_dune2",
                            "th_cgv_badinh",
                            "CGV Ba Đình",
                            "123 Đường ABC, Ba Đình, Hà Nội",
                            apr10_2026_1815,
                            90000.0,
                            56L
                    ));
                    batch.set(db.collection("showtimes").document("st_2026apr10_dune_lotte"), showtime(
                            "mv_dune2",
                            "th_lotte_hanoi",
                            "Lotte Cinema Hà Nội",
                            "Tầng 5 TTTM Lotte, Cầu Giấy, Hà Nội",
                            apr10_2026_1815,
                            95000.0,
                            56L
                    ));
                    batch.set(db.collection("showtimes").document("st_2026apr10_opp_lotte"), showtime(
                            "mv_oppenheimer",
                            "th_lotte_hanoi",
                            "Lotte Cinema Hà Nội",
                            "Tầng 5 TTTM Lotte, Cầu Giấy, Hà Nội",
                            apr10_2026_1815,
                            88000.0,
                            56L
                    ));
                    batch.set(db.collection("showtimes").document("st_2026apr10_spider_cgv"), showtime(
                            "mv_spider",
                            "th_cgv_badinh",
                            "CGV Ba Đình",
                            "123 Đường ABC, Ba Đình, Hà Nội",
                            apr10_2026_1815,
                            92000.0,
                            56L
                    ));
                    batch.set(db.collection("showtimes").document("st_2026apr10_spider_lotte"), showtime(
                            "mv_spider",
                            "th_lotte_hanoi",
                            "Lotte Cinema Hà Nội",
                            "Tầng 5 TTTM Lotte, Cầu Giấy, Hà Nội",
                            apr10_2026_1815,
                            93000.0,
                            56L
                    ));

                    batch.commit()
                            .addOnCompleteListener(commitTask -> {
                                if (commitTask.isSuccessful()) {
                                    Toast.makeText(context,
                                            "Đã tạo dữ liệu mẫu. Thêm suất 10/4/2026 18:15 + suất 18:15 linh hoạt.",
                                            Toast.LENGTH_LONG).show();
                                } else {
                                    Exception e = commitTask.getException();
                                    Toast.makeText(context,
                                            "Không seed được: " + (e != null ? e.getMessage() : "unknown") + ". Kiểm tra Firestore Rules (cần quyền ghi khi đã đăng nhập).",
                                            Toast.LENGTH_LONG).show();
                                }
                                callback.onDone();
                            });
                });
    }

    private static Map<String, Object> theater(String name, String address) {
        Map<String, Object> m = new HashMap<>();
        m.put("name", name);
        m.put("address", address);
        m.put("isActive", true);
        return m;
    }

    private static Map<String, Object> movie(String title, String description, long durationMinutes, double rating, String posterUrl) {
        Map<String, Object> m = new HashMap<>();
        m.put("title", title);
        m.put("description", description);
        m.put("durationMinutes", durationMinutes);
        m.put("rating", rating);
        m.put("posterUrl", posterUrl != null ? posterUrl : "");
        m.put("isActive", true);
        return m;
    }

    private static Map<String, Object> showtime(
            String movieId,
            String theaterId,
            String theaterName,
            String theaterAddress,
            Timestamp startTime,
            double price,
            long availableSeats
    ) {
        Map<String, Object> m = new HashMap<>();
        m.put("movieId", movieId);
        m.put("theaterId", theaterId);
        m.put("theaterName", theaterName);
        m.put("theaterAddress", theaterAddress);
        m.put("startTime", startTime);
        m.put("price", price);
        m.put("availableSeats", availableSeats);
        m.put("bookedSeatIds", new ArrayList<String>());
        return m;
    }

    /** Ngày-giờ cố định (month = {@link Calendar#APRIL} v.v., 0-based như {@link Calendar}). */
    private static Timestamp atFixedDateTime(int year, int month, int day, int hour, int minute) {
        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.set(year, month, day, hour, minute, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return new Timestamp(cal.getTime());
    }

    /**
     * Lần tới có giờ {@code hour}:{@code minute} (0–23, 0–59): hôm nay nếu chưa qua, không thì ngày mai.
     * Dùng để seed suất test cố định (vd 18:15).
     */
    private static Timestamp nextOccurrenceAt(int hour, int minute) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, minute);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        if (cal.getTimeInMillis() <= System.currentTimeMillis()) {
            cal.add(Calendar.DAY_OF_MONTH, 1);
        }
        return new Timestamp(cal.getTime());
    }
}
