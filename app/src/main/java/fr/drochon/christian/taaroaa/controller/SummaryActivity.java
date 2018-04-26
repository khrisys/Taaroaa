package fr.drochon.christian.taaroaa.controller;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import fr.drochon.christian.taaroaa.R;
import fr.drochon.christian.taaroaa.auth.AccountCreateActivity;
import fr.drochon.christian.taaroaa.auth.SearchUserActivity;
import fr.drochon.christian.taaroaa.base.BaseActivity;
import fr.drochon.christian.taaroaa.course.CoursesPupilsActivity;
import fr.drochon.christian.taaroaa.course.CoursesSupervisorsActivity;
import fr.drochon.christian.taaroaa.covoiturage.CovoiturageAccueilActivity;

public class SummaryActivity extends BaseActivity {

    Button mCompte;
    Button mModifCompte;
    Button mCours;
    Button mSortie;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary);

        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayShowHomeEnabled(true);

        configureToolbar();
        showPannelModification();


        mCompte = findViewById(R.id.adherents_btn);
        mCompte.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SummaryActivity.this, AccountCreateActivity.class);
                startActivity(intent);
            }
        });

        mModifCompte = findViewById(R.id.modif_adherents_btn);
        mModifCompte.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SummaryActivity.this, SearchUserActivity.class);
                startActivity(intent);
            }
        });


        // redirige un utilisateur vers le planning des cours des eleves ou des encadrants en fonction de sa fonction
        mCours = findViewById(R.id.cours_btn);
        mCours.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                db.collection("users").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot documentSnapshots) {
                        if (documentSnapshots.size() != 0) {
                            List<DocumentSnapshot> ds = documentSnapshots.getDocuments();
                            for (int i = 0; i < ds.size(); i++) {
                                Map<String, Object> user = ds.get(i).getData();
                                if (user.get("uid").equals(getCurrentUser().getUid()) &&
                                        user.get("fonction").equals("Moniteur")) {
                                    Intent intent = new Intent(SummaryActivity.this, CoursesSupervisorsActivity.class);
                                    startActivity(intent);
                                    break;
                                } else {
                                    Intent intent = new Intent(SummaryActivity.this, CoursesPupilsActivity.class);
                                    startActivity(intent);
                                }

                            }
                        }
                    }
                });
            }
        });


        // Redirige l'utilisateur vers les sorties
        mSortie = findViewById(R.id.sorties_btn);
        mSortie.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SummaryActivity.this, CovoiturageAccueilActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public int getFragmentLayout() {
        return R.layout.activity_summary;
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
        return optionsToolbar(this, item);
    }


    // --------------------
    // UI
    // --------------------

    /**
     * Fonction permettant d'afficher ou non la tuile de moficiation d'un adherent
     */
    private void showPannelModification() {
        //TODO afficher le graphique du panneau de modif si l'utilisateur connecté est un encadrant
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference documentReference = db.collection("users").document(Objects.requireNonNull(getCurrentUser()).getUid());
        documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(documentSnapshot.exists()){
                    Map<String, Object> user = documentSnapshot.getData();
                    if (user.get("fonction") == null || !user.get("fonction").equals("Moniteur")) {
                        mModifCompte.setVisibility(View.GONE);
                    }
                }
            }
        });
    }
}
