package fr.drochon.christian.taaroaa.covoiturage;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import fr.drochon.christian.taaroaa.R;
import fr.drochon.christian.taaroaa.base.BaseActivity;
import fr.drochon.christian.taaroaa.model.Covoiturage;

public class CovoiturageVehiclesActivity extends BaseActivity implements AdapterCovoiturageVehicles.Listener {

    // FOR COMMUNICATION
    TextView mTextViewEmptyListRecyclerView;
    CoordinatorLayout mCoordinatorLayoutRoot;
    LinearLayout mLinearLayoutVehicule;
    LinearLayout mLinearLayoutRecycleView;
    ScrollView mScrollViewRecyclerView;
    RecyclerView mRecyclerViewVehicules;

    // FOR DATA
    private AdapterCovoiturageVehicles mAdapterCovoiturageVehicles;
    List<Covoiturage> listCovoiturages;
    List<String> listPassagers;
    Covoiturage covoiturage;

    // --------------------
    // LIFECYCLE
    // --------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_covoiturage_vehicules);

        mTextViewEmptyListRecyclerView = findViewById(R.id.empty_list_textview);
        mCoordinatorLayoutRoot = findViewById(R.id.coordinatorLayoutRoot);
        mLinearLayoutVehicule = findViewById(R.id.linearLayoutVehicules);
        mLinearLayoutRecycleView = findViewById(R.id.linearLayoutRecyclerView);
        mScrollViewRecyclerView = findViewById(R.id.scrollviewRecyclerView);
        mRecyclerViewVehicules = findViewById(R.id.recyclerViewCovoitVehicules);

        listCovoiturages = new ArrayList<>();
        listPassagers = new ArrayList<>();

        this.configureRecyclerView();
        this.configureToolbar();
        this.giveToolbarAName(R.string.covoit_vehicule_name);
        this.giveToolbarAName(R.string.covoit_vehicule_name);

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
     * @param item
     * @return boolean
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
    private Query getAllCovoiturages() {

        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        Query mQuery = db.collection("covoiturages").orderBy("horaireRetour",Query.Direction.ASCENDING).whereGreaterThan("horaireRetour", Calendar.getInstance().getTime());
        mQuery.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot documentSnapshots) {
                if (documentSnapshots.size() != 0) {
                    List<DocumentSnapshot> ds = documentSnapshots.getDocuments();
                    for (int i = 0; i < ds.size(); i++) {
                        Map<String, Object> covoit = ds.get(i).getData();

                        listPassagers = new ArrayList<>();
                        listPassagers = (List<String>) covoit.get("passagers");

                        // recuperation de l'objet covoiturage
                        covoiturage = new Covoiturage(covoit.get("id").toString(), covoit.get("nomConducteur").toString(), covoit.get("prenomConducteur").toString(),
                                covoit.get("nbPlacesDispo").toString(), covoit.get("nbPlacesTotal").toString(), covoit.get("typeVehicule").toString(), stStringToDate(covoit.get("horaireAller").toString()),
                                stStringToDate(covoit.get("horaireRetour").toString()), covoit.get("lieuDepartAller").toString(), covoit.get("lieuDepartRetour").toString(), listPassagers);
                    }
                }
            }
        });
        return mQuery;
    }
}
