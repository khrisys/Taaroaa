package fr.drochon.christian.taaroaa.auth;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;

import fr.drochon.christian.taaroaa.R;
import fr.drochon.christian.taaroaa.api.UserHelper;
import fr.drochon.christian.taaroaa.base.BaseActivity;
import fr.drochon.christian.taaroaa.controller.MainActivity;
import fr.drochon.christian.taaroaa.controller.SummaryActivity;
import fr.drochon.christian.taaroaa.model.User;

public class AccountCreateActivity extends BaseActivity {

    // identifiant pour identifier la requete REST
    private static final int SIGN_OUT_TASK = 10;
    private static final int DELETE_USER_TASK = 20;
    private static final int UPDATE_USERNAME = 30;

    TextInputEditText mPrenom;
    TextInputEditText mNom;
    TextInputEditText mLicence;
    Spinner mNiveauPlongeespinner;
    Spinner mFonctionAuClubspinner;
    TextInputEditText mEmail;
    ProgressBar mProgressBar;
    Button mModificationCompte;
    Button mSuppressionCompte;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_create);
        this.configureToolbar();

        setTitle("Création de compte");

        mPrenom = findViewById(R.id.prenom_txt);
        mNom = findViewById(R.id.nom_txt);
        mLicence = findViewById(R.id.licence_txt);
        mNiveauPlongeespinner = findViewById(R.id.niveau_spinner);
        mFonctionAuClubspinner = findViewById(R.id.fonction_spinner);
        mEmail = findViewById(R.id.email_txt);
        mProgressBar = findViewById(R.id.progress_bar);
        mModificationCompte = findViewById(R.id.modificiation_compte_btn);
        //TODO n'afficher le bouton de suppression qu'aux encadrants
        mSuppressionCompte = findViewById(R.id.suppression_compte_btn);

        // methode à appeler apres l'initialisation des variables, sinon les variables auront des references null
        this.updateUIWhenCreating(); // recuperation des informations de l'user actuel


        //TODO verifier que tous les champs soient remplis


        // --------------------
        // LISTENERS
        // --------------------

        //Lorsqu'un utilisateur a rempli correctement le formulaire, il est renvoyé à la page Sommaire
        // 1 - lorsqu'un plongeur est connecté, le menu deroulant des fonctions est desactivé; tous les autres champs sont acesssibles.
        // 2 - lorsqu'un encadrant est connecté, tous les chamos sont en lecture seule, sauf le menu deroulant des fonctions
        mModificationCompte.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateUserInFirebase(); // update dans firebase
                startSummaryActivity();
            }
        });


        // Suppression d'un compte utilisateur
        mSuppressionCompte.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(mSuppressionCompte.getContext()).create();
                //TODO alertdialog
                //deleteUserFromFirebase();
                startMainActivity();
            }
        });


        // --------------------
        // SPINNERS & REMPLISSAGE
        // --------------------
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapterNiveau = ArrayAdapter.createFromResource(this,
                R.array.niveaux_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapterNiveau.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        mNiveauPlongeespinner.setAdapter(adapterNiveau);

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.fonctions_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        mFonctionAuClubspinner.setAdapter(adapter);
    }

    /**
     * Methode appellée lors d'un reaffichage de l'activité
     */
    @Override
    protected void onResume() {
        super.onResume();
        this.updateUIWhenResuming();
    }

    /**
     * Methode permettant de changer d'ecran lors d'une connexion valide
     */
    private void startSummaryActivity() {
        Intent intent = new Intent(AccountCreateActivity.this, SummaryActivity.class);
        startActivity(intent);
    }

    /**
     * Methode permettant de revenir à la page d'accueil lorsqu'un utilisateur a supprimer son compte
     */
    private void startMainActivity() {
        Intent intent = new Intent(AccountCreateActivity.this, MainActivity.class);
        startActivity(intent);
    }


    // --------------------
    // RECUPERATION DES INFORMATIONS USER
    // --------------------

    /**
     * Methode permettant d'afficher les informations de l'user sur l'ecran AccountCreateActivity lorsqu'un user vient de creer un compte
     */
    private void updateUIWhenCreating() {

        if (this.getCurrentUser() != null) { // retourne un user FirebaseUser. Permettra de remplir toutes les vues de l'activité

            //Get email & username from Firebase
            String email = TextUtils.isEmpty(this.getCurrentUser().getEmail()) ? getString(R.string.info_no_email_found) : this.getCurrentUser().getEmail();
            String username = TextUtils.isEmpty(this.getCurrentUser().getDisplayName()) ? getString(R.string.info_no_username_found) : this.getCurrentUser().getDisplayName();

            //Update views with data
            this.mNom.setText(username);
            this.mEmail.setText(email);
            this.mFonctionAuClubspinner.setEnabled(false);
            //TODO afficher toutes les informations d'un user

        }
    }

    /**
     * Methode permettant d'afficher les informations d'un user depuis la bdd firestore
     */
    private void updateUIWhenResuming() {

        if (this.getCurrentUser() != null) { // retourne un user FirebaseUser. Permettra de remplir toutes les vues de l'activité

            //Get email & username from Firebase
            String email = TextUtils.isEmpty(this.getCurrentUser().getEmail()) ? getString(R.string.info_no_email_found) : this.getCurrentUser().getEmail();
            String username = TextUtils.isEmpty(this.getCurrentUser().getDisplayName()) ? getString(R.string.info_no_username_found) : this.getCurrentUser().getDisplayName();
            // decomposition du nom et du prenom
            String nom = null, prenom = null;
            String[] parts;
            if(username.contains(" ")) {
                parts = username.split(" ");
                if(parts[1] != null)  nom = parts[1];  else nom = "";
                if(parts[0] != null) prenom = parts[0];  else prenom = "";
            } else {
                nom = username;
                prenom = "";
            }


            String licence;
            String niveau;
            String fonction;

            //Update views with data
            this.mNom.setText(nom);
            this.mPrenom.setText(prenom);
            this.mEmail.setText(email);
            this.mFonctionAuClubspinner.setEnabled(false);
            //TODO afficher toutes les informations d'un user
        }
    }

    // --------------------
    // REST REQUETES
    // --------------------

    /**
     * Cette methode ne comprend pas l'update d'une fonction dans le club, car seul les encadrants du club peuvent
     * le faire, et cette fonctionnalité est donc reservée à une fonction particuliere.
     */
    private void updateUserInFirebase() {

        this.mProgressBar.setVisibility(View.VISIBLE);
        String nom = this.mNom.getText().toString();
        String prenom = this.mPrenom.getText().toString();
        String licence = this.mLicence.getText().toString();
        String niveau = this.mNiveauPlongeespinner.getSelectedItem().toString();
        String fonction = this.mFonctionAuClubspinner.getSelectedItem().toString();
        String email = this.mEmail.getText().toString();

        if (this.getCurrentUser() != null) {
            //TODO alert dialog lorsque tous les champs ne sont pas remplis
            if (!nom.isEmpty() && !nom.equals(getString(R.string.info_no_username_found)) && !prenom.isEmpty() && !email.isEmpty()) { // verification que tous les champs vides soient remplis
                UserHelper.updateUser(this.getCurrentUser().getUid(), nom, prenom, licence, email, niveau, fonction).addOnFailureListener(this.onFailureListener()).addOnSuccessListener(this.updateUIAfterRESTRequestsCompleted(UPDATE_USERNAME));
            }
        }
    }



    /*    *//**
     * Methode permettant à un encadrant de changer la fonction d'un plongeur au sein du club
     *//*
    private void updateUserFonctionInFirebase(){

        this.mProgressBar.setVisibility(View.VISIBLE);
        String nom = this.mNom.getText().toString();
        String prenom = this.mPrenom.getText().toString();
        String licence = this.mLicence.getText().toString();
        String niveau = this.mNiveauPlongeespinner.getSelectedItem().toString();

        if (this.getCurrentUser() != null) {
            UserHelper.updateUserFonction(this.getCurrentUser().getUid(), this.mFonctionAuClubspinner.getSelectedItem().toString()).addOnFailureListener(this.onFailureListener());
        }
    }*/

    /**
     * Methode permettant à un encadrant de supprimer un compte. Retourne un objet de type Task permettant de realiser ces appels de maniere asynchrone
     */
    private void deleteUserFromFirebase() {
        if (this.getCurrentUser() != null) {

            //On supprime un utilisateur de la bdd firestore
            UserHelper.deleteUser(this.getCurrentUser().getUid()).addOnFailureListener(this.onFailureListener());
            //TODO mettre une notification si elle n'arrive pas avoir ajouté le deleteuser ci dessus
            AuthUI.getInstance()
                    .delete(this) // methode utilisée par le singleton authUI.getInstance()
                    .addOnSuccessListener(this, this.updateUIAfterRESTRequestsCompleted(DELETE_USER_TASK));
        }
    }

    /**
     * Methode permettant à un utilisateur de se deconnecter retournant un objet de type Task permettant d erealiser ces appels de maniere asynchrone
     */
    private void signOutUserFromFirebase() {
        AuthUI.getInstance()
                .signOut(this) // methode utilisée par le singleton authUI.getInstance()
                .addOnSuccessListener(this, this.updateUIAfterRESTRequestsCompleted(SIGN_OUT_TASK));
    }

}
