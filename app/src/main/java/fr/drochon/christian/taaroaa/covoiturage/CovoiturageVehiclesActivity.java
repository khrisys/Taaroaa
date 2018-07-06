package fr.drochon.christian.taaroaa.covoiturage;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.perf.FirebasePerformance;
import com.google.firebase.perf.metrics.Trace;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import fr.drochon.christian.taaroaa.R;
import fr.drochon.christian.taaroaa.base.BaseActivity;
import fr.drochon.christian.taaroaa.model.Covoiturage;

public class CovoiturageVehiclesActivity extends BaseActivity implements AdapterCovoiturageVehicles.Listener {

    // FOR COMMUNICATION
    private TextView mTextViewEmptyListRecyclerView;
    private RecyclerView mRecyclerViewVehicules;
    // FOR DATA
    private AdapterCovoiturageVehicles mAdapterCovoiturageVehicles;
    private List<String> listPassagers;

    // --------------------
    // LIFECYCLE
    // --------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_covoiturage_vehicules);

        mTextViewEmptyListRecyclerView = findViewById(R.id.empty_list_textview);
        mRecyclerViewVehicules = findViewById(R.id.recyclerViewCovoitVehicules);

        listPassagers = new ArrayList<>();

        // Test performance
        final Trace myTrace = FirebasePerformance.getInstance().newTrace("covoiturageVehiclesActivityShowAllVehicles_trace");
        myTrace.start();

        this.configureRecyclerView();
        this.configureToolbar();
        this.giveToolbarAName(R.string.covoit_vehicule_name);
        myTrace.stop();


        // --------------------
        // LISTENERS
        // --------------------

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CovoiturageVehiclesActivity.this, CovoiturageConducteursActivity.class);
                startActivity(intent);
            }
        });
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
     * @param item item de la toolbar
     * @return toolbar
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return optionsToolbar(this, item);
    }


    // --------------------
    // UI
    // --------------------

    @Override
    public int getFragmentLayout() {
        return 0;
    }

    /**
     * Configuration de la recyclerview
     * Cette methode créé l'adapter et lui passe en param pas mal d'informations 'comme par ex l'objet FireStoreRecyclerOptions generé par la methode
     * generateOptionsForAdapter.
     */
    private void configureRecyclerView() {

        //Configure Adapter & RecyclerView
        mAdapterCovoiturageVehicles = new AdapterCovoiturageVehicles(generateOptionsForAdapter(getAllCovoiturages()), this);
        mAdapterCovoiturageVehicles.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) { // c'est cette ligne de code qui insere les données dans la recyclerview
                mRecyclerViewVehicules.smoothScrollToPosition(mAdapterCovoiturageVehicles.getItemCount()); // Scroll to bottom on new messages
            }
        });
        mRecyclerViewVehicules.setLayoutManager(new LinearLayoutManager(this)); // layoutmanager indique comment seront positionnés les elements (linearlayout)
        mRecyclerViewVehicules.setAdapter(this.mAdapterCovoiturageVehicles);// l'adapter s'occupe du contenu
    }

    /**
     * La methode generateOptionsForAdapter utilise une requete passée en prama, recupérée depuis un methode definit dans la classe.
     * Cette requete permettra à la recyclerview d'afficher en temps reel le resultat de cette requete (la liste des utilisateurs en bdd, triés ou non).
     */
    private FirestoreRecyclerOptions<Covoiturage> generateOptionsForAdapter(Query query) {
        return new FirestoreRecyclerOptions.Builder<Covoiturage>()
                .setQuery(query, Covoiturage.class)
                .setLifecycleOwner(this)
                .build();
    }


    // --------------------
    // CALLBACK
    // --------------------

    /**
     * Permet d'afficher un message à l'user s'il n'y a pas de covoiturage
     **/
    @Override
    public void onDataChanged() {
        mTextViewEmptyListRecyclerView.setVisibility(this.mAdapterCovoiturageVehicles.getItemCount() == 0 ? View.VISIBLE : View.GONE);
    }

    // --------------------
    // REST REQUESTS
    // --------------------

    /**
     * Requete en bdd pour recuperer et afficher tous les covoiturages existants, excepté ceux dont la date de retour est passée.
     * Les covoiturages sont affichés par ordre de date de retour decroissante.
     *
     * @return query
     */
    @SuppressWarnings("unchecked")
    private Query getAllCovoiturages() {
        // Test performance
        final Trace myTrace1 = FirebasePerformance.getInstance().newTrace("covoiturageVehiclesActivityGetAllCovoiturages_trace");
        myTrace1.start();

        Query mQuery = setupDb().collection("covoiturages").orderBy("horaireRetour", Query.Direction.ASCENDING)
                .whereGreaterThan("horaireRetour", Calendar.getInstance().getTime());
        mQuery.addSnapshotListener(this, new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (queryDocumentSnapshots != null && queryDocumentSnapshots.size() != 0) {
                    List<DocumentSnapshot> ds = queryDocumentSnapshots.getDocuments();
                    for (int i = 0; i < ds.size(); i++) {
                        Map<String, Object> covoit = ds.get(i).getData();
                        listPassagers = new ArrayList<>();
                        if (covoit != null) {
                            listPassagers = (List<String>) covoit.get("listPassagers");

                            // recuperation de l'objet covoiturage
                            //TODO requete ne renvoi pas l'attribut des passagers
                            new Covoiturage(covoit.get("id").toString(), covoit.get("nomConducteur").toString(), covoit.get("prenomConducteur").toString(),
                                    covoit.get("nbPlacesDispo").toString(), covoit.get("nbPlacesTotal").toString(), covoit.get("typeVehicule").toString(),
                                    stStringToDate(covoit.get("horaireAller").toString()), stStringToDate(covoit.get("horaireRetour").toString()),
                                    covoit.get("lieuDepartAller").toString(), covoit.get("lieuDepartRetour").toString(), listPassagers);
                            myTrace1.stop();
                        }
                    }
                }
            }
        });
        return mQuery;
    }
}
