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
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import fr.drochon.christian.taaroaa.R;
import fr.drochon.christian.taaroaa.alarm.TimeAlarmCovoiturageSuppression;
import fr.drochon.christian.taaroaa.api.CovoiturageHelper;
import fr.drochon.christian.taaroaa.model.Covoiturage;

public class VehiculeViewHolder extends RecyclerView.ViewHolder {

    //DATA
    private final List<Covoiturage> mCovoiturageList;
    private final List<String> mListPassagers;
    private final FirebaseFirestore db;
    private final AlarmManager mAlarmManager;
    private Covoiturage sCovoiturage;
    // DESIGN
    @BindView(R.id.vehicle_linear_layout)
    LinearLayout mLinearLayoutGlobal;
    @BindView(R.id.clic_global)
    LinearLayout mGlobalClic;
    @BindView(R.id.covoit_conducteur_nom)
    TextView mNomConducteur;
    @BindView(R.id.poubelle_btn)
    ImageButton mPoubelleImg;
    @BindView(R.id.passager_titre_txt)
    TextView mTitrePassager;
    @BindView(R.id.passager_spinner)
    Spinner mPassagerSpinner;
    @BindView(R.id.lieu_depart_aller_txt)
    TextView mLieuDepart;
    @BindView(R.id.lieu_depart_retour_txt)
    TextView mLieuRetour;
    @BindView(R.id.vehicule_titre_txt)
    TextView mTitreVehicule;
    @BindView(R.id.typeVehicule_txt)
    TextView mTypeVehicule;
    @BindView(R.id.places_titre_txt)
    TextView mTitrePlace;
    @BindView(R.id.nbPlacesDispo_txt)
    TextView mNbPlaceDispo;
    @BindView(R.id.aller_txt)
    TextView mAller;
    @BindView(R.id.retour_txt)
    TextView mRetour;


