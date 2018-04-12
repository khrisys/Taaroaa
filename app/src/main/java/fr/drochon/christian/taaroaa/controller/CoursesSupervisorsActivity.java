package fr.drochon.christian.taaroaa.controller;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import fr.drochon.christian.taaroaa.R;
import fr.drochon.christian.taaroaa.base.BaseActivity;

public class CoursesSupervisorsActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_courses_supervisors);
        configureToolbar();

        //setTitle("Planning encadrants");

        // liste des cours en bas de l'ecran
        final RecyclerView recyclerView = findViewById(R.id.recyclerView);
        // layoutmanager indique comment seront positionnés les elements (linearlayout)
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        // l'adapter s'occupe du contenu
        recyclerView.setAdapter(new AdapterCoursesPupils());

        // bouton d'ajout de cours pour les encadrants : renvoi vers la page de gestion des cours si on clique sur l'icone
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();*/
                Intent intent = new Intent(CoursesSupervisorsActivity.this, CoursesManagementActivity.class);
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
        getMenuInflater().inflate(R.menu.course_supervisors_menu, menu);
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
            case R.id.app_bar_summary:
                setTitle("sommaire appelé");
                return true;
            case R.id.app_bar_deconnexion:
                setTitle("Deconnexion appelé");
                return true;
           /* case R.id.app_bar_search:
                setTitle("search activé");
                return true;*/
        }
        return false;
    }

}
