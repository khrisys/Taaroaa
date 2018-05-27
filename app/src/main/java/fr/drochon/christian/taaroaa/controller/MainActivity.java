package fr.drochon.christian.taaroaa.controller;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import fr.drochon.christian.taaroaa.R;
import fr.drochon.christian.taaroaa.alarm.NotificationReceiver;
import fr.drochon.christian.taaroaa.auth.AccountCreateActivity;
import fr.drochon.christian.taaroaa.auth.SearchUserActivity;
import fr.drochon.christian.taaroaa.base.BaseActivity;

import static fr.drochon.christian.taaroaa.R.id;
import static fr.drochon.christian.taaroaa.R.layout;
import static fr.drochon.christian.taaroaa.R.string;
import static fr.drochon.christian.taaroaa.R.string.app_name;
import static fr.drochon.christian.taaroaa.R.style;

//import fr.drochon.christian.taaroaa.R;

public class MainActivity extends BaseActivity {

    //Id de connexion dans l'activité courante
    private static final int RC_SIGN_IN = 123;
    // FOR COMMUNICATION
    Button mCreation;
    Button mConnexion;
    Button mDeconnexion;
    TextView mTextViewHiddenForSnackbar;
    public static boolean isAppRunning;

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
        getConnectedUser();

        mTextViewHiddenForSnackbar = findViewById(R.id.test_coordinator);
        mCreation = findViewById(R.id.creation_compte_btn);
        mConnexion = findViewById(id.connection_valid_btn);
        //if(isCurrentUserLogged()) mCreation.setVisibility(View.GONE);
        mDeconnexion = findViewById(id.deconnexion_btn);
        isAppRunning = true;

        // --------------------
        // LISTENERS
        // --------------------

