package fr.drochon.christian.taaroaa.covoiturage;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;

import java.util.Objects;

import fr.drochon.christian.taaroaa.R;
import fr.drochon.christian.taaroaa.base.BaseActivity;
import fr.drochon.christian.taaroaa.model.Covoiturage;

public class CovoituragePassagersActivity extends BaseActivity {

    TextInputEditText mNomConducteur;
    TextInputEditText mDateDepart;
    TextInputEditText mHeureDepart;
    TextInputEditText mDateretour;
    TextInputEditText mHeureRetour;
    TextInputEditText mNbPlaceDispo;
    TextInputEditText mTypeVehicule;
    TextInputEditText mNomPassager;
    TextInputEditText mNbPassager;
    Button mReservation;
    Intent mIntent;
    Covoiturage covoiturage;

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

        this.configureToolbar();
        this.updateUIWhenCreating();
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
     * Methode permettant d'afficher les informations de l'user sur l'ecran AccountCreateActivity lorsqu'un user vient de creer un compte
     */
    private void updateUIWhenCreating() {

        if (this.getCurrentUser() != null) { // retourne un user FirebaseUser. Permettra de remplir toutes les vues de l'activité
            getAndShowDatas();
        }
    }

    /**
     * Methode permettant d'afficher les informations d'un user depuis la bdd firestore lorsque le cycle de vie de l'application est à OnResume()
     */
    private void updateUIWhenResuming() {

        if (this.getCurrentUser() != null) { // retourne un user FirebaseUser. Permettra de remplir toutes les vues de l'activité
            getAndShowDatas();
        }
    }

    /**
     * Methode permettant de recuperer et d'afficher toutes les informations d'un covoiturage
     */
    private void getAndShowDatas(){
        mIntent  = getIntent();
        covoiturage = (Covoiturage) Objects.requireNonNull(mIntent.getExtras()).getSerializable("covoit");
        assert covoiturage != null;

        mNomConducteur.setText(Html.fromHtml("<b>Conducteur : </b>" + covoiturage.getPrenomConducteur() + " " + covoiturage.getNomConducteur()));
        mDateDepart.setText(Html.fromHtml("<b>Aller : départ le </b>" + stDateToString(covoiturage.getHoraireAller())));
        mDateretour.setText(Html.fromHtml("<b>Retour : départ le </b>" + stDateToString(covoiturage.getHoraireRetour())));
        mNbPlaceDispo.setText(Html.fromHtml("<b>Nombre de places disponibles : </b>" + covoiturage.getNbPlacesDispo()));
        mTypeVehicule.setText(Html.fromHtml("<b>Type Véhicule : </b>" + covoiturage.getTypeVehicule()));
/*        mNomPassager = findViewById(R.id.nom_passager_input);
        mNbPassager = findViewById(R.id.nb_passager_input);*/
    }
}
