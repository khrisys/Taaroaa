package fr.drochon.christian.taaroaa.covoiturage;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import fr.drochon.christian.taaroaa.R;
import fr.drochon.christian.taaroaa.api.CovoiturageHelper;
import fr.drochon.christian.taaroaa.base.BaseActivity;
import fr.drochon.christian.taaroaa.model.Covoiturage;

public class CovoituragePassagersActivity extends BaseActivity {

    static Covoiturage covoiturage;
    TextInputEditText mNomConducteur;
    TextInputEditText mDateDepart;
    TextInputEditText mHeureDepart;
    TextInputEditText mDateretour;
    TextInputEditText mHeureRetour;
    TextInputEditText mNbPlaceDispo;
    TextInputEditText mTypeVehicule;
    TextInputEditText mNomPassager;
    TextInputEditText mNbPassager;
    LinearLayout mTitreInscription;

    Button mReservation;
    Intent mIntent;
    ProgressBar mProgressBar;

    // --------------------
    // LIFECYCLE
    // --------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_covoiturage_passagers);

        mNomConducteur = findViewById(R.id.nom_conducteur_txt);
        mDateDepart = findViewById(R.id.date_depart_txt);
        mDateretour = findViewById(R.id.date_retour_txt);
        mNbPlaceDispo = findViewById(R.id.nb_place_dispo_txt);
        mTypeVehicule = findViewById(R.id.type_vehicule_txt);
        mNomPassager = findViewById(R.id.nom_passager_input);
        mNbPassager = findViewById(R.id.nb_passager_input);
        mReservation = findViewById(R.id.reservation_covoit_btn);
        mProgressBar = findViewById(R.id.progress_bar);
        mTitreInscription = findViewById(R.id.titre_inscription);

        this.configureToolbar();
        this.updateUIWhenCreating();

