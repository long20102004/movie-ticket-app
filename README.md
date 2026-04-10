## Movie Ticket App (Android Java + Firebase)

### Features
- Firebase Authentication (Email/Password): login + register (with validation)
- Firestore: `users`, `movies`, `showtimes`, `tickets`
- Booking flow: pick movie → pick showtime → book → save ticket to Firestore
- **Nhắc giờ chiếu (mặc định):** `AlarmManager` + `BroadcastReceiver` — notification **trên máy**, **30 phút trước** suất, không cần deploy server. FCM / Cloud Functions là **tuỳ chọn** (file `functions/`).

### Required setup (you must do)
- **Add Firebase config file**: place `google-services.json` at `app/google-services.json`
- **Enable Firebase services** in Firebase Console:
  - Authentication → Sign-in method → enable Email/Password
  - Firestore Database → create database
  - Cloud Messaging (FCM)
- **SHA-1 / SHA-256** (optional now, needed for Google Sign-In later):
  - Firebase Console → Project settings → Your apps → Add fingerprint

### Firestore collections (minimal fields)

#### `users/{uid}`
- `uid` (string)
- `email` (string)
- `displayName` (string)
- `phone` (string)
- `fcmToken` (string) (saved automatically after login)
- `createdAt`, `updatedAt` (timestamp)

#### `movies/{movieId}`
- `title` (string) **required**
- `description` (string)
- `durationMinutes` (number)
- `rating` (number)
- `posterUrl` (string) — URL ảnh poster (HTTPS); seed mẫu dùng ảnh Unsplash
- `isActive` (bool) **required** (must be `true` to show)

#### `showtimes/{showtimeId}`
- `movieId` (string) **required**
- `theaterId` (string) **required**
- `theaterName` (string) (recommended for denormalized display)
- `theaterAddress` (string) (recommended for denormalized display)
- `startTime` (timestamp) **required**
- `price` (number) **required**
- `availableSeats` (number)
- `bookedSeatIds` (array of strings, e.g. `A1`, `B4`) — ghế đã bán; app dùng sơ đồ **A1–G8** (56 ghế)

#### `theaters/{theaterId}`
- `name` (string) **required**
- `address` (string)
- `isActive` (bool)

#### `tickets/{ticketId}`
Created by the app when booking.

#### `notification_requests/{id}` (tuỳ chọn)
Chỉ dùng nếu bạn tự triển khai nhắc qua **FCM + Cloud Functions**. Flow mặc định hiện **không** ghi collection này.

**Dữ liệu mẫu để nhập tay trên Console:** xem [`firestore/sample-data.md`](firestore/sample-data.md) (thứ tự `theaters` → `movies` → `showtimes`; `users` / `tickets` do app tạo).

**Tự tạo dữ liệu mẫu (không cần nhập Console):** khi mở màn hình danh sách phim, app gọi `SeedDataHelper`: nếu collection `movies` **chưa có document nào** thì tự **batch ghi** `theaters`, `movies`, `showtimes` (cùng ID như file mẫu). Cần **Firestore Rules** cho phép user đã đăng nhập **ghi** các collection đó (ví dụ rule dev bên dưới). Nếu seed thất bại, app hiện Toast — thường do Rules chặn ghi.

### Cloud Functions (FCM reminder)
This repo includes a simple scheduled function in `functions/index.js` that sends reminders for due `notification_requests`.

To deploy you need Firebase CLI:

```bash
cd functions
npm install
firebase deploy --only functions
```

---

### Thông báo nhắc giờ chiếu — cần setup gì? (tiếng Việt)

#### Cách mặc định (nhanh — **không** cần Firebase Functions)

Sau khi đặt vé thành công, app gọi `ShowtimeAlarmScheduler`: đặt **AlarmManager** (RTC_WAKEUP) tại thời điểm **30 phút trước** giờ chiếu. Khi tới giờ, `ShowtimeReminderReceiver` chạy và hiện notification (kênh `showtime_reminders`).

**Bạn cần:** bật quyền **Thông báo** (Android 13+). Manifest đã khai báo `SCHEDULE_EXACT_ALARM` để báo thức đúng giờ (Android 12+). **Không** cần deploy Cloud Functions cho flow này.

**Lưu ý:** Sau khi **khởi động lại máy**, một số thiết bị có thể xóa báo thức đã lên lịch — bài tập/demo thường chấp nhận; production có thể thêm `BOOT_COMPLETED` + đọc lại vé từ Firestore để lên lịch lại.

#### Cách tuỳ chọn (FCM + Cloud Function)

**Cách hoạt động (legacy / tuỳ chọn):** Có thể dùng `notification_requests` + Cloud Function gửi FCM; `MyFirebaseMessagingService` vẫn hiển thị tin push nếu bạn gửi từ server.

#### 1. Trên Firebase Console (web)
- **Cloud Messaging**: bật / dùng mặc định (không cần “Server key” cũ cho FCM HTTP v1 nếu dùng Admin SDK trong Functions).
- **Firestore**: đã có database; deploy **indexes** (file `firestore.indexes.json`) vì query `status + sendAt` cần **composite index** (nếu thiếu, log Functions sẽ báo link tạo index — mở link đó cũng được).

#### 2. Trên máy bạn (deploy Functions)
1. Cài [Node.js](https://nodejs.org/) (LTS) và [Firebase CLI](https://firebase.google.com/docs/cli): `npm install -g firebase-tools`
2. Đăng nhập: `firebase login`
3. Trong thư mục project: `firebase use --add` và chọn **đúng project** Firebase của app.
4. Deploy:
   ```bash
   cd functions
   npm install
   cd ..
   firebase deploy --only functions
   firebase deploy --only firestore:indexes
   ```
5. **Gói Blaze (pay as you go):** Cloud Functions **v2** + **scheduler** (chạy mỗi phút) thường yêu cầu project ở gói **Blaze** (có free tier, thẻ thanh toán để bật — kiểm tra [pricing](https://firebase.google.com/pricing)). Nếu không nâng cấp, scheduled function có thể không deploy được.

#### 3. Trên điện thoại (app)
- Cài app có `google-services.json` đúng project.
- **Android 13+:** bật quyền **Thông báo** khi app hỏi (`POST_NOTIFICATIONS`).
- Đăng nhập → mở màn phim (app sync **FCM token** lên `users/{uid}`); đặt vé thành công → có document `notification_requests` với `status: PENDING`.
- **Thời gian test:** Giờ chiếu trong Firestore phải **đủ xa** để `sendAt` (trừ 30 phút) vẫn nằm **trong tương lai** gần — hoặc tạm sửa seed / suất chiếu để sau vài phút nữa là tới `sendAt`, rồi đợi Function chạy (tối đa ~1 phút theo lịch).

#### 4. Kiểm tra lỗi
- **Firestore → `notification_requests`:** có bản ghi mới sau khi đặt vé? `fcmToken` có giá trị? `sendAt` đã qua so với giờ hiện tại khi bạn chờ thông báo?
- **Functions → Logs:** có lỗi gửi FCM (token hết hạn, sai project, v.v.)?
- Nếu không deploy Functions: **sẽ không có** push tự động — chỉ có dữ liệu trên Firestore.

### Notes
- If you can't build in terminal: make sure your machine has JDK and `JAVA_HOME` set, or just build/run in Android Studio.

