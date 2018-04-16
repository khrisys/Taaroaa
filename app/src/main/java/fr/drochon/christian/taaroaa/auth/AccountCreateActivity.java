package fr.drochon.christian.taaroaa.auth;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import fr.drochon.christian.taaroaa.R;
import fr.drochon.christian.taaroaa.api.UserHelper;
import fr.drochon.christian.taaroaa.base.BaseActivity;
import fr.drochon.christian.taaroaa.controller.MainActivity;
import fr.drochon.christian.taaroaa.controller.SummaryActivity;

public class AccountCreateActivity extends BaseActivity {

    public static final int GET_USERNAME = 40;
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
        configureToolbar();

        //setTitle("Création de compte");

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

        // methode à appeler APRES l'initialisation des variables, sinon les variables auront des references null
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
                //updateUserInFirebase(); // update dans firebase
                updateData();
                startSummaryActivity();
            }
        });


        // Suppression d'un compte utilisateur avec alertdialog avant suppression
        mSuppressionCompte.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog.Builder adb = new AlertDialog.Builder(AccountCreateActivity.this);
                adb.setTitle(R.string.alertDialog_account);
                adb.setIcon(android.R.drawable.ic_dialog_alert);
                adb.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        EditText editText = findViewById(R.id.alertdialog_ok_account);
                        Toast.makeText(AccountCreateActivity.this, editText.getText(), Toast.LENGTH_LONG).show();
                        //deleteUserFromFirebase();
                        deleteData();
                        startMainActivity();
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        EditText editText = findViewById(R.id.alertdialog_delete_account);
                        Toast.makeText(AccountCreateActivity.this, editText.getText(), Toast.LENGTH_LONG).show();
                        //finish();
                        //TODO est ce qu'on change d'ecran ou pas ?
                    }
                });
                adb.show(); // affichage de l'artdialog

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

    @Override
    public int getFragmentLayout() {
        return R.layout.activity_account_create;
    }

    /**
     * Methode appellée lors d'un reaffichage de l'activité
     */
    @Override
    protected void onResume() {
        super.onResume();
        this.updateUIWhenResuming();
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
        getMenuInflater().inflate(R.menu.account_create_menu, menu);
        return true; // true affiche le menu
    }

    /**
     * recuperation  du clic d'un user.
     * On utilise un switch ici car il peut y avoir plusieurs options.
     * Surtout ne pas oublier le "true" apres chaque case sinon, ce sera toujours le dernier case qui sera executé!
     *
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return optionsToolbar(this, item);
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
            readData();
            //TODO afficher toutes les informations d'un user*/
        }
    }

    /**
     * Methode permettant d'afficher les informations d'un user depuis la bdd firestore lorsque le cycle de vie de l'application est à OnResume()
     */
    private void updateUIWhenResuming() {

        if (this.getCurrentUser() != null) { // retourne un user FirebaseUser. Permettra de remplir toutes les vues de l'activité
            readData();
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

    private void readData() {
        DocumentReference docRef1 = FirebaseFirestore.getInstance().collection("users").document(getCurrentUser().getUid()); // recup ref de l'obj courant en bdd de stockage
        // un DocumentReference fait référence à un emplacement de document dans une base de données Firestore et peut être utilisé pour
        // écrire, lire ou écouter l'emplacement. Il peut exister ou non un document à l'emplacement référencé.

        docRef1.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot doc = task.getResult(); //Un DocumentSnapshot contient des données lues à partir d'un document dans votre base de données Firestore.
                    if (doc.exists()) {
                        String nom = (String) doc.get("nom");
                        String prenom = (String) doc.get("prenom");
                        String email = (String) doc.get("email");
                        String fonction = (String) doc.get("fonction");
                        String licence = (String) doc.get("licence");
                        String niveau = (String) doc.get("niveau");

                        mNom.setText(nom);
                        mPrenom.setText(prenom);
                        mEmail.setText(email);
                        mFonctionAuClubspinner.setTag(fonction);
                        mLicence.setText(licence);
                        mNiveauPlongeespinner.setSelection(getIndexSpinner(mNiveauPlongeespinner, niveau));
                        mFonctionAuClubspinner.setEnabled(false);
                    }
                }
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                    }
                });
    }

    /**
     * Methode permettant de retrouver la position d'un item de la liste des niveaux de plongée d'un user
     *
     * @param spinner
     * @param myString
     * @return
     */
    private int getIndexSpinner(Spinner spinner, String myString) {
        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(myString)) {
                return i;
            }
        }
        return 0;
    }

    private void updateData() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        this.mProgressBar.setVisibility(View.VISIBLE);
        String nom = this.mNom.getText().toString();
        String prenom = this.mPrenom.getText().toString();
        String licence = this.mLicence.getText().toString();
        String niveau = this.mNiveauPlongeespinner.getSelectedItem().toString();
        String fonction = this.mFonctionAuClubspinner.getSelectedItem().toString();
        String email = this.mEmail.getText().toString();

        DocumentReference user = db.collection("users").document(getCurrentUser().getUid());
        user.update("nom", nom);
        user.update("prenom", prenom);
        user.update("licence", licence);
        user.update("niveau", niveau);
        user.update("fonction", fonction);
        user.update("email", email)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(AccountCreateActivity.this, R.string.update_account,
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void deleteData() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users").document(getCurrentUser().getUid())
                .delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(AccountCreateActivity.this, R.string.alertDialog_delete,
                        Toast.LENGTH_SHORT).show();
            }
        });
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
