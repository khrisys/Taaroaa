package fr.drochon.christian.taaroaa.controller;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CalendarView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import fr.drochon.christian.taaroaa.R;
import fr.drochon.christian.taaroaa.base.BaseActivity;

public class CoursesPupilsActivity extends BaseActivity {

    String calendrier;
    Calendar calendar;
    CalendarView mCalendarView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_courses_pupils);

        ActionBar ab = getSupportActionBar();
        // ajout d'un icone de l'appli à l'actionbar en haut à gauche
        ab.setDisplayShowHomeEnabled(true);
        ab.setIcon(R.mipmap.ic_launcher);
        //ab.setTitle("Cours Prépa Niveau *");

        mCalendarView = findViewById(R.id.calendrier_eleves);


        // liste des cours en bas de l'ecran
        final RecyclerView recyclerView = findViewById(R.id.recyclerView);
        // layoutmanager indique comment seront positionnés les elements (linearlayout)
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        // l'adapter s'occupe du contenu
        recyclerView.setAdapter(new AdapterCoursesPupils());
        showCourses();


        // --------------------
        // LISTENERS
        // --------------------

        // bouton d'ajout de cours pour les encadrants : renvoi vers la page de gestion des cours si on clique sur l'icone
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               /* Snackbar.make(view, "Redirection vers la page de gestion des cours", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();*/
                Intent intent = new Intent(CoursesPupilsActivity.this, CoursesManagementActivity.class);
                startActivity(intent);
            }
        });

        // recuperation de la date cliquée
        mCalendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                calendar = Calendar.getInstance();
                calendar.set(year, month, dayOfMonth);
                Date d = calendar.getTime();
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                //calendrier = sdf.format(calendar);
            }
        });
    }

    /**
     * Methode utilisée lorsque l'ecran est de nouveau appellé apres avoir été mis au second plan
     */
    @Override
    protected void onResume() {
        super.onResume();
        showCourses();
    }

    // --------------------
    // TOOLBAR
    // --------------------

    /**
     * Fait appel au fichier xml menu pour definir les icones du menu toolbar.
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
        switch (item.getItemId()) {
            case R.id.app_bar_summary:
                setTitle("sommaire appelé");
                return true;
            case R.id.app_bar_deconnexion:
                setTitle("deconnexion appelée");
                return true;
           /* case R.id.app_bar_search:
                setTitle("search activé");
                return true;*/
        }
        return false;
    }


    private void showCourses(){
        Date dayOfCourse;
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        //Sixth sixth = new Sixth();
        for(int i = 0 ; i <recyclerView.getAdapter().getItemCount(); i++) {

        }
    }

}
