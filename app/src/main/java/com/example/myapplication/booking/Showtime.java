package com.example.myapplication.booking;

import com.google.firebase.Timestamp;

import java.util.List;

public class Showtime {
    public String id;
    public String movieId;
    public String movieTitle;
    public String theaterId;
    public String theaterName;
    public String theaterAddress;
    public Timestamp startTime;
    public Double price;
    public Long availableSeats;
    /** Ghế đã bán (vd: A1, B3). Đồng bộ khi đặt vé. */
    public List<String> bookedSeatIds;

    public Showtime() {}
}

