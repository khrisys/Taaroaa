package fr.drochon.christian.taaroaa.covoiturage;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import fr.drochon.christian.taaroaa.R;
import fr.drochon.christian.taaroaa.base.BaseActivity;

public class CovoiturageAccueilActivity extends BaseActivity {

    Button btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_covoiturage_accueil);
        btn = findViewById(R.id.creation_covoit_btn);
        configureToolbar();
        giveToolbarAName(R.string.covoit_accueil_name);



 /*       // Build a Notification object : interieur de lappli et renvoi vers une activité definie via l'intent plus haut
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Europe/Paris"));
        Date currentDate = calendar.getTime();
        Date date1 = null;
        DateFormat dateFormat = new SimpleDateFormat("dd MM yyyy HH:mm:ss", Locale.FRANCE);
        dateFormat.setTimeZone(TimeZone.getTimeZone("Europe/Paris"));
        String d = "20 05 2018 16:58:00";
        try {
            date1 = dateFormat.parse(d);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (currentDate.after(date1))
            notif();
*/


        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CovoiturageAccueilActivity.this, CovoiturageVehiclesActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public int getFragmentLayout() {
        return 0;
    }


    // --------------------
    // TOOLBAR
    // --------------------

    /**
     * Fait appel au fichier xml menu pour definir les icones.
     * Definit differentes options dans le menu caché.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.course_pupils_menu, menu);
        return true; // true affiche le menu
    }

    /**
     * recuperation  du clic d'un user.
     * On utilise un switch ici car il peut y avoir plusieurs options.
     * Surtout ne pas oublier le "true" apres chaque case sinon, ce sera toujours le dernier case qui sera executé!
     *
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return optionsToolbar(this, item);
    }

/*    public void notif() {

        // Créé un intent qui renvoie l'user vers l'activité adequate
        Intent intent = new Intent(this, SearchUserActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 7, intent, PendingIntent.FLAG_ONE_SHOT);

        // Affichage de la notificaion cliquée, celle qui renvoie vers l'activité voulue
        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
        //inboxStyle.setBigContentTitle(getString(R.string.notification_title));
        inboxStyle.setBigContentTitle("TAAROAA"); // titre de la notif lorsq'uelle est ouverte
        inboxStyle.addLine("Covoiturage"); // sous titre affuché lorsque la notif est affichée
        inboxStyle.setSummaryText("Attention! Votre covoiturage partira de Millau dans 2 heures !"); // decription de la notif lorsqu'elle est ouverte

        String channelId = getString(R.string.default_notification_channel_id);

        // Affichage de la notifqui apparait en premier à l'ecran. Affichage defini par la priorité
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        // Set the notification content
                        .setSmallIcon(android.R.drawable.ic_notification_overlay)
                        .setContentTitle(getString(R.string.app_name))
                        .setContentText(getString(R.string.notification_title))
                        .setSubText("Covoiturage")
                        .setPriority(NotificationCompat.PRIORITY_HIGH) //affiche la notif clairement en haut de l'app
                        .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                        // Set the intent that will fire when the user taps the notification : renvoi vers l'activité definie
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true)
                        // style permettant une seconde notif personnalisée comprenant plusieurs lignes
                        .setStyle(inboxStyle);

        int NOTIFICATION_ID = 7;
        String NOTIFICATION_TAG = "TAAROAA";
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert notificationManager != null;
        notificationManager.notify(NOTIFICATION_TAG, NOTIFICATION_ID, notificationBuilder.build());
    }*/
}
