package fr.drochon.christian.taaroaa.covoiturage;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import fr.drochon.christian.taaroaa.R;
import fr.drochon.christian.taaroaa.api.CovoiturageHelper;
import fr.drochon.christian.taaroaa.base.BaseActivity;
import fr.drochon.christian.taaroaa.model.Reservation;
import fr.drochon.christian.taaroaa.model.User;

import static java.util.Calendar.MINUTE;

public class CovoiturageConducteursActivity extends BaseActivity {

    static TextInputEditText mDateDepart;
    static TextInputEditText mHeureDepart;
    static TextInputEditText mDateRetour;
    static TextInputEditText mHeureretour;
    TextInputEditText mPrenom;
    TextInputEditText mNom;
    TextInputEditText mNbPlaceDispo;
    Spinner mTypeVehicule;
    TextView mLieuDepart;
    TextView mLieuArrivee;
    ProgressBar mProgressBar;
    Button mValid;
    Button mSuppresion;
    EditText mNotifCreationCovoit;

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
        mLieuArrivee = findViewById(R.id.lieu_arrivee);
        mLieuDepart = findViewById(R.id.lieu_depart);
        mProgressBar = findViewById(R.id.progress_bar);
        mValid = findViewById(R.id.proposition_covoit_btn);
        mSuppresion = findViewById(R.id.suppression_covoit_btn);
        mNotifCreationCovoit = findViewById(R.id.alertdialog_ok_covoit);

        configureToolbar();
        findCurrentUser();

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
        SimpleDateFormat sdf = new SimpleDateFormat("EEE dd MMM yyyy", Locale.FRANCE);
        String d = sdf.format(currentTime);
        mDateDepart.setHint(d);
        mDateRetour.setHint(d);

        // --------------------
        // LISTENERS
        // --------------------

        mValid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createCovoiturageInFirebase();
            }
        });

        mSuppresion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteCovoiturageInFirebase();
            }
        });
    }

    // --------------------
    // UI
    // --------------------

    @Override
    public int getFragmentLayout() {
        return 0;
    }

    private void startMainCovoitActivity() {
        Intent intent = new Intent(CovoiturageConducteursActivity.this, CovoiturageVehiclesActivity.class);
        startActivity(intent);
    }

    /**
     * Methode permettant de signaler une erreur lorsqu'un champ est resté vide alors que la soumission du formulaire a été faite.
     */
    private void verificationChampsVides() {

        if (mPrenom.getText().toString().isEmpty()) mPrenom.setError("Merci de saisir ce champ !");
        if (mNom.getText().toString().isEmpty()) mNom.setError("Merci de saisir ce champ !");
        if (mNbPlaceDispo.getText().toString().isEmpty())
            mNbPlaceDispo.setError("Merci de saisir ce champ !");
        if(mLieuDepart.getText().toString().isEmpty()) mLieuDepart.setError("Merci de saisir ce champ !");
        if(mLieuArrivee.getText().toString().isEmpty()) mLieuArrivee.setError("Merci de saisir ce champs !");
        if (mDateDepart.getText().toString().isEmpty())
            mDateDepart.setError("Merci de saisir ce champ !");
        else mDateDepart.append(" ");
        if (mDateRetour.getText().toString().isEmpty())
            mDateRetour.setError("Merci de saisir ce champ !");
        else mDateRetour.append(" ");
        if (mHeureDepart.getText().toString().isEmpty())
            mHeureDepart.setError("Merci de saisir ce champ !");
        else mHeureDepart.append(" ");
        if (mHeureretour.getText().toString().isEmpty())
            mHeureretour.setError("Merci de saisir ce champ !");
        else mHeureretour.append(" ");
        mProgressBar.setVisibility(View.GONE);
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
     * @return boolean
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return optionsToolbar(this, item);
    }


    // --------------------
    // UI
    // --------------------
    /**
     * Methode permettant à l'utilisateur d'etre redirigé vers la pages principale des covoiturages
     */
    private void startActivityCovoiturageVehicule() {
        Intent intent = new Intent(CovoiturageConducteursActivity.this, CovoiturageVehiclesActivity.class);
        startActivity(intent);
    }

    // --------------------
    // REST REQUETES
    // --------------------

    /**
     * Methode permettant la creation d'un covoiturage dans le bdd. En cas de probleme,
     * la fonction renverra une notification à l'utilisateur.
     */
    private void createCovoiturageInFirebase() {

        // pas d'id pour un objet non créé : generation auto par firebase
        this.mProgressBar.setVisibility(View.VISIBLE);
        final String id = CovoiturageHelper.getCovoituragesCollection().document().getId();
        String prenom = mPrenom.getText().toString();
        String nom = mNom.getText().toString();
        String nbPlacesDispo = mNbPlaceDispo.getText().toString();
        String typeVehicule = mTypeVehicule.getSelectedItem().toString();
        String dateAller = mDateDepart.getText().toString();
        String dateRetour = mDateRetour.getText().toString();
        String lieuAller = mLieuDepart.getText().toString();
        String lieuRetour = mLieuArrivee.getText().toString();
        String heureDepart = mHeureDepart.getText().toString();
        String heureRetour = mHeureretour.getText().toString();
        List<User> users = new ArrayList<>();
        Reservation reservation = new Reservation();

        // formattage des dates
        String horaireAller = dateAller + " " + heureDepart;
        Date horaireDelAller = null;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.US);
        try {
            horaireDelAller = simpleDateFormat.parse(horaireAller);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        String horaireRetour = dateRetour + " " + heureRetour;
        Date horaireDuRetour = null;
        SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.US);
        try {
            horaireDuRetour = simpleDateFormat1.parse(horaireRetour);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        // Requetage et insertion en bdd
        final FirebaseFirestore db = FirebaseFirestore.getInstance();

        if (!nom.isEmpty() && !prenom.isEmpty() && !nbPlacesDispo.isEmpty() && !dateAller.isEmpty() && !dateRetour.isEmpty()
                && !heureDepart.isEmpty() && !heureRetour.isEmpty() && !lieuAller.isEmpty() && !lieuRetour.isEmpty()) {

            Map<String, Object> covoit = new HashMap<>();
            covoit.put("id", id);
            covoit.put("nomConducteur", nom.toUpperCase());
            covoit.put("prenomConducteur", prenom.toUpperCase());
            covoit.put("nbPlacesDispo", nbPlacesDispo);
            covoit.put("typeVehicule", typeVehicule);
            covoit.put("horaireAller", horaireDelAller);
            covoit.put("horaireRetour", horaireDuRetour);
            covoit.put("lieuDepartAller", lieuAller);
            covoit.put("lieuDepartRetour", lieuRetour);
            covoit.put("listPassagers", users);
            //TODO ligne à rajouter lors que l'obet Sortie existera
            //covoit.put("reservation", null);
            db.collection("covoiturages").document(id).set(covoit)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(CovoiturageConducteursActivity.this, R.string.create_covoit,
                                    Toast.LENGTH_LONG).show();
                            startMainCovoitActivity(); // renvoi l'covoit sur la page des covoiturages  apres validation de la creation du covoit
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(CovoiturageConducteursActivity.this, "ERROR" + e.toString(),
                                    Toast.LENGTH_SHORT).show();
                            Log.d("TAG", e.toString());
                        }
                    });
        }
        // prise en charge des champs non vides
        else {
            verificationChampsVides();
        }
    }

    /**
     * Methode permettant de supprimer un covoiturage si ce covoiturage ne comporte pas encore de passager
     */
    private void deleteCovoiturageInFirebase(){

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("covoiturages").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot documentSnapshots) {
                if(documentSnapshots.size() != 0){
                    List<DocumentSnapshot> docSps = documentSnapshots.getDocuments();
                    for (DocumentSnapshot ds:docSps
                         ) {
                        Map<String, Object> covoit = ds.getData();
                        if(covoit.get("nomConducteur").equals(mNom) && covoit.get("prenomConducteur").equals(mPrenom)){
                            if(covoit.get("listPassagers").equals(0)){
                                mSuppresion.setVisibility(View.VISIBLE);
                                System.out.println("delete");
                            }
                        }
                    }
                }
            }
        });

        final String id = CovoiturageHelper.getCovoituragesCollection().document().getId();

        //CRUD
