package fr.drochon.christian.taaroaa.auth;

import android.annotation.SuppressLint;
import android.content.ComponentCallbacks2;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.perf.FirebasePerformance;
import com.google.firebase.perf.metrics.Trace;

import java.util.Collections;
import java.util.Objects;

import fr.drochon.christian.taaroaa.R;
import fr.drochon.christian.taaroaa.base.BaseActivity;
import fr.drochon.christian.taaroaa.controller.SummaryActivity;

import static fr.drochon.christian.taaroaa.R.id;
import static fr.drochon.christian.taaroaa.R.layout;
import static fr.drochon.christian.taaroaa.R.string;
import static fr.drochon.christian.taaroaa.R.string.app_name;


public class MainActivity extends BaseActivity implements ComponentCallbacks2 {

    //Id de connexion dans l'activité courante
    private static final int RC_SIGN_IN = 123;
    public static boolean isAppRunning;
    private Button mConnexion;
    private Button creationCompte;
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
        creationCompte = findViewById(id.creation_compte_btn);
        mConnexion = findViewById(id.connection_valid_btn);
        mConnexion.requestFocus();
        Button deconnexion = findViewById(id.deconnexion_btn);

        // lorsque je suis connecté, c'est que j'ai un compte et je n'ai pas besoin de voir le bouton "creer un compte"
        //if(isCurrentUserLogged()) creationCompte.setVisibility(View.GONE);
        isAppRunning = true;

        // Souscription aux notifications
        FirebaseMessaging.getInstance().subscribeToTopic("courses");
        FirebaseMessaging.getInstance().subscribeToTopic("covoiturages");


        // --------------------
        // LISTENERS
        // --------------------
        // un user connecté se dirigera vers le sommaire s'il est dejç connecté ou vers
        // la page de connection s(il ne l'est pas ou q'uil veut creer son compte
        creationCompte.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Test performance de la creation d'un compte user
                final Trace myTrace = FirebasePerformance.getInstance()
                        .newTrace("mainActivityAccountCreation_trace");
                myTrace.start();

                if (!isCurrentUserLogged()) {
                    //startSignInActivity(); // non connecté : inscription
                    startConnectionActivity();
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
                creationCompte.setVisibility(View.VISIBLE);

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
     * Methode permettant un affichage different en fonction de si l'user a dejà été loggé ou pas
     */
    @Override
    protected void onResume() {
        super.onResume();
        this.updateUIWhenResuming();


        //CRASHLYTICS : force application to crash
        //Crashlytics.getInstance().crash();
    }

    /**
     * Methode permettant de changer d'ecran lors d'une connexion valide
     */
    private void startSummaryActivity() {
        Intent intent = new Intent(MainActivity.this, SummaryActivity.class);
        startActivity(intent);
    }

    /**
     * Methode permettant d'aller sur la page de rensignement des identifiants de l'utilisateur (email, password)
     */
    private void startConnectionActivity() {

        Intent intent = new Intent(MainActivity.this, ConnectionActivity.class);
        startActivity(intent);
    }

    /**
     * Methode permettant d'afficher sur le bouton de connexion soit la direction de l'ecran de connexion
     * soit la direction de l'ecran sommaire en fonction de si l'user est connexté ou pas, et de
     * rediriger l'user vers l'affichage de la page adequate
     */
    private void updateUIWhenResuming() {

        this.mConnexion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (isCurrentUserLogged()) {
                    startSummaryActivity();//connecté
                } else {
                    startSignInActivity(); //deconnecté = je lance la signin!!!
                }
            }
        });
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
                        .createSignInIntentBuilder() // lance une activité de connexion/inscrption autogeneree
                        //.setTheme(R.style.LoginTheme) // definir un style dans le fichier res/values/styles.xml
                        .setAvailableProviders( // ajoute des moyens divers de connexion (email, google, fb..)
                                Collections.singletonList(new AuthUI.IdpConfig.EmailBuilder().build()))
                        .setIsSmartLockEnabled(false, true)
                        .setLogo(R.mipmap.logo1)
                        .build(),
                RC_SIGN_IN); // identifiant de connexion
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
     * @param requestCode
     * @param resultCode
     * @param data
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
     * @param requestCode
     * @param resultCode
     * @param data
     */
    private void handleResponseAfterSignIn(int requestCode, int resultCode, Intent data) {

        IdpResponse response = IdpResponse.fromResultIntent(data);

        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {

                // RECUPERATION DES CARACTERISTIQUES DE LA PERSONNE CONNECTEE
                String[] parts;
                if (response != null) {
                    @SuppressLint("RestrictedApi") String mUsername = response.getUser().getName();
                    mEmailUser = response.getEmail();
                    mPassword = response.getProviderType();
                    // decomposition du nom et du prenom recu dans le param name
                    if (mUsername != null) {
                        if (mUsername.contains(" ")) {
                            parts = mUsername.split(" ");
                            try {
                                if (parts[1] != null) mName = parts[1];
                                else mName = "";
                            } catch (ArrayIndexOutOfBoundsException e1) {
                                Log.e("TAG", "ArrayOutOfBoundException " + e1.getMessage());
                            }
                            if (parts[0] != null) mFirstName = parts[0];
                            else mFirstName = "";
                        } else {
                            mName = mUsername;
                            mFirstName = " ";// donc firstname non null
                        }
                    }
                    //ENVOI VERS LE CONTROKE DE DECURITE DUMAIL DE LA PERSONNE CONNECTEE
                    // envoi des identifiants sur laclasse AccountCreateActivity pour verification que son email
                    // notamment ne soit pas erronée, chose la pls frequente
                    Intent intent = new Intent(MainActivity.this, AccountCreateActivity.class);
                    intent.putExtra("name", mName);
                    intent.putExtra("firstname", mFirstName);
                    intent.putExtra("email", mEmailUser);
                    intent.putExtra("password", mPassword);
                    startActivity(intent);
                }
            }
            // ERROR de recuperation de personne avec differents types d'erruer de connexion
        } else {
            if (response == null) {
                showSnackBar(getString(string.error_authentication_canceled));
            }
            if (response != null) {
                if (Objects.requireNonNull(response.getError()).getErrorCode() == ErrorCodes.NO_NETWORK) {
                    showSnackBar(getString(string.error_no_internet));
                }
                if (Objects.requireNonNull(response.getError()).getErrorCode() == ErrorCodes.UNKNOWN_ERROR) {
                    showSnackBar(getString(string.error_unknown_error));
                }
            }
        }
    }

    /**
     * Methode permettant l'affichage de la snackbar.
     * Dans cette app, n'ayant pas besoin de coordinatorLayout, j'ai créé un champ View vide dans mon IHM MainActivity.
     * Cette methode n'a donc pas besoin de 'CoordinatorLayout' en param non plus mais de la view.
     *
     * @param message
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
