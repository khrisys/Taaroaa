package fr.drochon.christian.taaroaa.covoiturage;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.perf.FirebasePerformance;
import com.google.firebase.perf.metrics.Trace;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import fr.drochon.christian.taaroaa.R;
import fr.drochon.christian.taaroaa.alarm.TimeAlarmCovoiturageAller;
import fr.drochon.christian.taaroaa.alarm.TimeAlarmCovoiturageRetour;
import fr.drochon.christian.taaroaa.api.CovoiturageHelper;
import fr.drochon.christian.taaroaa.base.BaseActivity;
import fr.drochon.christian.taaroaa.model.Covoiturage;
import fr.drochon.christian.taaroaa.model.User;

public class CovoituragePassagersActivity extends BaseActivity {

    private static Covoiturage covoiturage;
    // DESIGN
    private TextInputEditText mNomConducteur;
    private TextInputEditText mDateDepart;
    private TextInputEditText mDateretour;
    private TextInputEditText mNbPlaceDispo;
    private TextInputEditText mTypeVehicule;
    private LinearLayout mTitreInscription;
    private LinearLayout mLinearChampsDynamiques;
    private TextView mTitrePassager;
    private TextInputEditText mNbPassagerInput;
    private ProgressBar mProgressBar;
    // DATAS
    private int inputs;
    private List<User> listUsers;
    private List<String> listUsersStr;
    private List<String> listSelectedUsers;
    private AlarmManager mAlarmManagerAller;
    private AlarmManager mAlarmManagerRetour;

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
        Button reservation = findViewById(R.id.reservation_covoit_btn);
        mProgressBar = findViewById(R.id.progress_bar);
        mTitreInscription = findViewById(R.id.titre_inscription);
        mLinearChampsDynamiques = findViewById(R.id.linearLayoutDynamique);
        mTitrePassager = findViewById(R.id.nom_passager_txt);
        mNbPassagerInput = findViewById(R.id.nb_passager_input);
        mNbPassagerInput.requestFocus();

        listSelectedUsers = new ArrayList<>();
        listUsersStr = new ArrayList<>();
        listUsers = new ArrayList<>();
        //  les AlarmManager permettront de réveiller le téléphone et d'executer du code à une date précise
        mAlarmManagerAller = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        mAlarmManagerRetour = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        // Test performance
        final Trace myTrace = FirebasePerformance.getInstance().newTrace("covoituragePassagersActivityShowPassengers_trace");
        myTrace.start();

        this.configureToolbar();
        this.giveToolbarAName(R.string.covoit_passager_name);
        this.updateUIWhenCreating();
        this.getAllUsers();
        myTrace.stop();

        // --------------------
        // LISTENERS
        // --------------------

        mNbPassagerInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                showFieldsNamePassengers(s);
                myTrace.stop();
            }
        });

        reservation.setOnClickListener(new View.OnClickListener() {
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
                    // AFFICHAGE LISTE PASSAGERS
                    // 1 : creation de la liste des passagers à afficher sans le conducteur
                    for (int i = 0; i < listUsers.size(); i++) {
                        if (!(listUsers.get(i).getNom().equals(covoiturage.getNomConducteur()))) {
                            listUsersStr.add(listUsers.get(i).getPrenom() + " " + listUsers.get(i).getNom());
                        }
                    }
                    // 2 : filtre des paasagers pour ne pas afficher les passagers dejà existants dans le covoiturage
                    if (covoiturage.getListPassagers() != null) {
                        for (int j = 0; j < covoiturage.getListPassagers().size(); j++) {
                            for (int k = 0; k < listUsersStr.size(); k++) {
                                if (covoiturage.getListPassagers().get(j).equals(listUsersStr.get(k)))
                                    listUsersStr.remove(k);
                            }
                        }
                    }


                    if (inputs > 0) {
                        // creation des champs spinner de passagers dynamiquement
                        for (int i = 0; i < inputs; i++) {

                            // creation d'autant de spinner que desirés
                            Spinner spinnerPassagers = new Spinner(this);
                            // Create an ArrayAdapter using the string array and a default spinner layout
                            ArrayAdapter<String> adapterUser = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, listUsersStr);
                            // Specify the layout to use when the list of choices appears
                            adapterUser.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            // Apply the adapter to the spinner
                            spinnerPassagers.setAdapter(adapterUser);
                            // ajout dynamique du spinner au linearlayout
                            mLinearChampsDynamiques.addView(spinnerPassagers);

                            spinnerPassagers.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                @Override
                                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                    // boucle sur tous les spinners
                                    for (int j = 0; j < inputs; j++) {
                                        // garder une trace du premier objet affiché avant changement par l'user
                                        final Spinner spinner = (Spinner) mLinearChampsDynamiques.getChildAt(j);
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
                }
            }
        } else {
            mTitrePassager.setVisibility(View.GONE);
            mLinearChampsDynamiques.removeAllViews();
            listSelectedUsers.clear();
        }
    }

    // --------------------
    // ALARM
    // --------------------

    /**
     * Methode permettant de generer une alarm dans le systeme du telephone de maniere à envoyer une notification à l'utilisateur
     * 2 heures avant que le covoiturage aller parte.
     */
    private void alarmDepart(User user) {

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(covoiturage.getHoraireAller());
        // Declenchement de l'alarm de 2h avant le depart jusqu'au demarrage effectif du covoit
        calendar.add(Calendar.HOUR, -2);
        FirebaseUser auth = FirebaseAuth.getInstance().getCurrentUser();
        if (auth != null && user.getUid().equals(auth.getUid())) {
            Covoiturage covoit = new Covoiturage();
            covoit.setHoraireAller(covoiturage.getHoraireAller());

            Intent intent = new Intent(this, TimeAlarmCovoiturageAller.class).putExtra("covoiturageAlarm", covoit);//.putExtra("user", passager);
            PendingIntent operation = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
            // reveil de l'alarm
            mAlarmManagerAller.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), operation);
        }
    }

    /**
     * Methode permettant de generer une alarm dans le systeme du telephone de maniere à envoyer une notification à l'utilisateur
     * 2 heures avant que le covoiturage retour parte.
     */
    private void alarmRetour(User user) {

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(covoiturage.getHoraireRetour());
        // Declenchement de l'alarm de 2h avant le depart jusqu'au demarrage effectif du covoit
        calendar.add(Calendar.HOUR, -2);
        FirebaseUser auth = FirebaseAuth.getInstance().getCurrentUser();
        if (auth != null) {
            // personne qui s'inscrit soi meme à un covoiturgae
            if (user.getUid().equals(auth.getUid())) {
                Covoiturage covoit = new Covoiturage();
                covoit.setHoraireRetour(covoiturage.getHoraireRetour());

                Intent intent = new Intent(this, TimeAlarmCovoiturageRetour.class).putExtra("covoiturageAlarm", covoit);//.putExtra("user", passager);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 1, intent, PendingIntent.FLAG_ONE_SHOT);
                // reveil de l'alarm
                mAlarmManagerRetour.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
            }
        }
    }


    // --------------------
    // REST REQUETES
    // --------------------

    /**
     * Methode permettant de recuperer tous les utilisateurs existants en bdd, de maniere à pouvoir les
     * afficher dans un spinner pour le choix des passagers d'un covoiturage.
     */
    private void getAllUsers() {

        setupDb().collection("users").orderBy("nom").addSnapshotListener(this, new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (queryDocumentSnapshots != null && queryDocumentSnapshots.size() != 0) {
                    List<DocumentSnapshot> ds = queryDocumentSnapshots.getDocuments();
                    for (DocumentSnapshot doc : ds) {
                        Map<String, Object> user = doc.getData();
                        if (user != null) {
                            User u = new User(user.get("uid").toString(), user.get("nom").toString(), user.get("prenom").toString(), user.get("licence").toString(),
                                    user.get("email").toString(), user.get("niveau").toString(), user.get("fonction").toString());
                            listUsers.add(u);
                        }
                    }
                }
            }
        });
    }

    /**
     * Methode permettant de recuperer les passagers d'un covoiturage et de leur envoyer une notification à chacun
     * pour le depart aller et le depart retour 2 heures avant chaque depart.
     * Les autre utilisateurs de la bdd ne recevront pas de notification.
     *
     * @param nom    nom du passager
     * @param prenom prenom du passager
     */
    private void filteredUserWhoCallAlarm(String nom, String prenom) {
        setupDb().collection("users").whereEqualTo("nom", nom).whereEqualTo("prenom", prenom).addSnapshotListener(this, new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (queryDocumentSnapshots != null) {
                    if (queryDocumentSnapshots.size() != 0) {
                        List<DocumentSnapshot> ds = queryDocumentSnapshots.getDocuments();
                        for (DocumentSnapshot doc : ds) {
                            Map<String, Object> user = doc.getData();
                            if (user != null) {
                                User u = new User(user.get("uid").toString(), user.get("nom").toString(), user.get("prenom").toString(), user.get("licence").toString(),
                                        user.get("email").toString(), user.get("niveau").toString(), user.get("fonction").toString());

                                // notification d'alarme pour chacun des passagers d'un covoiturage
                                alarmDepart(u);
                                alarmRetour(u);
                            }
                        }
                    }
                }
            }
        });
    }

    /**
     * Methode permettant de recuperer et d'afficher toutes les informations d'un covoiturage
     */
    private void getAndShowDatas() {
        Intent intent = getIntent();
        covoiturage = (Covoiturage) Objects.requireNonNull(intent.getExtras()).getSerializable("covoit");
        if (covoiturage != null) {
            mNomConducteur.setText(Html.fromHtml("<b>Conducteur : </b>" + covoiturage.getPrenomConducteur() + " " + covoiturage.getNomConducteur()));
            mDateDepart.setText(Html.fromHtml("<b>Aller : départ le </b>" + stDateToString(covoiturage.getHoraireAller()) + "<b> depuis </b>" + covoiturage.getLieuDepartAller()));
            mDateretour.setText(Html.fromHtml("<b>Retour : départ le </b>" + stDateToString(covoiturage.getHoraireRetour()) + "<b> jusqu'à </b>" + covoiturage.getLieuArriveeRetour()));
            mNbPlaceDispo.setText(Html.fromHtml("<b>Places disponibles : </b>" + covoiturage.getNbPlacesDispo() + " / " + covoiturage.getNbPlacesTotal()));
            mTypeVehicule.setText(Html.fromHtml("<b>Type Véhicule : </b>" + covoiturage.getTypeVehicule()));
        }
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
                // Test performance
                final Trace myTrace1 = FirebasePerformance.getInstance().newTrace("covoituragePassagersActivityReservation_trace");
                myTrace1.start();

                String placesRestantes = String.valueOf(nbPlacesRestantes);
                mNbPlaceDispo.setText(placesRestantes);
                // recuperation passagers existants dejà pour ce covoit
                List<String> listPassagers = new ArrayList<>();
                if (covoiturage.getListPassagers() != null)
                    listPassagers = covoiturage.getListPassagers();

                // ajout de passager(s) dans l'objet covoiturage en plus de ceux qui existaient avant
                for (int i = 0; i < listSelectedUsers.size(); i++) {
                    listPassagers.add(listSelectedUsers.get(i).toUpperCase());

                    // decomposition du nom et du prenom recu dans le param name
                    String nom = null;
                    String prenom;
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
                    // declenchement des alarmes sur les passagers choisis dans les spinners + passagers dejà existants dans ce covoiturage
                    this.filteredUserWhoCallAlarm(nom, prenom);
                }
                this.mProgressBar.setVisibility(View.VISIBLE);
                //CRUD
                CovoiturageHelper.updateCovoiturage(covoiturage.getId(), placesRestantes, listPassagers)
                        .addOnFailureListener(this.onFailureListener())
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(CovoituragePassagersActivity.this, R.string.create_passager,
                                        Toast.LENGTH_SHORT).show();
                                /*alarmDepart();
                                alarmRetour();*/
                                startActivityCovoiturageVehicule(); // renvoi l'user sur la page des covoiturages apres validation de la creation de l'user dans les covoit
                                myTrace1.stop();
                            }
                        });
            }
        }
    }

    /**
     * Methode de calcul du nombre de places restantes dans un covoiturage
     *
     * @return nb de places restantes pour un covoiturage
     */
    private int calculNbPlacesRestantes() {
        // Test performance
        final Trace myTrace2 = FirebasePerformance.getInstance().newTrace("covoituragePassagersActivityCalculNbPlacesRestantes_trace");
        myTrace2.start();

        String passagers = mNbPassagerInput.getText().toString();
        int nbPassagers = 0;
        int nbPlacesDispo;
        int nbPlacesRestantes;
        if (!passagers.equals("")) {
            nbPassagers = Integer.parseInt(passagers);
        }
        // mis en place d'une securité apres les test firebase test lab en cas covoit null, renvoi le meme nb de passager que recu, comme
        // s'il n'y avait pas eu de calcul
        // erreur : "java.lang.NullPointerException: Attempt to read from field 'giv grg.c' on a null object reference" => Rien trouvé sur internet
        if (covoiturage != null) {
            nbPlacesDispo = Integer.parseInt(covoiturage.getNbPlacesDispo());
            nbPlacesRestantes = nbPlacesDispo - nbPassagers;
        } else
            nbPlacesRestantes = nbPassagers;
        myTrace2.stop();
        return nbPlacesRestantes;
    }
}
