package fr.drochon.christian.taaroaa.course;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.DialogFragment;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TimePicker;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.Query;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import fr.drochon.christian.taaroaa.R;
import fr.drochon.christian.taaroaa.api.CourseHelper;
import fr.drochon.christian.taaroaa.api.UserHelper;
import fr.drochon.christian.taaroaa.base.BaseActivity;
import fr.drochon.christian.taaroaa.model.Course;

import static fr.drochon.christian.taaroaa.api.CourseHelper.getCoursesCollection;

public class CoursesManagementActivity extends BaseActivity {

    // identifiant pour identifier la requete REST
    private static final int SIGN_OUT_TASK = 10;
    private static final int DELETE_USER_TASK = 20;
    private static final int UPDATE_USERNAME = 30;
    static EditText mHeureCours;
    static EditText mDateCours;
    TextInputEditText mMoniteurCours;
    TextInputEditText mSujetCours;
    Spinner mTypeCours;
    Spinner mNiveauCours;
    Button mCreerCours;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_courses_management);
        configureToolbar();

        mMoniteurCours = findViewById(R.id.monitor_txt);
        mSujetCours = findViewById(R.id.sujet_txt);
        mTypeCours = findViewById(R.id.type_cours_spinner);
        mNiveauCours = findViewById(R.id.niveau_plongee_spinner);
        mCreerCours = findViewById(R.id.creation_compte_btn);
        mDateCours = findViewById(R.id.dateText);
        mHeureCours = findViewById(R.id.timeText);

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
            }
        });

        mHeureCours.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimePickerDialog(v);
            }
        });

        mCreerCours.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // recuperation de l'id du cours
                String coursId = CourseHelper.getCoursesCollection().document().getId();

                //TODO verification d'un id existant dans la bdd et si c'est le cas, remplissage des champs de l'ecran cours + changement de la phrase du bouton ????
                mCreerCours.setText(R.string.button_create_course);
                createCourseInFirebase();
                /*if (CourseHelper.getCourse(coursId) == null) {
                    mCreerCours.setText(R.string.button_create_course);
                    createCourseInFirebase();
                } else {
                    mCreerCours.setText(R.string.button_update_course);
                    updateCourseInFirebase();
                }*/
                startCoursesSupervisorsActivity(); // renvoi l'encadrant sur la page de tous les cours
            }
        });
    }

    /**
     * Dispatch onResume() to fragments.  Note that for better inter-operation
     * with older versions of the platform, at the point of this call the
     * fragments attached to the activity are <em>not</em> resumed.  This means
     * that in some cases the previous state may still be saved, not allowing
     * fragment transactions that modify the state.  To correctly interact
     * with fragments in their proper state, you should instead override
     * {@link #onResumeFragments()}.
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
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.course_management_menu, menu);
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
                setTitle("Deconnexion appelé");
                return true;
           /* case R.id.app_bar_search:
                setTitle("search activé");
                return true;*/
        }
        return false;
    }

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

    /**
     * Methode permettant d'afficher les informations d'un user depuis la bdd firestore
     */
    private void updateUIWhenResuming() {


        // recuperation de l'id du cours
        String coursId = CourseHelper.getCoursesCollection().document().getId();

        // verification d'un id existant dans la bdd et si c'est le cas, remplissage des champs de l'ecran cours + changement de la phrase du bouton
        if (CourseHelper.getCourse(coursId) != null) {
            mCreerCours.setText(R.string.button_update_course);

            Course course = new Course(coursId);
            mMoniteurCours.setText(course.getNomDuMoniteur());
            mSujetCours.setText(course.getSujetDuCours());
            mTypeCours.setTag(course.getNiveauDuCours());
            mNiveauCours.setTag(course.getNiveauDuCours());
            //mDateCours.setText(course.getDateDuCours());


        }
    }

    /**
     * Methode permettant de renvoyer un encadrant sur sa page generale
     */
    private void startCoursesSupervisorsActivity() {
        Intent mIntent = new Intent(CoursesManagementActivity.this, CoursesSupervisorsActivity.class);
        startActivity(mIntent);
    }


    // --------------------
    // UI
    // --------------------

    private void createCourseInFirebase() {

        // pas d'id pour un objet non créé
        String id = CourseHelper.getCoursesCollection().document().getId();
        String moniteur = mMoniteurCours.getText().toString();
        String sujet = mSujetCours.getText().toString();
        String typeCours = mTypeCours.getSelectedItem().toString();
        String niveauCours = mNiveauCours.getSelectedItem().toString();
        String dateCours = mDateCours.getText().toString();
        String heureCours = mHeureCours.getText().toString();

        String horaireCours = dateCours + " " + heureCours;
        Date horaireCoursFormat = this.stringToDate(horaireCours); // changement de la string en date

       /* SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

        try {
            horaireCoursFormat = df.parse(horaireCours);
            df.setTimeZone(TimeZone.getTimeZone("UTC+2"));
            //System.out.println(horaireCoursFormat);
        } catch (ParseException e) {
            e.printStackTrace();
        }*/

        if (!moniteur.isEmpty() && !sujet.isEmpty() && !dateCours.isEmpty() && !heureCours.isEmpty()) {
            CourseHelper.createCourse(id, typeCours, sujet, niveauCours, moniteur, horaireCoursFormat);
            //TODO mettre un alertdialog pour notifier qu'un champ est vide
            //TODO apres la creation d'un cours, l'encadrant est redirigé vers l'ecran des encadrants
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
        String dateCours = mDateCours.getText().toString();
        String heureCours = mHeureCours.getText().toString();
        String horaireCours = dateCours + " " + heureCours;
        Date horaireCoursFormat = null;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MM yyyy HH:mm:ss", Locale.FRANCE);
        try {
            horaireCoursFormat = simpleDateFormat.parse(dateCours + " " + heureCours);
        } catch (ParseException e) {
            e.printStackTrace();
        }


        DocumentReference reference2 = getCoursesCollection().document(id);
        Query reference1 = CourseHelper.getCourse(reference2.getId());
        if (reference2 != null) {
            //TODO alert dialog lorsque tous les champs ne sont pas remplis
            if (!moniteur.isEmpty() && !sujet.isEmpty() && !dateCours.isEmpty() && !heureCours.isEmpty()) { // verification que tous les champs vides soient remplis
                CourseHelper.updateCourse(id, typeCours, sujet, niveauCours, moniteur, horaireCoursFormat)
                        .addOnFailureListener(this.onFailureListener())
                        .addOnSuccessListener(this.updateUIAfterRESTRequestsCompleted(UPDATE_USERNAME));
            }
        }
    }
    // --------------------
    // REST REQUETES
    // --------------------

    /**
     * Methode permettant à un encadrant de supprimer un compte. Retourne un objet de type Task permettant de realiser ces appels de maniere asynchrone
     */
    private void deleteCourseFromFirebase() {
        if (this.getCurrentUser() != null) {

            //On supprime un utilisateur de la bdd firestore
            UserHelper.deleteUser(this.getCurrentUser().getUid()).addOnFailureListener(this.onFailureListener());
            //TODO mettre une notification si elle n'arrive pas avoir ajouté le deleteuser ci dessus
            AuthUI.getInstance()
                    .delete(this) // methode utilisée par le singleton authUI.getInstance()
                    .addOnSuccessListener(this, this.updateUIAfterRESTRequestsCompleted(DELETE_USER_TASK));
        }
    }

    // --------------------
    // HEURE & DATE
    // --------------------

    /**
     * Classe static interne pour la date
     */
    public static class DatePickerFragment extends DialogFragment implements
            DatePickerDialog.OnDateSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();

            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);


            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), this, year, month, day + 1);
        }

        /**
         * @param view       the picker associated with the dialog
         * @param year       the selected year
         * @param month      the selected month (0-11 for compatibility with
         *                   {@link Calendar#MONTH})
         * @param dayOfMonth th selected day of the month (1-31, depending on
         */
        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
            // Do something with the date chosen by the user
            mDateCours.setText(mDateCours.getText() + " " + dayOfMonth + "-" + (month + 1) + "-" + year);
        }
    }

    /**
     * Classe interne statique pour l'heure
     */
    public static class TimePickerFragment extends DialogFragment implements
            TimePickerDialog.OnTimeSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current time as the default values for the picker
            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);

            // Create a new instance of TimePickerDialog and return it
            return new TimePickerDialog(getActivity(), this, hour, minute,
                    DateFormat.is24HourFormat(getActivity()));
        }

        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            // Do something with the time chosen by the user
            mHeureCours.setText(mHeureCours.getText() + " " + hourOfDay + ":" + minute + ":00");
        }
    }


    /**
     * Methode permettant de parser une date en string
     *
     * @param currentTime
     * @return
     */
    private String dateToString(Date currentTime) {
        //Date currentTime = Calendar.getInstance().getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        String s = sdf.format(currentTime);
        return s;
    }

    /**
     * Methode permettant de parse une string en date
     *
     * @param dateEtHeureCours
     * @return
     */
    private Date stringToDate(String dateEtHeureCours) {
        Date horaireCoursFormat = null;
        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

        try {
            horaireCoursFormat = df.parse(dateEtHeureCours);
            TimeZone timeZone = TimeZone.getTimeZone("Europe/Paris");
            df.setTimeZone(TimeZone.getTimeZone(timeZone.getID()));
            //System.out.println(horaireCoursFormat);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return horaireCoursFormat;
    }
}
