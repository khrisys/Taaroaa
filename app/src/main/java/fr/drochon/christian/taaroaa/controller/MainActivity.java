package fr.drochon.christian.taaroaa.controller;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
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
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import fr.drochon.christian.taaroaa.R;
import fr.drochon.christian.taaroaa.auth.AccountCreateActivity;
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
    //FOR DATA CONNEXION
    private static final int SIGN_OUT_TASK = 10;
    // FOR COMMUNICATION
    Button mCreation;
    Button mConnexion;
    Button mDeconnexion;
    TextView mTextViewHiddenForSnackbar;


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



        mTextViewHiddenForSnackbar = findViewById(R.id.test_coordinator);
        mCreation = findViewById(R.id.creation_compte_btn);
        mConnexion = findViewById(id.connection_valid_btn);
        mDeconnexion = findViewById(id.deconnexion_btn);




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
                                Arrays.asList(new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build(), //EMAIL
                                        new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build(), //GOOGLE
                                        new AuthUI.IdpConfig.Builder(AuthUI.FACEBOOK_PROVIDER).build())) // FACEBOOK
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

    private void startAccountCreationActivity(){
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
    // REST REQUESTS - DECONNEXION, CREATION D'USER
    // --------------------

    /**
     * Methode de creation d'un utilisateur, avec condition de creation en fonction de l'existance ou non d'un user dejà en bdd,
     * et decomposant le nom et le prenom saisi à l'enregistrement de la personne.
     */
    private void createUserInFirestore() {

        if (this.getCurrentUser() != null) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            Query mQuery = db.collection("users").whereEqualTo("uid", getCurrentUser().getUid());

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
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> newContact = new HashMap<>();
        newContact.put("uid", uid);
        newContact.put("nom", nom);
        newContact.put("prenom", prenom);
        newContact.put("email", email);
        db.collection("users").document(Objects.requireNonNull(getCurrentUser()).getUid()).set(newContact)
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
}
