package fr.drochon.christian.taaroaa.auth;

import android.annotation.SuppressLint;
import android.content.ComponentCallbacks2;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.perf.FirebasePerformance;
import com.google.firebase.perf.metrics.Trace;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import fr.drochon.christian.taaroaa.R;
import fr.drochon.christian.taaroaa.base.BaseActivity;
import fr.drochon.christian.taaroaa.controller.SummaryActivity;
import fr.drochon.christian.taaroaa.model.User;

import static fr.drochon.christian.taaroaa.R.layout;
import static fr.drochon.christian.taaroaa.R.string;
import static fr.drochon.christian.taaroaa.R.string.app_name;


public class MainActivity extends BaseActivity implements ComponentCallbacks2 {

    //Id de connexion dans l'activité courante
    private static final int RC_SIGN_IN = 123;
    public static boolean isAppRunning;

    private TextView mTextViewHiddenForSnackbar;

    private String mName;
    private String mFirstName;
    private String mEmailUser;
    private String mPassword;


    // --------------------
    // LIFE CYCLE
    // --------------------

    /**
     * Methode permettant de creer l'ecran de l'activité destinée à la connexion d'un utilisateur
     *
     * @param savedInstanceState : sauvegarde du tel
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layout.activity_main);

        configureToolbar();
        giveToolbarAName(app_name);
        //onTrimMemory(TRIM_MEMORY_BACKGROUND);


        mTextViewHiddenForSnackbar = findViewById(R.id.test_coordinator);
        Button creationCompte = findViewById(R.id.connection_valid_btn);
        Button deconnexion = findViewById(R.id.deconnexion_btn);


        // Souscription aux notifications
        FirebaseMessaging.getInstance().subscribeToTopic("courses");
        FirebaseMessaging.getInstance().subscribeToTopic("covoiturages");


        // --------------------
        // LISTENERS
        // --------------------
        // Redirection de l'user vers l'activité adequate en fonction s'il est dejà loggé ou pas
        creationCompte.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Test performance de la creation d'un compte user
                final Trace myTrace = FirebasePerformance.getInstance().newTrace("mainActivityAccountCreation_trace");
                myTrace.start();

                if (!isCurrentUserLogged()) {
                    startSignInActivity(); // non connecté : inscription ou entree valide
                } else {
                    Toast.makeText(MainActivity.this, "Vous etes déjà connecté, vous ne pouvez pas créer un compte !", Toast.LENGTH_LONG).show();
                    startSummaryActivity(); // connecté : renvoyé vers le sommaire
                }

                myTrace.stop();
            }
        });

        // Deconnexion de l'utilisateur à l'application avec une petite notification
        deconnexion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Test performance de l'update d'un compte user
                final Trace myTrace2 = FirebasePerformance.getInstance().newTrace("mainActivityDeconnexionFromAnExistingAccount_trace");
                myTrace2.start();

                showSnackBar(getString(string.connection_end));
                signOutUserFromFirebase();

                myTrace2.stop();
            }
        });
    }


    // --------------------
    // UI
    // --------------------

    @Override
    public int getFragmentLayout() {
        return layout.activity_main;
    }


    /**
     * Methode permettant de changer d'ecran lors d'une connexion valide
     */
    private void startSummaryActivity() {
        Intent intent = new Intent(MainActivity.this, SummaryActivity.class);
        startActivity(intent);
    }


    /**
     * Methode permettant un affichage different en fonction de si l'user a dejà été loggé ou pas startsummary
     */
    @Override
    protected void onResume() {
        super.onResume();
        //lancement de l"activite de connexin ou de login

       /* if (!isCurrentUserLogged()) {
            startSignInActivity(); // non connecté : inscription ou entree valide
        }*/


        //CRASHLYTICS : force application to crash
        //Crashlytics.getInstance().crash();
    }


    // --------------------
    // PROVIDERS & AUTHENTIFICATION
    // --------------------

