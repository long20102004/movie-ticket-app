package com.example.myapplication.movies;

import com.google.firebase.Timestamp;

public class Movie {
    public String id;
    public String title;
    public String description;
    public Long durationMinutes;
    public Double rating;
    public String posterUrl;
    public Boolean isActive;
    public Timestamp createdAt;

    public Movie() {}
}

