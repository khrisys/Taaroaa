package fr.drochon.christian.taaroaa.course;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CalendarView;
import android.widget.TextView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.perf.FirebasePerformance;
import com.google.firebase.perf.metrics.Trace;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import fr.drochon.christian.taaroaa.R;
import fr.drochon.christian.taaroaa.base.BaseActivity;
import fr.drochon.christian.taaroaa.model.Course;

/**
 * creer l'ihm
 * Cette classe permet l'affichage IHM de l'ensemble des messages dans le chat. Contient une recycler view
 * On y implemente MentorChatActivity pour gerer la recyclerview
 * <p>
 * On implement aussi un Listener qui nous permet d'etre alerter si la liste de message est vide grace au callback onDataChanged()
 * pour afficher un message à l'user
 */
public class CoursesSupervisorsActivity extends BaseActivity implements AdapterCoursesSupervisors.Listener {

    // CONTIENT LA RECYCLERVIEW


    private RecyclerView recyclerView;
    private TextView mTextView;

    // FOR DATA
    //Configure Adapter & RecyclerView
    private AdapterCoursesSupervisors mAdapterCoursesSupervisors = new AdapterCoursesSupervisors(generateOptionsForAdapter(queryAllCourses()), this);
    private Date calendrierClique;
    private Date calendrierFinJournee;


    // --------------------
    // LIFECYCLE
    // --------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_courses_supervisors);

        // FOR DESIGN
        // recuperation des var des objets graphiques du layout correspondant
        CalendarView calendarView = findViewById(R.id.calendrier_supervisors);
        recyclerView = findViewById(R.id.recyclerViewCoursesSupervisors); // liste des cours
        mTextView = findViewById(R.id.empty_list_textview_supervisors);
        FloatingActionButton floatingActionButton = findViewById(R.id.fab_supervisors);

        calendrierClique = new Date();
        calendrierFinJournee = new Date();

        // Test performance de l'update d'user en bdd
        final Trace myTrace = FirebasePerformance.getInstance().newTrace("coursesSupervisorsActivityShowAllCourses_trace");
        myTrace.start();

        configureRecyclerView();
        configureToolbar();
        giveToolbarAName(R.string.course_supervisors_name);
        myTrace.stop();

        // --------------------
        // LISTENERS
        // --------------------

