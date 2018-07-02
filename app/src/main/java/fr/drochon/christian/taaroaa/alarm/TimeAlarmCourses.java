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

import fr.drochon.christian.taaroaa.course.CoursesPupilsActivity;
import fr.drochon.christian.taaroaa.model.Course;

public class TimeAlarmCourses extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        Course cours;
        String dateCoursStr = null;
        String heureCoursStr = null;

        // --------------------
        // RECUPERATION BUNDLE
        // --------------------

        // recuperation de l'extra envoyé dans l'intent
        cours = (Course) Objects.requireNonNull(intent.getExtras()).getSerializable("cours");

        // --------------------
        // CONVERSION COVOITURAGE ALLER
        // --------------------
        if (cours != null) {

            // conversion de date pour affichage
            Date dateCours = null;
            SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy", Locale.US);
            try {
                dateCours = dateFormat.parse(cours.getHoraireDuCours().toString());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            SimpleDateFormat dateFormat1 = new SimpleDateFormat("EEE dd MMM yyyy", Locale.FRANCE);
            dateCoursStr = dateFormat1.format(dateCours);
            SimpleDateFormat dateFormat2 = new SimpleDateFormat("HH:mm", Locale.FRANCE);
            heureCoursStr = dateFormat2.format(dateCours);
        }

        // Créé un nouvel intent. Par contre, on ne renvoie pas l'user vers une nouvelle activité car on s'y trouve dejà.
        // On ne l'y renvoie uniquement que si l'user n'est pas sur la page activityPupilsActivity au moment du declenchement de la notif.
        // Sinon, ca fera une boucle infinie puisque la meme acticité sera rappellée sans cesse.
        if (intent.getExtras() != null) {
            Object activity = Objects.requireNonNull(intent.getExtras().get("activity"));
            if (activity != null && !activity.equals(CoursesPupilsActivity.class))
                intent = new Intent(context, CoursesPupilsActivity.class);
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 2, intent, PendingIntent.FLAG_ONE_SHOT);

        // --------------------
        // NOTIFICATION
        // --------------------

        // Affichage de la 2e notification. Cliquée, elle renvoie vers l'activité voulue
        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
        inboxStyle.setBigContentTitle("TAAROAA"); // titre de la notif lorsq'uelle est ouverte
        if (cours != null) {
            inboxStyle.addLine("Cours de niveau " + cours.getNiveauDuCours()); // sous titre affuché lorsque la notif est affichée
            inboxStyle.addLine(" ");
            inboxStyle.addLine("Le cours de " + cours.getSujetDuCours() + " dispensé par " + cours.getNomDuMoniteur());
            inboxStyle.addLine(" démarrera le " + dateCoursStr + " à " + heureCoursStr + " !"); // decription de la notif lorsqu'elle est ouverte

            // Create a Channel (Android 8) and set the importance
            String channelId = "fcm_default_channel";

            // Affichage de la notif qui apparait en premier à l'ecran. Affichage defini par la priorité
            NotificationCompat.Builder notificationBuilder =
                    new NotificationCompat.Builder(context, channelId)
                            // Set the notification content
                            .setSmallIcon(android.R.drawable.ic_notification_overlay)
                            .setContentTitle("TAAROAA")
                            .setContentText("COURS DE NIVEAU " + cours.getNiveauDuCours())
                            .setSubText("Votre prochain cours de " + cours.getTypeCours() + " démarrera à " + heureCoursStr + " !")
                            .setPriority(NotificationCompat.PRIORITY_HIGH) //affiche la notif clairement en haut de l'app
                            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                            // Set the intent that will fire when the user taps the notification : renvoi vers l'activité definie
                            .setContentIntent(pendingIntent)
                            .setAutoCancel(true)
                            // style permettant une seconde notif personnalisée comprenant plusieurs lignes
                            .setStyle(inboxStyle);

            int NOTIFICATION_ID = 2;
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
                if (notificationManager != null)
                    notificationManager.createNotificationChannel(channel);
            }

            // Show notification
            if (notificationManager1 != null)
                notificationManager1.notify(NOTIFICATION_TAG, NOTIFICATION_ID, notificationBuilder.build());
        }
    }
}
