package fr.drochon.christian.taaroaa.covoiturage;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import com.google.firebase.perf.FirebasePerformance;
import com.google.firebase.perf.metrics.Trace;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import fr.drochon.christian.taaroaa.R;
import fr.drochon.christian.taaroaa.api.CovoiturageHelper;
import fr.drochon.christian.taaroaa.base.BaseActivity;
import fr.drochon.christian.taaroaa.model.User;
import fr.drochon.christian.taaroaa.alarm.TimeAlarmCovoiturageAller;
import fr.drochon.christian.taaroaa.alarm.TimeAlarmCovoiturageRetour;

import static java.util.Calendar.MINUTE;

public class CovoiturageConducteursActivity extends BaseActivity {

    @SuppressLint("StaticFieldLeak")
    private static TextInputEditText mDateDepart;
    @SuppressLint("StaticFieldLeak")
    private static TextInputEditText mHeureDepart;
    @SuppressLint("StaticFieldLeak")
    private static TextInputEditText mDateRetour;
    @SuppressLint("StaticFieldLeak")
    private static TextInputEditText mHeureretour;
    private TextInputEditText mPrenom;
    private TextInputEditText mNom;
    private TextInputEditText mNbPlaceTotal;
    private Spinner mTypeVehicule;
    private TextView mLieuDepart;
    private TextView mLieuArrivee;
    private ProgressBar mProgressBar;
    private AlarmManager mAlarmManagerAller;
    private AlarmManager mAlarmManagerRetour;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_covoiturage_conducteurs);

        mPrenom = findViewById(R.id.prenom_txt);
        mNom = findViewById(R.id.nom_txt);
        mNbPlaceTotal = findViewById(R.id.nombre_place_dispo_txt);
        mTypeVehicule = findViewById(R.id.type_vehicule_spinner);
        mDateDepart = findViewById(R.id.date_depart_input);
        mHeureDepart = findViewById(R.id.heure_depart_input);
        mDateRetour = findViewById(R.id.date_retour_input);
        mHeureretour = findViewById(R.id.heure_retour_input);
        mLieuArrivee = findViewById(R.id.lieu_arrivee);
        mLieuDepart = findViewById(R.id.lieu_depart);
        mProgressBar = findViewById(R.id.progress_bar);
        Button valid = findViewById(R.id.proposition_covoit_btn);
        EditText notifCreationCovoit = findViewById(R.id.alertdialog_ok_covoit);

        //  les AlarmManager permettront de réveiller le téléphone et d'executer du code à une date précise
        mAlarmManagerAller = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        mAlarmManagerRetour = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        mNbPlaceTotal.requestFocus();

        // Test performance
        final Trace myTrace = FirebasePerformance.getInstance().newTrace("covoiturageConducteursActivityFromStartScreenToCreateCovoiturageIncludingFormùErrors_trace");
        myTrace.start();

        this.configureToolbar();
        this.giveToolbarAName(R.string.covoit_conducteur_name);
        this.getInfosCurrentUser();

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

        mDateDepart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog(v);
            }
        });

        mHeureDepart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimePickerDialog(v);
            }
        });

        mDateRetour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog2(v);
            }
        });

        mHeureretour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimePickerDialog2(v);
            }
        });

        valid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createCovoiturageInFirebase();
                myTrace.stop();
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
        getMenuInflater().inflate(R.menu.course_pupils_menu, menu);
        return true;
    }

    /**
     * recuperation  du clic d'un user.
     * On utilise un switch ici car il peut y avoir plusieurs options.
     * Surtout ne pas oublier le "true" apres chaque case sinon, ce sera toujours le dernier case qui sera executé!
     *
     * @param item menuitem
     * @return boolean
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

        if (mPrenom.getText().toString().isEmpty())
            mPrenom.setError("Merci de renseigner ce champ !");
        if (mNom.getText().toString().isEmpty()) mNom.setError("Merci de renseigner ce champ !");

        if (mLieuArrivee.getText().toString().isEmpty()) {
            mLieuArrivee.setError("Merci de renseigner ce champ !");
            mLieuArrivee.requestFocus();
        }

        if (mHeureretour.getText().toString().isEmpty()) {
            mHeureretour.setError("Merci de renseigner ce champ !");
            mHeureretour.requestFocus();
        } else mHeureretour.setError(null);

        if (mDateRetour.getText().toString().isEmpty()) {
            mDateRetour.setError("Merci de renseigner ce champ !");
            mDateRetour.requestFocus();
        } else mDateRetour.setError(null);


        if (mLieuDepart.getText().toString().isEmpty()) {
            mLieuDepart.setError("Merci de renseigner ce champ !");
            mLieuDepart.requestFocus();
        }

        if (mHeureDepart.getText().toString().isEmpty()) {
            mHeureDepart.setError("Merci de renseigner ce champ !");
            mHeureDepart.requestFocus();
        } else mHeureDepart.setError(null);

        if (mDateDepart.getText().toString().isEmpty()) {
            mDateDepart.setError("Merci de renseigner ce champ !");
            mDateDepart.requestFocus();
        } else mDateDepart.setError(null);


        if (mNbPlaceTotal.getText().toString().isEmpty()) {
            mNbPlaceTotal.setError("Merci de renseigner ce champ !");
            mNbPlaceTotal.requestFocus();
        }
        mProgressBar.setVisibility(View.GONE);
    }

    // --------------------
    // ALARM NOTIFICATION
    // --------------------

    /**
     * Methode permettant de generer une alarm dans le systeme du telephone de maniere à envoyer une notification à l'utilisateur
     * 2 heures avant que le covoiturage aller parte.
     *
     * @param horaireDelAller date et heure de l'aller
     */
    private void alarmDepart(Date horaireDelAller) {

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(horaireDelAller);
        calendar.add(Calendar.HOUR, -2);
        // condition de declenchement de l'alarm de 2h avant le depart jusqu'au demarrage effectif du covoit
        if (Calendar.getInstance().getTime().after(calendar.getTime()) && Calendar.getInstance().getTime().before(horaireDelAller)) {
            Intent intent = new Intent(this, TimeAlarmCovoiturageAller.class).putExtra("hAller", String.valueOf(horaireDelAller));
            PendingIntent operation = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
            // reveil de l'alarm
            mAlarmManagerAller.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), operation);
        }
    }

    /**
     * Methode permettant de generer une alarm dans le systeme du telephone de maniere à envoyer une notification à l'utilisateur
     * 2 heures avant que le covoiturage retour parte.
     *
     * @param horaireDuRetour date et heure du retour
     */
    private void alarmRetour(Date horaireDuRetour) {

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(horaireDuRetour);
        calendar.add(Calendar.HOUR, -2);
        // condition de declenchement de l'alarm de 2h avant le depart jusqu'au demarrage effectif du covoit
        if (Calendar.getInstance().getTime().after(calendar.getTime()) && Calendar.getInstance().getTime().before(horaireDuRetour)) {
            Intent intent1 = new Intent(this, TimeAlarmCovoiturageRetour.class).putExtra("hRetour", String.valueOf(horaireDuRetour));
            PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 1, intent1, PendingIntent.FLAG_ONE_SHOT);
            // reveil de l'alarm
            mAlarmManagerRetour.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        }
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
        final String id = CovoiturageHelper.getCovoituragesCollection().document().getId();
        String prenom = mPrenom.getText().toString();
        String nom = mNom.getText().toString();
        String nbPlacesDispo = mNbPlaceTotal.getText().toString();
        String nbPlacesTotal = mNbPlaceTotal.getText().toString();
        String typeVehicule = mTypeVehicule.getSelectedItem().toString();
        String dateAller = mDateDepart.getText().toString();
        String dateRetour = mDateRetour.getText().toString();
        String lieuAller = mLieuDepart.getText().toString();
        String lieuRetour = mLieuArrivee.getText().toString();
        String heureDepart = mHeureDepart.getText().toString();
        String heureRetour = mHeureretour.getText().toString();

        //TODO V2 : rajouter le champs reservation lorsque les sorties seront gerees
        //Reservation reservation = new Reservation();

        // formattage des dates : insertion de l'heure en format us en bdd
        String horaireAller = dateAller + " " + heureDepart;
        Date horaireDelAller = null;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.FRANCE);
        try {
            horaireDelAller = simpleDateFormat.parse(horaireAller);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        String horaireRetour = dateRetour + " " + heureRetour;
        Date horaireDuRetour = null;
        SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.FRANCE);
        try {
            horaireDuRetour = simpleDateFormat1.parse(horaireRetour);
        } catch (ParseException e) {
            e.printStackTrace();
        }


        //verification de la coherence des champs de saisie date et heure
        if (horaireDelAller != null && horaireDelAller.before(Calendar.getInstance().getTime())) {
            final AlertDialog.Builder adb = new AlertDialog.Builder(this);
            adb.setTitle("Date incorrecte");
            adb.setIcon(android.R.drawable.ic_delete);
            adb.setMessage("Le jour du départ ne peut pas être défini avant la date du jour !");
            adb.setPositiveButton("MODIFIER", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    mDateDepart.setText("");
                    mDateDepart.setError(null);
                    mHeureDepart.setError(null);
                    mDateDepart.requestFocus();
                }
            });
            adb.show();
        } else if (horaireDelAller != null && horaireDuRetour != null) {
            if (horaireDuRetour.before(horaireDelAller)) {
                final AlertDialog.Builder adb = new AlertDialog.Builder(this);
                adb.setTitle("Dates inccorectes");
                adb.setIcon(android.R.drawable.ic_delete);
                adb.setMessage("Le jour du départ doit être défini avant le jour de retour !");
                adb.setPositiveButton("MODIFIER", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        mDateDepart.setText("");
                        mDateRetour.setText("");
                        mHeureDepart.setText("");
                        mHeureretour.setText("");
                        mDateDepart.setError(null);
                        mDateRetour.setError(null);
                        mHeureDepart.setError(null);
                        mHeureretour.setError(null);
                        mProgressBar.setVisibility(View.GONE);
                        //mDateDepart.requestFocus();
                    }
                });
                adb.show();
            } else {
                if (!nom.isEmpty() && !prenom.isEmpty() && !nbPlacesDispo.isEmpty() && !dateAller.isEmpty() && !dateRetour.isEmpty()
                        && !heureDepart.isEmpty() && !heureRetour.isEmpty() && !lieuAller.isEmpty() && !lieuRetour.isEmpty()) {

                    //creation de l'objet covoiturage et insertion dans la bdd
                    Map<String, Object> covoit = new HashMap<>();
                    covoit.put("id", id);
                    covoit.put("nomConducteur", nom.toUpperCase());
                    covoit.put("prenomConducteur", prenom.toUpperCase());
                    covoit.put("nbPlacesDispo", nbPlacesDispo);
                    covoit.put("nbPlacesTotal", nbPlacesTotal);
                    covoit.put("typeVehicule", typeVehicule);
                    covoit.put("horaireAller", horaireDelAller);
                    covoit.put("horaireRetour", horaireDuRetour);
                    covoit.put("lieuDepartAller", lieuAller);
                    covoit.put("lieuDepartRetour", lieuRetour);
                    List<User> users = new ArrayList<>();
                    covoit.put("listPassagers", users);
                    //TODO V2 : ligne à rajouter lors que l'obet Sortie existera
                    //covoit.put("reservation", null);

                    //TODO faire une requete pour boucler sur les users et recuperer les passagers par leurs noms et prenom. Sur ces personnes :  declencher l'alarm
                    // envoi de l'alarm à la classe TimeAlarmCovoiturageAller pour que les notifications soient prises en compte et envoyées au moment voulu
                    for(int i = 0; i < users.size(); i++){

                        Intent intent = new Intent(CovoiturageConducteursActivity.this, TimeAlarmCovoiturageAller.class).putExtra("user", users.get(i));
                        startActivity(intent);
                        Intent intent1 = new Intent(CovoiturageConducteursActivity.this, TimeAlarmCovoiturageRetour.class).putExtra("user", users.get(i));
                        startActivity(intent1);
                    }

                    /*this.alarmDepart(horaireDelAller);
                    this.alarmRetour(horaireDuRetour);*/

                    this.mProgressBar.setVisibility(View.VISIBLE);
                    setupDb().collection("covoiturages").document(id).set(covoit)
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
                else {
                    verificationChampsVides();
                }
            }
        } else verificationChampsVides();
    }

    /**
     * Methode permettant de recuperer le nom et le prenom de la personne connectée. Ainsi, seule une personne connectée
     * avec un compte precis pourra creer un covoiturage.
     */
    private void getInfosCurrentUser() {
        // recup de la personne connectée avec son uid
        setupDb().collection("users").document(Objects.requireNonNull(getCurrentUser()).getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    Map<String, Object> user = documentSnapshot.getData();
                    assert user != null;
                    mNom.setText(user.get("nom").toString());
                    mPrenom.setText(user.get("prenom").toString());
                }
            }
        });
    }


    // --------------------
    // DATETIMEPICKERS
    // --------------------

    // Les pickers etant static, je créé une instance pour chaque champs : une instance ne peut pas servir à 2 champs
    public void showDatePickerDialog(View v) {
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getSupportFragmentManager(), "dateDepart");
    }

    public void showDatePickerDialog2(View v) {
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getSupportFragmentManager(), "dateRetour");
    }

    public void showTimePickerDialog(View v) {
        DialogFragment newFragment = new TimePickerFragment();
        newFragment.show(getSupportFragmentManager(), "timeDepart");
    }

    public void showTimePickerDialog2(View v) {
        //v.setTag("timePicker2");
        DialogFragment newFragment = new TimePickerFragment();
        newFragment.show(getSupportFragmentManager(), "timeRetour");
    }


    // --------------------
    // CLASSES INTERNES POUR PICKERS HEURE & DATE
    // --------------------

    /**
     * Classe static interne pour la date
     */
    public static class DatePickerFragment extends DialogFragment implements
            DatePickerDialog.OnDateSetListener {

        /**
         * Créé une instance de DatePicker et la renvoi
         *
         * @param savedInstanceState bundle
         * @return DatePickerDialog
         */
        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);
            return new DatePickerDialog(Objects.requireNonNull(getActivity()), this, year, month, day);
        }

        /**
         * Affiche la date choisi par l'utilisateur
         *
         * @param view       picker associé au dialog
         * @param year       annee
         * @param month      (0 à 11)
         * @param dayOfMonth jour du mois
         */
        @SuppressLint("SetTextI18n")
        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
            assert getTag() != null;
            if (getTag().equals("dateDepart"))
                mDateDepart.setText(mDateDepart.getText() + "" + dayOfMonth + "-" + (month + 1) + "-" + year);
            if (getTag().equals("dateRetour"))
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
         * @param savedInstanceState bundle
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
         * @param view      timepicker
         * @param hourOfDay heure du jour
         * @param minute    minute de l'heure
         */
        @SuppressLint("SetTextI18n")
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            assert getTag() != null;
            if (getTag().equals("timeDepart"))
                mHeureDepart.setText(mHeureDepart.getText() + "" + hourOfDay + ":" + minute + ":00");
            if (getTag().equals("timeRetour"))
                mHeureretour.setText(mHeureretour.getText() + "" + hourOfDay + ":" + minute + ":00");
        }
    }

}
