package fr.drochon.christian.taaroaa.auth;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AlertDialog;
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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.perf.FirebasePerformance;
import com.google.firebase.perf.metrics.Trace;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import fr.drochon.christian.taaroaa.R;
import fr.drochon.christian.taaroaa.api.CovoiturageHelper;
import fr.drochon.christian.taaroaa.api.UserHelper;
import fr.drochon.christian.taaroaa.base.BaseActivity;
import fr.drochon.christian.taaroaa.controller.SummaryActivity;
import fr.drochon.christian.taaroaa.model.User;

import static android.widget.Toast.LENGTH_SHORT;

public class AccountModificationActivity extends BaseActivity {


    // identifiant pour identifier la requete REST
    private static final int UPDATE_USERNAME = 30;
    String uid;
    private User user;
    private User summaryUser;
    private User searchedUser;
    //DESIGN
    private LinearLayout mLinearLayoutFonctionAdherent;
    private TextInputEditText mPrenom;
    private TextInputEditText mNom;
    private TextInputEditText mLicence;
    private Spinner mNiveauPlongeespinner;
    private Spinner mFonctionAuClubspinner;
    private TextInputEditText mEmail;
    private TextInputEditText mPassword;
    private ProgressBar mProgressBar;
    private Button mModificationCompte;
    private Button mSuppressionCompte;
    private TextView mTitrePage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_modification);

        mTitrePage = findViewById(R.id.titre_page_compte_txt);
        mLinearLayoutFonctionAdherent = findViewById(R.id.linearLayoutFonctionAdherent);
        mPrenom = findViewById(R.id.prenom_txt);
        mNom = findViewById(R.id.nom_txt);
        mLicence = findViewById(R.id.licence_txt);
        mNiveauPlongeespinner = findViewById(R.id.niveau_spinner);
        mNiveauPlongeespinner.setEnabled(false);
        mFonctionAuClubspinner = findViewById(R.id.fonction_spinner);
        mEmail = findViewById(R.id.email_txt);
        mPassword = findViewById(R.id.password_txt);
        mProgressBar = findViewById(R.id.progress_bar);
        mModificationCompte = findViewById(R.id.modificiation_compte_btn);
        mSuppressionCompte = findViewById(R.id.suppression_compte_btn);


        configureToolbar();
        this.giveToolbarAName(R.string.account_modif_name);
        this.designDependingOnGetUsers();
        //updateUserInFirebase();
        // methode à appeler APRES l'initialisation des variables, sinon les variables auront des references null
        this.updateUIWhenCreating(); // recuperation des informations de l'user actuel


        // --------------------
        // LISTENERS
        // --------------------

        //Lorsqu'un utilisateur a rempli correctement le formulaire, il est renvoyé à la page Sommaire
        // 1 - lorsqu'un plongeur est connecté, le menu deroulant du niveau de plongée est disabled / la fonction est invisible; tous les autres champs sont acesssibles.
        // 2 - lorsqu'un encadrant est connecté, tous les champs sont en lecture seule, sauf les menus deroulants des fonctions et des niveaux de plongée
        mModificationCompte.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateUserInFirebase();
            }
        });


        // Suppression d'un compte utilisateur avec alertdialog avant suppression
        mSuppressionCompte.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog.Builder adb = new AlertDialog.Builder(AccountModificationActivity.this);
                adb.setTitle(R.string.alertDialog_account);

                // ajouter une couleur à l'icon de warning
                Drawable warning = getResources().getDrawable(android.R.drawable.ic_dialog_alert);
                ColorFilter filter = new LightingColorFilter(Color.RED, Color.BLUE);
                warning.setColorFilter(filter);
                adb.setIcon(warning);
                adb.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        EditText editText = findViewById(R.id.alertdialog_ok_account);
                        Toast.makeText(AccountModificationActivity.this, editText.getText(), Toast.LENGTH_LONG).show();

                        // Test performance de l'update d'user en bdd
                        final Trace myTrace1 = FirebasePerformance.getInstance().newTrace("accountModificationActivityUserAccountDelete_trace");
                        myTrace1.start();

                        //TODO trouver solution pour finaliser reuete  avant de passer dans le sommaire, bref, la fin de la trace
                        deleteUser();
                        deleteUserAuth();
                        signOutUserFromFirebase();
                        startMainActivity();

                        myTrace1.stop();

                    }
                    //En cas de negation, l'utilisateur reste sur l'ecran de creation de son compte
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        EditText editText = findViewById(R.id.alertdialog_delete_account);
                        Toast.makeText(AccountModificationActivity.this, editText.getText(), Toast.LENGTH_LONG).show();
                        //finish();
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

    // --------------------
    // UI
    // --------------------

    @Override
    public int getFragmentLayout() {
        return R.layout.activity_account_modification;
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
     * Methode permettant d'afficher les informations de l'user sur l'ecran AccountCreateActivity lorsqu'un user vient de creer un compte
     */
    private void updateUIWhenCreating() {
        //updateUserInFirebase();
        designDependingOnGetUsers();
     /*   if (this.getCurrentUser().getEmail() != null) { // retourne un user FirebaseUser. Permettra de remplir toutes les vues de l'activité

        }*/
    }

    /**
     * Methode permettant d'afficher les informations d'un user depuis la bdd firestore lorsque le cycle de vie de l'application est à OnResume()
     */
    private void updateUIWhenResuming() {
        designDependingOnGetUsers();
        //getAndShowUserDatas();
      /*  if (this.getCurrentUser().getEmail() != null) { // retourne un user FirebaseUser. Permettra de remplir toutes les vues de l'activité

        }*/
    }

    /**
     * Methode permettant de changer d'ecran lors d'une connexion valide
     */
    private void startSummaryActivity() {
        Intent intent = new Intent(AccountModificationActivity.this, SummaryActivity.class);
        startActivity(intent);
    }

    /**
     * Methode permettant de revenir à la page d'accueil lorsqu'un utilisateur a supprimer son compte
     */
    private void startMainActivity() {
        Intent intent = new Intent(AccountModificationActivity.this, MainActivity.class);
        startActivity(intent);
    }

    /**
     * Methode permettant de signaler une erreur lorsqu'un champ est resté vide alors que la soumission du formulaire a été faite.
     */
    private void verificationChampsVides() {

        final String nom = mNom.getText().toString();
        final String prenom = mPrenom.getText().toString();
        if (nom.isEmpty()) {
            mNom.setError("Merci de renseigner ce champ !");
            mNom.isFocused();
        } else mNom.getText();
        if (prenom.isEmpty()) {
            mPrenom.setError("Merci de renseigner ce champ !");
            mPrenom.isFocused();
        } else mPrenom.getText();
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


    // --------------------
    // REST REQUETES
    // --------------------


    /**
     * Methode permettant de donner acces à la modification d'un adherent si l'utilisateur connecté est
     * un encadrant ou un initiateur.
     * Ceci permet de changer la fonction d'un adherent par un encadrant.
     * Cette methode desactive toutes les autres options pour empecher les erreurs de manipulation.
     */
    private void designDependingOnGetUsers() {


        // recup de l'user passé depuis l'activité SummaryActivity
        Intent intent1 = getIntent();
        if (intent1 != null) {
            summaryUser = (User) Objects.requireNonNull(intent1.getExtras()).getSerializable("summaryUser");
        }

        // recup de l'user passé par un intent depuis la classe SearchUser
        Intent intent = getIntent();
        if (intent != null) {
            searchedUser = (User) Objects.requireNonNull(intent.getExtras()).getSerializable("searchedUser");
        }


        // recup de l'user passé par un intent depuis la classe ConnectionActivity
        Intent intent2 = getIntent();
        if (intent2 != null) {
            user = (User) Objects.requireNonNull(intent2.getExtras()).getSerializable("user");
        }

        // affichage d'un user venant juste d'etre créé
        if (user != null) {
            // DESIGN
            mTitrePage.setText(R.string.bienvenue_sur_votre_compte);
            //mItemView.setVisible(true);
            mPrenom.setEnabled(true);
            mNom.setEnabled(true);
            mLicence.setEnabled(true);
            mNiveauPlongeespinner.setEnabled(false);
            mLinearLayoutFonctionAdherent.setVisibility(View.GONE);
            mModificationCompte.setText(R.string.modifiez_votre_compte);
            //Affichage du bouton de suppression uniquement aux proprietaires d'un compte
            mSuppressionCompte.setVisibility(View.VISIBLE);
            // DATAS
            if (user != null) {
                uid = user.getUid();
                mNom.setText(Objects.requireNonNull(user.getNom().toUpperCase()));
                mPrenom.setText(Objects.requireNonNull(user.getPrenom().toUpperCase()));
                mLicence.setText(Objects.requireNonNull(user.getLicence()));
                mNiveauPlongeespinner.setSelection(getIndexSpinner(mNiveauPlongeespinner, Objects.requireNonNull(user.getNiveau())));
                mFonctionAuClubspinner.setSelection(getIndexSpinner(mFonctionAuClubspinner, Objects.requireNonNull(user.getFonction())));
                mEmail.setText(Objects.requireNonNull(user.getEmail()));
                mPassword.setText(user.getPassword());
            }
        }

        // user deja créé provenant du sommaire ou de la recherche d'un adherent
        // si l'intent n'arrive pas de la recherche d'un encadrant, alors, c'est qu'elle arrive du sommaire.(utilisateur dejà connecté, donc)
        if (summaryUser != null && searchedUser == null) {
            // UN moniteur est sur son compte
            if (summaryUser.getFonction().equals("Initiateur") || summaryUser.getFonction().equals("Moniteur")) {
                //DESIGN
                mTitrePage.setText(R.string.bienvenue_sur_votre_compte);
                //mItemView.setVisible(true);
                mPrenom.setEnabled(true);
                mNom.setEnabled(true);
                mLicence.setEnabled(true);
                mNiveauPlongeespinner.setEnabled(true);
                mLinearLayoutFonctionAdherent.setVisibility(View.VISIBLE);
                mModificationCompte.setText(R.string.modifiez_votre_compte);
                //Affichage du bouton de suppression uniquement aux proprietaires d'un compte
                mSuppressionCompte.setVisibility(View.VISIBLE);
                // DATAS
                uid = summaryUser.getUid();
                mPrenom.setText(summaryUser.getPrenom());
                mNom.setText(summaryUser.getNom());
                mLicence.setText(summaryUser.getLicence());
                mNiveauPlongeespinner.setSelection(getIndexSpinner(mNiveauPlongeespinner, Objects.requireNonNull(summaryUser.getNiveau())));
                mFonctionAuClubspinner.setSelection(getIndexSpinner(mFonctionAuClubspinner, Objects.requireNonNull(summaryUser.getFonction())));
                mEmail.setText(Objects.requireNonNull(summaryUser.getEmail()));
                mPassword.setText(summaryUser.getPassword());
            }
            // il n'y a pas eu de recherche de plongeur par un encadrant. On affiche les caracteristique d'un plongeur classique
            else {
                // DESIGN
                mTitrePage.setText(R.string.bienvenue_sur_votre_compte);
                //mItemView.setVisible(true);
                mPrenom.setEnabled(true);
                mNom.setEnabled(true);
                mLicence.setEnabled(true);
                mNiveauPlongeespinner.setEnabled(false);
                mLinearLayoutFonctionAdherent.setVisibility(View.GONE);
                mModificationCompte.setText(R.string.modifiez_votre_compte);
                //Affichage du bouton de suppression uniquement aux proprietaires d'un compte
                mSuppressionCompte.setVisibility(View.VISIBLE);
                // DATAS
                if (summaryUser != null) {
                    uid = summaryUser.getUid();
                    mNom.setText(Objects.requireNonNull(summaryUser.getNom().toUpperCase()));
                    mPrenom.setText(Objects.requireNonNull(summaryUser.getPrenom().toUpperCase()));
                    mLicence.setText(Objects.requireNonNull(summaryUser.getLicence()));
                    mNiveauPlongeespinner.setSelection(getIndexSpinner(mNiveauPlongeespinner, Objects.requireNonNull(summaryUser.getNiveau())));
                    mFonctionAuClubspinner.setSelection(getIndexSpinner(mFonctionAuClubspinner, Objects.requireNonNull(summaryUser.getFonction())));
                    mEmail.setText(Objects.requireNonNull(summaryUser.getEmail()));
                    mPassword.setText(summaryUser.getPassword());
                }
            }
        }
        //l'intent arrive depuis l'activité de recherche. La mise à jour d'un adherent se fait ici par un encadrant
        else {
            if (summaryUser != null) {
                // DESIGN
                if (summaryUser.getFonction() != null && summaryUser.getNom() != null && summaryUser.getPrenom() != null) {
                    if (summaryUser.getFonction().equals("Initiateur") || summaryUser.getFonction().equals("Moniteur") &&
                            !summaryUser.getNom().equals(searchedUser.getNom()) && !summaryUser.getPrenom().equals(searchedUser.getPrenom())) {
                        mTitrePage.setText(R.string.modifiez_le_compte_d_un_adherent);
                        //mItemView.setVisible(false);
                        mPrenom.setEnabled(false);
                        mNom.setEnabled(false);
                        mLicence.setEnabled(false);
                        mNiveauPlongeespinner.setEnabled(true);
                        mLinearLayoutFonctionAdherent.setVisibility(View.VISIBLE);
                        mProgressBar.setVisibility(View.GONE);
                        //Affichage du bouton de suppression uniquement aux proprietaires d'un compte
                        mSuppressionCompte.setVisibility(View.GONE);

                        // DATAS
                        uid = summaryUser.getUid();
                        mPrenom.setText(summaryUser.getPrenom());
                        mNom.setText(summaryUser.getNom());
                        mLicence.setText(summaryUser.getLicence());
                        mNiveauPlongeespinner.setSelection(getIndexSpinner(mNiveauPlongeespinner, Objects.requireNonNull(summaryUser.getNiveau())));
                        mFonctionAuClubspinner.setSelection(getIndexSpinner(mFonctionAuClubspinner, Objects.requireNonNull(summaryUser.getFonction())));
                        mEmail.setText(Objects.requireNonNull(summaryUser.getEmail()));
                        mPassword.setText(summaryUser.getPassword());
                    }
                }
            }
        }
    }


    // user actuellement connecté dans l'app
    // creation de la fonction d'un plongeur si elle n'existe pas, notamment
    // lorsuq'un user vient juste de creer son compte et veut acceder à son compte tout de suite apres (depuis la tuile du sommaire)
               /* Object fonction;
                if (searchedUser.getFonction() == null) fonction = "Plongeur";
                else fonction = searchedUser.getFonction();

                Object nom = searchedUser.getNom();
                Object prenom = searchedUser.getPrenom();*/

    // Modification d'un compte adherent par un encadrant
                /*if (summaryUser != null) {
                    if (summaryUser.getFonction() != null && summaryUser.getNom() != null && summaryUser.getPrenom() != null) {
                        if (summaryUser.getFonction().equals("Initiateur") || summaryUser.getFonction().equals("Moniteur") &&
                                !summaryUser.getNom().equals(searchedUser.getNom()) && !summaryUser.getPrenom().equals(searchedUser.getPrenom())) {
                            mTitrePage.setText(R.string.modifiez_le_compte_d_un_adherent);
                            //mItemView.setVisible(false);
                            mPrenom.setEnabled(false);
                            mNom.setEnabled(false);
                            mLicence.setEnabled(false);
                            mNiveauPlongeespinner.setEnabled(true);
                            mLinearLayoutFonctionAdherent.setVisibility(View.VISIBLE);
                            mProgressBar.setVisibility(View.GONE);
                            //Affichage du bouton de suppression uniquement aux proprietaires d'un compte
                            mSuppressionCompte.setVisibility(View.GONE);
                        }
                        // moniteur etant sur son propre compte
                    } else {

                    }
                }
            }*/
    //});
    //}
    //}

    /**
     * Cette methode ne comprend pas l'update d'une fonction dans le club, car seul les encadrants du club peuvent
     * le faire, et cette fonctionnalité est donc reservée à une fonction particuliere.
     * Si la creation se deroule correctement, l'user est renvoyé vers la page "Sommaire".
     */
    private void updateUserInFirebase() {

        // Test performance de l'update d'user en bdd
        final Trace myTrace = FirebasePerformance.getInstance().newTrace("accountModificationActivityUserAccountUpdateIncludingCovoiturage_trace");
        myTrace.start();

        if (!mNom.getText().equals(getString(R.string.info_no_username_found)))
            if (!mNom.getText().toString().isEmpty() && !mPrenom.getText().toString().isEmpty() && !mEmail.getText().toString().isEmpty()) { // verification que tous les champs vides soient remplis

                // Update de la bdd covoiturage si l'user à updater a créé des covoiturages.
                // Cette fonction est appelée avant la fonction d'update de la bdd user
                updateCovoituragesIfCreated();

                // Update de la bdd user
                this.mProgressBar.setVisibility(View.VISIBLE);
                UserHelper.updateUser(uid, this.mNom.getText().toString().toUpperCase(), this.mPrenom.getText().toString().toUpperCase(), this.mLicence.getText().toString(),
                        this.mEmail.getText().toString(), this.mNiveauPlongeespinner.getSelectedItem().toString(),
                        this.mFonctionAuClubspinner.getSelectedItem().toString(), this.mPassword.getText().toString()).
                        addOnFailureListener(this.onFailureListener()).
                        addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(AccountModificationActivity.this, R.string.update_account,
                                        Toast.LENGTH_LONG).show();
                                updateUIAfterRESTRequestsCompleted(UPDATE_USERNAME);
                                startSummaryActivity();

                                myTrace.stop();
                            }
                        });
            } else {
                verificationChampsVides();
            }
        else {
            verificationChampsVides();
        }
    }
    // recuperation des données user depuis l'activité Sommaire (un user dejà connecté, doinc)
            /*else if (summaryUser != null) {

                this.mNom.setText(summaryUser.getNom());
                this.mPrenom.setText(summaryUser.getPrenom());
                this.mEmail.setText(summaryUser.getEmail());
                this.mLicence.setText(summaryUser.getEmail());

                if (!mNom.getText().toString().isEmpty() && !mNom.getText().equals(getString(R.string.info_no_username_found)) &&
                        !mPrenom.getText().toString().isEmpty() && !mEmail.getText().toString().isEmpty()) { // verification que tous les champs vides soient remplis

                    // Update de la bdd covoiturage si l'user à updater a créé des covoiturages.
                    // Cette fonction est appelée avant la fonction d'update de la bdd user
                    updateCovoituragesIfCreated();

                    // Update de la bdd user
                    this.mProgressBar.setVisibility(View.VISIBLE);
                    UserHelper.updateUser(summaryUser.getUid(), this.mNom.getText().toString().toUpperCase(), this.mPrenom.getText().toString().toUpperCase(), this.mLicence.getText().toString(),
                            this.mEmail.getText().toString(), this.mNiveauPlongeespinner.getSelectedItem().toString(), this.mFonctionAuClubspinner.getSelectedItem().toString()).
                            addOnFailureListener(this.onFailureListener()).
                            addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(AccountModificationActivity.this, R.string.update_account,
                                            Toast.LENGTH_SHORT).show();
                                    updateUIAfterRESTRequestsCompleted(UPDATE_USERNAME);
                                    startSummaryActivity();

                                    myTrace.stop();
                                }
                            });
                } else verificationChampsVides();
            }*/
    //}
    //}

    /**
     * Methode permettant de faire un update eventuel sur le nom et le prenom des covoiturages crées par l'utilisateur
     * si celui ci effectue ici un update sur son nom ou son prenom
     */
    private void updateCovoituragesIfCreated() {
        setupDb().collection("covoiturages").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot documentSnapshots) {
                if (documentSnapshots.size() > 0) {
                    List<DocumentSnapshot> covoits = documentSnapshots.getDocuments();
                    for (DocumentSnapshot covoiturage : covoits) {
                        Map<String, Object> covoit = covoiturage.getData();
                        if (covoit != null) {
                            if (covoit.get("nomConducteur").equals(mNom.getText().toString()) && covoit.get("prenomConducteur").equals(mPrenom.getText().toString())) {
                                CovoiturageHelper.updateCovoiturage(covoit.get("id").toString(), mNom.getText().toString(), mPrenom.getText().toString())
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                System.out.println("nok");
                                            }
                                        })
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                System.out.println("ok");
                                            }
                                        });
                            }
                        }
                    }
                }
            }
        });
    }

    /**
     * Methode permettant de recuperer et d'afficher les données de l'utilisateur actuellement connecté depuis
     * l'activité Sommaire qui a determiné que l'utilisateur voulant afficher ses informations etait enregistré en bdd.
     */
    /*private void getAndShowUserDatas() {

        // Test performance de l'update d'user en bdd
        final Trace myTrace1 = FirebasePerformance.getInstance().newTrace("accountModificationActivityGetAndShowUserDatas_trace");
        myTrace1.start();

        // recup de l'user passé par un intent depuis la classe ConnectionActivity
        Intent intent = getIntent();
        if (intent != null) {
            user = (User) Objects.requireNonNull(intent.getExtras()).getSerializable("user");


            if (user != null) {
                mNom.setText(Objects.requireNonNull(user.getNom().toUpperCase()));
                mPrenom.setText(Objects.requireNonNull(user.getPrenom().toUpperCase()));
                mLicence.setText(Objects.requireNonNull(user.getLicence()));
                mNiveauPlongeespinner.setSelection(getIndexSpinner(mNiveauPlongeespinner, Objects.requireNonNull(user.getNiveau())));
                mFonctionAuClubspinner.setSelection(getIndexSpinner(mFonctionAuClubspinner, Objects.requireNonNull(user.getFonction())));
                mEmail.setText(Objects.requireNonNull(user.getEmail()));

         *//*       // affichage different en fonction de la personne connectée
                //this.designDependingOnGetUsers(user);
                mTitrePage.setText(R.string.bienvenue_sur_votre_compte);
                //mItemView.setVisible(true);
                mPrenom.setEnabled(true);
                mNom.setEnabled(true);
                mLicence.setEnabled(true);
                mNiveauPlongeespinner.setEnabled(false);
                mLinearLayoutFonctionAdherent.setVisibility(View.GONE);
                mModificationCompte.setText(R.string.modifiez_votre_compte);
                //Affichage du bouton de suppression uniquement aux proprietaires d'un compte
                mSuppressionCompte.setVisibility(View.VISIBLE);*//*


                myTrace1.stop();
                //if (email != null) {
                *//*setupDb().collection("users").whereEqualTo("email", user.getEmail()).addSnapshotListener(this, new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        if (queryDocumentSnapshots != null) {
                            if (queryDocumentSnapshots.size() != 0) {
                                List<DocumentSnapshot> users = queryDocumentSnapshots.getDocuments();
                                for(DocumentSnapshot ds : users){
                                    mNom.setText(Objects.requireNonNull(ds.get("nom")).toString().toUpperCase());
                                    mPrenom.setText(Objects.requireNonNull(ds.get("prenom")).toString().toUpperCase());
                                    mLicence.setText(Objects.requireNonNull(ds.get("licence")).toString());
                                    mNiveauPlongeespinner.setSelection(getIndexSpinner(mNiveauPlongeespinner, Objects.requireNonNull(ds.get("niveau")).toString()));
                                    mFonctionAuClubspinner.setSelection(getIndexSpinner(mFonctionAuClubspinner, Objects.requireNonNull(ds.get("fonction")).toString()));
                                    mEmail.setText(Objects.requireNonNull(ds.get("email")).toString());

                                    myTrace1.stop();
                                }

                            }
                        }
                    }
                });*//*
            }
        }

       *//* // recup de l'user passé par un intent depuis la classe SearchUser
        Intent intent = getIntent();
        if (intent != null) {
            user = (User) Objects.requireNonNull(intent.getExtras()).getSerializable("user");
            if (user != null) {
                // requete avec l'uid de l'user recu
                setupDb().collection("users").document(user.getUid())
                        .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            Map<String, Object> user = task.getResult().getData();
                            if (user != null) {
                                mNom.setText(user.get("nom").toString());
                                mPrenom.setText(user.get("prenom").toString());
                                mLicence.setText(user.get("licence").toString());
                                mNiveauPlongeespinner.setSelection(getIndexSpinner(mNiveauPlongeespinner, user.get("niveau").toString()));
                                mFonctionAuClubspinner.setSelection(getIndexSpinner(mFonctionAuClubspinner, user.get("fonction").toString()));
                                mEmail.setText(user.get("email").toString());

                                myTrace1.stop();
                            }
                        }
                    }
                });
            }
        }*//*
    }
*/
    /**
     * Methode permettant de supprimer un utilisateur
     */
    private void deleteUser() {
        final FirebaseUser auth = FirebaseAuth.getInstance().getCurrentUser();
        if (auth != null) {
            setupDb().collection("users").document(auth.getUid())
                    .delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {

                    Toast.makeText(AccountModificationActivity.this, R.string.alertDialog_delete,
                            LENGTH_SHORT).show();
                }
            });
        }
    }

    /**
     * Methode permettant de supprimer les identifiants de l'user qui supprime son compte
     */
    private void deleteUserAuth() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            user.delete()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {

                            } else
                                System.out.println("nok");
                        }
                    });
        }
    }
}
