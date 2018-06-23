package fr.drochon.christian.taaroaa.controller;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import butterknife.BindView;
import fr.drochon.christian.taaroaa.R;
import fr.drochon.christian.taaroaa.auth.AccountCreateActivity;

public class SummaryActivity extends AppCompatActivity {

    Button mAdherent;
    Button mCours;
    Button mSortie;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary);
        mAdherent = findViewById(R.id.adherents_btn);
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
}
