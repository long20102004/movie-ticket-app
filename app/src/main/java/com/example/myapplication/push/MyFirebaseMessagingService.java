package com.example.myapplication.push;

import androidx.annotation.NonNull;

import com.example.myapplication.notify.ShowtimeNotificationHelper;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        String title = null;
        String body = null;

        if (message.getNotification() != null) {
            title = message.getNotification().getTitle();
            body = message.getNotification().getBody();
        }

        Map<String, String> data = message.getData();
        if ((title == null || title.trim().isEmpty()) && data != null) {
            title = data.get("title");
        }
        if ((body == null || body.trim().isEmpty()) && data != null) {
            body = data.get("body");
        }

        if (title == null || title.trim().isEmpty()) title = "Movie Ticket App";
        if (body == null || body.trim().isEmpty()) body = "You have a reminder.";

        int nid = (int) (System.currentTimeMillis() % Integer.MAX_VALUE);
        ShowtimeNotificationHelper.show(this, title, body, nid);
    }
}
