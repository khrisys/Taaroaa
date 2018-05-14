package fr.drochon.christian.taaroaa.auth;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
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
    //Spinner mFonctionAuClubspinner;
    TextInputEditText mEmail;
    TextInputEditText mPassword;
    ProgressBar mProgressBar;
    Button mCreateAccount;
    Button mSuppressionCompte;
    //MenuItem mItemView;
    TextView mTitrePage;
    String fonction;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_create);

        //setTitle("Création de compte");
        mTitrePage = findViewById(R.id.titre_page_compte_txt);
        mLinearLayoutFonctionAdherent = findViewById(R.id.linearLayoutFonctionAdherent);
        mPrenom = findViewById(R.id.prenom_txt);
        mNom = findViewById(R.id.nom_txt);
        mLicence = findViewById(R.id.licence_txt);
        mNiveauPlongeespinner = findViewById(R.id.niveau_spinner);
        mNiveauPlongeespinner.setEnabled(true);
        //mFonctionAuClubspinner = findViewById(R.id.fonction_spinner);
        mEmail = findViewById(R.id.email_txt);
        mProgressBar = findViewById(R.id.progress_bar);
        mCreateAccount = findViewById(R.id.modificiation_compte_btn);
        //TODO n'afficher le bouton de suppression qu'aux proprieraires des comptes
        mSuppressionCompte = findViewById(R.id.suppression_compte_btn);
        // recup de la barre de rehcerche pour ne pas qu'elle soit null (non declarée dans BaseActivity)
        //mItemView = findViewById(R.id.app_bar_search_adherents);
        fonction = "Plongeur"; // la fonction par defaut d'un adhrent qui créé son compte a pour fonction "Plongeur"

        configureToolbar();
        giveToolbarAName(R.string.account_create_name);

        //showManagementSupervisors();
        // methode à appeler APRES l'initialisation des variables, sinon les variables auront des references null
        this.updateUIWhenCreating(); // recuperation des informations de l'user actuel


        //TODO verifier que tous les champs soient remplis


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

/*        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.fonctions_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        mFonctionAuClubspinner.setAdapter(adapter);*/
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

        // recup de l'item de recherche des adherents
        //mItemView = menu.findItem(R.id.app_bar_search_adherents);
        //searchAndModifPupils();

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
            getInformationsNewUser();
            //TODO afficher toutes les informations d'un user*/
        }
    }

    /**
     * Methode permettant d'afficher les informations d'un user depuis la bdd firestore lorsque le cycle de vie de l'application est à OnResume()
     */
    private void updateUIWhenResuming() {

        if (this.getCurrentUser() != null) { // retourne un user FirebaseUser. Permettra de remplir toutes les vues de l'activité
            getAndShowUserDatas();
        }
    }

    /**
     * Methode permettant de recuperer les informations d'un user qui vient de creer un compte et de les afficher à l'ecran
     */
    private void getInformationsNewUser() {
        String username = Objects.requireNonNull(getCurrentUser()).getDisplayName();
        String email = getCurrentUser().getEmail();
        String nom = null, prenom = null;
        String[] parts;
        assert username != null;
        if (username.contains(" ")) {
            parts = username.split(" ");
            try {
                if (parts[1] != null) nom = parts[1].toUpperCase();
                else nom = "";
            } catch (ArrayIndexOutOfBoundsException e1) {
                Log.e("TAG", "ArrayOutOfBoundException " + e1.getMessage());
            }
            if (parts[0] != null) prenom = parts[0].toUpperCase();
            else prenom = "";
        } else {
            nom = username;
            prenom = "";
        }
        mNom.setText(nom);
        mPrenom.setText(prenom);
        mEmail.setText(email);
        mNiveauPlongeespinner.setEnabled(true);
        mCreateAccount.setText(R.string.complete_account);
        mSuppressionCompte.setVisibility(View.VISIBLE);
    }


    /**
     * Methode permettant de recuperer depuis la bdd et d'afficher les données de l'utilisateur actuellement connecté
     */
    private void getAndShowUserDatas() {
        DocumentReference docRef1 = FirebaseFirestore.getInstance().collection("users").document(getCurrentUser().getUid()); // recup ref de l'obj courant en bdd de stockage
        // un DocumentReference fait référence à un emplacement de document dans une base de données Firestore et peut être utilisé pour
        // écrire, lire ou écouter l'emplacement. Il peut exister ou non un document à l'emplacement référencé.

        docRef1.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot doc = task.getResult(); //Un DocumentSnapshot contient des données lues à partir d'un document dans votre base de données Firestore.
                    if (doc.exists()) {
                        mSuppressionCompte.setEnabled(true);
                        //String uid = FirebaseAuth.getInstance().getUid();
                        String nom = (String) doc.get("nom");
                        String prenom = (String) doc.get("prenom");
                        String email = (String) doc.get("email");
                        String licence = (String) doc.get("licence");
                        String niveau = (String) doc.get("niveau");

                        mNom.setText(nom);
                        mPrenom.setText(prenom);
                        mEmail.setText(email);
                        mLicence.setText(licence);
                        mNiveauPlongeespinner.setSelection(getIndexSpinner(mNiveauPlongeespinner, niveau));
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
     * Methode permettant de signaler une erreur lorsqu'un champ est resté vide alors que la soumission du formulaire a été faite.
     */
    private void verificationChampsVides() {

        final String nom = mNom.getText().toString();
        final String prenom = mPrenom.getText().toString();
        if (nom.isEmpty()) mNom.setError("Merci de saisir ce champ !");
        if (prenom.isEmpty()) mPrenom.setError("Merci de saisir ce champ !");
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
        this.mProgressBar.setVisibility(View.VISIBLE);
        String nom = this.mNom.getText().toString();
        String prenom = this.mPrenom.getText().toString();
        String licence = this.mLicence.getText().toString();
        String niveau = this.mNiveauPlongeespinner.getSelectedItem().toString();
        String email = this.mEmail.getText().toString();


        final FirebaseFirestore db = FirebaseFirestore.getInstance();

        if (!nom.isEmpty() && !prenom.isEmpty() && !niveau.isEmpty() && !fonction.isEmpty() && !email.isEmpty()) {

            Map<String, Object> user = new HashMap<>();
            user.put("uid", uid);
            user.put("nom", nom.toUpperCase());
            user.put("prenom", prenom.toUpperCase());
            user.put("licence", licence);
            user.put("niveau", niveau);
            user.put("fonction", fonction);
            user.put("email", email);
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
        } else {
            //TODO verifier si l'alertdialog ici affiche les bonnes informations attendues
            final AlertDialog.Builder adb = new AlertDialog.Builder(AccountCreateActivity.this);
            adb.setTitle(R.string.alertDialog_account);
            adb.setIcon(android.R.drawable.ic_dialog_alert);
            adb.setTitle(R.string.verif_every_fields_complete);
            adb.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    // rien à appeler. pas la peine de faire de toast
                }
            });
            adb.show(); // affichage de l'artdialog
            verificationChampsVides();
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
