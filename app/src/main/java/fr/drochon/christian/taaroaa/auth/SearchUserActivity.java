package fr.drochon.christian.taaroaa.auth;

import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import fr.drochon.christian.taaroaa.R;
import fr.drochon.christian.taaroaa.base.BaseActivity;

public class SearchUserActivity extends BaseActivity implements AdapterSearchedUser.Listener {

    TextView mEmptyListMessage;
    RecyclerView mRecyclerViewUser;
    SearchView mSearchView;
    Button mButtonSearch;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_user);

        mEmptyListMessage = findViewById(R.id.empty_list_textview);
        mRecyclerViewUser = findViewById(R.id.recyclerViewSearchedUser);
        mSearchView = findViewById(R.id.searchbar_user);
        mButtonSearch = findViewById(R.id.search_valid_btn);

        configureToolbar();

        // --------------------
        // LISTENERS
        // --------------------

        mSearchView.setQueryHint("Saisissez un adhérent");
        mSearchView.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        mSearchView.setEnabled(true);

        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String name) {
                System.out.println("getclic");
                // decomposition du nom et du prenom recu dans le param name
                String nom = null, prenom = null;
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
                showSearchedDatas(name, prenom);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                System.out.println(newText);
                return false;
            }
        });

        mButtonSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO la rechecreh ? A voir si ce n'est pas directement la barre de rehcerche qui lance la recherche
                System.out.println("clic");
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
        //getMenuInflater().inflate(R.menu.covoit_search_vehicle_menu, menu);

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
    // SEARCH
    // --------------------

    /**
     * Methode permettant de chercher un adherent dans la bdd par son nom et son prenom
     *
     * @param name
     * @param prenom
     */
    private void showSearchedDatas(String name, String prenom) {
        //TODO
    }

    @Override
    public void onDataChanged() {

    }
}
