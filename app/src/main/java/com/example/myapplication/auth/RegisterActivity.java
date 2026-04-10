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
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    public static Intent newIntent(Context ctx) {
        return new Intent(ctx, RegisterActivity.class);
    }

    private EditText etEmail;
    private EditText etPassword;
    private EditText etConfirmPassword;
    private EditText etDisplayName;
    private EditText etPhone;
    private Button btnRegister;
    private TextView tvGoLogin;
    private ProgressBar progress;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        etDisplayName = findViewById(R.id.etDisplayName);
        etPhone = findViewById(R.id.etPhone);
        btnRegister = findViewById(R.id.btnRegister);
        tvGoLogin = findViewById(R.id.tvGoLogin);
        progress = findViewById(R.id.progress);

        btnRegister.setOnClickListener(v -> doRegister());
        tvGoLogin.setOnClickListener(v -> finish());
    }

    private void setLoading(boolean loading) {
        progress.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnRegister.setEnabled(!loading);
        tvGoLogin.setEnabled(!loading);
    }

    private String validateDisplayName(String name) {
        if (name == null || name.trim().isEmpty()) return "Display name is required";
        if (name.trim().length() < 2) return "Display name is too short";
        return null;
    }

    private String validatePhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) return "Phone is required";
        String p = phone.trim();
        if (p.length() < 9) return "Phone is invalid";
        return null;
    }

    private void doRegister() {
        String email = etEmail.getText() != null ? etEmail.getText().toString() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString() : "";
        String confirm = etConfirmPassword.getText() != null ? etConfirmPassword.getText().toString() : "";
        String displayName = etDisplayName.getText() != null ? etDisplayName.getText().toString() : "";
        String phone = etPhone.getText() != null ? etPhone.getText().toString() : "";

        String emailErr = AuthValidators.validateEmail(email);
        if (emailErr != null) {
            etEmail.setError(emailErr);
            etEmail.requestFocus();
            return;
        }
        String passErr = AuthValidators.validateConfirmPassword(password, confirm);
        if (passErr != null) {
            etConfirmPassword.setError(passErr);
            etConfirmPassword.requestFocus();
            return;
        }
        String nameErr = validateDisplayName(displayName);
        if (nameErr != null) {
            etDisplayName.setError(nameErr);
            etDisplayName.requestFocus();
            return;
        }
        String phoneErr = validatePhone(phone);
        if (phoneErr != null) {
            etPhone.setError(phoneErr);
            etPhone.requestFocus();
            return;
        }

        setLoading(true);
        FirebaseAuth.getInstance()
                .createUserWithEmailAndPassword(email.trim(), password)
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        setLoading(false);
                        String msg = task.getException() != null ? task.getException().getMessage() : "Register failed";
                        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
                        return;
                    }

                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    if (user == null) {
                        setLoading(false);
                        Toast.makeText(this, "Register failed", Toast.LENGTH_LONG).show();
                        return;
                    }

                    Map<String, Object> data = new HashMap<>();
                    data.put("uid", user.getUid());
                    data.put("email", email.trim());
                    data.put("displayName", displayName.trim());
                    data.put("phone", phone.trim());
                    data.put("createdAt", FieldValue.serverTimestamp());
                    data.put("updatedAt", FieldValue.serverTimestamp());

                    FirebaseFirestore.getInstance()
                            .collection("users")
                            .document(user.getUid())
                            .set(data)
                            .addOnCompleteListener(t2 -> {
                                setLoading(false);
                                if (t2.isSuccessful()) {
                                    startActivity(MoviesActivity.newIntent(this));
                                    finishAffinity();
                                } else {
                                    String msg = t2.getException() != null ? t2.getException().getMessage() : "Profile save failed";
                                    Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
                                }
                            });
                });
    }
}

