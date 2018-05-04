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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import fr.drochon.christian.taaroaa.R;
import fr.drochon.christian.taaroaa.base.BaseActivity;
import fr.drochon.christian.taaroaa.model.Covoiturage;
import fr.drochon.christian.taaroaa.model.User;

public class CovoiturageVehiclesActivity extends BaseActivity implements AdapterCovoiturageVehicles.Listener {

    // FOR COMMUNICATION
    TextView mTextView;
    TextView mTextViewEmptyListRecyclerView;
    CoordinatorLayout mCoordinatorLayoutRoot;
    LinearLayout mLinearLayoutVehicule;
    LinearLayout mLinearLayoutRecycleView;
    ScrollView mScrollViewRecyclerView;
    RecyclerView mRecyclerViewVehicules;
    List<Covoiturage> listCovoiturages;
    List<User> listPassagers;
    Covoiturage covoiturage;
    // FOR DATA
    private AdapterCovoiturageVehicles mAdapterCovoiturageVehicles;

    // --------------------
    // LIFECYCLE
    // --------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_covoiturage_vehicules);
        //Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);

        mTextViewEmptyListRecyclerView = findViewById(R.id.empty_list_textview);
        mCoordinatorLayoutRoot = findViewById(R.id.coordinatorLayoutRoot);
        mLinearLayoutVehicule = findViewById(R.id.linearLayoutVehicules);
        mLinearLayoutRecycleView = findViewById(R.id.linearLayoutRecyclerView);
        mScrollViewRecyclerView = findViewById(R.id.scrollviewRecyclerView);
        mRecyclerViewVehicules = findViewById(R.id.recyclerViewCovoitVehicules);

        listCovoiturages = new ArrayList<Covoiturage>();
        listPassagers = new ArrayList<User>();

        configureRecyclerView();
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
    // SEARCH REQUESTS
    // --------------------

    /**
     * Requete en bdd pour recuperer tous les covoiturages existants
     *
     * @return query
     */
    private Query getAllCovoiturages() {

        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        Query mQuery = db.collection("covoiturages"); //.orderBy("nomConducteur", Query.Direction.ASCENDING);
        mQuery.addSnapshotListener(this, new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                if (documentSnapshots.size() != 0) {
                    List<DocumentSnapshot> ds = documentSnapshots.getDocuments();
                    for (int i = 0; i < ds.size(); i++) {
                        Map<String, Object> covoit = ds.get(i).getData();
                        // recuperation des passagers d'un covoiturage
                        listPassagers = new ArrayList<>();
                        listPassagers = (List<User>) covoit.get("passagers");

                        // recuperation de l'objet covoiturage
                        /*covoiturage = new Covoiturage(covoit.get("id").toString(), covoit.get("nomConducteur").toString(), covoit.get("prenomConducteur").toString(),
                                covoit.get("nbPlacesDispo").toString(), covoit.get("typeVehicule").toString(),
                                stStringToDate(covoit.get("horaireAller").toString()), stStringToDate(covoit.get("horaireRetour").toString()), (List<User>)covoit.get("passagers"));*/
                        Covoiturage covoiturage = new Covoiturage();
                        covoiturage.setId(covoit.get("id").toString());
                        covoiturage.setNomConducteur(covoit.get("nomConducteur").toString());
                        covoiturage.setPrenomConducteur(covoit.get("prenomConducteur").toString());
                        covoiturage.setNbPlacesDispo(covoit.get("nbPlacesDispo").toString());
                        covoiturage.setTypeVehicule(covoit.get("typeVehicule").toString());
                        covoiturage.setHoraireAller(stStringToDate(covoit.get("horaireAller").toString()));
                        covoiturage.setHoraireRetour(stStringToDate(covoit.get("horaireRetour").toString()));
                        covoiturage.setListPassagers(listPassagers);

                        /*for(int o = 0; o < ((List<User>) covoit.get("passagers")).size(); o++) {
                            User us = new User();
                            String passager = ds.get(o).get("passagers").toString();
                            us.setNom(passager);
                            listPassagers.add(us);
                        }*/


                        listCovoiturages.add(covoiturage);
                    }
                }
            }
        });
        return mQuery;
    }


/*    *//**
     * Methode permettant de recuperer et d'afficher l'integralité de la liste des snapshots et d'en faire des objets "Covoiturage"
     *
     * @param
     *//*
    private void readDataInList(final List<DocumentSnapshot> documentSnapshot) {

        // un DocumentReference fait référence à un emplacement de document dans une base de données Firestore et peut être utilisé pour
        // écrire, lire ou écouter l'emplacement. Il peut exister ou non un document à l'emplacement référencé.
        for (int i = 0; i < documentSnapshot.size(); i++) {
            DocumentSnapshot doc = documentSnapshot.get(i); //Un DocumentSnapshot contient des données lues à partir d'un document dans votre base de données Firestore.
            if (doc.exists()) {
                if(listPassagers.size() == 0) {
                    java.util.Date aller = stStringToDate(doc.get("horaireAller").toString());
                    java.util.Date retour = stStringToDate(doc.get("horaireRetour").toString());
                    covoiturage = new Covoiturage(doc.getId(), doc.get("nomConducteur").toString(), doc.get("prenomConducteur").toString(), doc.get("nbPlacesDispo").toString(),
                            doc.get("typeVehicule").toString(), aller, retour, listPassagers);
                    listCovoiturages.add(covoiturage);
                }
                else{
                    covoiturage = new Covoiturage(doc.getId(), doc.get("nomConducteur").toString(), doc.get("prenomConducteur").toString(), doc.get("nbPlacesDispo").toString(),
                            doc.get("typeVehicule").toString(), stStringToDate(doc.get("horaireAller").toString()),stStringToDate(doc.get("horaireRetour").toString()), listPassagers);
                    listCovoiturages.add(covoiturage);
                }
            }
        }
    }*/

/*    private java.util.Date stStringToDate(String horaire){
        SimpleDateFormat formatter = new SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy", Locale.US);
        java.util.Date dateFormatee = null;

        try {
            dateFormatee = formatter.parse(horaire);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return dateFormatee;
    }*/

/*    private void getListCovoiturages() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("covoiturages").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot documentSnapshots) {
                if (documentSnapshots.size() != 0) {
                    List<DocumentSnapshot> ds = documentSnapshots.getDocuments();
                    for (int i = 0; i < ds.size(); i++) {
                        Map<String, Object> covoit = ds.get(i).getData();
                        covoiturage = new Covoiturage(covoit.get("id").toString(), covoit.get("nomConducteur").toString(), covoit.get("prenomConducteur").toString(),
                                covoit.get("nbPlacesDispo").toString(), covoit.get("typeVehicule").toString(),
                                stStringToDate(covoit.get("horaireAller").toString()), stStringToDate(covoit.get("horaireRetour").toString()), listPassagers);
                        User user = new User();
                        for(int j = 0 ; j < listPassagers.size(); j++){
                            user.setNom(covoit.get("passagers").toString());
                        }
                        listCovoiturages.add(covoiturage);
                    }
                }
            }
        });
    }*/
/*
    *//**
     * Methode permettant de recuperer tous les passagers d'un covoiturage
     *
     * @return
     *//*
    private List<User> getPassagers() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        // recuperation de tous les docs covoiturages
        db.collection("covoiturages").document().get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(documentSnapshot.exists()){
                    Map<String, Object> covoit = documentSnapshot.getData();
                    // recuperation de tous les passagers d'un covoit

                }
            }
        });
        return listPassagers;
    }*/
}