        // lancement de l'activité de creation de compte
        mCreation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isCurrentUserLogged()) {

                    startAccountCreationActivity(); // creation de compte
                    /*//CREATION DU USER
                    createUserInFirestore();
                    startSummaryActivity(); // connecté : renvoyé vers le sommaire*/
                }
            }
        });

        // Lancement de la page de connection à un compte existant
        mConnexion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isCurrentUserLogged()) {
                    //CREATION DU USER
                    createUserInFirestore();
                    startSummaryActivity(); // connecté : renvoyé vers le sommaire
                } else {
                    startSignInActivity(); // non connecté : inscription
                }
            }
        });

        // Deconnexion de l'utilisateur
        mDeconnexion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSnackBar(getString(string.connection_end));
                signOutUserFromFirebase();
            }
        });

        // Si l'utilisateur a oublié son mot de passe, il clique sur le lien et est envoyé sur la page de recuperation du mot de passe
        EditText passwordRecovery = findViewById(id.mdp_oubli_lien);
        passwordRecovery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, PasswordActivity.class);
                startActivity(intent);
            }
        });
    }

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
        this.updateUIWhenResuming(); // affiche la vue lorsque le tel est dans le cycle de vie onResume()

        //CRASHLYTICS : force application to crash
        //Crashlytics.getInstance().crash();
    }


    // --------------------
    // PROVIDERS & AUTHENTIFICATION
    // --------------------

    /**
     * Methode lancant une page autogenerée par Firebase permettant la connexion/inscription à l'app
     */
    private void startSignInActivity() {
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder() // lance une activité de connexion/inscrption autogeneree
                        .setTheme(style.LoginTheme) // definir un style dans le fichier res/values/styles.xml
                        .setAvailableProviders( // ajoute des moyens divers de connexion (email, google, fb..)
                                Arrays.asList(new AuthUI.IdpConfig.EmailBuilder().build()))
                        .setIsSmartLockEnabled(false, true)
                        .setLogo(R.mipmap.logo1)
                        .build(),
                RC_SIGN_IN); // identifiant de connexion
    }

    /**
     * Methode permettant de changer d'ecran lors d'une connexion valide
     */
    private void startSummaryActivity() {
        Intent intent = new Intent(MainActivity.this, SummaryActivity.class);
        startActivity(intent);
    }

    private void startAccountCreationActivity() {
        Intent intent = new Intent(MainActivity.this, AccountCreateActivity.class);
        startActivity(intent);
    }


    // --------------------
    // CREATION D'USER DANS FIRESTORE + SNACKBAR + REDIRECTION
    // --------------------

    /**
     * Methode de recuperer le resultat renvoyé par l'activité autogénérée lors d'une inscription/connexion.
     * POur utiliser ce resultat, on va dans la methode handleResponseAfterSignIn()
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
     * Methode permettant d'afficher un message personnalisé dans une snackbar en fonction du resultat renvoyé par l'activité d'inscription/connexion
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    private void handleResponseAfterSignIn(int requestCode, int resultCode, Intent data) {

        IdpResponse response = IdpResponse.fromResultIntent(data);

        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) { // SUCCESS

                //TODO condition de creation d'un utilisateur ???????????????
                //getUserFromFirestore();
                this.createUserInFirestore();
                //showSnackBar(getString(string.connection_succeed));
                //TODO mettre un thread sleep ici?
                this.startSummaryActivity(); // connexion et renvoi vers la page sommaire
            }
        } else { // ERRORS
            if (response == null) {
                showSnackBar(getString(string.error_authentication_canceled));
            } else if (response.getErrorCode() == ErrorCodes.NO_NETWORK) {
                showSnackBar(getString(string.error_no_internet));
            } else if (response.getErrorCode() == ErrorCodes.UNKNOWN_ERROR) {
                showSnackBar(getString(string.error_unknown_error));
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
                    //mConnexion.setText(string.button_create_account);
                    startSummaryActivity();
                } else {
                    //mConnexion.setText(string.button_go_summary);
                    startSignInActivity();
                }
            }
        });
    }

    // --------------------
    // ALARM
    // --------------------

    private void clenchAlarm(){

    }

    // --------------------
    // REST REQUESTS - DECONNEXION, CREATION D'USER
    // --------------------

    /**
     * Methode de creation d'un utilisateur, avec condition de creation en fonction de l'existance ou non d'un user dejà en bdd,
     * et decomposant le nom et le prenom saisi à l'enregistrement de la personne.
     */
    private void createUserInFirestore() {

        if (this.getCurrentUser() != null) {
            Query mQuery = setupDb().collection("users").whereEqualTo("uid", getCurrentUser().getUid());

            // RAJOUTER LE THIS DANS LE LUSTENER PERMET DE RESTREINDRE LE CONTEXT A CETTE ACTIVITE, EVITANT AINSI DE METTRE LES DONNEES
            // A JOUR A CHAUQE FOIS QU'IL Y A UN UPDATE DANS L'APP.
            // SI ON ENLEVE LE THIS, ON CREERA UN NOUVEAU DOCUMENT A CHAQUE FOIS QU'ON EN SUPPRIMERA UN, PAR EX !
            mQuery.addSnapshotListener(this, new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                    // condition de creation d'un user ou affichage simple d'un message indiquant que l'user existe dejà en bdd.
                    // Avec les uid, il ne peut y avoir de doublon.
                    if (documentSnapshots.size() == 1) {
                        Log.e("TAG", "Le document existe !");
                    } else {
                        // recuperation des données de l'user
                        String username = getCurrentUser().getDisplayName();
                        // decomposition du nom et du prenom recu dans username
                        String nom = null, prenom = null;
                        String[] parts;
                        assert username != null;
                        if (username.contains(" ")) {
                            parts = username.split(" ");
                            try {
                                if (parts[1] != null) nom = parts[1];
                                else nom = "";
                            } catch (ArrayIndexOutOfBoundsException e1) {
                                Log.e("TAG", "ArrayOutOfBoundException " + e1.getMessage());
                            }
                            if (parts[0] != null) prenom = parts[0];
                            else prenom = "";
                        } else {
                            nom = username;
                            prenom = "";
                        }
                        String uid = getCurrentUser().getUid();
                        String email = getCurrentUser().getEmail();

                        //UserHelper.createUser(uid, username, email).addOnFailureListener(this.onFailureListener());
                        addNewUser(uid, nom, prenom, email);
                    }
                }
            });
        }
        // si l(utilisateur n'a pas de compte , on lui en fait creer un
        else {
            startSignInActivity();
        }
    }

    /**
     * Methode permettant de creer un user lorsque celui ci vient de se connecter pour la 1ere fois.
     *
     * @param uid
     * @param nom
     * @param prenom
     * @param email
     */
    private void addNewUser(String uid, String nom, String prenom, String email) {

        Map<String, Object> newContact = new HashMap<>();
        newContact.put("uid", uid);
        newContact.put("nom", nom);
        newContact.put("prenom", prenom);
        newContact.put("email", email);
        setupDb().collection("users").document(Objects.requireNonNull(getCurrentUser()).getUid()).set(newContact)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(MainActivity.this, string.create_account,
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, "ERROR" + e.toString(),
                                Toast.LENGTH_SHORT).show();
                        Log.d("TAG", e.toString());
                    }
                });
    }

    private void getConnectedUser(){
        setupDb().collection("users").document(getCurrentUser().getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(documentSnapshot.exists()){
                    Map<String, Object> user = documentSnapshot.getData();
                    int hash = (int) Long.parseLong(user.get("hash").toString());
                    sendVisualNotification(hash);
                }
            }
        });
    }
    private void sendVisualNotification(int hash) {
        int NOTIFICATION_ID = 7;
        String NOTIFICATION_TAG = "TAAROAA";
        // Créé un intent qui ouvre l'activité voulue
        Intent intent = new Intent(this, SearchUserActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 7, intent, PendingIntent.FLAG_ONE_SHOT);


        // Create a Style for the Notification
        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
        inboxStyle.setBigContentTitle(getString(R.string.notification_title));
        inboxStyle.addLine("le sans du rat");

        // Create a Channel (Android 8) and set the importance
        String channelId = getString(R.string.default_notification_channel_id);

        // Build a Notification object : interieur de lappli et renvoi vers une activité definie via l'intent plus haut
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        // Set the notification content
                        .setSmallIcon(android.R.drawable.ic_notification_overlay)
                        .setContentTitle(getString(R.string.app_name))
                        .setContentText(getString(R.string.notification_title))
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                        // Set the intent that will fire when the user taps the notification : renvoi vers l'activité definie
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true)
                        // style permettant une notif de plusieurs lignes
                        .setStyle(inboxStyle);

        // Create a Channel (Android 8) and set the importance
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // Support Version >= Android 8
        this.createNotificationChannel(channelId);
        // Show notification
        assert notificationManager != null;
        notificationManager.notify(hash, notificationBuilder.build());
    }

    /**
     * Methode permettant de creer une channel et de determiner osn importance. Avant de pouvoir delivrer une
     * notification sur Android 8 ou +, il aut determiner une notification's channel de l'app.
     * Grace à cette chaine, l'user aura alors directement la possibilité de modifier les paramètres (comme l'importance,
     * le son, la lumière, la vibration, etc...) des notifications inscrites dans un canal, sans avoir besoin de les coder
     * dans votre application Android.
     *
     * @param CHANNEL_ID
     */
    private void createNotificationChannel(String CHANNEL_ID) {

        NotificationManager notificationManager = null;
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Message provenant de Firebase";
            String description = "Description de la chaine de notification";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            channel.setName(name);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            notificationManager = getSystemService(NotificationManager.class);
            assert notificationManager != null;
            notificationManager.createNotificationChannel(channel);
        }
    }
}
