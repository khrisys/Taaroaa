package fr.drochon.christian.taaroaa.covoiturage;

import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;

import fr.drochon.christian.taaroaa.R;
import fr.drochon.christian.taaroaa.base.BaseActivity;

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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_covoiturage_passagers);

        mNomConducteur = findViewById(R.id.nom_conducteur_txt);
        mDateDepart = findViewById(R.id.date_depart_txt);
        mHeureDepart = findViewById(R.id.heure_depart_txt);
        mDateretour = findViewById(R.id.date_retour_txt);
        mHeureRetour = findViewById(R.id.heure_depart_retour_txt);
        mNbPlaceDispo = findViewById(R.id.nb_place_dispo_txt);
        mTypeVehicule = findViewById(R.id.type_vehicule_txt);
        mNomPassager = findViewById(R.id.nom_passager_input);
        mNbPassager = findViewById(R.id.nb_passager_input);
        mReservation = findViewById(R.id.reservation_covoit_btn);


        configureToolbar();
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
}
