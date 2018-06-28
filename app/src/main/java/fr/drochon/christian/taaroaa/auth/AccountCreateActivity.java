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
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.perf.FirebasePerformance;
import com.google.firebase.perf.metrics.Trace;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import fr.drochon.christian.taaroaa.R;
import fr.drochon.christian.taaroaa.base.BaseActivity;
import fr.drochon.christian.taaroaa.controller.SummaryActivity;
import fr.drochon.christian.taaroaa.model.User;

public class AccountCreateActivity extends BaseActivity {

    protected Spinner mFonctionPlongeur;
    // DESIGN
    private TextInputEditText mPrenom;
    private TextInputEditText mNom;
    private TextInputEditText mLicence;
    private Spinner mNiveauPlongeespinner;
    private TextInputEditText mEmail;
    private TextInputEditText mPassword;
    private ProgressBar mProgressBar;


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


    // --------------------
    // TOOLBAR
    // --------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_create);

        //PARAMETRAGE DES DONNES A AFFICHER A L ECRAN
        mPrenom = findViewById(R.id.prenom_txt);
        mNom = findViewById(R.id.nom_txt);
        mLicence = findViewById(R.id.licence_txt);
        mNiveauPlongeespinner = findViewById(R.id.niveau_spinner);
        mNiveauPlongeespinner.setEnabled(true);
        mFonctionPlongeur = findViewById(R.id.fonction_spinner);
        mFonctionPlongeur.setEnabled(true);
        mEmail = findViewById(R.id.email_txt);
        mPassword = findViewById(R.id.password_input);
        mProgressBar = findViewById(R.id.progress_bar);
        Button mModificationCompte = findViewById(R.id.modificiation_compte_btn);
        Button mSuppressionCompte = findViewById(R.id.suppression_compte_btn);

        configureToolbar();
        giveToolbarAName(R.string.account_create_name);

        // --------------------
        // RECUPERATION ET AFFICHAGE DES INFOS PERSONNELLES DE LA PERSONNE QUI CREE SON COMPTE
        // OU QUI EST EN TRAIN DE SE CONNECTER
        // --------------------

        this.getConnectedUser();

        // Fonction de verification pour la saisie d'un email valide via un
        // token envoyé sur le compte mail designé
        //alertDialogValidationEmail();

        // --------------------
        // LISTENERS
        // --------------------

        //Lorsqu'un utilisateur a rempli correctement le formulaire, il est renvoyé à la page Sommaire
        mModificationCompte.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Objects.requireNonNull(getCurrentUser()).reload();

                // Test performance de l'update d'user en bdd
                final Trace myTrace = FirebasePerformance.getInstance().newTrace("accountCreateActivityCreateAUser_trace");
                myTrace.start();

