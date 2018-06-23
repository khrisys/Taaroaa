package fr.drochon.christian.taaroaa.controller;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import fr.drochon.christian.taaroaa.R;

public class CoursesSupervisorsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_courses_supervisors);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setTitle("Planning encadrants");

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

}
