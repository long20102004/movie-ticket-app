package com.example.myapplication.booking;

import com.google.firebase.Timestamp;

import java.util.List;

public class Ticket {
    public String id;
    public String userId;
    public String movieId;
    public String movieTitle;
    public String showtimeId;
    public String theaterId;
    public String theaterName;
    public Timestamp showtimeStartTime;
    public Long quantity;
    public List<String> seatIds;
    public String seatsLabel;
    public Double pricePerTicket;
    public Double totalPrice;
    public String buyerName;
    public String buyerPhone;
    public Timestamp bookedAt;
    public String status;

    public Ticket() {}
}