                FirebaseAuth auth = FirebaseAuth.getInstance(FirebaseFirestore.getInstance().getApp());
                FirebaseUser firebaseUser = auth.getCurrentUser();
                if (firebaseUser != null) {
                    //if (Objects.requireNonNull(firebaseUser.isEmailVerified())) {
                        createUserInFirebase();

                        // fin de trace
                        myTrace.stop();

                        // affichaga de l'alertdialog pendant à nouveau 5s pour avertir l(user de valider son email
                    /*} else {
                        if (!mNom.getText().toString().isEmpty() && !mPrenom.getText().toString().isEmpty() && !mEmail.getText().toString().isEmpty() && isValidEmail(mEmail.getText()) && !mPassword.getText().toString().isEmpty()) {
                            System.out.println("nok");
                            //alertDialogValidationEmail();
                        } else verificationChampsVides();
                    }*/
                }
            }
        });

        // Suppression d'un compte utilisateur ainsi que de son authentifiant
        // avec alertdialog avant suppression
        mSuppressionCompte.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final android.support.v7.app.AlertDialog.Builder adb = new android.support.v7.app.AlertDialog.Builder(AccountCreateActivity.this);
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

                        // Test performance de l'update d'user en bdd
                        final Trace myTrace1 = FirebasePerformance.getInstance().newTrace("accountModificationActivityUserAccountDelete_trace");
                        myTrace1.start();

                        deleteUser();
                        deleteUserAuth();
                        signOutUserFromFirebase();

                        myTrace1.stop();

                    }
                    //En cas de negation, l'utilisateur reste sur l'ecran de creation de son compte
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        EditText editText = findViewById(R.id.alertdialog_delete_account);
                        Toast.makeText(AccountCreateActivity.this, editText.getText(), Toast.LENGTH_LONG).show();
                    }
                });
                adb.show();
            }
        });

        // --------------------
        // SPINNERS & REMPLISSAGE
        // --------------------
        // Create an ArrayAdapter using the string array and a default spinner layout pour les niveaux
        ArrayAdapter<CharSequence> adapterNiveau = ArrayAdapter.createFromResource(this, R.array.niveaux_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapterNiveau.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        mNiveauPlongeespinner.setAdapter(adapterNiveau);

        // Default spinner layout pour les fonctions
        ArrayAdapter<CharSequence> adapterFonction = ArrayAdapter.createFromResource(this, R.array.fonctions_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapterFonction.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        mFonctionPlongeur.setAdapter(adapterFonction);
    }

    /**
     * Fait appel au fichier xml menu pour definir les icones.
     * Definit differentes options dans le menu caché.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.summary_menu, menu);
        return true; // true affiche le menu
    }


    // --------------------
    // UI
    // --------------------

    /**
     * recuperation  du clic d'un user.
     * On utilise un switch ici car il peut y avoir plusieurs options.
     * Surtout ne pas oublier le "true" apres chaque case sinon, ce sera toujours le dernier case qui sera executé!
     *
     * @param item item de toolbar
     * @return option de menu
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return optionsToolbar(this, item);
    }

    @Override
    public int getFragmentLayout() {
        return R.layout.activity_account_create;
    }

    /**
     * Recuperation et affichage des données d'un  utilisateur qui s'est
     * connecté en possedant un compte ou qui vient de se creer un compte
     * En gros, qui n'a renseigné ni de nom ni de prenom
     */
    private void getConnectedUser() {
        Intent intent = getIntent();
        // compte d'un nouvel user
        if (intent.getStringExtra("email") != null) {
            mPrenom.setText(intent.getStringExtra("prenom").toUpperCase());
            mNom.setText(intent.getStringExtra("nom").toUpperCase());
            mLicence.setText("");
            mNiveauPlongeespinner.setSelection(getIndexSpinner(mNiveauPlongeespinner, "1"));
            mFonctionPlongeur.setSelection(getIndexSpinner(mFonctionPlongeur, "Plongeur"));
            mEmail.setText(intent.getStringExtra("email"));
            mPassword.setText(intent.getStringExtra("password"));
        }

        //personne possedant un compte
        else {
            User user = (User) intent.getSerializableExtra("connectedUser");

            if (user.getPrenom() == null) mPrenom.setText(" ");
            else mPrenom.setText(user.getPrenom().toUpperCase());
            mNom.setText(user.getNom().toUpperCase());
            mLicence.setText(user.getLicence());
            mNiveauPlongeespinner.setSelection(getIndexSpinner(mNiveauPlongeespinner, user.getNiveau()));
            mFonctionPlongeur.setSelection(getIndexSpinner(mFonctionPlongeur, user.getFonction()));
            mEmail.setText(user.getEmail());
            mPassword.setText(user.getPassword());
        }
    }

    /**
     * Methode permettant d'afficher une alertdialog à un user pour lui indiquer qu'il doit aller
     * valider son adresse mail saisi lors de la creation de son compte.
     * Cette methode rafraichit la validation de son email en bdd firebase (sinon, sa validation,
     * meme effectuée, ne serait jamais prise en compte par firebase)
     */
    private void alertDialogValidationEmail() {
        if (getCurrentUser() != null) Objects.requireNonNull(getCurrentUser()).reload();

        AlertDialog.Builder adb = new AlertDialog.Builder(this);
        adb.setTitle("Sécurité !");

        // ajouter une couleur à l'icon de warning
        Drawable warning = getResources().getDrawable(android.R.drawable.ic_dialog_alert);
        ColorFilter filter = new LightingColorFilter(Color.RED, Color.BLUE);
        warning.setColorFilter(filter);
        adb.setIcon(warning);

        adb.setMessage("Avant de pouvoir créer votre compte, vous devez valider votre adresse mail via le lien qui vous a été envoyé à : '" +
                Objects.requireNonNull(getCurrentUser()).getEmail() + "'");
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
            setupDb().collection("users").document().addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                    if (documentSnapshot != null) {
                        // creation de l'(utilisateur en bdd
                        String uid = auth.getUid();
                        String nom = mNom.getText().toString();
                        String prenom = mPrenom.getText().toString();
                        String licence = mLicence.getText().toString();
                        String niveau = mNiveauPlongeespinner.getSelectedItem().toString();
                        String fonction = mFonctionPlongeur.getSelectedItem().toString();
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
                            user.put("password", password);

                            setupDb().collection("users").document(uid).set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(AccountCreateActivity.this, R.string.create_account, Toast.LENGTH_LONG).show();
                                    startSummaryActivity(); // renvoi l'user sur la page sommaire   pres validation de la creation de l'user
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(AccountCreateActivity.this, "ERROR" + e.toString(), Toast.LENGTH_LONG).show();
                                    Log.d("TAG", e.toString());
                                }
                            });
                        } else verificationChampsVides();
                    }
                }
            });
        }
    }

    /*
     */

    /**
     * Methode permettant de supprimer un utilisateur
     * <p>
     * Methode permettant de supprimer les identifiants de l'user qui supprime son compte
     */
    private void deleteUser() {
        final FirebaseUser auth = FirebaseAuth.getInstance().getCurrentUser();
        if (auth != null) {
            setupDb().collection("users").document(auth.getUid())
                    .delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {

                    Toast.makeText(AccountCreateActivity.this, R.string.alertDialog_delete,
                            Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    /**
     * Methode permettant de supprimer les identifiants de l'user qui supprime son compte
     */
    private void deleteUserAuth() {
        final FirebaseAuth auth2 = FirebaseAuth.getInstance(FirebaseFirestore.getInstance().getApp());
        Objects.requireNonNull(auth2.getCurrentUser()).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    System.out.println("deleteAuth ok !");
                }
            }
        });
    }
}