/*        CovoiturageHelper.deleteCovoiturage(id)
                .addOnFailureListener(this.onFailureListener())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(CovoiturageConducteursActivity.this, R.string.delete_covoit,
                                Toast.LENGTH_LONG).show();
                        startActivityCovoiturageVehicule(); // renvoi l'user sur la page des covoiturages apres validation de la creation de l'user dans les covoit
                    }
                });*/
    }

    /**
     * Methode permettant de recuperer le nom et le prenom de la personne connectée. Ainsi, seule une personne connectée
     * avec un compte precis pourra creer un covoiturage.
     */
    private void findCurrentUser(){
        String username = getCurrentUser().getDisplayName();
        String nom = null, prenom = null;
        String[] parts;
        assert username != null;
        if (username.contains(" ")) {
            parts = username.split(" ");
            try {
                if (parts[1] != null) nom = parts[1].toUpperCase();
                else nom = "";
            } catch (ArrayIndexOutOfBoundsException e1) {
                Log.e("TAG", "ArrayOutOfBoundException " + e1.getMessage());
            }
            if (parts[0] != null) prenom = parts[0].toUpperCase();
            else prenom = "";
        } else {
            nom = username;
            prenom = "";
        }
        mNom.setText(nom);
        mPrenom.setText(prenom);
    }


    // --------------------
    // DATETIMEPICKERS
    // --------------------

    // Les pickers etant static, je créé une instance pour chaque champs : une instance ne peut pas servir à 2 champs
    public void showDatePickerDialog(View v) {
        DialogFragment newFragment = new CovoiturageConducteursActivity.DatePickerFragment();
        newFragment.show(getSupportFragmentManager(), "dateDepart");
    }

    public void showDatePickerDialog2(View v) {
        DialogFragment newFragment = new CovoiturageConducteursActivity.DatePickerFragment();
        newFragment.show(getSupportFragmentManager(), "dateRetour");
    }

    public void showTimePickerDialog(View v) {
        DialogFragment newFragment = new CovoiturageConducteursActivity.TimePickerFragment();
        newFragment.show(getSupportFragmentManager(), "timeDepart");
    }

    public void showTimePickerDialog2(View v) {
        v.setTag("timePicker2");
        DialogFragment newFragment = new CovoiturageConducteursActivity.TimePickerFragment();
        newFragment.show(getSupportFragmentManager(), "timeRetour");
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
         * @return DatePickerDialog
         */
        @NonNull
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
            if(getTag() == "dateDepart")
            mDateDepart.setText(mDateDepart.getText() + "" + dayOfMonth + "-" +(month + 1) + "-" + year);
            else if (getTag() == "dateRetour")
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
         * @return TimePickerDialog
         */
        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
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
            if(getTag() == "timeDepart")
            mHeureDepart.setText(mHeureDepart.getText() + "" + hourOfDay + ":" + minute + ":00");
            else if(getTag() == "timeRetour")
            mHeureretour.setText(mHeureretour.getText() + "" + hourOfDay + ":" + minute + ":00");
        }
    }

}
