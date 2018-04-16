package fr.drochon.christian.taaroaa.course;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CalendarView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import fr.drochon.christian.taaroaa.R;
import fr.drochon.christian.taaroaa.base.BaseActivity;
import fr.drochon.christian.taaroaa.model.Course;

/**
 * 3 : creer l'ihm
 * Cette classe permet l'affichage IHM de l'ensemble des messages dans le chat. Contient une recycler view
 * On y implemente MentorChatActivity pour gerer la recyclerview
 * <p>
 * 12
 * On implement aussi un Listener qui nous permet d'etre alerter si la liste de message est vide grace au callback onDataChanged()
 * pour afficher un message à l'user
 */
public class CoursesPupilsActivity extends BaseActivity implements AdapterCoursesPupils.Listener {

    // CONTIENT LA RECYCLERVIEW

    // FOR DESIGN
    CoordinatorLayout mCoordinatorLayout;
    String calendrier;
    LinearLayout mLinearLayout;

    CalendarView mCalendarView;
    RecyclerView recyclerView;
    TextView mTextView;
    ScrollView mScrollView;
    FloatingActionButton mFloatingActionButton;

    // FOR DATA
    private AdapterCoursesPupils mAdapterCoursesPupils;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_courses_pupils);
        //setContentView(R.layout.list_cell);

        // FOR DESIGN
        // recuperation des var des objets graphiques du layout correspondant
        mCoordinatorLayout = findViewById(R.id.coordinator_layout_root);
        mLinearLayout = findViewById(R.id.linearLayoutRoot);
        mCalendarView = findViewById(R.id.calendrier_eleves);
        recyclerView = findViewById(R.id.recyclerViewCoursesPupils); // liste des cours
        mTextView = findViewById(R.id.empty_list_textview);
        mScrollView = findViewById(R.id.scrollviewRecyclerView);
        mFloatingActionButton = findViewById(R.id.fab);

        configureRecyclerView();
        configureToolbar();

        //showCourses();


        // --------------------
        // LISTENERS
        // --------------------

        // bouton d'ajout de cours pour les encadrants : renvoi vers la page de gestion des cours si on clique sur l'icone
        mFloatingActionButton.setOnClickListener(new View.OnClickListener() {
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
                Calendar calendar = Calendar.getInstance();
                calendar.set(year, month, dayOfMonth);
                Date d = calendar.getTime();
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                calendrier = sdf.format(calendar);
            }
        });
    }


    @Override
    public int getFragmentLayout() {
        return R.layout.activity_courses_pupils;
    }

    /**
     * Methode utilisée lorsque l'ecran est de nouveau appellé apres avoir été mis au second plan
     */
    @Override
    protected void onResume() {
        super.onResume();
        //showCourses();
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
        return optionsToolbar(this, item);
    }

    // --------------------
    // CALLBACK
    // --------------------

    /**
     * 13
     * Permet d'afficher un message à l'user s'il n'y a pas de messages
     */
    @Override
    public void onDataChanged() {
        mTextView.setVisibility(this.mAdapterCoursesPupils.getItemCount() == 0 ? View.VISIBLE : View.INVISIBLE);
    }

    // --------------------
    // UI
    // --------------------

    /**
     * 10
     * Configuration de la recyclerview
     * Cette methode créé l'adapter et lui passe en param pas mal d'informations 'comme par ex l'objet FireStoreRecyclerOptions generé par la methode
     * generateOptionsForAdapter.
     */
    private void configureRecyclerView() {
        //Track current chat name
        //this.currentChatName = chatName;
        //Configure Adapter & RecyclerView

        //queryAllCourses();

        mAdapterCoursesPupils = new AdapterCoursesPupils(generateOptionsForAdapter(queryAllCourses()), this);

        mAdapterCoursesPupils.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) { // c'est cette ligne de code qui insere les données dans la recyclerview
                recyclerView.smoothScrollToPosition(mAdapterCoursesPupils.getItemCount()); // Scroll to bottom on new messages
            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(this)); // layoutmanager indique comment seront positionnés les elements (linearlayout)
        recyclerView.setAdapter(this.mAdapterCoursesPupils);// l'adapter s'occupe du contenu
    }

    private Query queryAllCourses() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Query mq = CourseHelper.getCoursesCollection().document().collection("users");
        Query mQuery = db.collection("courses");
        mQuery.addSnapshotListener(this, new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                // condition de creation d'un user ou affichage simple d'un message indiquant que l'user existe dejà en bdd.
                // Avec les uid, il ne peut y avoir de doublon.
                if (documentSnapshots.size() != 0) {
                    Log.e("TAG", "Le document existe !");
                    // remplir la liste
                    //for (int i = 0; i < documentSnapshots.size(); i++)
                        readDataInList(documentSnapshots.getDocuments());
                }

            }
        });
        return mQuery;
    }

    private void readDataInList(final List<DocumentSnapshot> documentSnapshot) {
        //DocumentReference docRef1 = FirebaseFirestore.getInstance().collection("courses").document(documentSnapshot.); // recup ref de l'obj courant en bdd de stockage
        // un DocumentReference fait référence à un emplacement de document dans une base de données Firestore et peut être utilisé pour
        // écrire, lire ou écouter l'emplacement. Il peut exister ou non un document à l'emplacement référencé.
        for (int i = 0; i < documentSnapshot.size(); i++) {
            DocumentSnapshot doc = documentSnapshot.get(i); //Un DocumentSnapshot contient des données lues à partir d'un document dans votre base de données Firestore.
            String uid = doc.getId();
            String niveauDuCours = (String) doc.get("niveauDuCours");
            String nomDuMoniteur = (String) doc.get("nomDuMoniteur");
            String sujetDuCours = (String) doc.get("sujetDuCours");
            String typeCours = (String) doc.get("typeCours");
            Date dateDucours = (Date) doc.get("dateDuCours");

            Course course = new Course(uid);
            course.setUid(uid);
            course.setNiveauDuCours(niveauDuCours);
            course.setNomDuMoniteur(nomDuMoniteur);
            course.setSujetDuCours(sujetDuCours);
            course.setTypeCours(typeCours);
            course.setDateDuCours(dateDucours);


/*            PupilsViewHolder pupilsViewHolder = new PupilsViewHolder(recyclerView);
            pupilsViewHolder.updateWithCourse(course);*/
        }
    }


    /**
     * 11
     * La methode generateOptionsForAdapter utilise la methode query, precedemment definit dans la classe MessageHelper
     * permettra à la recyclerview d'afficher en temps reel le resultat de cette requete (la liste des derniers messages du chat correspondant, soit android/firebase/bug).
     */
    private FirestoreRecyclerOptions<Course> generateOptionsForAdapter(Query query) {
        return new FirestoreRecyclerOptions.Builder<Course>()
                .setQuery(query, Course.class)
                .setLifecycleOwner(this)
                .build();
    }
}
