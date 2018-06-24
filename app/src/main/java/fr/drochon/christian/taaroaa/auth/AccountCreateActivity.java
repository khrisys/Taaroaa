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
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.perf.FirebasePerformance;
import com.google.firebase.perf.metrics.Trace;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import fr.drochon.christian.taaroaa.R;
import fr.drochon.christian.taaroaa.api.UserHelper;
import fr.drochon.christian.taaroaa.base.BaseActivity;
import fr.drochon.christian.taaroaa.controller.SummaryActivity;

public class AccountCreateActivity extends BaseActivity {


    public static final int GET_USERNAME = 40;
    // identifiant pour identifier la requete REST
    private static final int DELETE_USER_TASK = 20;
    // DESIGN
    private TextInputEditText mPrenom;
    private TextInputEditText mNom;
    private TextInputEditText mLicence;
    private Spinner mNiveauPlongeespinner;
    private TextInputEditText mEmail;
    private TextInputEditText mPassword;
    private ProgressBar mProgressBar;
    // DATA
    private String fonction;

    // --------------------
    // LIFECYCLE
    // --------------------

    /**
     * Methode permettant de verifier la validité d'une adresse email
     *
     * @param target adresse email
     * @return validité de l'adresse email
     */
    private static boolean isValidEmail(CharSequence target) {
        return target != null && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_create);

        mPrenom = findViewById(R.id.prenom_txt);
        mNom = findViewById(R.id.nom_txt);
        mLicence = findViewById(R.id.licence_txt);
        mNiveauPlongeespinner = findViewById(R.id.niveau_spinner);
        mNiveauPlongeespinner.setEnabled(true);
        mEmail = findViewById(R.id.email_txt);
        mPassword = findViewById(R.id.password_input);
        mProgressBar = findViewById(R.id.progress_bar);
        final Button createAccount = findViewById(R.id.modificiation_compte_btn);
        fonction = "Plongeur"; // la fonction par defaut d'un adhrent qui créé son compte est considéré comme un "Plongeur" et non comme un encadrant

        configureToolbar();
        giveToolbarAName(R.string.account_create_name);
        getConnectedUser();


 /*       // recup de l'user passé par un intent depuis la classe ConnectionActivity
        Intent intent = getIntent();
        *//*if (intent != null) {
            user = (User) Objects.requireNonNull(intent.getExtras()).getSerializable("user");
        }*//*
        final String email = intent.getStringExtra("email");
        final String password = intent.getStringExtra("password");
        mEmail.setText(email);
        mPassword.setText(password);*/

        alertDialogValidationEmail();

        // --------------------
        // LISTENERS
        // --------------------

        //Lorsqu'un utilisateur a rempli correctement le formulaire, il est renvoyé à la page Sommaire
        createAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Objects.requireNonNull(getCurrentUser()).reload();

                // Test performance de l'update d'user en bdd
                final Trace myTrace = FirebasePerformance.getInstance().newTrace("accountCreateActivityCreateAUser_trace");
                myTrace.start();

                FirebaseAuth auth = FirebaseAuth.getInstance(FirebaseFirestore.getInstance().getApp());
                FirebaseUser firebaseUser = auth.getCurrentUser();
                if (firebaseUser != null) {
                    if (Objects.requireNonNull(firebaseUser.isEmailVerified())) {
                        createUserInFirebase();

                        // fin de trace
                        myTrace.stop();
                    } else {
                        if (!mNom.getText().toString().isEmpty() && !mPrenom.getText().toString().isEmpty()
                                && !mEmail.getText().toString().isEmpty() && isValidEmail(mEmail.getText())
                                && !mPassword.getText().toString().isEmpty()) {
                            System.out.println("nok");
                            alertDialogValidationEmail();
                        } else
                            verificationChampsVides();
                    }
                }
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

    // --------------------
    // UI
    // --------------------
    @Override
    public int getFragmentLayout() {
        return R.layout.activity_account_create;
    }

    /**
     * Recuperation et affichage des données d'un  utilisateur qui s'est connecté ou qui s'est créé un compte
     */
    private  void getConnectedUser(){
        Intent intent = getIntent();
        if(intent != null){
            mPrenom.setText(getIntent().getStringExtra("firstname"));
            mNom.setText(getIntent().getStringExtra("name"));
            mLicence.setText("");
            mEmail.setText(getIntent().getStringExtra("email"));
            mNiveauPlongeespinner.setSelection(getIndexSpinner(mNiveauPlongeespinner, "Plongeur"));
            mPassword.setText(getIntent().getStringExtra("password"));
        }
    }

