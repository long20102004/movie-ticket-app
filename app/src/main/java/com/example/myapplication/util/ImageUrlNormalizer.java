package com.example.myapplication.util;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Chuẩn hóa URL ảnh để Glide load được.
 * <p>
 * Link GitHub dạng trang web {@code github.com/.../blob/...} trả về HTML — cần đổi sang
 * {@code raw.githubusercontent.com/.../.../...} (file ảnh thật).
 */
public final class ImageUrlNormalizer {

    private ImageUrlNormalizer() {}

    @NonNull
    public static String posterUrlForGlide(@Nullable String url) {
        if (url == null) return "";
        String u = url.trim();
        if (u.isEmpty()) return "";

        u = u.replace("www.github.com", "github.com");

        // .../blob/branch/path → raw host + .../branch/path
        if (u.contains("github.com/") && u.contains("/blob/")) {
            u = u.replace("://github.com/", "://raw.githubusercontent.com/");
            u = u.replaceFirst("/blob/", "/");
        }

        // .../raw/branch/path (một số link copy từ GitHub)
        if (u.contains("github.com/") && u.contains("/raw/") && !u.contains("raw.githubusercontent.com")) {
            u = u.replaceFirst(
                    "^https?://github\\.com/([^/]+)/([^/]+)/raw/([^/]+)/(.+)$",
                    "https://raw.githubusercontent.com/$1/$2/$3/$4"
            );
        }

        return u;
    }
}
