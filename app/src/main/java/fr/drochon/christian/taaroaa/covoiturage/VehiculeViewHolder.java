package fr.drochon.christian.taaroaa.covoiturage;

import android.annotation.SuppressLint;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import fr.drochon.christian.taaroaa.R;
import fr.drochon.christian.taaroaa.model.Covoiturage;

public class VehiculeViewHolder extends RecyclerView.ViewHolder {

    private Covoiturage sCovoiturage;
    // DESIGN
    @BindView(R.id.vehicle_linear_layout)
    LinearLayout mLinearLayoutGlobal;
    @BindView(R.id.clic_global) LinearLayout mGlobalClic;
    @BindView(R.id.covoit_conducteur_nom)
    TextView mNomConducteur;
    @BindView(R.id.passager_titre_txt)
    TextView mTitrePassager;
    @BindView(R.id.passager_spinner)
    Spinner mPassagerSpinner;
    @BindView(R.id.lieu_depart_aller_txt) TextView mLieuDepart;
    @BindView(R.id.lieu_depart_retour_txt) TextView mLieuRetour;
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
    //DATA
    private List<Covoiturage> mCovoiturageList;
    private List<String> mListPassagers;

    /**
     * Contructeur qui prend en param la vue affichée.
     * Je recupere les 2 textview du layout list_cell.
     * responsable du clic sur les cellules.
     *
     * @param itemView : cellule d'une liste comprenant le nom du conducteur, la liste des passagers pour le covoiturage,
     *                 le type de vehicule et le nombre de place disponible.
     */
    public VehiculeViewHolder(final View itemView) {
        super(itemView);
        // liaison des elements du layout recyclerview et list_cell avec les variables declarées ici
        ButterKnife.bind(this, itemView);

        mCovoiturageList = new ArrayList<>();
        mListPassagers = new ArrayList<>();

        // clic sur le nom du conducteur qui renvoi l'utilisateur à la page de reservation
        mGlobalClic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                FirebaseFirestore db = FirebaseFirestore.getInstance();
                db.collection("users").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot documentSnapshots) {
                        if(documentSnapshots.size() != 0){
                            List<DocumentSnapshot> docSps = documentSnapshots.getDocuments();
                            for (DocumentSnapshot ds:docSps) {
                                Map<String, Object> covoit = ds.getData();
                                if(mNomConducteur.getText() ==  covoit.get("nomConducteur").toString() + " " +covoit.get("prenomConducteur").toString()){
                                    if(covoit.get("listPassagers").equals(0)){
                                        System.out.println("delete");
                                    }
                                }
                            }
                        }
                    }
                });
        /*        if(mNomConducteur.getText() == sCovoiturage.getPrenomConducteur() + " " + sCovoiturage.getNomConducteur()){
                    if(Integer.parseInt(mNbPlaceDispo.getText().toString()) == 0){
                        System.out.println("alertdialog pour suppression de covoit");
                    }
                } else {
                    Intent intent = new Intent(itemView.getContext(), CovoituragePassagersActivity.class).putExtra("covoit", sCovoiturage);
                    itemView.getContext().startActivity(intent);
                }*/
            }
        });
    }

    /**
     * Methode appellée via l'adapter. Cette methode mettra à jour les differentes view du viewholder en fonction de l'utilisateur connecté
     *
     * @param covoiturage
     */
    @SuppressLint("ResourceType")
    public void updateWithCovoiturage(final Covoiturage covoiturage) {
        // ajout des Covoit dans une liste afin de les retrouver pour l'affichage de chaque cours particulier sous forme de notification
        mCovoiturageList.add(covoiturage);
        mListPassagers.addAll(covoiturage.getListPassagers());
        sCovoiturage = covoiturage;

        for (int i = 0; i < mCovoiturageList.size(); i++) {
            String username = covoiturage.getPrenomConducteur() + "  " + covoiturage.getNomConducteur();
            mNomConducteur.setText(username);
            if (covoiturage.getListPassagers() != null) {

                // --------------------
                // REMPLISSAGE SPINNER
                // --------------------
                // Create an ArrayAdapter using the string array and a default spinner layout
                ArrayAdapter<String> adapterNiveau = new ArrayAdapter<String>(itemView.getContext(), android.R.layout.simple_spinner_item, mListPassagers);
                // Specify the layout to use when the list of choices appears
                adapterNiveau.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                // Apply the adapter to the spinner
                mPassagerSpinner.setAdapter(adapterNiveau);
                }
            }
            mTypeVehicule.setText(covoiturage.getTypeVehicule());
            mNbPlaceDispo.setText(covoiturage.getNbPlacesDispo());
            mAller.setText(stDateToString(covoiturage.getHoraireAller()));
            mRetour.setText(stDateToString(covoiturage.getHoraireRetour()));
            mLieuDepart.setText(covoiturage.getLieuDepartAller());
            mLieuRetour.setText(covoiturage.getLieuDepartRetour());
    }

    /**
     * Methode permettant de formatter une date en string avec locale en francais
     *
     * @param horaireDuCours
     * @return
     */
    private String stDateToString(Date horaireDuCours) {

        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE dd MMM yyyy ' à ' HH'h'mm", Locale.FRANCE);
        String dateDuCours = dateFormat.format(horaireDuCours);
        return dateDuCours;
    }


    // --------------------
    // UI
    // --------------------

    /**
     * Methode permettant de retrouver la position d'un item de la liste des niveaux de plongée d'un user
     *
     * @param spinner
     * @param myString
     * @return int
     */
    protected int getIndexSpinner(Spinner spinner, String myString) {
        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(myString)) {
                return i;
            }
        }
        return 0;
    }
}
