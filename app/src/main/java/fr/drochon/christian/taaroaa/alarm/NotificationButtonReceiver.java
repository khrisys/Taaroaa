package fr.drochon.christian.taaroaa.alarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NotificationButtonReceiver extends BroadcastReceiver {

    private Context mContext;

    @Override
    public void onReceive(Context context, Intent intent) {
        this.mContext = context;
    }
}
