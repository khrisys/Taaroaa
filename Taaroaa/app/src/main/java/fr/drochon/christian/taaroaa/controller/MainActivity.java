package fr.drochon.christian.taaroaa.controller;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;

import java.util.Arrays;

import fr.drochon.christian.taaroaa.R;
import fr.drochon.christian.taaroaa.api.UserHelper;
import fr.drochon.christian.taaroaa.base.BaseActivity;

import static fr.drochon.christian.taaroaa.R.drawable;
import static fr.drochon.christian.taaroaa.R.id;
import static fr.drochon.christian.taaroaa.R.layout;
import static fr.drochon.christian.taaroaa.R.string;
import static fr.drochon.christian.taaroaa.R.style;

//import fr.drochon.christian.taaroaa.R;

public class MainActivity extends BaseActivity {

    //Id de connexion dans l'activité courante
    private static final int RC_SIGN_IN = 123;

    //FOR DATA CONNEXION
    private static final int SIGN_OUT_TASK = 10;
    private static final int DELETE_USER_TASK = 20;
    private static final int UPDATE_USERNAME = 30;

    TextView mTextView;
    /*@BindView(id.deconnexion_btn) Button mDeconnexion;
    @BindView(id.connection_valid_btn) Button mConnexion;*/
    Button mConnexion;
    Button mDeconnexion;
    TextView mTextViewHiddenForSnackbar;

    //@BindView(R.id.progress_bar) ProgressBar progressBar;

    // --------------------
    // IDENTIFICATION PAR FIREBASE
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
                                Arrays.asList(new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build(), //EMAIL
                                        new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build(), //GOOGLE
                                        new AuthUI.IdpConfig.Builder(AuthUI.FACEBOOK_PROVIDER).build())) // FACEBOOK
                        .setIsSmartLockEnabled(false, true)
                        .setLogo(drawable.logo_vgt)
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


    // --------------------
    // NOTIFICATION DE CONNEXION AFFICHANT LE RESULTAT DE L'INSCRIPTION
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
                //TODO condition de creation d'un utilisateur
                //this.createUserInFirestore();
                showSnackBar(getString(string.connection_succeed));
                this.startSummaryActivity(); // connexion et renvoi vers la page sommaire
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
    }


    /**
     * Methode permettant l'affichage de la snackbar.
     * Dans cette app, n'ayant pas besoin de coordinatorLayout, j'ai créé un champ View vide dans mon IHM MainActivity.
     * Cette methode n'a donc pas besoin de 'CoordinatorLayout' en param non plus.
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
    // REST REQUESTS - DECONNEXION
    // --------------------

    /**
     * Methode permettant à un utilisateur de se deconnecter retournant un objet de type Task permettant d erealiser ces appels de maniere asynchrone
     */
    private void signOutUserFromFirebase() {
        AuthUI.getInstance()
                .signOut(this) // methode utilisée par le singleton authUI.getInstance()
                .addOnSuccessListener(this, this.updateUIAfterRESTRequestsCompleted(SIGN_OUT_TASK));
    }

    /**
     * Methode de creation d'un utilisateur
     */
    private void createUserInFirestore() {

        if (this.getCurrentUser() != null) {

            String username = this.getCurrentUser().getDisplayName();
            String uid = this.getCurrentUser().getUid();
            String email = this.getCurrentUser().getEmail();

            UserHelper.createUser(uid, username, email).addOnFailureListener(this.onFailureListener());
        }
    }


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

        // ajout d'un textView centré dans l'actionbar
        //final ActionBar actionBar = getSupportActionBar();
        //Objects.requireNonNull(getSupportActionBar()).setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        //getSupportActionBar().setCustomView(R.layout.activity_main);
        //actionBar.setTitle(R.string.app_name);

        mTextViewHiddenForSnackbar = findViewById(R.id.test_coordinator);
        mConnexion = findViewById(id.connection_valid_btn);
        mDeconnexion = findViewById(id.deconnexion_btn);

        // --------------------
        // LISTENERS
        // --------------------
        /**
         * Lancement de la page de connection lors d'un clic sur le bouton "Connectez vous"
         */
        mConnexion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isCurrentUserLogged()) {
                    startSummaryActivity(); // connecté : renvoyé vers le sommaire
                } else {
                    startSignInActivity(); // non connecté : inscription
                }
            }
        });

        // Si l'utilisateur clique sur le bouton de creation d'un compte, il sera redirigé vers la page adequate.
        /* Button creationCompte = findViewById(id.creation_compte_btn);
        creationCompte.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AccountCreateActivity.class);
                startActivity(intent);
            }
        });*/
        // Deconnexion de l'utilisateur
        mDeconnexion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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

    /**
     * Methode permettant un affichage different en fonction de si l'user a dejà été loggé ou pas
     */
    @Override
    protected void onResume() {
        super.onResume();
        this.updateUIWhenResuming(); // affiche la vue lorsque le tel est dans le cycle de vie onResume()
    }
}
