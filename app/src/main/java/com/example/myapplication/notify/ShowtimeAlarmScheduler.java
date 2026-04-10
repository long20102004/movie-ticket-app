package com.example.myapplication.notify;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.AlarmManagerCompat;

import com.example.myapplication.R;

import java.util.concurrent.TimeUnit;

/**
 * Lên lịch nhắc giờ chiếu bằng {@link AlarmManager} (broadcast), không cần Cloud Functions.
 */
public final class ShowtimeAlarmScheduler {

    private static final String TAG = "ShowtimeAlarm";
    /** Nhắc trước giờ chiếu (phút). */
    public static final int REMINDER_MINUTES_BEFORE = 30;

    private ShowtimeAlarmScheduler() {}

    /**
     * Đặt báo thức tại {@code showtimeMillis - REMINDER_MINUTES_BEFORE}.
     *
     * @param ticketId dùng để tạo requestCode ổn định (hủy/trùng lịch).
     */
    public static void scheduleReminderBeforeShowtime(
            @NonNull Context context,
            @NonNull String ticketId,
            long showtimeMillis,
            @NonNull String movieTitle
    ) {
        long remindAt = showtimeMillis - TimeUnit.MINUTES.toMillis(REMINDER_MINUTES_BEFORE);
        long now = System.currentTimeMillis();

        if (remindAt <= now) {
            Log.i(TAG, "Skip alarm: reminder time already passed or showtime too soon.");
            return;
        }

        int requestCode = stableRequestCode(ticketId);
        int notificationId = requestCode;

        Intent intent = new Intent(context, ShowtimeReminderReceiver.class);
        intent.setAction(ShowtimeReminderReceiver.ACTION);
        intent.putExtra(ShowtimeReminderReceiver.EXTRA_TITLE, context.getString(R.string.reminder_title_default));
        intent.putExtra(
                ShowtimeReminderReceiver.EXTRA_BODY,
                context.getString(R.string.reminder_body_fmt, movieTitle)
        );
        intent.putExtra(ShowtimeReminderReceiver.EXTRA_NOTIFICATION_ID, notificationId);

        PendingIntent pi = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (am == null) return;

        AlarmManagerCompat.setExactAndAllowWhileIdle(am, AlarmManager.RTC_WAKEUP, remindAt, pi);
        Log.i(TAG, "Scheduled local reminder at " + remindAt + " for ticket " + ticketId);
    }

    private static int stableRequestCode(@NonNull String ticketId) {
        return (ticketId.hashCode() & 0x7FFFFFFF);
    }
}
