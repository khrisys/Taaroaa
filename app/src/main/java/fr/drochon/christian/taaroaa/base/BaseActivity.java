package fr.drochon.christian.taaroaa.base;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import butterknife.ButterKnife;
import fr.drochon.christian.taaroaa.R;

/**
 * Created by Philippe on 12/01/2018.
 */

public abstract class BaseActivity extends AppCompatActivity {

    public static final int GET_USERNAME = 40;
    //FOR DATA CONNEXION
    private static final int SIGN_OUT_TASK = 10;
    private static final int DELETE_USER_TASK = 20;
    private static final int UPDATE_USERNAME = 30;
    ProgressBar mProgressBar;


    // --------------------
    // LIFE CYCLE
    // --------------------

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //this.setContentView(this.getFragmentLayout());
        ButterKnife.bind(this); //Configure Butterknife
    }

    //public abstract int getFragmentLayout();

    // --------------------
    // TOOLBAR
    // --------------------

    /**
     * Methode permettant la configuration basic d'une toolbar pour les classes qui heritent de BaseActivity
     */
    protected void configureToolbar() {
        ActionBar ab = getSupportActionBar();
        // ajout d'un icone de l'appli à l'actionbar en haut à gauche
        ab.setDisplayShowHomeEnabled(true);
        ab.setIcon(R.mipmap.ic_launcher);


        //ab.setTitle(R.string.app_name);
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

}
