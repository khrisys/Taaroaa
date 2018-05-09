package fr.drochon.christian.taaroaa.covoiturage;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import fr.drochon.christian.taaroaa.R;
import fr.drochon.christian.taaroaa.base.BaseActivity;

public class CovoiturageVehiclesActivity extends BaseActivity implements AdapterCovoiturageVehicles.Listener {

    TextView mTextView;
    TextView mTextViewEmptyListRecyclerView;
    MenuItem mItemView;
    CoordinatorLayout mCoordinatorLayoutRoot;
    LinearLayout mLinearLayoutVehicule;
    LinearLayout mLinearLayoutRecycleView;
    ScrollView mScrollViewRecyclerView;
    RecyclerView mRecyclerViewVehicules;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_covoiturage_vehicules);
        //Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);

        mTextView = findViewById(R.id.covoit_presentation_txt);
        mTextViewEmptyListRecyclerView = findViewById(R.id.empty_list_textview);
        mCoordinatorLayoutRoot = findViewById(R.id.coordinatorLayoutRoot);
        mLinearLayoutVehicule = findViewById(R.id.linearLayoutVehicules);
        mLinearLayoutRecycleView = findViewById(R.id.linearLayoutRecyclerView);
        mScrollViewRecyclerView = findViewById(R.id.scrollviewRecyclerView);
        mRecyclerViewVehicules = findViewById(R.id.recyclerViewCovoitVehicules);

        configureToolbar();

        // --------------------
        // LISTENERS
        // --------------------

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CovoiturageVehiclesActivity.this, CovoiturageConducteursActivity.class);
                startActivity(intent);
            /*    Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();*/
            }
        });
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
        getMenuInflater().inflate(R.menu.covoit_search_vehicle_menu, menu);
        //searchVehicles();

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
     * Methode permettant de gerer la barre de recherche des adherents pour l'affichage de leurs comptes
     * afin que les encadrants puissent les modifier.
     */
    private void searchVehicles() {

        android.widget.SearchView searchView = (android.widget.SearchView) mItemView.getActionView();
        searchView.setQueryHint("Trouvez un véhicule");
        searchView.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        searchView.setOnQueryTextListener(new android.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String name) {
                // decomposition du nom et du prenom recu dans le param name
           /*     String nom = null, prenom = null;
                String[] parts;
                if (name.contains(" ")) {
                    parts = name.split(" ");
                    try {
                        if (parts[1] != null) nom = parts[1];
                        else nom = "";
                    } catch (ArrayIndexOutOfBoundsException e1) {
                        Log.e("TAG", "ArrayOutOfBoundException " + e1.getMessage());
                    }
                    if (parts[0] != null) prenom = parts[0];
                    else prenom = "";
                } else {
                    nom = name;
                    prenom = "";
                }
                System.out.println("clic");
                showSearchedDatas(name, prenom);*/
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                System.out.println(newText);
                return true;
            }
        });
    }

    @Override
    public void onDataChanged() {

    }
}
