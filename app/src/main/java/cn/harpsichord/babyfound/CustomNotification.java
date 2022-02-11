package cn.harpsichord.babyfound;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class CustomNotification {

    private static final String CHANNEL_ID = "BabyFoundChannel";
    private static NotificationManager notificationManager;

    public static void init(Context context) {
        if (notificationManager == null) {
            notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        }

        createChannel();

    }

    private static void createChannel() {
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "BabyFoundChannel",
                NotificationManager.IMPORTANCE_DEFAULT
        );
        notificationManager.createNotificationChannel(channel);
    }

    public static void send(Information information, Context context) {

        Intent intent = new Intent(context, NotificationClickReceiver.class);
        intent.putExtra("data", information);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_CANCEL_CURRENT);
        Notification notification = new Notification.Builder(context, CHANNEL_ID)
                 .setAutoCancel(true)
                .setContentTitle("寻人启事")
                .setContentText(information.informationText)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.test_baby)
                .setContentIntent(pendingIntent)
                .build();

        notificationManager.notify(1, notification);
    }

}