        mReservation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createPassagerInCovoiturage();
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();
        this.updateUIWhenResuming();
    }

    @Override
    public int getFragmentLayout() {
        return 0;
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
        getMenuInflater().inflate(R.menu.course_pupils_menu, menu);
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
    // UI
    // --------------------

    /**
     * Methode permettant de desactiver les champs de saisi en cas de covoiturage complet
     */
    private void updateUI() {
        if (Integer.parseInt(covoiturage.getNbPlacesDispo()) == 0) {
            mTitreInscription.setEnabled(false);
        }
    }
    /**
     * Methode permettant d'afficher les informations de l'user sur l'ecran AccountCreateActivity lorsqu'un user vient de creer un compte
     */
    private void updateUIWhenCreating() {

        if (this.getCurrentUser() != null) { // retourne un user FirebaseUser. Permettra de remplir toutes les vues de l'activité
            getAndShowDatas();
            updateUI();
        }
    }

    /**
     * Methode permettant d'afficher les informations d'un user depuis la bdd firestore lorsque le cycle de vie de l'application est à OnResume()
     */
    private void updateUIWhenResuming() {

        if (this.getCurrentUser() != null) { // retourne un user FirebaseUser. Permettra de remplir toutes les vues de l'activité
            getAndShowDatas();
            updateUI();
        }
    }

    /**
     * Methode permettant à l'utilisateur d'etre redirigé vers la pages principale des covoiturages
     */
    private void startActivityCovoiturageVehicule() {
        Intent intent = new Intent(CovoituragePassagersActivity.this, CovoiturageVehiclesActivity.class);
        startActivity(intent);
    }

    /**
     * Methode permettant de signaler une erreur lorsqu'un champ est resté vide alors que la soumission du formulaire a été faite.
     */
    private boolean verificationChampsVides() {

        if (!mNomPassager.getText().toString().isEmpty() && !mNbPassager.getText().toString().isEmpty())
            return true;
        else if (mNomPassager.getText().toString().isEmpty() && mNbPassager.getText().toString().isEmpty()) {
            mProgressBar.setVisibility(View.GONE);
            mNomPassager.setError("Merci de saisir ce champ !");
            mNbPassager.setError("Merci de saisir ce champ !");
            return false;
        } else if (mNomPassager.getText().toString().isEmpty()) {
            mProgressBar.setVisibility(View.GONE);
            mNomPassager.setError("Merci de saisir ce champ !");
            return false;
        } else if (mNbPassager.getText().toString().isEmpty()) {
            mProgressBar.setVisibility(View.GONE);
            mNbPassager.setError("Merci de saisir ce champ !");
            return false;
        }
        return false;
    }



    // --------------------
    // REST REQUETES
    // --------------------


    /**
     * Methode permettant de recuperer et d'afficher toutes les informations d'un covoiturage
     */
    private void getAndShowDatas() {
        mIntent = getIntent();
        covoiturage = (Covoiturage) Objects.requireNonNull(mIntent.getExtras()).getSerializable("covoit");
        assert covoiturage != null;

        mNomConducteur.setText(Html.fromHtml("<b>Conducteur : </b>" + covoiturage.getPrenomConducteur() + " " + covoiturage.getNomConducteur()));
        mDateDepart.setText(Html.fromHtml("<b>Aller : départ le </b>" + stDateToString(covoiturage.getHoraireAller()) + "<b> depuis </b>" + covoiturage.getLieuDepartAller()));
        mDateretour.setText(Html.fromHtml("<b>Retour : départ le </b>" + stDateToString(covoiturage.getHoraireRetour()) + "<b> jusqu'à </b>" + covoiturage.getLieuDepartRetour()));
        mNbPlaceDispo.setText(Html.fromHtml("<b>Places disponibles : </b>" + covoiturage.getNbPlacesDispo()  + " / " + covoiturage.getNbPlacesTotal()));
        mTypeVehicule.setText(Html.fromHtml("<b>Type Véhicule : </b>" + covoiturage.getTypeVehicule()));
    }

    /**
     * Methode permettant la creation d'un user dans le bdd. En cas d'insertion ou de probleme,
     * la fonction renverra une notification à l'utilisateur.
     */
    private void createPassagerInCovoiturage() {

        this.mProgressBar.setVisibility(View.VISIBLE);

        if (verificationChampsVides()) {
            int nbPlacesRestantes = calculNbPlacesRestantes();

            // verif que le nb de places demandées ne depassent pas le nb de places dispo
            if (nbPlacesRestantes < 0 && Integer.parseInt(covoiturage.getNbPlacesDispo()) >= 0) {
                mNomPassager.setText("");
                mNbPassager.setText("");
                mProgressBar.setVisibility(View.GONE);
                // alertdialog
                final AlertDialog.Builder adb = new AlertDialog.Builder(CovoituragePassagersActivity.this);
                adb.setTitle(R.string.alertDialog_places_restantes);
                adb.setIcon(android.R.drawable.ic_dialog_alert);
                adb.setTitle(R.string.rectif_demande);
                adb.setPositiveButton("MODIFIER", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // rien à appeler. pas la peine de faire de toast
                    }
                });
                adb.show(); // affichage de l'artdialog
/*            } else if (nbPlacesRestantes < 0 && Integer.parseInt(covoiturage.getNbPlacesDispo()) == 0) {
                final AlertDialog.Builder adb = new AlertDialog.Builder(CovoituragePassagersActivity.this);
                adb.setTitle(R.string.alertDialog_places_restantes);
                adb.setIcon(android.R.drawable.ic_dialog_alert);
                adb.setTitle(R.string.rectif_demande2);
                adb.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        startActivityCovoiturageVehicule();
                    }
                });
                adb.show(); // affichage de l'artdialog*/
            } else {
                String placesRestantes = String.valueOf(nbPlacesRestantes);
                mNbPlaceDispo.setText(placesRestantes);
                List<String> listPassagers = new ArrayList<>();
                listPassagers.addAll(covoiturage.getListPassagers());

                // ajout des infos du passager dans l'objet covoiturage
                listPassagers.add(mNomPassager.getText().toString());

                //CRUD
                CovoiturageHelper.updateCovoiturage(covoiturage.getId(), placesRestantes, listPassagers)
                        .addOnFailureListener(this.onFailureListener())
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(CovoituragePassagersActivity.this, R.string.create_passager,
                                        Toast.LENGTH_SHORT).show();
                                startActivityCovoiturageVehicule(); // renvoi l'user sur la page des covoiturages apres validation de la creation de l'user dans les covoit
                            }
                        });
            }
        }
    }

    /**
     * Methode permettant de calculer le nombre de place restantes dans un covoiturage
     *
     * @return
     */
    private int calculNbPlacesRestantes() {
        String passagers = mNbPassager.getText().toString();
        int nbPassagers = Integer.parseInt(passagers);
        int nbPlacesDispo = Integer.parseInt(covoiturage.getNbPlacesDispo());
        int nbPlacesRestantes = nbPlacesDispo - nbPassagers;

        return nbPlacesRestantes;
    }


}
