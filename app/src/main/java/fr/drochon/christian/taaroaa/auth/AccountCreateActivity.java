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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.ActionCodeSettings;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

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


        // recuperation des identifiants de connexion
        Intent intent = getIntent();
        final String email = intent.getStringExtra("email");
        final String password = intent.getStringExtra("password");
        mEmail.setText(email);
        mPassword.setText(password);

        alertDialogValidationEmail();
   /*    *//* Toast.makeText(AccountCreateActivity.this, "Avant de pouvoir créer votre compte, vous devez valider votre adresse mail " +
                "via le lien qui vous a été envoyé à " + getCurrentUser().getEmail(), Toast.LENGTH_LONG).show();*//*
        AlertDialog.Builder adb = new AlertDialog.Builder(this);
        adb.setTitle("Validation d'email !");
        // ajouter une couleur à l'icon de warning
        Drawable warning = getResources().getDrawable(android.R.drawable.ic_dialog_alert);
        ColorFilter filter = new LightingColorFilter(Color.RED, Color.BLUE);
        warning.setColorFilter(filter);
        adb.setIcon(warning);
        adb.setMessage("Avant de pouvoir créer votre compte, vous devez valider votre adresse mail via le lien qui vous a été envoyé à " + getCurrentUser().getEmail());
        adb.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // recuperation des identifiants de connexion
                Intent intent = getIntent();
                final String email = intent.getStringExtra("email");
                final String password = intent.getStringExtra("password");
                mEmail.setText(email);
                mPassword.setText(password);
            }
        });
        adb.show();*/



        // --------------------
        // LISTENERS
        // --------------------

        //Lorsqu'un utilisateur a rempli correctement le formulaire, il est renvoyé à la page Sommaire
        // 1 - lorsqu'un plongeur est connecté, le menu deroulant des fonctions est desactivé; tous les autres champs sont acesssibles.
        // 2 - lorsqu'un encadrant est connecté, tous les chamos sont en lecture seule, sauf le menu deroulant des fonctions
        mCreateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Objects.requireNonNull(getCurrentUser()).reload();

                FirebaseAuth auth = FirebaseAuth.getInstance(FirebaseFirestore.getInstance().getApp());
                FirebaseUser firebaseUser = auth.getCurrentUser();
                assert firebaseUser != null;
                if (Objects.requireNonNull(firebaseUser.isEmailVerified())) {
                    createUserInFirebase();
                    startSummaryActivity();
                } else {
                    System.out.println("nok");
                    alertDialogValidationEmail();
                    /*Toast.makeText(AccountCreateActivity.this, "Avant de pouvoir créer votre compte, vous devez valider votre adresse mail " +
                            "via le lien qui vous a été envoyé !", Toast.LENGTH_LONG).show();*/
                }
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
                ColorFilter filter = new LightingColorFilter(Color.RED, Color.BLUE);
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


    // --------------------
    // UI
    // --------------------

    @Override
    public int getFragmentLayout() {
        return R.layout.activity_account_create;
    }

    /** Methode permettant de verifier la validité d'une adresse email
     *
     * @param target adresse email
     * @return validité de l'adresse email
     */
    private static boolean isValidEmail(CharSequence target) {
        return target != null && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    public void alertDialogValidationEmail(){

        Objects.requireNonNull(getCurrentUser()).reload();

        AlertDialog.Builder adb = new AlertDialog.Builder(this);
        adb.setTitle("Sécurité !");
        // ajouter une couleur à l'icon de warning
        Drawable warning = getResources().getDrawable(android.R.drawable.ic_dialog_alert);
        ColorFilter filter = new LightingColorFilter(Color.RED, Color.BLUE);
        warning.setColorFilter(filter);
        adb.setIcon(warning);
        adb.setMessage("Avant de pouvoir créer votre compte, vous devez valider votre adresse mail via le lien qui vous a été envoyé à '" + Objects.requireNonNull(getCurrentUser()).getEmail() + "'");
        adb.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

                connectToFirebaseWithEmailAndPassword();
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
        final FirebaseAuth auth = FirebaseAuth.getInstance(FirebaseFirestore.getInstance().getApp());

        setupDb().collection("users").document(getCurrentUser().getUid()).get()
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


                                assert uid != null;
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

                           /* Log.d("TAG", "Successfully created an account !");
                            Toast.makeText(AccountCreateActivity.this, "Successfully created an account !",
                                    Toast.LENGTH_SHORT).show();*/
                            //startSummaryActivity();
                            // You can access the new user via result.getUser()
                            // Additional user info profile *not* available via:
                            // result.getAdditionalUserInfo().getProfile() == null
                            // You can check if the user is new or existing:
                            // result.getAdditionalUserInfo().isNewUser()
                        } else {
                            Toast.makeText(AccountCreateActivity.this, "Error creating an account : "
                                            + Objects.requireNonNull(task.getException()).getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void connectToFirebaseWithEmailAndPassword() {
        // recuperation de la bdd FirebaseAuth avec en param l'app taaroaa
        final FirebaseAuth auth = FirebaseAuth.getInstance(FirebaseFirestore.getInstance().getApp());
        FirebaseUser firebaseUser = auth.getCurrentUser();

        // 1 : creation d'un user avec email et password en bdd FirebaseAuth
        auth.createUserWithEmailAndPassword(this.mEmail.getText().toString(), mPassword.getText().toString())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // appel de la methode de verification d'email depuis firebase
                            verifEmailUser();
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("TAG", "createUserWithEmailIndba:success");
                        /*    Toast.makeText(AccountCreateActivity.this, "Registration email succeed.",
                                    Toast.LENGTH_SHORT).show();*/
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(AccountCreateActivity.this, "Registration email failed or already in use by another account.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    /**
     * Methode permettant d'envoyer un email via un token pour la confirmation d'adresse mail d'un nouvek utilisateur
     */
    private void verifEmailUser() {
        final FirebaseAuth auth = FirebaseAuth.getInstance(FirebaseFirestore.getInstance().getApp());
        // An ActionCodeSettings instance needs to be provided when sending a password reset email or a verification email.

        // In order to securely pass a continue URL, the domain for the URL will need to be whitelisted in the Firebase console.
        // This is done in the Authentication section by adding this domain to the list of OAuth redirect domains if it is not already there.
        String url = "https://taaroaa-fe93c.firebaseapp.com/verify?uid=" + auth.getCurrentUser();
        ActionCodeSettings actionCodeSettings = ActionCodeSettings.newBuilder()
                //.setUrl("https://taaroaa-fe93c.firebaseapp.com/__/auth/action?mode=%3Caction%3E&oobCode=%3Ccode%3E")
                //.setUrl(url)
                .setUrl("https://dhu3y.app.goo.gl/taaroaa")
                .setHandleCodeInApp(true)
                //.setIOSBundleId("com.example.ios")
                // The default for this is populated with the current android package name.
                .setAndroidPackageName(
                        "fr.drochon.christian.taaroaa",
                        true,
                        "19")
                .build();

        if (!Objects.requireNonNull(auth.getCurrentUser()).isEmailVerified()) {
            Objects.requireNonNull(auth.getCurrentUser()).sendEmailVerification()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(AccountCreateActivity.this, "Verification email sent to " + Objects.requireNonNull(getCurrentUser()).getEmail(), Toast.LENGTH_LONG).show();
                                Log.d("TAG", "Verification Email sent.");

                                //createUserInFirebase();
                            } else
                                System.out.println("nok");
                        }
                    });
        }
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
