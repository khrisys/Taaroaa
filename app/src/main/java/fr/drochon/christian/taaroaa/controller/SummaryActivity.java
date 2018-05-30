package fr.drochon.christian.taaroaa.controller;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import fr.drochon.christian.taaroaa.R;
import fr.drochon.christian.taaroaa.auth.AccountModificationActivity;
import fr.drochon.christian.taaroaa.auth.SearchUserActivity;
import fr.drochon.christian.taaroaa.base.BaseActivity;
import fr.drochon.christian.taaroaa.course.CoursesPupilsActivity;
import fr.drochon.christian.taaroaa.course.CoursesSupervisorsActivity;
import fr.drochon.christian.taaroaa.covoiturage.CovoiturageAccueilActivity;
import fr.drochon.christian.taaroaa.model.User;

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

        mCompte = findViewById(R.id.adherents_btn);
        mModifCompte = findViewById(R.id.modif_adherents_btn);

        configureToolbar();
        showPannelModification();
        giveToolbarAName(R.string.summary_name);


        // --------------------
        // LISTENERS
        // --------------------

        /*
        Affichage de l'activité de creation de compte ou de modification de compte en fonction de l'existence
        en bdd ou non de l'utilisateur connecté
         */
        mCompte.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              /*  Intent intent = new Intent(SummaryActivity.this, AccountCreateActivity.class);
                startActivity(intent);*/
                FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

                setupDb().collection("users").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot documentSnapshots) {
                        //int compteur = 0;
                        if (documentSnapshots.size() != 0) {
                            List<DocumentSnapshot> ds = documentSnapshots.getDocuments();
                            for (DocumentSnapshot doc : ds) {
                                Map<String, Object> user = doc.getData();
                                //TODO mauvaise condition : ne passe dans aucune des 2 et la creation d'un user ici n'a plus lieu d'etre

                                // Si l'user connecté existe en bdd, on recupere l'ensemble de l'objet user et on le passe en param de l'activité
                                assert user != null;
                                if (user.get("fonction") != null && user.get("uid").equals(Objects.requireNonNull(getCurrentUser()).getUid())) {
                                    User u = new User(user.get("uid").toString(), user.get("nom").toString(), user.get("prenom").toString(), user.get("licence").toString(),
                                            user.get("email").toString(), user.get("niveau").toString(), user.get("fonction").toString());
                                    Intent intent = new Intent(SummaryActivity.this, AccountModificationActivity.class).putExtra("user", u);
                                    startActivity(intent);

                                    break;
                                }
                                // Si l'user connecté vient juste de creer son compte, on recupere ses seules infos disponibles et on les passe en param de l'activité de modif d'un compte
                                else if (user.get("fonction") == null) {
                                    User u = new User(user.get("uid").toString(), user.get("nom").toString(), user.get("prenom").toString(), user.get("email").toString());
                                    Intent intent = new Intent(SummaryActivity.this, AccountModificationActivity.class).putExtra("user", u);
                                    startActivity(intent);
                                    break;
                                }
                         /*       compteur++;

                                // nouvel utilisateur
                                if (compteur > documentSnapshots.size()) {
                                    User u = new User(user.get("uid").toString(), user.get("nom").toString(), user.get("prenom").toString(), user.get("email").toString());
                                    Intent intent = new Intent(SummaryActivity.this, AccountCreateActivity.class);
                                    startActivity(intent);
                                }
*/
                            }
                        }
                    }
                });

            }
        });


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
                setupDb().collection("users").document(Objects.requireNonNull(getCurrentUser()).getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            Map<String, Object> user = documentSnapshot.getData();
                            assert user != null;
                            User user1 = new User(user.get("uid").toString(), user.get("nom").toString(), user.get("prenom").toString(), user.get("licence").toString(),
                                    user.get("email").toString(), user.get("niveau").toString(), user.get("fonction").toString());
                            if (user.get("uid").equals(Objects.requireNonNull(getCurrentUser()).getUid()) &&
                                    user.get("fonction").equals("Moniteur")) {
                                Intent intent = new Intent(SummaryActivity.this, CoursesSupervisorsActivity.class);
                                startActivity(intent);
                            } else {
                                Intent intent = new Intent(SummaryActivity.this, CoursesPupilsActivity.class).putExtra("user", user1);
                                startActivity(intent);
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
        DocumentReference documentReference = setupDb().collection("users").document(Objects.requireNonNull(getCurrentUser()).getUid());
        documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    Map<String, Object> user = documentSnapshot.getData();
                    if (user.get("fonction") == null || !user.get("fonction").equals("Moniteur")) {
                        mModifCompte.setVisibility(View.GONE);
                    }
                    //lors de la creation d'un compte, enleve la tuile de modification d'un compte
                } else {
                    mModifCompte.setVisibility(View.GONE);
                }
            }
        });
    }
}
