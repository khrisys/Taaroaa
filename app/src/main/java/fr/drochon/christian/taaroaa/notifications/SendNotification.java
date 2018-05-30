package fr.drochon.christian.taaroaa.notifications;

import android.util.Log;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Objects;

public class SendNotification extends FirebaseMessagingService {

    FirebaseMessaging fm = FirebaseMessaging.getInstance();

    /**
     * Called by the system when the service is first created.  Do not call this method directly.
     */
    @Override
    public void onCreate() {
        String SENDER_ID = "963968628408";
        String notificationId = "7";

        RemoteMessage remoteMessage = new RemoteMessage.Builder(SENDER_ID + "@gcm.googleapis.com")
                .setMessageId(notificationId)
                .addData("title", "Hello World")
                .addData("text", "SAY_HELLO")
                .build();

        String TAG = "TAAROAA";
        if (!remoteMessage.getData().isEmpty()) {
            Log.e(TAG, "UpstreamData: " + remoteMessage.getData());
        }

        if (!Objects.requireNonNull(remoteMessage.getMessageId()).isEmpty()) {
            Log.e(TAG, "UpstreamMessageId: " + remoteMessage.getMessageId());
        }


        fm.send(remoteMessage);
    }


}
