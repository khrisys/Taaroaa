package fr.drochon.christian.taaroaa.alarm;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import fr.drochon.christian.taaroaa.covoiturage.CovoiturageVehiclesActivity;
import fr.drochon.christian.taaroaa.model.Covoiturage;

public class TimeAlarmCovoiturageRetour extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        String dateRetourStr = null;
        String heureRetourStr = null;

        // --------------------
        // RECUPERATION BUNDLE
        // --------------------

        String retour = intent.getStringExtra("hRetour");

        // recuperation de l'extra envoyé dans l'intent
        Bundle bundle = intent.getExtras();
        if(bundle != null) {
            //hAller = bundle.getString("hAller");
            Covoiturage c = (Covoiturage) bundle.getSerializable("covoiturageAlarm");

            // --------------------
            // CONVERSION COVOITURAGE RETOUR
            // --------------------
            if (c != null) {
                // conversion de date pour affichage
                Date dateRetour = null;
                SimpleDateFormat dateFormat3 = new SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy", Locale.US);
                try {
                    dateRetour = dateFormat3.parse(c.getHoraireRetour().toString());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                SimpleDateFormat dateFormat4 = new SimpleDateFormat("EEE dd MMM yyyy", Locale.FRANCE);
                dateRetourStr = dateFormat4.format(dateRetour);
                SimpleDateFormat dateFormat5 = new SimpleDateFormat("HH:mm", Locale.FRANCE);
                heureRetourStr = dateFormat5.format(dateRetour);
            }
            // provient de la creation d'un covoiturage
            else if(retour != null){
                Date dateRetour = null;
                SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy", Locale.US);
                try {
                    dateRetour = dateFormat.parse(retour);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                SimpleDateFormat dateFormat4 = new SimpleDateFormat("EEE dd MMM yyyy", Locale.FRANCE);
                dateRetourStr = dateFormat4.format(dateRetour);
                SimpleDateFormat dateFormat5 = new SimpleDateFormat("HH:mm", Locale.FRANCE);
                heureRetourStr = dateFormat5.format(dateRetour);
            }

            // Créé un nouvel intent qui renvoie l'user vers l'activité adequate
            intent = new Intent(context, CovoiturageVehiclesActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 1, intent, PendingIntent.FLAG_ONE_SHOT);

            // --------------------
            // NOTIFICATION
            // --------------------

            // Affichage de la 2e notification. Cliquée, elle renvoie vers l'activité voulue
            NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();

            inboxStyle.setBigContentTitle("TAAROAA"); // titre de la notif lorsq'uelle est ouverte
            inboxStyle.addLine("COVOITURAGE"); // sous titre affuché lorsque la notif est affichée
            inboxStyle.setSummaryText("Votre covoiturage Retour partira ");
            inboxStyle.setSummaryText("\t" + dateRetourStr + " à " + heureRetourStr + " !");// decription de la notif lorsqu'elle est ouverte


            // Create a Channel (Android 8) and set the importance
            String channelId = "fcm_default_channel";

            // Affichage de la notif qui apparait en premier à l'ecran. Affichage defini par la priorité
            NotificationCompat.Builder notificationBuilder =
                    new NotificationCompat.Builder(context, channelId)
                            // Set the notification content
                            .setSmallIcon(android.R.drawable.ic_notification_overlay)
                            .setContentTitle("TAAROAA")
                            .setContentText("COVOITURAGE")
                            .setSubText("Départ " + dateRetourStr + " à " + heureRetourStr + " !")
                            .setContentInfo("Trajet Retour")
                            .setPriority(NotificationCompat.PRIORITY_HIGH) //affiche la notif clairement en haut de l'app
                            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                            // Set the intent that will fire when the user taps the notification : renvoi vers l'activité definie
                            .setContentIntent(pendingIntent)
                            .setAutoCancel(true)
                            // style permettant une seconde notif personnalisée comprenant plusieurs lignes
                            .setStyle(inboxStyle);

            int NOTIFICATION_ID = 7;
            String NOTIFICATION_TAG = "TAAROAA";
            // Create a Channel (Android 8) and set the importance
            NotificationManager notificationManager1 = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            /*
            Methode permettant de creer une channel et de determiner osn importance. Avant de pouvoir delivrer une
            notification sur Android 8 ou +, il aut determiner une notification's channel de l'app.
            Grace à cette chaine, l'user aura alors directement la possibilité de modifier les paramètres (comme l'importance,
            le son, la lumière, la vibration, etc...) des notifications inscrites dans un canal, sans avoir besoin de les coder
            dans votre application Android.
             */
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                CharSequence name = "Message provenant de Firebase";
                String description = "Description de la chaine de notification";
                int importance = NotificationManager.IMPORTANCE_HIGH;
                NotificationChannel channel = new NotificationChannel(channelId, name, importance);
                channel.setDescription(description);
                channel.setName(name);
                // Register the channel with the system; you can't change the importance
                // or other notification behaviors after this
                NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
                if(notificationManager != null)
                notificationManager.createNotificationChannel(channel);
            }

            // Show notification

            if(notificationManager1 != null)
            notificationManager1.notify(NOTIFICATION_TAG, NOTIFICATION_ID, notificationBuilder.build());
        }
    }
}
