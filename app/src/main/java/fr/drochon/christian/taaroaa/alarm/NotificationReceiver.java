package fr.drochon.christian.taaroaa.alarm;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import java.util.Date;

public class NotificationReceiver extends BroadcastReceiver {

    public final static String NOTIFICATION_ID = "7";
    public final static String NOTIFICATION = "notification";
    String NOTIFICATION_TAG = "TAAROAA";

    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        Bundle bundle = intent.getExtras();
        assert bundle != null;
        //hAller = bundle.getString("hAller");
        Date date = (Date) bundle.get("date");

        Notification notification = intent.getParcelableExtra(NOTIFICATION);
        int id = intent.getIntExtra(NOTIFICATION_ID, 0);
        assert notificationManager != null;
        notificationManager.notify(id, notification);
    }
}
