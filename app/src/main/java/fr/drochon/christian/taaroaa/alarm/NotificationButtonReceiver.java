package fr.drochon.christian.taaroaa.alarm;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;

import fr.drochon.christian.taaroaa.R;

public class NotificationButtonReceiver extends BroadcastReceiver {

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent != null) {
            String body = null, title = null;
            if (intent.hasExtra(Notification.EXTRA_NOTIFICATION_ID)) {
                body = intent.getStringExtra(Notification.EXTRA_NOTIFICATION_ID);
            }
            if (intent.hasExtra(Notification.EXTRA_TITLE)) {
                title = intent.getStringExtra(Notification.EXTRA_TITLE);
            }
            if (body != null && title != null) {
                sendNotification(context, body, title);
            }
        }
    }

    private void sendNotification(Context context, String message, String title) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            NotificationChannel channel;
            if (Build.VERSION.SDK_INT >= 26) {
                channel = new NotificationChannel("default",
                        "Task",
                        NotificationManager.IMPORTANCE_DEFAULT);
                channel.setDescription("Task Channel");
                notificationManager.createNotificationChannel(channel);
            }
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, "default")
                    .setSmallIcon(R.mipmap.logo1)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setAutoCancel(true)
                    .setVibrate(new long[]{0, 500})
                    .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
            notificationManager.notify(0, notificationBuilder.build());
        }
    }

}
