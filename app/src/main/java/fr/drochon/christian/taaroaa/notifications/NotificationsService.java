package fr.drochon.christian.taaroaa.notifications;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import fr.drochon.christian.taaroaa.R;
import fr.drochon.christian.taaroaa.auth.SearchUserActivity;

@SuppressLint("Registered")
public class NotificationsService extends FirebaseMessagingService {


    int NOTIFICATION_ID = 7;
    String NOTIFICATION_TAG = "TAAROAA";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        if (remoteMessage.getNotification() != null) {
            String message = remoteMessage.getNotification().getBody();
            // Show notification after received message
            this.sendVisualNotification(message);
        }
    }

    /**
     * Methode permettant de creer et d'afficher les notifications provenant de la bdd messaging
     *
     * @param messageBody
     */
    private void sendVisualNotification(String messageBody) {

        // Créé un intent qui ouvre l'activité voulue
        Intent intent = new Intent(this, SearchUserActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 7, intent, PendingIntent.FLAG_ONE_SHOT);


        // Create a Style for the Notification
        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
        inboxStyle.setBigContentTitle(getString(R.string.notification_title));
        inboxStyle.addLine(messageBody);

        // Create a Channel (Android 8) and set the importance
        String channelId = getString(R.string.default_notification_channel_id);

        // Build a Notification object : interieur de lappli et renvoi vers une activité definie via l'intent plus haut
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        // Set the notification content
                        .setSmallIcon(android.R.drawable.ic_notification_overlay)
                        .setContentTitle(getString(R.string.app_name))
                        .setContentText(getString(R.string.notification_title))
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                        // Set the intent that will fire when the user taps the notification : renvoi vers l'activité definie
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true)
                        // style permettant une notif de plusieurs lignes
                        .setStyle(inboxStyle);

        // Create a Channel (Android 8) and set the importance
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // Support Version >= Android 8
        this.createNotificationChannel(channelId);
        // Show notification
        assert notificationManager != null;
        notificationManager.notify(NOTIFICATION_TAG, NOTIFICATION_ID, notificationBuilder.build());
    }

    /**
     * Methode permettant de creer une channel et de determiner osn importance. Avant de pouvoir delivrer une
     * notification sur Android 8 ou +, il aut determiner une notification's channel de l'app.
     * Grace à cette chaine, l'user aura alors directement la possibilité de modifier les paramètres (comme l'importance,
     * le son, la lumière, la vibration, etc...) des notifications inscrites dans un canal, sans avoir besoin de les coder
     * dans votre application Android.
     *
     * @param CHANNEL_ID
     */
    private void createNotificationChannel(String CHANNEL_ID) {

        NotificationManager notificationManager = null;
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Message provenant de Firebase";
            String description = "Description de la chaine de notification";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            channel.setName(name);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            notificationManager = getSystemService(NotificationManager.class);
            assert notificationManager != null;
            notificationManager.createNotificationChannel(channel);
        }
    }
}