        // bouton d'ajout de cours pour les encadrants : renvoi vers la page de gestion des cours si on clique sur l'icone
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CoursesSupervisorsActivity.this, CoursesManagementActivity.class);
                startActivity(intent);
            }
        });

        // recuperation de la date cliquée
        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                // Test performance de l'update d'user en bdd
                final Trace myTrace1 = FirebasePerformance.getInstance().newTrace("coursesSupervisorsActivityShowFilteredCourses_trace");
                myTrace1.start();

                Calendar calendar = Calendar.getInstance();
                calendar.set(year, month, dayOfMonth);
                // formattage de la date pour le debut et la fin de journée
                DateFormat dateFormatEntree = new SimpleDateFormat("dd MM yyyy", Locale.FRANCE);
                DateFormat dateFormatSortie = new SimpleDateFormat("dd MM yyyy HH:mm:ss", Locale.FRANCE);
                String s = dateFormatEntree.format(calendar.getTime());
                String ss = s.concat(" 00:00:00");
                String sss = s.concat(" 23:59:59");
                try {
                    calendrierClique = dateFormatSortie.parse(ss);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                try {
                    calendrierFinJournee = dateFormatSortie.parse(sss);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                configureRecyclerViewSorted();
                myTrace1.stop();
            }
        });
    }

    @Override
    public int getFragmentLayout() {
        return R.layout.activity_courses_supervisors;
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
        getMenuInflater().inflate(R.menu.course_supervisors_menu, menu);
        return true; // true affiche le menu
    }

    /**
     * recuperation  du clic d'un user.
     * On utilise un switch ici car il peut y avoir plusieurs options.
     * Surtout ne pas oublier le "true" apres chaque case sinon, ce sera toujours le dernier case qui sera executé!
     *
     * @return item
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return optionsToolbar(this, item);
    }

    // --------------------
    // CALLBACK
    // --------------------

    /**
     * Permet d'afficher un message à l'user s'il n'y a pas de messages
     */
    @Override
    public void onDataChanged() {
        mTextView.setVisibility(this.mAdapterCoursesSupervisors.getItemCount() == 0 ? View.VISIBLE : View.INVISIBLE);
    }

    // --------------------
    // ADAPTER ET RECYCLERVIEW
    // --------------------

    /**
     * Configuration de la recyclerview
     * Cette methode créé l'adapter et lui passe en param pas mal d'informations 'comme par ex l'objet FireStoreRecyclerOptions generé par la methode
     * generateOptionsForAdapter.
     */
    private void configureRecyclerView() {

        mAdapterCoursesSupervisors.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) { // c'est cette ligne de code qui insere les données dans la recyclerview
                recyclerView.smoothScrollToPosition(mAdapterCoursesSupervisors.getItemCount()); // Scroll to bottom on new messages
            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(this)); // layoutmanager indique comment seront positionnés les elements (linearlayout)
        recyclerView.setAdapter(this.mAdapterCoursesSupervisors);// l'adapter s'occupe du contenu
    }

    /**
     * Configuration de l'adapter et de la recyclerview
     * Cette methode créé l'adapter et lui passe en param la requete de tri des cours en fonction
     * de la date cliquée sur le calendrier
     */
    private void configureRecyclerViewSorted() {
        mAdapterCoursesSupervisors = new AdapterCoursesSupervisors(generateOptionsForAdapter(queryCoursesFiltered()), this);
        mAdapterCoursesSupervisors.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) { // c'est cette ligne de code qui insere les données dans la recyclerview
                recyclerView.smoothScrollToPosition(mAdapterCoursesSupervisors.getItemCount()); // Scroll to bottom on new messages
            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(this)); // layoutmanager indique comment seront positionnés les elements (linearlayout)
        recyclerView.setAdapter(this.mAdapterCoursesSupervisors);// l'adapter s'occupe du contenu
        onDataChanged(); // appel explicite du callback pour l'affichage d'un message en cas d'absence de cours à la date cliquée
    }


    /**
     * La methode generateOptionsForAdapter utilise la methode query, precedemment definit dans la classe MessageHelper
     * permettra à la recyclerview d'afficher en temps reel le resultat de cette requete (la liste des derniers messages du chat correspondant, soit android/firebase/bug).
     */
    private FirestoreRecyclerOptions<Course> generateOptionsForAdapter(Query query) {
        return new FirestoreRecyclerOptions.Builder<Course>()
                .setQuery(query, Course.class)
                .setLifecycleOwner(this)
                .build();
    }


    // --------------------
    // REQUETES
    // --------------------

    /**
     * Requete en bdd pour recuperer tous les cours existants
     *
     * @return query
     */
    private Query queryAllCourses() {
        // Test performance de l'update d'user en bdd
        final Trace myTrace2 = FirebasePerformance.getInstance().newTrace("coursesSupervisorsActivityAllCoursesQuerys_trace");
        myTrace2.start();

        Query mQuery = setupDb().collection("courses")
                .orderBy("horaireDuCours", Query.Direction.ASCENDING)
                .whereGreaterThanOrEqualTo("horaireDuCours", Calendar.getInstance().getTime());
        mQuery.addSnapshotListener(this, new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                if (documentSnapshots != null && documentSnapshots.size() != 0) {
                    Log.e("TAG", "Le document existe !");
                    // liste des docs
                    readDataInList(documentSnapshots.getDocuments());

                    myTrace2.stop();
                }
            }
        });
        return mQuery;
    }

    /**
     * Methode permettant de requeter avec les conditions suivantes :
     * n'affiche que les cours de la personne connectée + n'affiche que les cours du jour de la date cliquée
     *
     * @return query
     */
    private Query queryCoursesFiltered() {
        // Test performance de l'update d'user en bdd
        final Trace myTrace3 = FirebasePerformance.getInstance().newTrace("coursesSupervisorsActivityFilteredCoursesQuery_trace");
        myTrace3.start();
        Query mQ = setupDb().collection("courses").orderBy("horaireDuCours").startAt(calendrierClique).endAt(calendrierFinJournee);
        mQ.addSnapshotListener(this, new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                if (documentSnapshots != null) {
                    if (documentSnapshots.size() != 0) {
                        readDataInList(documentSnapshots.getDocuments());
                        myTrace3.stop();
                    }
                }
            }
        });
        return mQ;
    }

    /**
     * Methode permettant de recuperer l'integralité de la liste des snapshots et d'en faire des objets "Course"
     *
     * @param documentSnapshot donnees recuperees de la requete de recherche de l'utilisateur connecté
     */
    private void readDataInList(final List<DocumentSnapshot> documentSnapshot) {
        for (int i = 0; i < documentSnapshot.size(); i++) {
            //Un DocumentSnapshot contient des données lues à partir d'un document dans votre base de données Firestore.
            DocumentSnapshot doc = documentSnapshot.get(i);
            String uid = doc.getId();
            String niveauDuCours = (String) doc.get("niveauDuCours");
            String nomDuMoniteur = (String) doc.get("nomDuMoniteur");
            String sujetDuCours = (String) doc.get("sujetDuCours");
            String typeCours = (String) doc.get("typeCours");
            Date horaireDuCours = (Date) doc.get("horaireDuCours");

            Course course = new Course(uid);
            course.setUid(uid);
            course.setNiveauDuCours(niveauDuCours);
            course.setNomDuMoniteur(nomDuMoniteur);
            course.setSujetDuCours(sujetDuCours);
            course.setTypeCours(typeCours);
            course.setHoraireDuCours(horaireDuCours); // format date
        }
    }
}
