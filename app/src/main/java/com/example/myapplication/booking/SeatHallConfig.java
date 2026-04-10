package com.example.myapplication.booking;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Sơ đồ cố định: 7 hàng A–G, 8 cột 1–8 → A1 … G8. */
public final class SeatHallConfig {

    public static final int ROWS = 7;
    public static final int COLS = 8;

    private SeatHallConfig() {}

    @NonNull
    public static List<String> allSeatIds() {
        List<String> list = new ArrayList<>(ROWS * COLS);
        for (int r = 0; r < ROWS; r++) {
            char row = (char) ('A' + r);
            for (int c = 0; c < COLS; c++) {
                list.add(row + String.valueOf(c + 1));
            }
        }
        return list;
    }

    public static int totalSeats() {
        return ROWS * COLS;
    }

    /** Sắp xếp A1, A2, … để lưu ticket / hiển thị ổn định. */
    @NonNull
    public static List<String> sortSeatIds(@NonNull Iterable<String> ids) {
        List<String> list = new ArrayList<>();
        for (String id : ids) {
            if (id != null && !id.trim().isEmpty()) list.add(id.trim());
        }
        Collections.sort(list);
        return list;
    }
}
