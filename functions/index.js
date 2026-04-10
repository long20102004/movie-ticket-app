const { onSchedule } = require("firebase-functions/v2/scheduler");
const admin = require("firebase-admin");

admin.initializeApp();

/**
 * Scheduled job every 1 minute:
 * - finds notification_requests where status=PENDING and sendAt <= now
 * - sends FCM to fcmToken
 * - marks request as SENT (or FAILED with error)
 */
exports.sendShowtimeReminders = onSchedule("every 1 minutes", async () => {
  const db = admin.firestore();
  const now = admin.firestore.Timestamp.now();

  const snap = await db
    .collection("notification_requests")
    .where("status", "==", "PENDING")
    .where("sendAt", "<=", now)
    .limit(100)
    .get();

  if (snap.empty) return;

  const batch = db.batch();

  for (const doc of snap.docs) {
    const data = doc.data() || {};
    const token = data.fcmToken;
    const title = data.title || "Showtime reminder";
    const body = data.body || "Your movie starts soon.";

    if (!token) {
      batch.update(doc.ref, { status: "FAILED", error: "Missing fcmToken", processedAt: now });
      continue;
    }

    try {
      await admin.messaging().send({
        token,
        notification: { title, body },
        data: {
          ticketId: String(data.ticketId || ""),
          showtimeId: String(data.showtimeId || ""),
          movieId: String(data.movieId || ""),
        },
      });

      batch.update(doc.ref, { status: "SENT", processedAt: now });
    } catch (err) {
      batch.update(doc.ref, {
        status: "FAILED",
        processedAt: now,
        error: (err && err.message) ? err.message : String(err),
      });
    }
  }

  await batch.commit();
});

