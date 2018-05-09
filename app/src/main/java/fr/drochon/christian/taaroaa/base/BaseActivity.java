package fr.drochon.christian.taaroaa.base;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.Spinner;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import butterknife.ButterKnife;
import fr.drochon.christian.taaroaa.R;
import fr.drochon.christian.taaroaa.controller.MainActivity;
import fr.drochon.christian.taaroaa.controller.SummaryActivity;

/**
 * Created by Philippe on 12/01/2018.
 */

public abstract class BaseActivity extends AppCompatActivity {

    public static final int GET_USERNAME = 40;
    //FOR DATA CONNEXION
    private static final int SIGN_OUT_TASK = 10;
    private static final int DELETE_USER_TASK = 20;
    private static final int UPDATE_USERNAME = 30;


    // --------------------
    // LIFE CYCLE
    // --------------------

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this); //Configure Butterknife
    }

    public abstract int getFragmentLayout();

    // --------------------
    // TOOLBAR
    // --------------------

    /**
     * Methode permettant la configuration basic d'une toolbar pour les classes qui heritent de BaseActivity
     */
    protected void configureToolbar() {
        ActionBar ab = getSupportActionBar();
        // ajout d'un icone de l'appli à l'actionbar en haut à gauche
        assert ab != null;
        ab.setDisplayShowHomeEnabled(true);
        ab.setIcon(R.mipmap.ic_launcher);
        //ab.setTitle(R.string.app_name);
    }

    /**
     * Methode permettant de gerer les actions dee options de la toolbar.
     * Recuperation  du clic d'un user = switch car 2 options.
     * Surtout ne pas oublier le "true" apres chaque case sinon, ce sera toujours le dernier case qui sera executé!
     *
     * @param item
     * @return boolean
     */
    protected boolean optionsToolbar(Activity activity, MenuItem item) {
        switch (item.getItemId()) {
/*            case R.id.app_bar_search_adherents:
                // afichage de la barre de recherche
                return true;*/
            case R.id.app_bar_summary:
                // redirection à la page sommaire
                Intent intent = new Intent(activity, SummaryActivity.class);
                startActivity(intent);
                return true;

            case R.id.app_bar_deconnexion:
                // deconnexion
                AuthUI.getInstance()
                        .signOut(this) // methode utilisée par le singleton authUI.getInstance()
                        .addOnSuccessListener(this, this.updateUIAfterRESTRequestsCompleted(SIGN_OUT_TASK));
                // redirection vers la page d'accueil
                Intent intent1 = new Intent(activity, MainActivity.class);
                startActivity(intent1);
                return true;
        }
        return false;
    }

    // --------------------
    // CONNEXION ET AUTHENTIFICATION DES USERS
    // --------------------
    // Recuperation d'un utilisateur et si cette personne est connectée

    /**
     * Methode permettant de recuperer un utilisateur actuellement connecté à l'app.
     * FirebaseAuth?getInstance() est un singleton qui nous permet de ne recuperer qu'un seul utilisateur à la fois
     *
     * @return FirebaseUser
     */
    @Nullable
    protected FirebaseUser getCurrentUser() {
        return FirebaseAuth.getInstance().getCurrentUser();
    }


    /**
     * Methode permettant de savoir si un utilisateur est correctement identifié à Firebase
     *
     * @return bool
     */
    protected Boolean isCurrentUserLogged() {
        return (this.getCurrentUser() != null);
    }

    /**
     * Methode permettant d'afficher le floating button à l'ecran si l'utilisateur est un encadrant ou un initiateur.
     */
    private Boolean rightsSupervisors() {

        if (this.getCurrentUser() != null) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference mQuery = db.collection("users").document(getCurrentUser().getUid());

            mQuery.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                    if(documentSnapshot.exists()){
                        Object ds = documentSnapshot.get("niveau");
                        if(ds.equals("4") || ds.equals("MEF1") || ds.equals("MEF2"))
                            return;
                            //mFloatingActionButton.setVisibility(View.VISIBLE);
                    }
                }
            });
        }
        return false;
    }


    // --------------------
    // GESTION DES REQUETES REST
    // --------------------

    /**
     * Task Async utilsant un callback onsuccesslistener que l'onreutilisera dans les methodes de deconnexion et de suppresision,
     * permettant de verifier si la deconnexion d'un utilisateur ou la suppression d'un compte s'est correctement terminé.
     *
     * @param origin
     * @return
     */
    protected OnSuccessListener<Void> updateUIAfterRESTRequestsCompleted(final int origin) {
        return new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                switch (origin) {
                    case UPDATE_USERNAME:
                        finish();
                        break;
                    case GET_USERNAME:
                        finish();
                        break;
                    case SIGN_OUT_TASK:
                        finish();
                        break;
                    case DELETE_USER_TASK:
                        finish();
                        break;
                    default:
                        break;
                }
            }
        };
    }

    /**
     * Methode renvoyant une notification d'erreur de type inconnue dans un toast si Firestore a renvoyé un erreur lors des requetes CRUD
     *
     * @return OnFailureListener
     */
    protected OnFailureListener onFailureListener() {
        return new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), getString(R.string.error_unknown_error), Toast.LENGTH_LONG).show();
            }
        };
    }

    /**
     * Methode permettant à un utilisateur de se deconnecter retournant un objet de type Task permettant d erealiser ces appels de maniere asynchrone
     */
    protected void signOutUserFromFirebase() {
        AuthUI.getInstance()
                .signOut(this) // methode utilisée par le singleton authUI.getInstance()
                .addOnSuccessListener(this, this.updateUIAfterRESTRequestsCompleted(SIGN_OUT_TASK));
    }

    // --------------------
    // HEURE & DATE PARSING
    // --------------------

    /**
     * Methode permettant de formatter une date en string avec locale en francais
     * @param horaireDuCours
     * @return
     */
    public String  stDateToString(Date horaireDuCours){

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.FRANCE);
        String dateDuCours = dateFormat.format(horaireDuCours);
        return dateDuCours;

    }

    /**
     * Methode permettant de formatter une date en format heure
     * @param horaireDuCours
     * @return
     */
    public String stTimeToString(Date horaireDuCours){

        SimpleDateFormat dateFormat1 = new SimpleDateFormat("HH:mm:ss");
        String heureDuCours = dateFormat1.format(horaireDuCours);
        return heureDuCours;
    }

    // --------------------
    // UI
    // --------------------

    /**
     * Methode permettant de retrouver la position d'un item de la liste des niveaux de plongée d'un user
     *
     * @param spinner
     * @param myString
     * @return int
     */
    protected int getIndexSpinner(Spinner spinner, String myString) {
        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(myString)) {
                return i;
            }
        }
        return 0;
    }
}
