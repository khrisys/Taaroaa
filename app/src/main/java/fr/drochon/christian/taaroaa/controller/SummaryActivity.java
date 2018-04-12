package fr.drochon.christian.taaroaa.controller;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import fr.drochon.christian.taaroaa.R;
import fr.drochon.christian.taaroaa.auth.AccountCreateActivity;
import fr.drochon.christian.taaroaa.base.BaseActivity;

public class SummaryActivity extends BaseActivity {

    Button mAdherent;
    Button mCours;
    Button mSortie;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary);
        configureToolbar();

        mAdherent = findViewById(R.id.adherents_btn);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(true);

        mAdherent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SummaryActivity.this, AccountCreateActivity.class);
                startActivity(intent);
            }
        });

        // redirige un utilisateur vers le planning des cours des eleves
        mCours = findViewById(R.id.cours_btn);
        mCours.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SummaryActivity.this, CoursesPupilsActivity.class);
                startActivity(intent);
            }
        });

        // Redirige l'utilisateur vers les sorties
        mSortie = findViewById(R.id.sorties_btn);
        mSortie.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SummaryActivity.this, CoursesSupervisorsActivity.class);
                startActivity(intent);
            }
        });
    }


    // --------------------
    // TOOLBAR
    // --------------------

    /**
     *  Fait appel au fichier xml menu pour definir les icones.
     * Definit differentes options dans le menu caché.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.summary_menu, menu);
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
        switch (item.getItemId()) {
            case R.id.app_bar_deconnexion:
                setTitle("switch activé");
                return true;
/*            case R.id.media_route_menu_item:
                setTitle("Cast appelé");
                return true;
            case R.id.app_bar_search:
                setTitle("search activé");
                return true;*/
        }
        return false;
    }
}
