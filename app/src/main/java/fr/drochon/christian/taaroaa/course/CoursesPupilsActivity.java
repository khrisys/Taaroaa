package fr.drochon.christian.taaroaa.course;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
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
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

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
public class CoursesPupilsActivity extends BaseActivity implements AdapterCoursesPupils.Listener {

    // CONTIENT LA RECYCLERVIEW


    // FOR DESIGN
    CoordinatorLayout mCoordinatorLayout;
    Date calendrierClique;
    LinearLayout mLinearLayout;
    CalendarView mCalendarView;
    RecyclerView recyclerView;
    TextView mTextView;
    ScrollView mScrollView;
    FloatingActionButton mFloatingActionButton;

    // FOR DATA
    private AdapterCoursesPupils mAdapterCoursesPupils;
    List<DocumentSnapshot> listSnapshot;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_courses_pupils);

        // FOR DESIGN
        // recuperation des var des objets graphiques du layout correspondant
        mCoordinatorLayout = findViewById(R.id.pupils_layout_root);
        mLinearLayout = findViewById(R.id.linearLayoutRoot);
        mCalendarView = findViewById(R.id.calendrier_eleves);
        recyclerView = findViewById(R.id.recyclerViewCoursesPupils); // liste des cours
        mTextView = findViewById(R.id.empty_list_textview);
        mScrollView = findViewById(R.id.scrollviewRecyclerView);
        mFloatingActionButton = findViewById(R.id.fab);
        calendrierClique = new Date();
        listSnapshot = new ArrayList<DocumentSnapshot>();
        configureRecyclerView();
        configureToolbar();
        showFloatingButton();

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
                //DateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                calendrierClique = calendar.getTime();

            }
        });
    }


    @Override
    public int getFragmentLayout() {
        return R.layout.activity_courses_pupils;
    }


    // --------------------
    // TOOLBAR
    // --------------------

    /**
     * Fait appel au fichier xml menu pour definir les icones du menu toolbar.
     * Defini differentes options dans le menu caché.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.course_pupils_menu, menu);
        return true; // true affiche le menu
    }

    /**
     * recuperation  du clic d'un user sur une option de la toolbar.
     * On utilise un switch ici car il peut y avoir plusieurs options.
     * Surtout ne pas oublier le "true" apres chaque case sinon, ce sera toujours le dernier case qui sera executé!
     *
     * @param item
     * @return bool
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
        mTextView.setVisibility(this.mAdapterCoursesPupils.getItemCount() == 0 ? View.VISIBLE : View.INVISIBLE);
    }

    // --------------------
    // UI
    // --------------------

    /**
     * Configuration de la recyclerview
     * Cette methode créé l'adapter et lui passe en param pas mal d'informations 'comme par ex l'objet FireStoreRecyclerOptions generé par la methode
     * generateOptionsForAdapter.
     */
    private void configureRecyclerView() {

        // application du filtre sur la liste des cours
        filterDateCalendar();
        //Configure Adapter & RecyclerView
            mAdapterCoursesPupils = new AdapterCoursesPupils(generateOptionsForAdapter(queryAllCourses()), this);
            //mAdapterCoursesPupils = new AdapterCoursesPupils(generateOptionsForAdapter(queryDateCourses(calendrierClique)), this);
        mAdapterCoursesPupils.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) { // c'est cette ligne de code qui insere les données dans la recyclerview
                recyclerView.smoothScrollToPosition(mAdapterCoursesPupils.getItemCount()); // Scroll to bottom on new messages
            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(this)); // layoutmanager indique comment seront positionnés les elements (linearlayout)
        recyclerView.setAdapter(this.mAdapterCoursesPupils);// l'adapter s'occupe du contenu
    }


    /**
     * La methode generateOptionsForAdapter utilise la methode query, precedemment definit dans la classe MessageHelper
     * permettra à la recyclerview d'afficher en temps reel le resultat de cette requete (la liste de tous les cours (ou triés par date)).
     */
    private FirestoreRecyclerOptions<Course> generateOptionsForAdapter(Query query) {
        return new FirestoreRecyclerOptions.Builder<Course>()
                .setQuery(query, Course.class)
                .setLifecycleOwner(this)
                .build();
    }

    /**
     * Methode permettant d'afficher le floating button à l'ecran si l'utilisateur est un encadrant ou un initiateur.
     */
    private void showFloatingButton() {

        if (this.getCurrentUser() != null) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference mQuery = db.collection("users").document(getCurrentUser().getUid());

            mQuery.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                    if(documentSnapshot.exists()){
                        Object ds = documentSnapshot.get("fonction");
                        //TODO : decision : est ce que je met le bouton dispo pour les initiateurs?
                        if(ds.equals("Moniteur") || ds.equals("Initiateur"))
                            mFloatingActionButton.setVisibility(View.VISIBLE);
                    }
                }
            });
        }
    }

    public void notifCompleteAccount() {
        AlertDialog.Builder adb = new AlertDialog.Builder(this);
        adb.setIcon(android.R.drawable.ic_dialog_alert);
        adb.setTitle("Merci de completer votre compte pour acceder à la liste des cours !");
        adb.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // rien à appeler. pas la peine de faire de toast
            }
        });
        adb.show(); // affichage de l'artdialog
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
        // Affichage en fonction du niveau de la personne connectée
        //final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Query mQuery = db.collection("courses").orderBy("horaireDuCours", Query.Direction.ASCENDING);
        mQuery.addSnapshotListener(this, new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                // condition de creation d'un user ou affichage simple d'un message indiquant que l'user existe dejà en bdd.
                // Avec les uid, il ne peut y avoir de doublon, on peut donc etre sur qu'il n'y a qu'un seule doc qui existe s'il en existe un.
                if (documentSnapshots != null) {
                    if (documentSnapshots.size() != 0) {
                        Log.e("TAG", "Le document existe !");
                        // liste des docs
                        readDataInList(documentSnapshots.getDocuments());
                    }
                }
            }
        });
        return mQuery;
    }

    private Query queryDateCourses(Object dateCours) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Query mQ = db.collection("courses").whereEqualTo("horaireDuCours", dateCours);
        mQ.addSnapshotListener(this, new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                // condition de creation d'un user ou affichage simple d'un message indiquant que l'user existe dejà en bdd.
                // Avec les uid, il ne peut y avoir de doublon, on peut donc etre sur qu'il n'y a qu'un seule doc qui existe s'il en existe un.
                if (documentSnapshots != null) {
                    if (documentSnapshots.size() != 0) {
                        Log.e("TAG", "Le document existe !");
                        // liste des docs
                        listSnapshot.addAll(documentSnapshots.getDocuments());
                        readDataInList(listSnapshot);
                    }
                }
            }
        });
        return mQ;
    }

    /**
     * Methode permettant de recuperer l'integralité de la liste des snapshots et d'en faire des objets "Course"
     *
     * @param documentSnapshot
     */
    private void readDataInList(final List<DocumentSnapshot> documentSnapshot) {

        // un DocumentReference fait référence à un emplacement de document dans une base de données Firestore et peut être utilisé pour
        // écrire, lire ou écouter l'emplacement. Il peut exister ou non un document à l'emplacement référencé.
        for (int i = 0; i < documentSnapshot.size(); i++) {
            DocumentSnapshot doc = documentSnapshot.get(i); //Un DocumentSnapshot contient des données lues à partir d'un document dans votre base de données Firestore.
            String uid = doc.getId();
            String niveauDuCours = (String) doc.get("niveauDuCours");
            String nomDuMoniteur = (String) doc.get("nomDuMoniteur");
            String sujetDuCours = (String) doc.get("sujetDuCours");
            String typeCours = (String) doc.get("typeCours");
            Date horaireDucours = (Date) doc.get("horaireDuCours");

            Course course = new Course(uid);
            course.setUid(uid);
            course.setNiveauDuCours(niveauDuCours);
            course.setNomDuMoniteur(nomDuMoniteur);
            course.setSujetDuCours(sujetDuCours);
            course.setTypeCours(typeCours);
            course.setHoraireDuCours(horaireDucours);
        }
    }


    /**
     * Methode permettant de filtrer les cours à afficher sur l'ecran des eleves lorsqu'un utilisateur clique sur une date du calendrier.
     * Ce clic agit comme un filtre sur la liste des cours  à afficher.
     */
    private void filterDateCalendar() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        //Task<QuerySnapshot> mQuery = db.collection("courses").whereEqualTo("horaireDuCours", "2018-04-04").get();
        CollectionReference cr = db.collection("courses");
        cr.addSnapshotListener(this, new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                if (documentSnapshots != null && documentSnapshots.size() != 0) {
                    List<DocumentSnapshot> ds = documentSnapshots.getDocuments();
                    for (int i = 0; i < documentSnapshots.size(); i++) {
                        // recuperation des documents comprenant l'horaire du cours
                        DocumentSnapshot map = documentSnapshots.getDocuments().get(i);
                        Object horaireDuCours = map.getData().get("horaireDuCours");
                        // formatage des donnees recues de la bdd et du clic sur la calendrier et condition
                        DateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                        if (sdf.format(horaireDuCours).equals(sdf.format(calendrierClique))) {
                            System.out.println("ok");
                            queryDateCourses(horaireDuCours);
                            configureRecyclerView();
                        }
                    }
                }
            }
        });
    }
}
