package com.example.myapplication.notify;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.myapplication.R;

/**
 * Nhận báo thức từ {@link AlarmManager} và hiển thị notification nhắc giờ chiếu (không cần server).
 */
public class ShowtimeReminderReceiver extends BroadcastReceiver {

    public static final String ACTION = "com.example.myapplication.action.SHOWTIME_REMINDER";

    public static final String EXTRA_TITLE = "title";
    public static final String EXTRA_BODY = "body";
    public static final String EXTRA_NOTIFICATION_ID = "notification_id";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || !ACTION.equals(intent.getAction())) return;

        String title = intent.getStringExtra(EXTRA_TITLE);
        String body = intent.getStringExtra(EXTRA_BODY);
        int nid = intent.getIntExtra(EXTRA_NOTIFICATION_ID, (int) (System.currentTimeMillis() % Integer.MAX_VALUE));

        if (title == null || title.isEmpty()) title = context.getString(R.string.reminder_title_default);
        if (body == null || body.isEmpty()) body = context.getString(R.string.reminder_body_default);

        ShowtimeNotificationHelper.show(context.getApplicationContext(), title, body, absNotifId(nid));
    }

    private static int absNotifId(int id) {
        return id & 0x7FFFFFFF;
    }
}