    /**
     * Contructeur qui prend en param la vue affichée.
     * Je recupere les 2 textview du layout pupils_cell.
     * responsable du clic sur les cellules.
     *
     * @param itemView : cellule d'une liste comprenant le nom du conducteur, la liste des passagers pour le covoiturage,
     *                 le type de vehicule et le nombre de place disponible.
     */
    VehiculeViewHolder(final View itemView) {
        super(itemView);
        // liaison des elements du layout recyclerview et pupils_cell avec les variables declarées ici
        ButterKnife.bind(this, itemView);

        mCovoiturageList = new ArrayList<>();
        mListPassagers = new ArrayList<>();
        db = FirebaseFirestore.getInstance();
        mAlarmManager = (AlarmManager) itemView.getContext().getSystemService(Context.ALARM_SERVICE);


        // --------------------
        // LISTENERS
        // --------------------

        mGlobalClic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // clic sur le nom du conducteur qui renvoi l'utilisateur à la page de reservation
                if (Integer.parseInt(sCovoiturage.getNbPlacesDispo()) > 0)
                    startActivityCovoituragePassagers();
                    //alterdialog de covoit complet et renvoi vers la page des covoiturages
                else {
                    final AlertDialog.Builder adb = new AlertDialog.Builder(itemView.getContext());
                    adb.setTitle("Covoiturage complet !");
                    adb.setIcon(android.R.drawable.ic_delete);
                    adb.setTitle("Ce covoiturage est complet !");
                    adb.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            startActivityCovoiturageVehicule();
                        }
                    });
                    adb.show();
                }
            }
        });

        // suppression d'un covoiturage
        mPoubelleImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                db.collection("users").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot documentSnapshots) {
                        if (documentSnapshots.size() != 0) {
                            List<DocumentSnapshot> docSps = documentSnapshots.getDocuments();
                            for (DocumentSnapshot ds : docSps) {
                                final Map<String, Object> user = ds.getData();
                                // comparaison entre les users cde la bdd et l'user ayant créé le covoiturage
                                if(user != null) {
                                    if (mNomConducteur.getText().equals(user.get("prenom") + "  " + user.get("nom"))) {
                                        //suppression de covoit
                                        final AlertDialog.Builder adb = new AlertDialog.Builder(itemView.getContext());
                                        adb.setTitle(R.string.alertDialog_delete_covoit);

                                        // ajouter une couleur à l'icon de warning
                                        Drawable warning = itemView.getResources().getDrawable(android.R.drawable.ic_dialog_alert);
                                        ColorFilter filter = new LightingColorFilter(Color.RED, Color.BLUE);
                                        warning.setColorFilter(filter);
                                        adb.setIcon(android.R.drawable.ic_dialog_alert);

                                        adb.setTitle(R.string.alertDialog_delete_covoit);
                                        adb.setPositiveButton("SUPPRIMER ?", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                deleteCovoiturageInFirebase(user.get("prenom").toString(), user.get("nom").toString());

                                                //TODO notification aux passagers que le covoit est annulé
                                                //TODO faire une requete pour boucler sur les users et recuperer les passagers du covoit annulé par leurs noms et prenom. Sur ces personnes :  declencher l'alarm
                                                Calendar calendar = Calendar.getInstance();
                                                calendar.getTime();
                                                Intent intent = new Intent(itemView.getContext(), TimeAlarmCovoiturageSuppression.class)
                                                        .putExtra("covoit", sCovoiturage);
                                                PendingIntent operation = PendingIntent.getBroadcast(itemView.getContext(), 3, intent, PendingIntent.FLAG_ONE_SHOT);
                                                // reveil de l'alarm
                                                if(mAlarmManager != null)
                                                mAlarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), operation);

                                                startActivityCovoiturageVehicule();
                                            }
                                        });
                                        adb.setNegativeButton("ANNULER", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                // rien : rester sur l'ecran actuel
                                            }
                                        });
                                        adb.show();
                                    }
                                }
                            }
                        }
                    }

                });
            }
        });
    }


    // --------------------
    // UI
    // --------------------

    /**
     * Methode appellée via l'adapter. Cette methode mettra à jour les differentes view du viewholder en fonction de l'utilisateur connecté
     *
     * @param covoiturage
     */
    @SuppressLint({"ResourceType", "SetTextI18n"})
    public void updateWithCovoiturage(final Covoiturage covoiturage) {
        // ajout des Covoit dans une liste afin de les retrouver pour l'affichage de chaque cours particulier sous forme de notification
        mCovoiturageList.add(covoiturage);
        mListPassagers.addAll(covoiturage.getListPassagers());
        sCovoiturage = covoiturage;

        for (int i = 0; i < mCovoiturageList.size(); i++) {
            String username = covoiturage.getPrenomConducteur() + "  " + covoiturage.getNomConducteur();
            mNomConducteur.setText(username);
            // affichage evetnuel de la poubelle des covoits de l'user actuellement connecté
            showPoubelle(covoiturage);

            // --------------------
            // REMPLISSAGE SPINNER
            // --------------------
            if (covoiturage.getListPassagers() != null) {
                // Create an ArrayAdapter using the string array and a default spinner layout
                ArrayAdapter<String> adapterNiveau = new ArrayAdapter<>(itemView.getContext(), android.R.layout.simple_spinner_item, mListPassagers);
                // Specify the layout to use when the list of choices appears
                adapterNiveau.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                // Apply the adapter to the spinner
                mPassagerSpinner.setAdapter(adapterNiveau);
            }
        }
        mTypeVehicule.setText(covoiturage.getTypeVehicule());
        String ratioPlaces = covoiturage.getNbPlacesDispo() + "/" + covoiturage.getNbPlacesTotal();
        if (Integer.parseInt(covoiturage.getNbPlacesDispo()) > 0)
            mNbPlaceDispo.setText(Html.fromHtml("<font color='green'><b>" + ratioPlaces + "</b></font>"));
        else
            mNbPlaceDispo.setText(Html.fromHtml("<font color='red'><b>" + ratioPlaces + "</b></font>"));
        mAller.setText(stDateToString(covoiturage.getHoraireAller()));
        mRetour.setText(stDateToString(covoiturage.getHoraireRetour()));
        mLieuDepart.setText(covoiturage.getLieuDepartAller());
        mLieuRetour.setText(covoiturage.getLieuDepartRetour());
    }

    /**
     * Methode permettant à l'utilisateur d'etre redirigé vers la pages principale des covoiturages
     */
    private void startActivityCovoiturageVehicule() {
        Intent intent = new Intent(itemView.getContext(), CovoiturageVehiclesActivity.class);
        itemView.getContext().startActivity(intent);
    }

    /**
     * Methode permettant à un utilisateur d'etre redirigé vers la page du detail des covoiturages
     */
    private void startActivityCovoituragePassagers() {
        Intent intent = new Intent(itemView.getContext(), CovoituragePassagersActivity.class).putExtra("covoit", sCovoiturage);
        itemView.getContext().startActivity(intent);
    }


    // --------------------
    // REST REQUESTS
    // --------------------

    /**
     * Methode permettant de supprimer un covoiturage si ce covoiturage ne comporte pas encore de passager
     */
    private void deleteCovoiturageInFirebase(final String prenom, final String nom) {

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("covoiturages").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot documentSnapshots) {
                if (documentSnapshots.size() != 0) {
                    List<DocumentSnapshot> docSps = documentSnapshots.getDocuments();
                    for (DocumentSnapshot ds : docSps) {
                        Map<String, Object> covoit = ds.getData();
                        if(covoit != null) {
                            if (covoit.get("nomConducteur").toString().equals(nom) && covoit.get("prenomConducteur").toString().equals(prenom)) {
                                //CRUD
                                CovoiturageHelper.deleteCovoiturage(covoit.get("id").toString())
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                //TODO envoyer une notification à tous les passagers qui auraient été inscrits à ce covoiturage desormais supprimé
                                                Toast.makeText(itemView.getContext(), R.string.delete_covoit,
                                                        Toast.LENGTH_LONG).show();
                                                startActivityCovoiturageVehicule(); // renvoi l'user sur la page des covoiturages apres validation de la creation de l'user dans les covoit
                                            }
                                        });
                            }
                        }
                    }
                }
            }
        });
    }

    /**
     * Methode permettant de supprimer un covoiturage si l'utiliateur actuellement connecté est le createur
     * d'un ou plusieurs covoiturage.
     */
    private void showPoubelle(final Covoiturage currentCovoit) {
        // erreur crashlytics lors du reveil du tel pour les notifs lorsque l'application est eteinte : l'id est null
        FirebaseUser auth =  FirebaseAuth.getInstance().getCurrentUser();
        if(auth != null)
        db.collection("users").document(auth.getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    final Map<String, Object> user = documentSnapshot.getData();
                    if(user != null) {
                        if (currentCovoit.getNomConducteur().equals(user.get("nom").toString()) && currentCovoit.getPrenomConducteur().equals(user.get("prenom").toString())) {
                            mPoubelleImg.setVisibility(View.VISIBLE);
                        }
                    }
                }
            }
        });
    }


    // --------------------
    // DATES
    // --------------------

    /**
     * Methode permettant de formatter une date en string avec locale en francais
     *
     * @param horaireDuCours
     * @return
     */
    private String stDateToString(Date horaireDuCours) {

        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE dd MMM yyyy ' à ' HH'h'mm", Locale.FRANCE);
        return dateFormat.format(horaireDuCours);
    }
}
