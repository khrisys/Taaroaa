package fr.drochon.christian.taaroaa.course;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.DialogFragment;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;

import fr.drochon.christian.taaroaa.R;
import fr.drochon.christian.taaroaa.api.CourseHelper;
import fr.drochon.christian.taaroaa.base.BaseActivity;
import fr.drochon.christian.taaroaa.model.Course;

import static fr.drochon.christian.taaroaa.api.CourseHelper.getCoursesCollection;
import static java.util.Calendar.MINUTE;

public class CoursesManagementActivity extends BaseActivity {

    // identifiant pour identifier la requete REST
    private static final int SIGN_OUT_TASK = 10;
    private static final int DELETE_USER_TASK = 20;
    private static final int UPDATE_USERNAME = 30;

    // id objets graphiques
    @SuppressLint("StaticFieldLeak")
    static TextInputEditText mHeureCours;
    @SuppressLint("StaticFieldLeak")
    static TextInputEditText mDateCours;
    TextInputEditText mMoniteurCours;
    TextInputEditText mSujetCours;
    Spinner mTypeCours;
    Spinner mNiveauCours;
    Button mCreerCours;

    // --------------------
    // CYCLE DE VIE
    // --------------------

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_courses_management);

        mMoniteurCours = findViewById(R.id.monitor_txt);
        mSujetCours = findViewById(R.id.sujet_txt);
        mTypeCours = findViewById(R.id.type_cours_spinner);
        mNiveauCours = findViewById(R.id.niveau_plongee_spinner);
        mCreerCours = findViewById(R.id.creation_compte_btn);
        mDateCours = findViewById(R.id.dateText);

        configureToolbar();
        giveToolbarAName(R.string.course_management_name);


        // hint pour la date du edittext
        Date currentTime = Calendar.getInstance().getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("EEE dd MMM yyyy", Locale.FRANCE);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        String d = sdf.format(currentTime);
        mDateCours.setHint(d);

        //hint pour l'heure du edittext
        mHeureCours = findViewById(R.id.timeText);
        SimpleDateFormat sdf1 = new SimpleDateFormat("HH:mm:ss", Locale.FRANCE);
        sdf1.setTimeZone(TimeZone.getTimeZone("GMT"));
        String s = sdf1.format(currentTime);
        mHeureCours.setHint(s);

        // --------------------
        // SPINNERS & REMPLISSAGE
        // --------------------
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapterNiveau = ArrayAdapter.createFromResource(this,
                R.array.type_course_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapterNiveau.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        mTypeCours.setAdapter(adapterNiveau);

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.niveaux_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        mNiveauCours.setAdapter(adapter);


        // --------------------
        // LISTENERS
        // --------------------

        mDateCours.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog(v);

                //if(mDateCours.getText().toString().isEmpty()) mDateCours.setError("Merci de saisir ce champ !"); else mDateCours.append(" ");
            }
        });

        mHeureCours.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimePickerDialog(v);
            }
        });

        mCreerCours.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                //TODO verification d'un id existant dans la bdd et si c'est le cas, remplissage des champs de l'ecran cours +  A voir si il ne vaut mieux pas mettre toujours la fonction create, parce que soit, ca creera le doc soit ca l'ecrasera
                createCourseInFirebase();
            }
        });
    }

    @Override
    public int getFragmentLayout() {
        return R.layout.activity_courses_management;
    }

    /**
     * Methode rappelant l'ecran lorsque celui ci revient au premier plan apres avoir été mis au second plan.
     */
    @Override
    protected void onResume() {
        super.onResume();
        this.updateUIWhenResuming(); // recuperation des informations du cours à updater ou deleter
    }


    // --------------------
    // TOOLBAR
    // --------------------

    /**
     * Fait appel au fichier xml menu pour definir les icones.
     * Definit differentes options dans le menu caché.
     *
     * @param menu
     * @return bool
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.course_management_menu, menu);
        return true;
    }

    /**
     * recuperation  du clic d'un user. On utilise un switch ici car il peut y avoir plusieurs options.
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
    // UI
    // --------------------

    /**
     * Methode permettant d'afficher les informations d'un user depuis la bdd firestore
     */
    private void updateUIWhenResuming() {
        // recuperation de l'id du cours
        String coursId = CourseHelper.getCoursesCollection().document().getId();
        // choix de l'affichage en fonction de la creation ou de l'update d'un doc
        createOrUpdateAffichage(coursId);
    }

    /**
     * Methode permettant d'afficher soit un bouton "creer un cours" s'il n'existe pas en bdd, soit d'afficher les informations
     * d'un cours en cas de modification d'un cours avec un bouton "modifier le cours"
     *
     * @param uid
     */
    private void createOrUpdateAffichage(final String uid) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Query mQuery = db.collection("courses").whereEqualTo("uid", uid);

        // RAJOUTER LE THIS DANS LE 1ER ARG DU LUSTENER PERMET DE RESTREINDRE LE CONTEXT A CETTE ACTIVITE, EVITANT AINSI DE METTRE LES DONNEES
        // A JOUR A CHAUQE FOIS QU'IL Y A UN UPDATE DANS TOUTE L'APP.
        // SI ON ENLEVE LE THIS, ON CREERA UN NOUVEAU DOCUMENT A CHAQUE FOIS QU'ON EN SUPPRIMERA UN, PAR EX !
        mQuery.addSnapshotListener(this, new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                // condition de creation d'un user ou affichage simple d'un message indiquant que l'user existe dejà en bdd.
                // Avec la generation d'uid aleatoire géré par firebase, il ne peut y avoir de doublon.
                if (documentSnapshots.size() == 1) {
                    Log.e("TAG", "Le document existe !");
                    // verification d'un id existant dans la bdd et si c'est le cas, remplissage des champs de l'ecran cours + changement de la phrase du bouton
                    mCreerCours.setText(R.string.button_update_course);

                    Course course = new Course(uid);
                    mMoniteurCours.setText(course.getNomDuMoniteur());
                    mSujetCours.setText(course.getSujetDuCours());
                    mTypeCours.setTag(course.getNiveauDuCours());
                    mNiveauCours.setTag(course.getNiveauDuCours());
                    //TODO normalement, l'ehure et la date doivent arriver decomposé depuis le clic d'un encadrant sur un item de la liste des cours
                    mDateCours.setText(course.getDateDuCours().toString());
                    mHeureCours.setText(course.getTimeDuCours().toString());
                } else {
                    mCreerCours.setText(R.string.button_create_course);

                }
            }
        });
    }

    /**
     * Methode permettant de renvoyer un encadrant sur sa page generale
     */
    private void startCoursesSupervisorsActivity() {
        Intent mIntent = new Intent(CoursesManagementActivity.this, CoursesSupervisorsActivity.class);
        startActivity(mIntent);
    }


    // --------------------
    // REST REQUETES
    // --------------------

    /**
     * Methode permettant la creation d'un cours dans le bdd. En cas d'insertion ou de probleme,
     * la fonction renverra une notification à l'utilisateur.
     */
    private void createCourseInFirebase() {

        // pas d'id pour un objet non créé
        final String id = CourseHelper.getCoursesCollection().document().getId();
        final String moniteur = mMoniteurCours.getText().toString();
        final String sujet = mSujetCours.getText().toString();
        final String typeCours = mTypeCours.getSelectedItem().toString();
        final String niveauCours = mNiveauCours.getSelectedItem().toString();
        final String dateCoursTxt = mDateCours.getText().toString();
        final String timeCoursTxt = mHeureCours.getText().toString();

        String horaireCours = dateCoursTxt + " " + timeCoursTxt;
        //final Date horaires = stringToDate(horaireCours);
        Date horaireDuCours = null;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.US);
        try {
            horaireDuCours = simpleDateFormat.parse(horaireCours);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        //verification de la coherence des champs de saisie date et heure
        if (horaireDuCours != null && horaireDuCours.before(Calendar.getInstance().getTime())) {
            final AlertDialog.Builder adb = new AlertDialog.Builder(CoursesManagementActivity.this);
            adb.setTitle("Date inccorecte");
            adb.setIcon(android.R.drawable.ic_delete);
            adb.setMessage("Le jour du départ ne peut pas être défini avant la date du jour !");
            adb.setPositiveButton("MODIFIER", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    mDateCours.setText("");
                    mDateCours.setError(null);
                    mHeureCours.setText("");
                    mHeureCours.setError(null);
                    mHeureCours.requestFocus();
                }
            });
            adb.show();
        } else {
            final FirebaseFirestore db = FirebaseFirestore.getInstance();

            if (!moniteur.isEmpty() && !sujet.isEmpty() && !mDateCours.getText().toString().isEmpty() && !mHeureCours.getText().toString().isEmpty()) {

                Map<String, Object> newCourse = new HashMap<>();
                newCourse.put("id", id);
                newCourse.put("niveauDuCours", niveauCours);
                newCourse.put("nomDuMoniteur", moniteur);
                newCourse.put("sujetDuCours", sujet);
                newCourse.put("typeCours", typeCours);
                newCourse.put("horaireDuCours", horaireDuCours);
                db.collection("courses").document(id).set(newCourse)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(CoursesManagementActivity.this, R.string.create_course,
                                        Toast.LENGTH_SHORT).show();
                                startCoursesSupervisorsActivity(); // renvoi l'encadrant sur la page de tous les cours
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(CoursesManagementActivity.this, "ERROR" + e.toString(),
                                        Toast.LENGTH_SHORT).show();
                                Log.d("TAG", e.toString());
                            }
                        });
            } else {
                verificationChampsVides();
            }
        }
    }

    /**
     * Methode permettant de signaler une erreur lorsqu'un champ est resté vide alors que la soumission du formulaire a été faite.
     */
    private void verificationChampsVides() {

        if (mDateCours.getText().toString().equals("")) {
            mDateCours.setError("Merci de renseigner ce champ !");
            mDateCours.requestFocus();
        } else mDateCours.setError(null);
        if (mHeureCours.getText().toString().equals("")) {
            mHeureCours.setError("Merci de renseigner ce champ !");
            mHeureCours.requestFocus();
        } else mHeureCours.setError(null);
        if (mSujetCours.getText().toString().isEmpty()) {
            mSujetCours.setError("Merci de renseigner ce champ !");
            mSujetCours.requestFocus();
        }
        if (mMoniteurCours.getText().toString().isEmpty()) {
            mMoniteurCours.setError("Merci de renseigner ce champ !");
            mMoniteurCours.requestFocus();
        }
    }

    /**
     * Cette methode ne comprend pas l'update d'une fonction dans le club, car seul les encadrants du club peuvent
     * le faire, et cette fonctionnalité est donc reservée à une fonction particuliere.
     */
    private void updateCourseInFirebase() {

        String id = getCoursesCollection().document().getId();
        String moniteur = mMoniteurCours.getText().toString();
        String sujet = mSujetCours.getText().toString();
        String typeCours = mTypeCours.getSelectedItem().toString();
        String niveauCours = mNiveauCours.getSelectedItem().toString();
        String dateCoursTxt = mDateCours.getText().toString();
        /*Date dateCours = null;
        try {
            dateCours = SimpleDateFormat.getDateInstance().parse(String.valueOf(mDateCours));
        } catch (ParseException e) {
            e.printStackTrace();
        }*/
        String heureCours = mHeureCours.getText().toString();
        String horaireCours = dateCoursTxt + " " + heureCours;
        Date horaireCoursFormat = null;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MM yyyy HH:mm:ss Z", Locale.FRANCE);
        try {
            horaireCoursFormat = simpleDateFormat.parse(horaireCours);
        } catch (ParseException e) {
            e.printStackTrace();
        }


        DocumentReference reference2 = getCoursesCollection().document(id);
        Query reference1 = CourseHelper.getCourse(reference2.getId());
        if (!moniteur.isEmpty() && !sujet.isEmpty() && !heureCours.isEmpty()) { // verification que tous les champs vides soient remplis
            CourseHelper.updateCourse(id, typeCours, sujet, niveauCours, moniteur, horaireCoursFormat)
                    .addOnFailureListener(this.onFailureListener())
                    .addOnSuccessListener(this.updateUIAfterRESTRequestsCompleted(UPDATE_USERNAME));
        }
    }

    /*    */

    /**
     * Methode permettant à un encadrant de supprimer un compte. Retourne un objet de type Task permettant de realiser ces appels de maniere asynchrone
     *//*
    private void deleteCourseFromFirebase() {
        if (this.getCurrentUser() != null) {

            //On supprime un utilisateur de la bdd firestore
            UserHelper.deleteUser(this.getCurrentUser().getUid()).addOnFailureListener(this.onFailureListener());
            //TODO mettre une notification si elle n'arrive pas avoir ajouté le deleteuser ci dessus
            AuthUI.getInstance()
                    .delete(this) // methode utilisée par le singleton authUI.getInstance()
                    .addOnSuccessListener(this, this.updateUIAfterRESTRequestsCompleted(DELETE_USER_TASK));
        }
    }*/


    // --------------------
    // DATETIMEPICKERS
    // --------------------
    public void showDatePickerDialog(View v) {
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getSupportFragmentManager(), "datePicker");
    }

    public void showTimePickerDialog(View v) {
        DialogFragment newFragment = new TimePickerFragment();
        newFragment.show(getSupportFragmentManager(), "timePicker");
    }

    // --------------------
    // CLASSES POUR PICKERS HEURE & DATE
    // --------------------

    /**
     * Classe static interne pour la date
     */
    public static class DatePickerFragment extends DialogFragment implements
            DatePickerDialog.OnDateSetListener {

        /**
         * Créé une instance de DatePicker et la renvoi
         *
         * @param savedInstanceState
         * @return
         */
        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);
            return new DatePickerDialog(Objects.requireNonNull(getActivity()), this, year, month, day + 1);
        }

        /**
         * Affiche la date choisi par l'utilisateur
         *
         * @param view       picker associé au dialog
         * @param year       annee
         * @param month      (0 à 11)
         * @param dayOfMonth jour du mois
         */
        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {

            //mDateCours.setText(mDateCours.getText() + "" + dayOfMonth + "-" + (month + 1) + "-" + year);
            mDateCours.setText(mDateCours.getText() + "" + dayOfMonth + "-" + (month + 1) + "-" + year);
        }
    }

    /**
     * Classe interne statique pour l'heure
     */
    public static class TimePickerFragment extends DialogFragment implements
            TimePickerDialog.OnTimeSetListener {

        /**
         * Créé une nouvelle instance d'un datepicket et la renvoi
         *
         * @param savedInstanceState
         * @return
         */
        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            //dateFormatGmt.format(hour);
            int minute = c.get(MINUTE);

            return new TimePickerDialog(getActivity(), this, hour, minute, DateFormat.is24HourFormat(getActivity()));
        }

        /**
         * Affichage de l'heure obtenue dans l'edittext
         *
         * @param view
         * @param hourOfDay
         * @param minute
         */
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            //mHeureCours.setText(mHeureCours.getText() + "" + hourOfDay + ":" + minute + ":00");
            mHeureCours.setText(mHeureCours.getText() + "" + hourOfDay + ":" + minute + ":00");
        }
    }
}
