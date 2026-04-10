package com.example.myapplication.auth;

import android.util.Patterns;

public final class AuthValidators {
    private AuthValidators() {}

    public static String validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) return "Email is required";
        String e = email.trim();
        if (!Patterns.EMAIL_ADDRESS.matcher(e).matches()) return "Invalid email";
        return null;
    }

    public static String validatePassword(String password) {
        if (password == null || password.isEmpty()) return "Password is required";
        if (password.length() < 6) return "Password must be at least 6 characters";
        return null;
    }

    public static String validateConfirmPassword(String password, String confirm) {
        String pErr = validatePassword(password);
        if (pErr != null) return pErr;
        if (confirm == null || confirm.isEmpty()) return "Confirm password is required";
        if (!password.equals(confirm)) return "Passwords do not match";
        return null;
    }
}

