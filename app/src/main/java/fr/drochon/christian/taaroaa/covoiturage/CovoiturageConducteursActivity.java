package fr.drochon.christian.taaroaa.covoiturage;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TimePicker;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import fr.drochon.christian.taaroaa.R;
import fr.drochon.christian.taaroaa.base.BaseActivity;
import fr.drochon.christian.taaroaa.course.CoursesManagementActivity;

import static java.util.Calendar.MINUTE;

public class CovoiturageConducteursActivity extends BaseActivity {

    TextInputEditText mPrenom;
    TextInputEditText mNom;
    TextInputEditText mNbPlaceDispo;
    Spinner mTypeVehicule;
    static TextInputEditText mDateDepart;
    static TextInputEditText mHeureDepart;
    static TextInputEditText mDateRetour;
    static TextInputEditText mHeureretour;
    ProgressBar mProgressBar;
    Button mValid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_covoiturage_conducteurs);

        mPrenom = findViewById(R.id.prenom_txt);
        mNom = findViewById(R.id.nom_txt);
        mNbPlaceDispo = findViewById(R.id.nombre_place_dispo_txt);
        mTypeVehicule = findViewById(R.id.type_vehicule_spinner);
        mDateDepart = findViewById(R.id.date_depart_input);
        mHeureDepart = findViewById(R.id.heure_depart_input);
        mDateRetour = findViewById(R.id.date_retour_input);
        mHeureretour = findViewById(R.id.heure_retour_input);
        mProgressBar = findViewById(R.id.progress_bar);
        mValid = findViewById(R.id.proposition_covoit_btn);

        configureToolbar();

        // --------------------
        // SPINNERS & REMPLISSAGE
        // --------------------
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapterTypeVehicule = ArrayAdapter.createFromResource(this,
                R.array.type_vehicule_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapterTypeVehicule.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        mTypeVehicule.setAdapter(adapterTypeVehicule);

        // hint pour la date du edittext
        Date currentTime = Calendar.getInstance().getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("dd / MM / yyyy");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        String d = sdf.format(currentTime);
        mDateDepart.setHint(d);
        mDateRetour.setHint(d);
    }

    @Override
    public int getFragmentLayout() {
        return 0;
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
    // DATETIMEPICKERS
    // --------------------
    public void showDatePickerDialog(View v) {
        DialogFragment newFragment = new CovoiturageConducteursActivity.DatePickerFragment();
        newFragment.show(getSupportFragmentManager(), "datePicker");
    }

    public void showTimePickerDialog(View v) {
        DialogFragment newFragment = new CovoiturageConducteursActivity.TimePickerFragment();
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
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);
            return new DatePickerDialog(getActivity(), this, year, month, day + 1);
        }

        /**
         * Affiche la date choisi par l'utilisateur
         *
         * @param view       picker associé au dialog
         * @param year
         * @param month      (0 à 11)
         * @param dayOfMonth
         */
        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {

            mDateDepart.setText(mDateDepart.getText() + "" + dayOfMonth + "-" + (month + 1) + "-" + year);
            mDateRetour.setText(mDateRetour.getText() + "" + dayOfMonth + "-" + (month + 1) + "-" + year);
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
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            //UTC
/*            final String format = "dd-MMM-yyyy HH:mm:ss";
            final SimpleDateFormat dateFormatGmt = new SimpleDateFormat(format);
            dateFormatGmt.setTimeZone(TimeZone.getTimeZone("UTC"));*/

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

            mHeureDepart.setText(mHeureDepart.getText() + "" + hourOfDay + ":" + minute + ":00");
            mHeureretour.setText(mHeureretour.getText() + "" + hourOfDay + ":" + minute + ":00");
        }
    }

}