    /**
     * Methode permettant d'afficher une alertdialog à un user pour lui indiquer qu'il doit aller
     * valider son adresse mail saisi lors de la creation de son compte.
     * Cette methode rafraichit la validation de son email en bdd firebase (sinon, sa validation,
     * meme effectuée, ne serait jamais prise en compte par firebase)
     */
    private void alertDialogValidationEmail() {
        if (getCurrentUser() != null)
            Objects.requireNonNull(getCurrentUser()).reload();

        AlertDialog.Builder adb = new AlertDialog.Builder(this);
        adb.setTitle("Sécurité !");

        // ajouter une couleur à l'icon de warning
        Drawable warning = getResources().getDrawable(android.R.drawable.ic_dialog_alert);
        ColorFilter filter = new LightingColorFilter(Color.RED, Color.BLUE);
        warning.setColorFilter(filter);
        adb.setIcon(warning);

        adb.setMessage("Avant de pouvoir créer votre compte, vous devez valider votre adresse mail via le lien qui vous a été envoyé à : '" + Objects.requireNonNull(getCurrentUser()).getEmail() + "'");
        adb.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        adb.setNegativeButton("CHANGER D'ADRESSE MAIL ?", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteUserAuth();
                signOutUserFromFirebase();
                Intent intent = new Intent(AccountCreateActivity.this, ConnectionActivity.class);
                startActivity(intent);
            }
        });
        adb.show();
    }

    /**
     * Methode permettant de changer d'ecran lors d'une connexion valide
     */
    private void startSummaryActivity() {
        Intent intent = new Intent(AccountCreateActivity.this, SummaryActivity.class);
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
        if (!isValidEmail(mEmail.getText().toString())) {
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

    // --------------------
    // REST REQUETES
    // --------------------

    /**
     * Methode permettant la creation d'un user dans le bdd. En cas d'insertion ou de probleme,
     * la fonction renverra une notification à l'utilisateur.
     */
    private void createUserInFirebase() {
        final FirebaseUser auth = FirebaseAuth.getInstance(FirebaseFirestore.getInstance().getApp()).getCurrentUser();
        if (auth != null) {
            setupDb().collection("users").document(auth.getUid()).get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {

                                // creation de l'(utilisateur en bdd
                                String uid = auth.getUid();
                                String nom = mNom.getText().toString();
                                String prenom = mPrenom.getText().toString();
                                String licence = mLicence.getText().toString();
                                String niveau = mNiveauPlongeespinner.getSelectedItem().toString();
                                String email = mEmail.getText().toString();
                                String password = mPassword.getText().toString();


                                if (!nom.isEmpty() && !prenom.isEmpty() && !email.isEmpty() && isValidEmail(email) && !password.isEmpty()) {
                                    mProgressBar.setVisibility(View.VISIBLE);
                                    Map<String, Object> user = new HashMap<>();
                                    user.put("uid", uid);
                                    user.put("nom", nom.toUpperCase());
                                    user.put("prenom", prenom.toUpperCase());
                                    user.put("licence", licence);
                                    user.put("niveau", niveau);
                                    user.put("fonction", fonction);
                                    user.put("email", email);

                                    setupDb().collection("users").document(uid).set(user)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    Toast.makeText(AccountCreateActivity.this, R.string.create_account,
                                                            Toast.LENGTH_LONG).show();
                                                    startSummaryActivity(); // renvoi l'user sur la page sommaire   pres validation de la creation de l'user
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Toast.makeText(AccountCreateActivity.this, "ERROR" + e.toString(),
                                                            Toast.LENGTH_LONG).show();
                                                    Log.d("TAG", e.toString());
                                                }
                                            });
                                } else verificationChampsVides();
                                // erreur de creation de compte
                            } else {
                                Toast.makeText(AccountCreateActivity.this, "Error creating an account : "
                                                + Objects.requireNonNull(task.getException()).getMessage(),
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    /**
     * Methode permettant à un encadrant de supprimer un compte. Retourne un objet de type Task permettant de realiser ces appels de maniere asynchrone
     */
    private void deleteUserFromFirebase() {
        FirebaseUser auth = FirebaseAuth.getInstance().getCurrentUser();
        if (auth != null) {

            UserHelper.deleteUser(auth.getUid()).addOnFailureListener(this.onFailureListener());
            AuthUI.getInstance()// methode utilisée par le singleton authUI.getInstance()
                    .delete(this)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(AccountCreateActivity.this, R.string.alertDialog_delete,
                                    Toast.LENGTH_LONG).show();
                            updateUIAfterRESTRequestsCompleted(DELETE_USER_TASK);
                        }
                    });
        }
    }

    /**
     * Methode permettant de supprimer les identifiants de l'user qui supprime son compte
     */
    private void deleteUserAuth() {
        final FirebaseAuth auth = FirebaseAuth.getInstance(FirebaseFirestore.getInstance().getApp());
        Objects.requireNonNull(auth.getCurrentUser()).delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        task.isSuccessful();
                    }
                });
    }
}
