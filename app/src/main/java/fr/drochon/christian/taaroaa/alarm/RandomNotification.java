package fr.drochon.christian.taaroaa.alarm;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import fr.drochon.christian.taaroaa.alarm.NotificationButtonReceiver;
import fr.drochon.christian.taaroaa.controller.MainActivity;
import fr.drochon.christian.taaroaa.covoiturage.CovoituragePassagersActivity;
import fr.drochon.christian.taaroaa.covoiturage.CovoiturageVehiclesActivity;

public class RandomNotification {

    private Context context;

    public RandomNotification(Context context) {
        this.context = context;
    }

    public Notification getNotification(String title, String message, int imageResourceId, Bitmap largeResource, int colour) {

        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Notification.Builder builder = new Notification.Builder(context)
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(imageResourceId)
                .setLargeIcon(largeResource)
                .setTicker("Ticker")
                .addAction(0, "Button", getButtonIntent())
                .setSound(soundUri)
                .setLights(Color.BLUE, 300, 100)
                .setAutoCancel(true)
                .setPriority(Notification.PRIORITY_MAX)
                .setContentIntent(getPendingNotificationIntent());

        this.handleNotificationColor(builder, colour);

        Notification notification = builder.build();
        notification.flags = Notification.FLAG_AUTO_CANCEL | Notification.FLAG_SHOW_LIGHTS;
        return notification;
    }

    private Notification.Builder handleNotificationColor(Notification.Builder builder, int colour) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder.setColor(colour);
        }
        return builder;
    }

    private PendingIntent getPendingNotificationIntent() {
        Intent notificationIntent = new Intent(context, CovoiturageVehiclesActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        return PendingIntent.getActivity(context, 1,
                notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private PendingIntent getButtonIntent() {
        Intent notificationButtonReceiver = new Intent(context, NotificationButtonReceiver.class);
        return PendingIntent.getBroadcast(context, 0, notificationButtonReceiver, PendingIntent.FLAG_CANCEL_CURRENT);
    }
}
