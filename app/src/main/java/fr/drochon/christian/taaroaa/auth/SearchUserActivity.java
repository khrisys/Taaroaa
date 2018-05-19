package fr.drochon.christian.taaroaa.auth;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import fr.drochon.christian.taaroaa.R;
import fr.drochon.christian.taaroaa.base.BaseActivity;
import fr.drochon.christian.taaroaa.model.User;

public class SearchUserActivity extends BaseActivity {

    // FOR COMMUNICATION
    TextView mEmptyListMessage;
    RecyclerView mRecyclerViewUser;
    SearchView mSearchView;
    // FOR DATA
    private AdapterSearchedUser mAdapterSearchedUser;
    List<User> listUsers;


    // --------------------
        // LIFECYCLE
    // --------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_user);

        mEmptyListMessage = findViewById(R.id.empty_list_textview);
        mRecyclerViewUser = findViewById(R.id.recyclerViewSearchedUser);
        mSearchView = findViewById(R.id.searchbar_user);

        listUsers = new ArrayList<>();
        getListUsers();

        configureRecyclerView();
        configureToolbar();
        this.giveToolbarAName(R.string.account_search_name);

        // --------------------
        // LISTENERS
        // --------------------

        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String name) {
                System.out.println("getclic");
                // decomposition du nom et du prenom recu dans le param name
/*                String nom = null, prenom = null;
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
                }*/
                return true;
            }

            //Configure Adapter & RecyclerView
            @Override
            public boolean onQueryTextChange(String newText) {
                System.out.println(newText);
                //TODO FAIRE UN FILTRE PLUTO QU'UNE REQUETE
                // filtre d'affichage sur la liste des users
                if (!newText.equals(""))
                    mAdapterSearchedUser = new AdapterSearchedUser(generateOptionsForAdapter(getFilteredUser(newText)));
                else
                    mAdapterSearchedUser = new AdapterSearchedUser(generateOptionsForAdapter(getAllUsers()));

                mAdapterSearchedUser.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
                    @Override
                    public void onItemRangeInserted(int positionStart, int itemCount) { // c'est cette ligne de code qui insere les données dans la recyclerview
                        mRecyclerViewUser.smoothScrollToPosition(mAdapterSearchedUser.getItemCount()); // Scroll to bottom on new messages
                    }
                });
                mRecyclerViewUser.setLayoutManager(new LinearLayoutManager(SearchUserActivity.this)); // layoutmanager indique comment seront positionnés les elements (linearlayout)
                mRecyclerViewUser.setAdapter(mAdapterSearchedUser);// l'adapter s'occupe du contenu
                return true;
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

        return true; // true affiche le menu
    }

    /**
     * recuperation  du clic d'un user sur une option de la toolbar.
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
     * Configuration de la recyclerview
     * Cette methode créé l'adapter et lui passe en param pas mal d'informations 'comme par ex l'objet FireStoreRecyclerOptions generé par la methode
     * generateOptionsForAdapter.
     */
    private void configureRecyclerView() {

        //Configure Adapter & RecyclerView
        mAdapterSearchedUser = new AdapterSearchedUser(generateOptionsForAdapter(getAllUsers()));
        //mAdapterCoursesPupils = new AdapterCoursesPupils(generateOptionsForAdapter(queryDateCourses(calendrierClique)), this);
        mAdapterSearchedUser.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) { // c'est cette ligne de code qui insere les données dans la recyclerview
                mRecyclerViewUser.smoothScrollToPosition(mAdapterSearchedUser.getItemCount()); // Scroll to bottom on new messages
            }
        });
        mRecyclerViewUser.setLayoutManager(new LinearLayoutManager(this)); // layoutmanager indique comment seront positionnés les elements (linearlayout)
        mRecyclerViewUser.setAdapter(this.mAdapterSearchedUser);// l'adapter s'occupe du contenu
    }

    /**
     * La methode generateOptionsForAdapter utilise une requete passée en prama, recupérée depuis un methode definit dans la classe.
     * Cette requete permettra à la recyclerview d'afficher en temps reel le resultat de cette requete (la liste des utilisateurs en bdd, triés ou non).
     */
    private FirestoreRecyclerOptions<User> generateOptionsForAdapter(Query query) {
        return new FirestoreRecyclerOptions.Builder<User>()
                .setQuery(query, User.class)
                .setLifecycleOwner(this)
                .build();
    }


    // --------------------
    // SEARCH REQUESTS
    // --------------------

    /**
     * Requete en bdd pour recuperer tous les cours existants
     *
     * @return query
     */
    private Query getAllUsers() {

        Query mQuery = setupDb().collection("users").orderBy("nom", Query.Direction.ASCENDING);
        mQuery.addSnapshotListener(this, new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                // condition de creation d'un user ou affichage simple d'un message indiquant que l'user existe dejà en bdd.
                // Avec les uid, il ne peut y avoir de doublon, on peut donc etre sur qu'il n'y a qu'un seule doc qui existe s'il en existe un.
                if (documentSnapshots != null) {
                    if (documentSnapshots.size() != 0) {
                        Log.e("TAG", "Le document existe !");
                        // liste des docs
                        readDataInList(documentSnapshots.getDocuments());
                    }
                }
            }
        });
        return mQuery;
    }

    /**
     * Methode permettant de filtrer les noms saisis dans la barre de recherche
     *
     * @return query
     */
    private Query getFilteredUser(final String nom) {

        Query mQ = setupDb().collection("users").orderBy("nom").startAt(nom).endAt(nom+'\uf8ff');
        mQ.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot documentSnapshots) {
                if (documentSnapshots != null) {
                    if (documentSnapshots.size() != 0) {
                        List<DocumentSnapshot> docs = documentSnapshots.getDocuments();
                        for (DocumentSnapshot ds : docs) {
                            Map<String, Object> user = ds.getData();
                            filter(listUsers, nom);
                        }
                    }
                }
            }
        });

        return mQ;
    }


    /**
     * Methode permettant de filtrer la liste des utilisateurs affichés grace à la barre de recherche
     *
     * @param models
     * @param query
     * @return
     */
    private static List<User> filter(List<User> models, String query) {
        final String lowerCaseQuery = query.toLowerCase();

        final List<User> filteredModelList = new ArrayList<>();
        for (User model : models) {
            final String text = model.getNom().toLowerCase();
            if (text.contains(lowerCaseQuery)) {
                filteredModelList.add(model);
            }
        }
        return filteredModelList;
    }

    /**
     * Methode permettant de remplir la liste de tous les utilisateurs contenus dans la bdd
     */
    private void getListUsers(){
        setupDb().collection("users").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot documentSnapshots) {
                if (documentSnapshots.size() != 0) {
                    List<DocumentSnapshot> ds = documentSnapshots.getDocuments();
                    for (int i = 0; i < ds.size(); i++) {
                        Map<String, Object> map = ds.get(i).getData();
                        User user = new User(map.get("uid").toString(), map.get("nom").toString(), map.get("prenom").toString());
                        listUsers.add(user);
                    }
                }
            }
        });
    }

    /**
     * Methode permettant de recuperer l'integralité de la liste des snapshots et d'en faire des objets "User"
     *
     * @param documentSnapshot
     */
    private void readDataInList(final List<DocumentSnapshot> documentSnapshot) {

        // un DocumentReference fait référence à un emplacement de document dans une base de données Firestore et peut être utilisé pour
        // écrire, lire ou écouter l'emplacement. Il peut exister ou non un document à l'emplacement référencé.
        for (int i = 0; i < documentSnapshot.size(); i++) {
            DocumentSnapshot doc = documentSnapshot.get(i); //Un DocumentSnapshot contient des données lues à partir d'un document dans votre base de données Firestore.
            User user = new User(doc.getId(), doc.get("nom").toString(), doc.get("prenom").toString(), doc.get("licence").toString(), doc.get("email").toString(),
                    doc.get("niveau").toString(), doc.get("fonction").toString());
        }
    }
}
