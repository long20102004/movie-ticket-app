package com.example.myapplication;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.auth.LoginActivity;
import com.example.myapplication.movies.MoviesActivity;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            startActivity(MoviesActivity.newIntent(this));
        } else {
            startActivity(LoginActivity.newIntent(this));
        }
        finish();
    }
}

