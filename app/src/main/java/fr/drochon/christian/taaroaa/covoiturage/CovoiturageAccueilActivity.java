package fr.drochon.christian.taaroaa.covoiturage;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Objects;

import fr.drochon.christian.taaroaa.R;
import fr.drochon.christian.taaroaa.base.BaseActivity;

public class CovoiturageAccueilActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_covoiturage_accueil);
        Button btn = findViewById(R.id.creation_covoit_btn);

        configureToolbar();
        giveToolbarAName(R.string.covoit_accueil_name);

        // --------------------
        // LISTENER
        // --------------------

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CovoiturageAccueilActivity.this, CovoiturageVehiclesActivity.class);
                startActivity(intent);
            }
        });
    }

    // --------------------
    // UI
    // --------------------

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
     * @param item menuitem
     * @return optionsToolBar
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return optionsToolbar(this, item);
    }


    // --------------------
    // CLASSE INTERNE DE NOTIFICATION
    // --------------------
    @SuppressLint("StaticFieldLeak")
    private class Notif extends AsyncTask<Void, Void, Void>{

        @Override
        protected Void doInBackground(Void... voids) {
            String TAG = "TAAROAA";
            String SENDER_ID = "963968628408";
            String notificationId = "7"; //new Random().nextInt(60000);

            FirebaseMessaging fm = FirebaseMessaging.getInstance();

            RemoteMessage remoteMessage = new RemoteMessage.Builder(SENDER_ID)
                    .setMessageId(notificationId)
                    .addData("my_message", "Hello World")
                    .addData("my_action", "SAY_HELLO")
                    .build();

            if (!remoteMessage.getData().isEmpty()) {
                Log.e(TAG, "UpstreamData: " + remoteMessage.getData());
            }

            if (!Objects.requireNonNull(remoteMessage.getMessageId()).isEmpty()) {
                Log.e(TAG, "UpstreamMessageId: " + remoteMessage.getMessageId());
            }


            fm.send(remoteMessage);
            return null;
        }
    }
}


