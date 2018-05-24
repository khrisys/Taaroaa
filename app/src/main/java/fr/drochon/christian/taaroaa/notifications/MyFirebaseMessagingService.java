package fr.drochon.christian.taaroaa.notifications;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Random;

import fr.drochon.christian.taaroaa.R;
import fr.drochon.christian.taaroaa.controller.MainActivity;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String NOTIFICATION_ID_EXTRA = "notificationId";
    private static final String IMAGE_URL_EXTRA = "imageUrl";
    private static final String ADMIN_CHANNEL_ID = "admin_channel";
    private NotificationManager notificationManager;

    // RECEPTION D'UNE NOTIFICATION
    /*
    BACKGROUND COMPATIBILY
    nous utilisons remoteMessage.getData () pour accéder aux valeurs de la notification reçue.
    Maintenant, il existe deux types de notifications Firebase: les messages de données et les messages de notification.

    1 : Les messages de données sont gérés ici dans onMessageReceived, que l'application soit au premier plan ou en arrière-plan.
    Cependant, les messages de notification ne sont reçus que lorsque l'application est au premier plan, ce qui les rend un
    peu inutiles ou au moins plutôt ennuyeux, n'est-ce pas?

    Pour un système de notifications unifié, nous utilisons des messages qui ont une charge utile de données uniquement.

    2 : Les messages contenant des notifications et des données utiles sont traités comme des messages de notification,
    ils ne seront donc pas gérés par MyFirebaseMessagingService lorsque l'application est en arrière-plan!

    Si vous n'avez pas encore configuré votre serveur, vous pouvez toujours tester vos notifications push avec une requête
    POST http directement dans Firebase. Vous pouvez utiliser n'importe quelle application qui vous convient, nous utilisons le
    plugin Google Postman.
     */
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        /**
         * NOTIFICATION ID
         * Ici, pour obtenir des notifications uniques chaque fois que vous recevez un nouveau message,
         * dans l'intérêt de cet exemple, nous générons un nombre aléatoire et l'utilisons comme identifiant
         * de notification. Avec cet ID, vous pouvez faire plusieurs choses à vos notifications. En tant que
         * tel, vous devriez probablement les regrouper s'ils sont du même type ou les mettre à jour. Si
         * vous voulez voir chaque notification individuellement des autres, leurs ID doivent être différents.
         */
        //You should use an actual ID instead
        int notificationId = 1;

        /**
         * NOTIFICATIONS TESTS
         * Apres validation du test postman, nous pouvons envoyer et tester des notifications, nous pouvons les rendre plus fantaisistes.
         * Tout d'abord, ajoutons une fonctionnalité de clic pour la notification:
         */
        Intent notificationIntent = new Intent(this, MainActivity.class);
        if (MainActivity.isAppRunning) {
            //Some action
            notificationIntent = new Intent(this, MainActivity.class);
        } else {
            //Show notification as usual
            notificationIntent = new Intent(this, MainActivity.class);
        }

        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        final PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0 /* Request code */, notificationIntent,
                PendingIntent.FLAG_ONE_SHOT);

        Bitmap bitmap = getBitmapfromUrl(remoteMessage.getData().get("image-url"));

        Intent likeIntent = new Intent(this, MainActivity.class);
        likeIntent.putExtra(NOTIFICATION_ID_EXTRA, notificationId);
        likeIntent.putExtra(IMAGE_URL_EXTRA, remoteMessage.getData().get("image-url"));
        PendingIntent likePendingIntent = PendingIntent.getService(this,
                notificationId, likeIntent, PendingIntent.FLAG_ONE_SHOT);


        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        /**
         * NOTIFICATIONS CHANNEL
         * Chaque application qui cible le SDK 26 ou supérieur (Android O) doit implémenter des canaux de
         * notification et ajouter ses notifications à au moins l'un d'entre eux. Autrement dit, vous séparez
         * vos notifications en canaux en fonction de leur fonction et de leur niveau d'importance. Avoir plus
         * de canaux donne aux utilisateurs plus de contrôle sur les notifications qu'ils reçoivent. Si vous
         * souhaitez recevoir les notifications de vos nouveaux téléphones, collez cette méthode dans votre service.
         */
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            setupChannels();
        }
        /**
         *Initialisation d'une constante ADMIN_CHANNEL_ID qui est de type String. J'utilise cette variable
         * id pour faire référence à ma nouvelle chaîne. Donc, chaque fois que j'utilise NotificationCompat.Builder
         * pour créer une nouvelle notification, j'initialise l'objet builder et passe l'identifiant dans le constructeur, comme ceci:
         */
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, ADMIN_CHANNEL_ID)// canal de noification
                        .setLargeIcon(bitmap) // on met l'image dans lma notif
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(remoteMessage.getData().get("title"))
                        // Le NotificationCompat.Builder prend en charge plusieurs types de styles différents pour
                        // les notifications, y compris un lecteur et ceux avec des dispositions personnalisées:
                        .setStyle(new NotificationCompat.BigPictureStyle()
                                .setSummaryText(remoteMessage.getData().get("message"))
                                .bigPicture(bitmap))/*Notification with Image*/
                        .setContentText(remoteMessage.getData().get("message"))
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        // Ajouter des boutons à la notif
                        .addAction(R.drawable.logo_vgt,
                                getString(R.string.account_search_name), likePendingIntent)
                        .setContentIntent(pendingIntent);

        notificationManager.notify(notificationId, notificationBuilder.build());

    }

    /**
     * Les photos dans les notifications peuvent être très attrayantes. Voici comment nous pouvons ajouter une image à notre notification push:
     * Simple method for image downloading
     *
     * @param imageUrl
     * @return
     */
    public Bitmap getBitmapfromUrl(String imageUrl) {
        try {
            /**
             Dans ce cas, nous envoyons une URL d'image dans la charge utile de notification pour l'application à télécharger. Habituellement,
             de tels processus sont exécutés sur un thread séparé. Cependant, dans ce cas, cette classe est un service, donc une fois le code
             dans onMessageReceived exécuté, le service, qui est un thread différent du thread principal, est détruit et va avec chaque thread
             créé par le service. Par conséquent, nous pouvons nous permettre de télécharger l'image de manière synchrone. Cela ne devrait pas
             constituer une menace pour les performances, car le thread de service n'est pas le thread principal.
             **/
            URL url = new URL(imageUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            return BitmapFactory.decodeStream(input);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void setupChannels() {
        CharSequence adminChannelName = getString(R.string.error_authentication_canceled);
        String adminChannelDescription = getString(R.string.account_create_name);

        NotificationChannel adminChannel;
        adminChannel = new NotificationChannel(ADMIN_CHANNEL_ID, adminChannelName, NotificationManager.IMPORTANCE_LOW);
        adminChannel.setDescription(adminChannelDescription);
        adminChannel.enableLights(true);
        adminChannel.setLightColor(Color.RED);
        adminChannel.enableVibration(true);
        if (notificationManager != null) {
            notificationManager.createNotificationChannel(adminChannel);
        }
    }
}
