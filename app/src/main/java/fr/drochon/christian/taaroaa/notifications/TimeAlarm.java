package fr.drochon.christian.taaroaa.notifications;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import fr.drochon.christian.taaroaa.covoiturage.CovoiturageVehiclesActivity;

public class TimeAlarm extends BroadcastReceiver {

    NotificationManager notificationManager;

    @Override
    public void onReceive(Context context, Intent intent) {

        // recuperation de l'extra envoyé dans l'intent
        Bundle bundle = intent.getExtras();
        assert bundle != null;
        String hAller = bundle.getString("hAller");

        // conversion de date pour affichage
        Date dateAller = null;
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy", Locale.US);
        try {
            dateAller = dateFormat.parse(hAller);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        SimpleDateFormat dateFormat1 = new SimpleDateFormat("EEE dd MMM yyyy", Locale.FRANCE);
        String dateAllerStr = dateFormat1.format(dateAller);
        SimpleDateFormat dateFormat2 = new SimpleDateFormat("HH:mm", Locale.FRANCE);
        String heureAllerStr = dateFormat2.format(dateAller);

        // Créé un nouvel intent qui renvoie l'user vers l'activité adequate
        intent = new Intent(context, CovoiturageVehiclesActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 7, intent, PendingIntent.FLAG_ONE_SHOT);

        // Affichage de la 2e notification. Cliquée, elle renvoie vers l'activité voulue
        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
        inboxStyle.setBigContentTitle("TAAROAA"); // titre de la notif lorsq'uelle est ouverte
        inboxStyle.addLine("Covoiturage"); // sous titre affuché lorsque la notif est affichée
        inboxStyle.setSummaryText("Votre covoiturage partira le "+ dateAllerStr + " à " + heureAllerStr + " !"); // decription de la notif lorsqu'elle est ouverte

        // Create a Channel (Android 8) and set the importance
        String channelId = "fcm_default_channel";

        // Affichage de la notif qui apparait en premier à l'ecran. Affichage defini par la priorité
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(context, channelId)
                        // Set the notification content
                        .setSmallIcon(android.R.drawable.ic_notification_overlay)
                        .setContentTitle("TAAROAA")
                        .setContentText("Covoiturage")
                        .setSubText("Votre covoit sera pret dans  2 heures !")
                        .setPriority(NotificationCompat.PRIORITY_HIGH) //affiche la notif clairement en haut de l'app
                        .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                        // Set the intent that will fire when the user taps the notification : renvoi vers l'activité definie
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true)
                        // style permettant une seconde notif personnalisée comprenant plusieurs lignes
                        .setStyle(inboxStyle);

        int NOTIFICATION_ID = 7;
        String NOTIFICATION_TAG = "TAAROAA";
        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        assert notificationManager != null;
        notificationManager.notify(NOTIFICATION_TAG, NOTIFICATION_ID, notificationBuilder.build());
    }
}
