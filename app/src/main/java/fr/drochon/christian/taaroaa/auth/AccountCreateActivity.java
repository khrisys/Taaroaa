package fr.drochon.christian.taaroaa.auth;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import fr.drochon.christian.taaroaa.R;
import fr.drochon.christian.taaroaa.api.UserHelper;
import fr.drochon.christian.taaroaa.base.BaseActivity;
import fr.drochon.christian.taaroaa.controller.MainActivity;
import fr.drochon.christian.taaroaa.controller.SummaryActivity;

import static android.widget.Toast.LENGTH_SHORT;

public class AccountCreateActivity extends BaseActivity {

    public static final int GET_USERNAME = 40;
    // identifiant pour identifier la requete REST
    private static final int SIGN_OUT_TASK = 10;
    private static final int DELETE_USER_TASK = 20;
    private static final int UPDATE_USERNAME = 30;
    LinearLayout mLinearLayoutFonctionAdherent;
    TextInputEditText mPrenom;
    TextInputEditText mNom;
    TextInputEditText mLicence;
    Spinner mNiveauPlongeespinner;
    TextInputEditText mEmail;
    TextInputEditText mPassword;
    ProgressBar mProgressBar;
    Button mCreateAccount;
    Button mSuppressionCompte;
    TextView mTitrePage;
    String fonction;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_create);

        mTitrePage = findViewById(R.id.titre_page_compte_txt);
        mLinearLayoutFonctionAdherent = findViewById(R.id.linearLayoutFonctionAdherent);
        mPrenom = findViewById(R.id.prenom_txt);
        mNom = findViewById(R.id.nom_txt);
        mLicence = findViewById(R.id.licence_txt);
        mNiveauPlongeespinner = findViewById(R.id.niveau_spinner);
        mNiveauPlongeespinner.setEnabled(true);
        mEmail = findViewById(R.id.email_txt);
        mPassword = findViewById(R.id.password_input);
        mProgressBar = findViewById(R.id.progress_bar);
        mCreateAccount = findViewById(R.id.modificiation_compte_btn);
        //TODO n'afficher le bouton de suppression qu'aux proprieraires des comptes
        mSuppressionCompte = findViewById(R.id.suppression_compte_btn);
        fonction = "Plongeur"; // la fonction par defaut d'un adhrent qui créé son compte a pour fonction "Plongeur"

        configureToolbar();
        giveToolbarAName(R.string.account_create_name);

        // --------------------
        // LISTENERS
        // --------------------

        //Lorsqu'un utilisateur a rempli correctement le formulaire, il est renvoyé à la page Sommaire
        // 1 - lorsqu'un plongeur est connecté, le menu deroulant des fonctions est desactivé; tous les autres champs sont acesssibles.
        // 2 - lorsqu'un encadrant est connecté, tous les chamos sont en lecture seule, sauf le menu deroulant des fonctions
        mCreateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createUserInFirebase();
                //updateData(mNom.getText().toString(), mPrenom.getText().toString());
               //startSummaryActivity();
            }
        });


        // Suppression d'un compte utilisateur avec alertdialog avant suppression
        mSuppressionCompte.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog.Builder adb = new AlertDialog.Builder(AccountCreateActivity.this);
                adb.setTitle(R.string.alertDialog_account);
                // ajouter une couleur à l'icon de warning
                Drawable warning = getResources().getDrawable(android.R.drawable.ic_dialog_alert);
                ColorFilter filter = new LightingColorFilter( Color.RED, Color.BLUE);
                warning.setColorFilter(filter);
                adb.setIcon(warning);
                adb.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        EditText editText = findViewById(R.id.alertdialog_ok_account);
                        Toast.makeText(AccountCreateActivity.this, editText.getText(), Toast.LENGTH_LONG).show();
                        deleteUserFromFirebase();
                        //deleteUser();
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
    }

    @Override
    public int getFragmentLayout() {
        return R.layout.activity_account_create;
    }


    // --------------------
    // UI
    // --------------------

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

    /**
     * Methode permettant de signaler une erreur lorsqu'un champ est resté vide alors que la soumission du formulaire a été faite.
     */
    private void verificationChampsVides() {

        if (mPassword.getText().toString().isEmpty()) {
            mPassword.setError("Merci de saisir ce champ !");
            mPassword.requestFocus();
        }
        if(!isValidEmail(mEmail.getText().toString())) {
            mEmail.setError("Adresse email non valide !");
            mEmail.requestFocus();
        }
        if (mEmail.getText().toString().isEmpty()) {
            mEmail.setError("Merci de saisir ce champ !");
            mEmail.requestFocus();
        }
        if (mNom.getText().toString().isEmpty()) {
            mNom.setError("Merci de saisir ce champ !");
            mNom.requestFocus();
        }
        if (mPrenom.getText().toString().isEmpty()) {
            mPrenom.setError("Merci de saisir ce champ !");
            mPrenom.requestFocus();
        }
    }


    private final static boolean isValidEmail(CharSequence target) {
        if (target == null)
            return false;

        return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    // --------------------
    // REST REQUETES
    // --------------------

    /**
     * Methode permettant la creation d'un user dans le bdd. En cas d'insertion ou de probleme,
     * la fonction renverra une notification à l'utilisateur.
     */
    private void createUserInFirebase() {

        // pas d'id pour un objet non créé
        String uid = FirebaseAuth.getInstance().getUid();
        String nom = this.mNom.getText().toString();
        String prenom = this.mPrenom.getText().toString();
        String licence = this.mLicence.getText().toString();
        String niveau = this.mNiveauPlongeespinner.getSelectedItem().toString();
        String email = this.mEmail.getText().toString();
        String password = mPassword.getText().toString();

        if (!nom.isEmpty() && !prenom.isEmpty() && !email.isEmpty() && isValidEmail(email) && !password.isEmpty()) {
            this.mProgressBar.setVisibility(View.VISIBLE);
            Map<String, Object> user = new HashMap<>();
            user.put("uid", uid);
            user.put("nom", nom.toUpperCase());
            user.put("prenom", prenom.toUpperCase());
            user.put("licence", licence);
            user.put("niveau", niveau);
            user.put("fonction", fonction);
            user.put("email", email);

            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("users").document(uid).set(user)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(AccountCreateActivity.this, R.string.create_account,
                                    Toast.LENGTH_SHORT).show();
                            startSummaryActivity(); // renvoi l'user sur la page sommaire   pres validation de la creation de l'user
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(AccountCreateActivity.this, "ERROR" + e.toString(),
                                    Toast.LENGTH_SHORT).show();
                            Log.d("TAG", e.toString());
                        }
                    });
        }
        else verificationChampsVides();
    }

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
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(AccountCreateActivity.this, R.string.alertDialog_delete,
                                    LENGTH_SHORT).show();
                            updateUIAfterRESTRequestsCompleted(DELETE_USER_TASK);
                        }
                    });
        }
    }
}
