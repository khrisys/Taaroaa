package fr.drochon.christian.taaroaa.covoiturage;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import fr.drochon.christian.taaroaa.R;
import fr.drochon.christian.taaroaa.base.BaseActivity;

public class CovoiturageConducteursActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_covoiturage_conducteurs);
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
