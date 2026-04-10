package com.example.myapplication.auth;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.example.myapplication.movies.MoviesActivity;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    public static Intent newIntent(Context ctx) {
        return new Intent(ctx, LoginActivity.class);
    }

    private EditText etEmail;
    private EditText etPassword;
    private Button btnLogin;
    private TextView tvGoRegister;
    private ProgressBar progress;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvGoRegister = findViewById(R.id.tvGoRegister);
        progress = findViewById(R.id.progress);

        btnLogin.setOnClickListener(v -> doLogin());
        tvGoRegister.setOnClickListener(v -> {
            startActivity(RegisterActivity.newIntent(this));
        });
    }

    private void setLoading(boolean loading) {
        progress.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnLogin.setEnabled(!loading);
        tvGoRegister.setEnabled(!loading);
    }

    private void doLogin() {
        String email = etEmail.getText() != null ? etEmail.getText().toString() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString() : "";

        String emailErr = AuthValidators.validateEmail(email);
        if (emailErr != null) {
            etEmail.setError(emailErr);
            etEmail.requestFocus();
            return;
        }
        String passErr = AuthValidators.validatePassword(password);
        if (passErr != null) {
            etPassword.setError(passErr);
            etPassword.requestFocus();
            return;
        }

        setLoading(true);
        FirebaseAuth.getInstance()
                .signInWithEmailAndPassword(email.trim(), password)
                .addOnCompleteListener(task -> {
                    setLoading(false);
                    if (task.isSuccessful()) {
                        startActivity(MoviesActivity.newIntent(this));
                        finish();
                    } else {
                        String msg = task.getException() != null ? task.getException().getMessage() : "Login failed";
                        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
                    }
                });
    }
}

