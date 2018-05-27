package fr.drochon.christian.taaroaa.covoiturage;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import fr.drochon.christian.taaroaa.R;
import fr.drochon.christian.taaroaa.alarm.NotificationReceiver;
import fr.drochon.christian.taaroaa.alarm.RandomNotification;
import fr.drochon.christian.taaroaa.alarm.TimeAlarmCovoiturageAller;
import fr.drochon.christian.taaroaa.alarm.TimeAlarmCovoiturageRetour;
import fr.drochon.christian.taaroaa.api.CovoiturageHelper;
import fr.drochon.christian.taaroaa.base.BaseActivity;
import fr.drochon.christian.taaroaa.model.Covoiturage;
import fr.drochon.christian.taaroaa.model.User;

public class CovoituragePassagersActivity extends BaseActivity {


    private static Covoiturage covoiturage;
    TextInputEditText mNomConducteur;
    TextInputEditText mDateDepart;
    TextInputEditText mDateretour;
    TextInputEditText mNbPlaceDispo;
    TextInputEditText mTypeVehicule;
    LinearLayout mTitreInscription;
    LinearLayout mLinearChampsDynamiques;
    TextView mTitrePassager;
    TextInputEditText mNbPassagerInput;
    Button mReservation;
    Intent mIntent;
    ProgressBar mProgressBar;
    TextInputEditText mFieldNamePassengers;
    Spinner mSpinnerPassagers;
    // DATAS
    int inputs;
    List<User> listUsers;
    List<User> listFilteredUsers;
    List<String> listUsersStr;
    List<String> listSelectedUsers;
    AlarmManager mAlarmManagerAller;
    AlarmManager mAlarmManagerRetour;

    // --------------------
    // LIFECYCLE
    // --------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_covoiturage_passagers);

        mNomConducteur = findViewById(R.id.nom_conducteur_txt);
        mDateDepart = findViewById(R.id.date_depart_txt);
        mDateretour = findViewById(R.id.date_retour_txt);
        mNbPlaceDispo = findViewById(R.id.nb_place_dispo_txt);
        mTypeVehicule = findViewById(R.id.type_vehicule_txt);
        mReservation = findViewById(R.id.reservation_covoit_btn);
        mProgressBar = findViewById(R.id.progress_bar);
        mTitreInscription = findViewById(R.id.titre_inscription);
        mLinearChampsDynamiques = findViewById(R.id.linearLayoutDynamique);
        mTitrePassager = findViewById(R.id.nom_passager_txt);
        mNbPassagerInput = findViewById(R.id.nb_passager_input);
        mNbPassagerInput.requestFocus();

        listSelectedUsers = new ArrayList<>();
        listFilteredUsers = new ArrayList<>();
        listUsersStr = new ArrayList<>();
        listUsers = new ArrayList<>();
        //  les AlarmManager permettront de réveiller le téléphone et d'executer du code à une date précise
        mAlarmManagerAller = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        mAlarmManagerRetour = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        this.configureToolbar();
        this.giveToolbarAName(R.string.covoit_passager_name);
        this.updateUIWhenCreating();
        this.getAllUsers();

        // --------------------
        // LISTENERS
        // --------------------

        mNbPassagerInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                showFieldsNamePassengers(s);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        mReservation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createPassagerInCovoiturage();
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();
        this.updateUIWhenResuming();
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
     * @param item menuitem
     * @return optionsToolBar
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return optionsToolbar(this, item);
    }


    // --------------------
    // UI
    // --------------------

    /**
     * Methode permettant de desactiver les champs de saisi en cas de covoiturage complet
     */
    private void updateUI() {
        if (Integer.parseInt(covoiturage.getNbPlacesDispo()) == 0) {
            mTitreInscription.setEnabled(false);
        }
    }

    /**
     * Methode permettant d'afficher les informations de l'user sur l'ecran AccountCreateActivity lorsqu'un user vient de creer un compte
     */
    private void updateUIWhenCreating() {

        if (this.getCurrentUser() != null) { // retourne un user FirebaseUser. Permettra de remplir toutes les vues de l'activité
            getAndShowDatas();
            updateUI();
        }
    }

    /**
     * Methode permettant d'afficher les informations d'un user depuis la bdd firestore lorsque le cycle de vie de l'application est à OnResume()
     */
    private void updateUIWhenResuming() {

        if (this.getCurrentUser() != null) { // retourne un user FirebaseUser. Permettra de remplir toutes les vues de l'activité
            getAndShowDatas();
            updateUI();
        }
    }

    /**
     * Methode permettant à l'utilisateur d'etre redirigé vers la pages principale des covoiturages
     */
    private void startActivityCovoiturageVehicule() {
        Intent intent = new Intent(CovoituragePassagersActivity.this, CovoiturageVehiclesActivity.class);
        startActivity(intent);
    }

    /**
     * Methode permettant de signaler une erreur lorsqu'un champ de nom de passager est resté vide
     * alors que la soumission du formulaire a été faite.
     */
    private boolean verificationChampsVides() {
        //int i = 0;
        if (mNbPassagerInput.getText().toString().equals("")) {
            mNbPassagerInput.setError("Merci de renseigner ce champ !");
            mNbPassagerInput.requestFocus();
            return false;
        }

        return true;
    }

    /**
     * Methode permettant d'afficher dynamiquement le nb de champ saisi par l'utilisateur correspondant au nb
     * de passager voulu pour saisir le nom de chacun des passagers
     */
    @SuppressLint("SetTextI18n")
    private void showFieldsNamePassengers(CharSequence charSequence) {
        if (!mNbPassagerInput.getText().toString().equals("")) {
            // empecher un user de demander trop de places en fonction des places dispos
            if (calculNbPlacesRestantes() < 0) {
                final AlertDialog.Builder adb = new AlertDialog.Builder(CovoituragePassagersActivity.this);
                adb.setTitle(R.string.rectif_demande);
                // ajouter une couleur à l'icon de warning
                Drawable warning = getResources().getDrawable(android.R.drawable.ic_dialog_alert);
                ColorFilter filter = new LightingColorFilter(Color.RED, Color.BLUE);
                warning.setColorFilter(filter);
                adb.setIcon(warning);
                adb.setMessage(R.string.alertDialog_places_restantes);
                adb.setPositiveButton("MODIFIER", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        mNbPassagerInput.setText("");
                    }
                });
                adb.show();
            } else {
                // condition de creation des spinners de passagers dynamiquement
                if (verificationChampsVides()) {
                    mTitrePassager.setVisibility(View.VISIBLE);
                    inputs = Integer.parseInt(charSequence.toString());
                    for (int i = 0; i < listUsers.size(); i++) {
                        listUsersStr.add(listUsers.get(i).getPrenom() + " " + listUsers.get(i).getNom());
                    }

                    if (inputs > 0) {
                        // creation des champs spinner de passagers dynamiquement
                        for (int i = 0; i < inputs; i++) {
                            // creation d'autant de spinner que desirés
                            mSpinnerPassagers = new Spinner(this);
                            // Create an ArrayAdapter using the string array and a default spinner layout
                            ArrayAdapter<String> adapterUser = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, listUsersStr);
                            // Specify the layout to use when the list of choices appears
                            adapterUser.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            // Apply the adapter to the spinner
                            mSpinnerPassagers.setAdapter(adapterUser);
                            // ajout dynamique du spinner au linearlayout
                            mLinearChampsDynamiques.addView(mSpinnerPassagers);

                            mSpinnerPassagers.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                @Override
                                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                    // boucle sur tous les spinners
                                    for (int j = 0; j < inputs; j++) {
                                        // garder une trace du premier objet affiché avant changement par l'user
                                        final Spinner spinner = (Spinner) mLinearChampsDynamiques.getChildAt(j);
                                        final String precedentUsername = spinner.getSelectedItem().toString();
                                        // si l'user n'existe pas en bdd, supprimer le precedent user et insertion du nouvel user à la place du precedent dans la liste
                                        if (!listSelectedUsers.contains(spinner.getSelectedItem().toString())) {
                                            // capture de l'exception lors de la creation de la liste, car les index n'exitent pas encore
                                            try {
                                                if (!listSelectedUsers.get(j).equals(""))
                                                    listSelectedUsers.remove(listSelectedUsers.get(j));
                                            } catch (IndexOutOfBoundsException e) {
                                                e.getMessage();
                                            }
                                            listSelectedUsers.add(spinner.getSelectedItem().toString());
                                        }
                                    }
                                }

                                @Override
                                public void onNothingSelected(AdapterView<?> parent) {

                                }
                            });
                        }
                    }
                } else {
                    mTitrePassager.setVisibility(View.GONE);
                    mLinearChampsDynamiques.removeAllViews();
                    listSelectedUsers.clear();
                }
            }
        }
    }


    // --------------------
    // NOTIFICATION
    // --------------------

    private void scheduleNotificationAller(Notification notification, Date alarmTime) {

        Intent notificationIntent = new Intent(this, NotificationReceiver.class);
        notificationIntent.putExtra(NotificationReceiver.NOTIFICATION_ID, 7);
        notificationIntent.putExtra(NotificationReceiver.NOTIFICATION, notification);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        assert alarmManager != null;
        alarmManager.set(AlarmManager.RTC_WAKEUP, alarmTime.getTime(), pendingIntent);
    }

    private void scheduleNotificationRetour(Notification notification, Date alarmTime) {

        Intent notificationIntent = new Intent(this, NotificationReceiver.class);
        notificationIntent.putExtra(NotificationReceiver.NOTIFICATION_ID, 7);
        notificationIntent.putExtra(NotificationReceiver.NOTIFICATION, notification);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        assert alarmManager != null;
        alarmManager.set(AlarmManager.RTC_WAKEUP, alarmTime.getTime(), pendingIntent);
    }

    private Notification getNotification() {
        int colour = getNotificationColour();
        Bitmap largeNotificationImage = getLargeNotificationImage();
        return new RandomNotification(this).getNotification(
                "TAAROAA",
                "More text",
                getNotificationImage(),
                largeNotificationImage,
                colour);
    }

    private int getNotificationImage() {
        return R.mipmap.logo;
    }

    private int getNotificationColour() {
        return ContextCompat.getColor(this, R.color.colorAccent);
    }

    private Bitmap getLargeNotificationImage() {
        return BitmapFactory.decodeResource(this.getResources(),
                R.mipmap.logo1);
    }

    // --------------------
    // ALARM NOTIFICATION
    // --------------------

    /**
     * Methode permettant de generer une alarm dans le systeme du telephone de maniere à envoyer une notification à l'utilisateur
     * 2 heures avant que le covoiturage aller parte.
     */
    private void alarmDepart(User passager) {

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(covoiturage.getHoraireAller());
        calendar.add(Calendar.HOUR, -2);
        // condition de declenchement de l'alarm de 2h avant le depart jusqu'au demarrage effectif du covoit
        if (Calendar.getInstance().getTime().after(calendar.getTime()) && Calendar.getInstance().getTime().before(covoiturage.getHoraireAller())) {
            //for(int i = 0 ; i < passager.size(); i++){
            Covoiturage covoit = new Covoiturage();
            covoit.setHoraireAller(covoiturage.getHoraireAller());

            //Intent intent = new Intent(this, TimeAlarmCovoiturageAller.class).putExtra("hAller", String.valueOf(covoiturage.getHoraireAller()));
            Intent intent = new Intent(this, TimeAlarmCovoiturageAller.class).putExtra("covoiturageAlarm", covoit).putExtra("user", passager);
            PendingIntent operation = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
            // reveil de l'alarm
            mAlarmManagerAller.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), operation);
            //}

        }
    }


    /**
     * Methode permettant de generer une alarm dans le systeme du telephone de maniere à envoyer une notification à l'utilisateur
     * 2 heures avant que le covoiturage retour parte.
     */
    private void alarmRetour() {

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(covoiturage.getHoraireRetour());
        calendar.add(Calendar.HOUR, -2);
        // condition de declenchement de l'alarm de 2h avant le depart jusqu'au demarrage effectif du covoit
        if (Calendar.getInstance().getTime().after(calendar.getTime()) && Calendar.getInstance().getTime().before(covoiturage.getHoraireRetour())) {
            Intent intent1 = new Intent(this, TimeAlarmCovoiturageRetour.class)
                    .putExtra("hRetour", String.valueOf(covoiturage.getHoraireRetour()));
            PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 1, intent1, PendingIntent.FLAG_ONE_SHOT);
            // reveil de l'alarm
            mAlarmManagerRetour.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        }
    }

    // --------------------
    // REST REQUETES
    // --------------------

    /**
     * Methode permettant de recuperer tous les utilisateurs existants en bdd, de maniere à pouvoir les
     * afficher dans un spinner pour le choix des passagers d'un covoiturage.
     *
     * @return liste de tous les users de la bdd
     */
    private List<User> getAllUsers() {

        setupDb().collection("users").orderBy("nom").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if (queryDocumentSnapshots.size() != 0) {
                    List<DocumentSnapshot> ds = queryDocumentSnapshots.getDocuments();
                    for (DocumentSnapshot doc : ds) {
                        Map<String, Object> user = doc.getData();
                        User u = new User(user.get("uid").toString(), user.get("nom").toString(), user.get("prenom").toString(), user.get("licence").toString(),
                                user.get("email").toString(), user.get("niveau").toString(), user.get("fonction").toString(), Long.parseLong(user.get("hash").toString()));
                        //User u = new User(user.get("uid").toString());
                        //listUsers.add(user.get("prenom") + " " + user.get("nom"));
                        listUsers.add(u);
                    }
                }
            }
        });
        return listUsers;
    }

    private void getFilteredUsers(String nom, String prenom) {
        setupDb().collection("users").whereEqualTo("nom", nom).whereEqualTo("prenom", prenom).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (queryDocumentSnapshots.size() != 0) {
                            List<DocumentSnapshot> ds = queryDocumentSnapshots.getDocuments();
                            for (DocumentSnapshot doc : ds) {
                                Map<String, Object> user = doc.getData();
                                User u = new User(user.get("uid").toString(), user.get("nom").toString(), user.get("prenom").toString(), user.get("licence").toString(),
                                        user.get("email").toString(), user.get("niveau").toString(), user.get("fonction").toString(), Long.parseLong(user.get("hash").toString()));
                                //User u = new User(user.get("uid").toString());
                                //listUsers.add(user.get("prenom") + " " + user.get("nom"));

                                // notification d'alarme à chacun des passagers : mais il faut que le nom rentré correspondent à quelque chose!
                                //TODO faire une requete pour boucler sur les users et recuperer les passagers par leurs noms et prenom pour notifier le depart du covoit souscris. Sur ces personnes :  declencher l'alarm
                                alarmDepart(u);
                                alarmRetour();
                            }
                        }
                    }
                });
    }

    /**
     * Methode permettant de recuperer et d'afficher toutes les informations d'un covoiturage
     */
    private void getAndShowDatas() {
        mIntent = getIntent();
        covoiturage = (Covoiturage) Objects.requireNonNull(mIntent.getExtras()).getSerializable("covoit");
        assert covoiturage != null;

        mNomConducteur.setText(Html.fromHtml("<b>Conducteur : </b>" + covoiturage.getPrenomConducteur() + " " + covoiturage.getNomConducteur()));
        mDateDepart.setText(Html.fromHtml("<b>Aller : départ le </b>" + stDateToString(covoiturage.getHoraireAller()) + "<b> depuis </b>" + covoiturage.getLieuDepartAller()));
        mDateretour.setText(Html.fromHtml("<b>Retour : départ le </b>" + stDateToString(covoiturage.getHoraireRetour()) + "<b> jusqu'à </b>" + covoiturage.getLieuDepartRetour()));
        mNbPlaceDispo.setText(Html.fromHtml("<b>Places disponibles : </b>" + covoiturage.getNbPlacesDispo() + " / " + covoiturage.getNbPlacesTotal()));
        mTypeVehicule.setText(Html.fromHtml("<b>Type Véhicule : </b>" + covoiturage.getTypeVehicule()));
    }

    /**
     * Methode permettant la creation d'un user dans le bdd. En cas d'insertion ou de probleme,
     * la fonction renverra une notification à l'utilisateur.
     */
    private void createPassagerInCovoiturage() {

        int nbPlacesRestantes = calculNbPlacesRestantes();

        // empecher un user de demander trop de places en fonction des places dispos
        if (nbPlacesRestantes < 0 && listSelectedUsers.size() > Integer.parseInt(covoiturage.getNbPlacesDispo())) {

            final AlertDialog.Builder adb = new AlertDialog.Builder(CovoituragePassagersActivity.this);
            adb.setTitle(R.string.rectif_demande);
            // ajouter une couleur à l'icon de warning
            Drawable warning = getResources().getDrawable(android.R.drawable.ic_dialog_alert);
            ColorFilter filter = new LightingColorFilter(Color.RED, Color.BLUE);
            warning.setColorFilter(filter);
            adb.setIcon(warning);
            adb.setMessage(R.string.alertDialog_places_restantes);
            adb.setPositiveButton("MODIFIER", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    mNbPassagerInput.setText("");
                }
            });
            adb.show();

            // si le nb de places demandées est bon, on insere tous les noms des passagers dans la bdd
        } else {
            if (verificationChampsVides()) {
                String placesRestantes = String.valueOf(nbPlacesRestantes);
                mNbPlaceDispo.setText(placesRestantes);
                List<String> listPassagers = new ArrayList<>();
                // recuperation passagers existants dejà pour ce covoit
                listPassagers.addAll(covoiturage.getListPassagers());

                // ajout des infos du passager dans l'objet covoiturage
                for (int i = 0; i < listSelectedUsers.size(); i++) {
                    listPassagers.add(listSelectedUsers.get(i).toUpperCase());

                    // decomposition du nom et du prenom recu dans le param name
                    String nom = null, prenom = null;
                    String[] parts;
                    if (listSelectedUsers.get(i).contains(" ")) {
                        parts = listSelectedUsers.get(i).split(" ");
                        try {
                            if (parts[1] != null) nom = parts[1];
                            else nom = "";
                        } catch (ArrayIndexOutOfBoundsException e1) {
                            Log.e("TAG", "ArrayOutOfBoundException " + e1.getMessage());
                        }
                        if (parts[0] != null) prenom = parts[0];
                        else prenom = "";
                    } else {
                        nom = listSelectedUsers.get(i);
                        prenom = "";
                    }
                    // declenchement des alarmes sur les passagers choisis dans les spinners
                    this.getFilteredUsers(nom, prenom);
                }


/*                // alarm depart aller
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(covoiturage.getHoraireAller());
                calendar.add(Calendar.HOUR, -2);
                // condition de declenchement de l'alarm de 2h avant le depart jusqu'au demarrage effectif du covoit
                if (Calendar.getInstance().getTime().after(calendar.getTime()) && Calendar.getInstance().getTime().before(covoiturage.getHoraireAller())) {
                    scheduleNotificationAller(getNotification(), calendar.getTime());
                }

                // alarm depart retour
                Calendar cal = Calendar.getInstance();
                cal.setTime(covoiturage.getHoraireRetour());
                cal.add(Calendar.HOUR, -2);
                // condition de declenchement de l'alarm de 2h avant le depart jusqu'au demarrage effectif du covoit
                if (Calendar.getInstance().getTime().after(cal.getTime()) && Calendar.getInstance().getTime().before(covoiturage.getHoraireRetour())) {
                    scheduleNotificationRetour(getNotification(), cal.getTime());
                }*/

                this.mProgressBar.setVisibility(View.VISIBLE);
                //CRUD
                CovoiturageHelper.updateCovoiturage(covoiturage.getId(), placesRestantes, listPassagers)
                        .addOnFailureListener(this.onFailureListener())
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(CovoituragePassagersActivity.this, R.string.create_passager,
                                        Toast.LENGTH_SHORT).show();
                                startActivityCovoiturageVehicule(); // renvoi l'user sur la page des covoiturages apres validation de la creation de l'user dans les covoit
                            }
                        });
            }
        }
    }

    /**
     * Methode de calcul du nombre de places restantes dans un covoiturage
     *
     * @return
     */
    private int calculNbPlacesRestantes() {
        String passagers = mNbPassagerInput.getText().toString();
        int nbPassagers = 0;
        if (!passagers.equals("")) {
            nbPassagers = Integer.parseInt(passagers);
        }
        int nbPlacesDispo = Integer.parseInt(covoiturage.getNbPlacesDispo());
        int nbPlacesRestantes = nbPlacesDispo - nbPassagers;

        return nbPlacesRestantes;
    }
}
