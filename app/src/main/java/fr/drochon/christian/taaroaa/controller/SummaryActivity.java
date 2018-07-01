package fr.drochon.christian.taaroaa.controller;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.perf.FirebasePerformance;
import com.google.firebase.perf.metrics.Trace;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import fr.drochon.christian.taaroaa.R;
import fr.drochon.christian.taaroaa.auth.AccountCreateActivity;
import fr.drochon.christian.taaroaa.auth.AccountModificationActivity;
import fr.drochon.christian.taaroaa.auth.SearchUserActivity;
import fr.drochon.christian.taaroaa.base.BaseActivity;
import fr.drochon.christian.taaroaa.course.CoursesPupilsActivity;
import fr.drochon.christian.taaroaa.course.CoursesSupervisorsActivity;
import fr.drochon.christian.taaroaa.covoiturage.CovoiturageAccueilActivity;
import fr.drochon.christian.taaroaa.model.User;

public class SummaryActivity extends BaseActivity {

    private Button mModifCompte;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setDisplayShowHomeEnabled(true);

        Button comptePerso = findViewById(R.id.adherents_btn);
        mModifCompte = findViewById(R.id.modif_adherents_btn);

        // Test performance de l'update d'user en bdd
        final Trace myTrace = FirebasePerformance.getInstance().newTrace("summaryActivityShowTiles_trace");
        myTrace.start();

        configureToolbar();
        showPannelModification();
        giveToolbarAName(R.string.summary_name);

        myTrace.stop();


        // --------------------
        // LISTENERS
        // --------------------

        /*
        Modification de compte de l'utilisateur connecté
         */
        comptePerso.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Test performance de l'update d'user en bdd
                final Trace myTrace1 = FirebasePerformance.getInstance().newTrace("summaryActivityGoToPersonnalAccountWithBundle_trace");
                myTrace1.start();

                setupDb().collection("users").addSnapshotListener(SummaryActivity.this, new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        if (queryDocumentSnapshots != null) {
                            if (queryDocumentSnapshots.size() != 0) {
                                List<DocumentSnapshot> ds = queryDocumentSnapshots.getDocuments();
                                for (DocumentSnapshot doc : ds) {
                                    Map<String, Object> user = doc.getData();

                                    if (user != null) {
                                        // Si l'user connecté existe en bdd, on recupere l'ensemble de l'objet user et on le passe en extra de l'intent
                                        if (user.get("uid").toString().equals(Objects.requireNonNull(getCurrentUser()).getUid())) {
                                            User u = new User(user.get("uid").toString(),
                                                    user.get("nom").toString(),
                                                    user.get("prenom").toString(),
                                                    user.get("licence").toString(),
                                                    user.get("email").toString(),
                                                    user.get("niveau").toString(),
                                                    user.get("fonction").toString(),
                                                    user.get("password").toString());
                                            Intent intent = new Intent(SummaryActivity.this, AccountModificationActivity.class).putExtra("summaryUser", u);
                                            startActivity(intent);

                                            myTrace1.stop();
                                            break;
                                        }
                                    }
                                }

                              }else startAccountActivity();
                        }
                    }
                });
            }
        });

        // recherche d'un adherent par un encadrant
        mModifCompte.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(SummaryActivity.this, SearchUserActivity.class);
                startActivity(intent);
            }
        });


        // redirige un utilisateur vers le planning des cours des eleves ou des encadrants en fonction de sa fonction
        Button cours = findViewById(R.id.cours_btn);
        cours.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Test performance de l'update d'user en bdd
                final Trace myTrace1 = FirebasePerformance.getInstance().newTrace("summaryActivityGoToCoursesDependingOnLevelCurrentUser_trace");
                myTrace1.start();

                final FirebaseUser auth = FirebaseAuth.getInstance().getCurrentUser();
                if (auth != null) {
                    setupDb().collection("users").document(auth.getUid()).addSnapshotListener(SummaryActivity.this, new EventListener<DocumentSnapshot>() {
                        @Override
                        public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                            if (documentSnapshot != null) {
                                Map<String, Object> user = documentSnapshot.getData();
                                if (user != null) {
                                    User user1 = new User(user.get("uid").toString(), user.get("nom").toString(), user.get("prenom").toString(), user.get("licence").toString(),
                                            user.get("email").toString(), user.get("niveau").toString(), user.get("fonction").toString(), user.get("password").toString());
                                    if (user.get("uid").equals(auth.getUid()) &&
                                            user.get("fonction").equals("Moniteur")) {
                                        Intent intent = new Intent(SummaryActivity.this, CoursesSupervisorsActivity.class);
                                        startActivity(intent);
                                    } else {
                                        Intent intent = new Intent(SummaryActivity.this, CoursesPupilsActivity.class).putExtra("user", user1);
                                        startActivity(intent);
                                    }

                                    myTrace1.stop();
                                }
                            }
                        }
                    });
                }
            }
        });


        // Redirige l'utilisateur vers les sorties
        Button sortie = findViewById(R.id.sorties_btn);
        sortie.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Test performance de l'update d'user en bdd
                final Trace myTrace2 = FirebasePerformance.getInstance().newTrace("summaryActivityGoToCovoiturages_trace");
                myTrace2.start();

                Intent intent = new Intent(SummaryActivity.this, CovoiturageAccueilActivity.class);
                startActivity(intent);

                myTrace2.stop();
            }
        });
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

    @Override
    public int getFragmentLayout() {
        return R.layout.activity_summary;
    }

    /**
     * Fonction permettant d'afficher ou non la tuile de moficiation d'un adherent si
     * la personne connectée est un encadrant
     */
    private void showPannelModification() {
        FirebaseUser auth = FirebaseAuth.getInstance().getCurrentUser();
        // changé apres Robo test firebase
        if (auth != null) {
            DocumentReference documentReference = setupDb().collection("users").document(auth.getUid());
            documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                    if (documentSnapshot != null) {
                        Map<String, Object> user = documentSnapshot.getData();
                        if (user != null) {
                            if (user.get("fonction") == null || !user.get("fonction").equals("Moniteur")) {
                                mModifCompte.setVisibility(View.GONE);
                            }
                            else if(user.get("fonction").equals("Moniteur")){
                                mModifCompte.setVisibility(View.VISIBLE);
                            }
                        }
                        //lors de la creation d'un compte, enleve la tuile de modification d'un compte
                    } else {
                        mModifCompte.setVisibility(View.GONE);
                    }
                }
            });
        }
    }

    private void startAccountActivity(){
        Intent intent = new Intent(SummaryActivity.this, AccountCreateActivity.class);
        startActivity(intent);
    }
}
