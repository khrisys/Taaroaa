package fr.drochon.christian.taaroaa.base;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import butterknife.ButterKnife;
import fr.drochon.christian.taaroaa.R;
import fr.drochon.christian.taaroaa.auth.MainActivity;
import fr.drochon.christian.taaroaa.controller.SummaryActivity;

/**
 * Created by Philippe on 12/01/2018.
 */

public abstract class BaseActivity extends AppCompatActivity {


    //FOR DATA CONNEXION
    private static final int SIGN_OUT_TASK = 10;
    private static final int DELETE_USER_TASK = 20;
    private static final int UPDATE_USERNAME = 30;
    private static final int GET_USERNAME = 40;


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

        if(ab != null) {
            ab.setDisplayShowTitleEnabled(false); // empeche l'affichage du titre de l'app dans les toolbars de l'app
            ab.setDisplayShowHomeEnabled(true);
            // ajout d'un icone de l'appli à l'actionbar en haut à gauche
            //ab.setIcon(R.mipmap.logo);
        }
    }

    /**
     * Methode permettant de donner un nom à chacune des pages de l'application
     * @param title
     */
    protected void giveToolbarAName(int title){
        ActionBar ab = getSupportActionBar();
        if(ab != null) {
            ab.setDisplayShowCustomEnabled(true);
            ActionBar.LayoutParams layoutParams = new ActionBar.LayoutParams(ActionBar.LayoutParams.WRAP_CONTENT, ActionBar.LayoutParams.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);

            TextView tv = new TextView(this);
            tv.setGravity(View.TEXT_ALIGNMENT_CENTER);
            tv.setTextColor(Color.WHITE);
            tv.setTextSize(20f);
            tv.setTypeface(tv.getTypeface(), Typeface.BOLD);
            tv.setText(title);

            ab.setCustomView(tv, layoutParams);
        }
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
            case R.id.app_bar_summary:
                // redirection à la page sommaire
                Intent intent = new Intent(activity, SummaryActivity.class);
                startActivity(intent);
                return true;

            case R.id.app_bar_deconnexion:
                // deconnexion
                AuthUI.getInstance()// methode utilisée par le singleton authUI.getInstance()
                        .signOut(this)
                        .addOnSuccessListener(this, this.updateUIAfterRESTRequestsCompleted(SIGN_OUT_TASK));
                // redirection vers la page d'accueil, avec un extra à false pour afficher le bouton de creation de compte
                Intent intent1 = new Intent(activity, MainActivity.class);
                startActivity(intent1);
                return true;
        }
        return false;
    }

    // --------------------
    // CONNEXION ET AUTHENTIFICATION DES USERS
    // --------------------

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
                        //finish();
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

    /**
     * Methode permettant de faire appel à la classe FirebaseFirestore via son singleton, et pour chaque requete
     * qui utilisera cette instance, de lui assigner des parametres de persistance des données à true, de maniere
     * à pouvoir travailler avec des donnée s hors connexion.
     * @return
     */
    protected FirebaseFirestore setupDb(){

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();

        db.setFirestoreSettings(settings);
        return db;
    }


    // --------------------
    // HEURE & DATE PARSING
    // --------------------

    /**
     * Methode permettant de formatter une date en string avec locale en francais
     * @param horaireDuCours
     * @return
     */
    protected String  stDateToString(Date horaireDuCours){

        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE dd MMM yyyy ' à ' HH'h'mm", Locale.FRANCE);
        return dateFormat.format(horaireDuCours);
    }

    /**
     * Methode permettant de formatter une date en format heure
     * @param horaireDuCours horaire du cours sous forme de date
     * @return date sous forme de string
     */
    public String stTimeToString(Date horaireDuCours){

        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat dateFormat1 = new SimpleDateFormat("HH:mm:ss");
        return dateFormat1.format(horaireDuCours);
    }

    protected Date stStringToDate(String horaire){
        SimpleDateFormat formatter = new SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy", Locale.US);
        Date dateFormatee = null;

        try {
            dateFormatee = formatter.parse(horaire);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return dateFormatee;
    }


    // --------------------
    // UI
    // --------------------

    /**
     * Methode permettant de retrouver la position d'un item de la liste des niveaux de plongée d'un user
     *
     * @param spinner menu deroulant
     * @param myString item choisi dans la liste
     * @return int retour la position de l'item choisi
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
