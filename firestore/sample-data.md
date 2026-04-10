# Dữ liệu mẫu Firestore (đủ cho mọi chức năng)

Làm trên **Firebase Console → Firestore Database → Start collection** (hoặc **Add document**).

**Thứ tự nên tạo:** `theaters` → `movies` → `showtimes` (vì `showtimes` trỏ tới `theaterId` và `movieId`).

Dùng **Document ID tùy chỉnh** (Auto-ID cũng được, nhưng khi đó bạn phải copy đúng ID vào `movieId` / `theaterId` trong `showtimes`).

---

## 1. Collection `theaters`

### Document ID: `th_cgv_badinh`

| Field        | Type      | Giá trị mẫu                          |
|-------------|-----------|--------------------------------------|
| `name`      | string    | CGV Ba Đình                          |
| `address`   | string    | 123 Đường ABC, Ba Đình, Hà Nội       |
| `isActive`  | boolean   | `true`                               |

### Document ID: `th_lotte_hanoi`

| Field        | Type      | Giá trị mẫu                          |
|-------------|-----------|--------------------------------------|
| `name`      | string    | Lotte Cinema Hà Nội                  |
| `address`   | string    | Tầng 5 TTTM Lotte, Cầu Giấy          |
| `isActive`  | boolean   | `true`                               |

---

## 2. Collection `movies`

### Document ID: `mv_dune2`

| Field             | Type    | Giá trị mẫu |
|------------------|---------|-------------|
| `title`          | string  | Dune: Part Two |
| `description`    | string  | Paul Atreides hợp nhất với Fremen để chống lại kẻ thù. |
| `durationMinutes`| number  | `166` |
| `rating`         | number  | `8.5` |
| `posterUrl`      | string  | *(để trống hoặc URL ảnh poster nếu có)* |
| `isActive`       | boolean | `true` |

### Document ID: `mv_oppenheimer`

| Field             | Type    | Giá trị mẫu |
|------------------|---------|-------------|
| `title`          | string  | Oppenheimer |
| `description`    | string  | Tiểu sử J. Robert Oppenheimer và bom nguyên tử. |
| `durationMinutes`| number  | `180` |
| `rating`         | number  | `8.4` |
| `posterUrl`      | string  | *(tùy chọn)* |
| `isActive`       | boolean | `true` |

### Document ID: `mv_spider`

| Field             | Type    | Giá trị mẫu |
|------------------|---------|-------------|
| `title`          | string  | Spider-Man: Across the Spider-Verse |
| `description`    | string  | Miles Morales du hành đa vũ trụ. |
| `durationMinutes`| number  | `140` |
| `rating`         | number  | `8.7` |
| `isActive`       | boolean | `true` |

---

## 3. Collection `showtimes`

**Lưu ý:**  
- `movieId` = **đúng Document ID** của phim (ví dụ `mv_dune2`).  
- `theaterId` = **đúng Document ID** của rạp (ví dụ `th_cgv_badinh`).  
- `startTime`: chọn kiểu **timestamp**, đặt **giờ trong tương lai** (ví dụ 2–3 ngày tới lúc 19:30) để còn test đặt vé và nhắc giờ.  
- `price`: number (đơn vị VNĐ trong app hiển thị dạng `90,000 đ`).  
- `availableSeats`: number (ví dụ `50`).

### Document ID: `st_dune_cgv_1`

| Field            | Type      | Giá trị mẫu        |
|------------------|-----------|--------------------|
| `movieId`        | string    | `mv_dune2`         |
| `theaterId`      | string    | `th_cgv_badinh`    |
| `theaterName`    | string    | CGV Ba Đình        |
| `theaterAddress` | string    | 123 Đường ABC, Ba Đình, Hà Nội |
| `startTime`      | timestamp | *(chọn ngày giờ tương lai)* |
| `price`          | number    | `90000`            |
| `availableSeats` | number    | `50`               |

### Document ID: `st_dune_lotte_1`

| Field            | Type      | Giá trị mẫu        |
|------------------|-----------|--------------------|
| `movieId`        | string    | `mv_dune2`         |
| `theaterId`      | string    | `th_lotte_hanoi`   |
| `theaterName`    | string    | Lotte Cinema Hà Nội |
| `theaterAddress` | string    | Tầng 5 TTTM Lotte, Cầu Giấy |
| `startTime`      | timestamp | *(khác suất 1)*    |
| `price`          | number    | `95000`            |
| `availableSeats` | number    | `40`               |

### Document ID: `st_opp_cgv_1`

| Field            | Type      | Giá trị mẫu        |
|------------------|-----------|--------------------|
| `movieId`        | string    | `mv_oppenheimer`   |
| `theaterId`      | string    | `th_cgv_badinh`    |
| `theaterName`    | string    | CGV Ba Đình        |
| `theaterAddress` | string    | 123 Đường ABC, Ba Đình, Hà Nội |
| `startTime`      | timestamp | *(tương lai)*      |
| `price`          | number    | `85000`            |
| `availableSeats` | number    | `60`               |

---

## 4. Collection `users` — **không cần nhập tay**

- Khi bạn **Đăng ký** trong app, document `users/{uid}` được tạo tự động (`uid` = User ID trong Authentication).  
- Sau khi đăng nhập, app cập nhật thêm `fcmToken` (push).  
→ **Không cần** tạo sẵn `users` trên Console trừ khi bạn muốn test thủ công (khi đó Document ID phải **trùng UID** trong Authentication).

---

## 5. Collection `tickets` — **không cần nhập tay**

- Tạo khi bạn **đặt vé** trong app (sau khi đã có `movies` + `showtimes`).

---

## 6. Collection `notification_requests` — **không cần nhập tay**

- Tạo khi đặt vé thành công (nhắc trước giờ chiếu ~30 phút).  
- Để push thực sự gửi: cần **deploy Cloud Functions** (`functions/`) như trong `README.md`.

---

## Rules Firestore (dev nhanh — chỉ dùng khi học / demo)

Trong **Firestore → Rules**, có thể tạm dùng (⚠️ không public production):

```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /{document=**} {
      allow read, write: if request.auth != null;
    }
  }
}
```

Nếu chưa login mà muốn đọc `movies` từ app thì user vẫn phải **đăng nhập** trước khi vào danh sách phim (flow hiện tại). Collection `movies`/`showtimes` có thể cho phép `read` public nếu bạn muốn — khi đó chỉnh rule riêng cho từng collection.

---

## Checklist sau khi nhập xong

1. Authentication: bật **Email/Password**.  
2. `app/google-services.json` đã đặt đúng.  
3. Chạy app → **Đăng ký / Đăng nhập** → thấy **3 phim** → chọn phim → thấy **suất chiếu** → đặt vé → kiểm tra `tickets` và `notification_requests` trên Firestore.
