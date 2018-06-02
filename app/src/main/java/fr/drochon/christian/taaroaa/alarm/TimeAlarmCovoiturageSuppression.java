package fr.drochon.christian.taaroaa.alarm;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import fr.drochon.christian.taaroaa.covoiturage.CovoiturageVehiclesActivity;
import fr.drochon.christian.taaroaa.model.Covoiturage;

public class TimeAlarmCovoiturageSuppression extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        Covoiturage covoiturage;
        String dateCovoitAllerStr = null;
        String heureCovoitAllerStr = null;
        String dateCovoitRetourStr = null;
        String heureCovoitRetourStr = null;


        // --------------------
        // RECUPERATION BUNDLE
        // --------------------

        // recuperation de l'extra envoyé dans l'intent
        covoiturage = (Covoiturage) Objects.requireNonNull(intent.getExtras()).getSerializable("covoit");

        // Créé un nouvel intent qui renvoie l'user vers l'activité adequate
        intent = new Intent(context, CovoiturageVehiclesActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 3, intent, PendingIntent.FLAG_ONE_SHOT);

        // --------------------
        // CONVERSION COVOITURAGE ALLER
        // --------------------
        if (covoiturage != null) {

            // conversion de date pour affichage
            Date dateCovoitAller = null;
            Date dateCovoitRetour = null;
            SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy", Locale.US);
            try {
                dateCovoitAller = dateFormat.parse(covoiturage.getHoraireAller().toString());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            SimpleDateFormat dateFormat1 = new SimpleDateFormat("EEE dd MMM yyyy", Locale.FRANCE);
            dateCovoitAllerStr = dateFormat1.format(dateCovoitAller);
            SimpleDateFormat dateFormat2 = new SimpleDateFormat("HH:mm", Locale.FRANCE);
            heureCovoitAllerStr = dateFormat2.format(dateCovoitAller);

            try {
                dateCovoitRetour = dateFormat.parse(covoiturage.getHoraireRetour().toString());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            dateCovoitRetourStr = dateFormat1.format(dateCovoitRetour);
            heureCovoitRetourStr = dateFormat2.format(dateCovoitRetour);
        }

        // --------------------
        // NOTIFICATION
        // --------------------

        // Affichage de la 2e notification. Cliquée, elle renvoie vers l'activité voulue
        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();

        inboxStyle.setBigContentTitle("TAAROAA"); // titre de la notif lorsq'uelle est ouverte
        inboxStyle.addLine("Annulation d'un covoiturage"); // sous titre affuché lorsque la notif est affichée
        inboxStyle.addLine(" ");
        assert covoiturage != null;
        inboxStyle.addLine(covoiturage.getPrenomConducteur() + " " + covoiturage.getNomConducteur() + " a annulé le covoiturage partant ");
        inboxStyle.addLine("le " + dateCovoitAllerStr + " à " + heureCovoitAllerStr + " depuis " + covoiturage.getLieuDepartAller());
        inboxStyle.addLine("et revenant le " + dateCovoitRetourStr + " à " + heureCovoitRetourStr + " à " + covoiturage.getLieuDepartRetour() + " !"); // decription de la notif lorsqu'elle est ouverte

        // Create a Channel (Android 8) and set the importance
        String channelId = "fcm_default_channel";

        // Affichage de la notif qui apparait en premier à l'ecran. Affichage defini par la priorité
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(context, channelId)
                        // Set the notification content
                        .setSmallIcon(android.R.drawable.ic_notification_overlay)
                        .setContentTitle("TAAROAA")
                        .setContentText("ANNULATION DE COVOITURAGE")
                        .setSubText("Votre covoiturage a été annulé !")
                        .setPriority(NotificationCompat.PRIORITY_HIGH) //affiche la notif clairement en haut de l'app
                        .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                        // Set the intent that will fire when the user taps the notification : renvoi vers l'activité definie
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true)
                        // style permettant une seconde notif personnalisée comprenant plusieurs lignes
                        .setStyle(inboxStyle);

        int NOTIFICATION_ID = 3;
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
            assert notificationManager != null;
            notificationManager.createNotificationChannel(channel);
        }

        // Show notification
        assert notificationManager1 != null;
        notificationManager1.notify(NOTIFICATION_TAG, NOTIFICATION_ID, notificationBuilder.build());
    }
}