    /**
     * Methode lancant une page autogenerée par Firebase permettant la connexion/inscription à l'app.
     * Methode permettant egalement de savoir si la personne se connecte en mode hors connexion.
     */
    private void startSignInActivity() {
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        //.setTheme(R.style.LoginTheme)
                        .setAvailableProviders(
                                Arrays.asList(new AuthUI.IdpConfig.EmailBuilder().build(), //EMAIL
                                        new AuthUI.IdpConfig.GoogleBuilder().build())) // SUPPORT GOOGLE
                        .setIsSmartLockEnabled(false, true)
                        //.setLogo(R.mipmap.logo1)
                        .build(),
                RC_SIGN_IN);// identifiant de connexion
    }


    // --------------------
    // AUTHENTIFICATION ET VERIFICATION VALIDITE EMAIL
    // --------------------

    /**
     * Methode permettant de recuperer le resultat renvoyé par l'activité autogénérée l
     * ors d'une inscription/connexion.
     * POur utiliser ce resultat et creer un compte ou simplement connecter un user,
     * on va dans la methode handleResponseAfterSignIn()
     *
     * @param requestCode int de l'user
     * @param resultCode  resultat de la requete
     * @param data        intent comportant ls données necessaires à l'affichage de l'user
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        this.handleResponseAfterSignIn(requestCode, resultCode, data);
    }

    /**
     * Methode permettant d'afficher un message personnalisé dans une snackbar en fonction du
     * resultat renvoyé par l'activité d'inscription/connexion
     *
     * @param requestCode int de l'user
     * @param resultCode  resultat de la requete
     * @param data        intent comportant les données
     */
    @SuppressLint("RestrictedApi")
    private void handleResponseAfterSignIn(int requestCode, int resultCode, Intent data) {

        final IdpResponse response = IdpResponse.fromResultIntent(data);

        if (requestCode == RC_SIGN_IN && resultCode == RESULT_OK && response != null) {
            final String user1 = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
            setupDb().collection("users").addSnapshotListener(this, new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                    if (queryDocumentSnapshots != null) {
                        ///LA PERSONNE CONNECTEE EST DEJA ENREGISTREE EN BDD. ON RECUPERE ET AFFICHE TOUTES SES DONNEES
                        User u;
                        List<DocumentSnapshot> ds = queryDocumentSnapshots.getDocuments();
                        for (int i = 0; i < ds.size(); i++) {
                            if (ds.get(i).getId().equals(user1)) {

                                u = new User(ds.get(i).getId(),
                                        Objects.requireNonNull(ds.get(i).get("nom")).toString(),
                                        ds.get(i).get("prenom").toString(), //le prenom peut etre null
                                        Objects.requireNonNull(ds.get(i).get("licence")).toString(),
                                        Objects.requireNonNull(ds.get(i).get("email")).toString(),
                                        Objects.requireNonNull(ds.get(i).get("niveau")).toString(),
                                        Objects.requireNonNull(ds.get(i).get("fonction")).toString(),
                                        Objects.requireNonNull(ds.get(i).get("password")).toString());


                                Intent intent = new Intent(MainActivity.this, AccountCreateActivity.class).putExtra("connectedUser", u);
                                startActivity(intent);
                                break;
                            }

                        }
                        //LA PERSONNE CONNECTEE N'EST PAS EN BDD OU ELLE EST ENTRAIN DE CREER SON COMPTE
                        if (response.getEmail() != null) {
                            String mUsername = Objects.requireNonNull(getCurrentUser()).getDisplayName();
                            // decomposition du nom et du prenom recu dans le param username
                            if (mUsername != null) {
                                String nom = null;
                                String prenom;
                                String[] parts;
                                if (mUsername.contains(" ")) {
                                    parts = mUsername.split(" ");
                                    try {
                                        if (parts[1] != null) nom = parts[1];
                                        else nom = "";
                                    } catch (ArrayIndexOutOfBoundsException e1) {
                                        Log.e("TAG", "ArrayOutOfBoundException " + e1.getMessage());
                                    }
                                    if (parts[0] != null) prenom = parts[0];
                                    else prenom = "";
                                } else {
                                    nom = getCurrentUser().getDisplayName();
                                    prenom = "";
                                }
                                mName = nom;
                                mFirstName = prenom;
                                mEmailUser = response.getEmail();
                                mPassword = response.getUser().getProviderId();
                                //ENVOI VERS LE CONTROLE DE DECURITE VIA MAIL DE LA PERSONNE CONNECTEE
                                // envoi des identifiants sur laclasse AccountCreateActivity pour verification que son email
                                // notamment ne soit pas erronée, chose la pls frequente
                                Intent intent = new Intent(MainActivity.this, AccountCreateActivity.class);
                                intent.putExtra("nom", mName);
                                intent.putExtra("prenom", mFirstName);
                                intent.putExtra("email", mEmailUser);
                                intent.putExtra("password", mPassword);
                                startActivity(intent);
                            }
                        }
                    }
                    // ERROR de recuperation de personne avec differents types d'erruer de connexion
                    else {
                        showSnackBar(getString(string.error_authentication_canceled));
                        signOutUserFromFirebase();
                    }
                }
            });
        }
    }


    /**
     * Methode permettant l'affichage de la snackbar.
     * Dans cette app, n'ayant pas besoin de coordinatorLayout, j'ai créé un champ View vide dans mon IHM MainActivity.
     * Cette methode n'a donc pas besoin de 'CoordinatorLayout' en param non plus mais de la view.
     *
     * @param message de deconnexion
     */
    private void showSnackBar(String message) {
        Snackbar.make(mTextViewHiddenForSnackbar, message, Snackbar.LENGTH_LONG).show();
    }


    // --------------------
    // REST REQUESTS
    // --------------------


// --------------------
    // PERSO : OBSERVATION DANS LE LOGCAT DE LA MEMOIRE UTILISEE POUR LE DEBUG DE MON TEL
    // --------------------

    /**
     * Release memory when the UI becomes hidden or when system resources become low.
     *
     * @param level the memory-related event that was raised.
     */
    public void onTrimMemory(int level) {

        // Determine which lifecycle or system event was raised.
        switch (level) {

            case ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN:

                /*
                   Release any UI objects that currently hold memory.

                   The user interface has moved to the background.
                */

                break;

            case ComponentCallbacks2.TRIM_MEMORY_RUNNING_MODERATE:
            case ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW:
            case ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL:

                /*
                   Release any memory that your app doesn't need to run.

                   The device is running low on memory while the app is running.
                   The event raised indicates the severity of the memory-related event.
                   If the event is TRIM_MEMORY_RUNNING_CRITICAL, then the system will
                   begin killing background processes.
                */

                break;

            case ComponentCallbacks2.TRIM_MEMORY_BACKGROUND:
            case ComponentCallbacks2.TRIM_MEMORY_MODERATE:
            case ComponentCallbacks2.TRIM_MEMORY_COMPLETE:

                /*
                   Release as much memory as the process can.

                   The app is on the LRU list and the system is running low on memory.
                   The event raised indicates where the app sits within the LRU list.
                   If the event is TRIM_MEMORY_COMPLETE, the process will be one of
                   the first to be terminated.
                */

                break;

            default:
                /*
                  Release any non-critical data structures.

                  The app received an unrecognized memory level value
                  from the system. Treat this as a generic low-memory message.
                */
                break;
        }
    }
}
