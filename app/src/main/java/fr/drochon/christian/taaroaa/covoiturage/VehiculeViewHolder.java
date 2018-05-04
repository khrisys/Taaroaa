package fr.drochon.christian.taaroaa.covoiturage;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import fr.drochon.christian.taaroaa.R;
import fr.drochon.christian.taaroaa.model.Covoiturage;

public class VehiculeViewHolder extends RecyclerView.ViewHolder {

    // DESIGN
    @BindView(R.id.vehicle_linear_layout)
    LinearLayout mLinearLayoutGlobal;
    @BindView(R.id.covoit_conducteur_nom)
    TextView mNomConducteur;
    @BindView(R.id.passager_titre_txt)
    TextView mTitrePassager;
    @BindView(R.id.passager_spinner)
    Spinner mPassagerSpinner;
    @BindView(R.id.vehicule_titre_txt)
    TextView mTitreVehicule;
    @BindView(R.id.typeVehicule_txt)
    TextView mTypeVehicule;
    @BindView(R.id.places_titre_txt)
    TextView mTitrePlace;
    @BindView(R.id.nbPlacesDispo_txt)
    TextView mNbPlaceDispo;
    //DATA
    private List<Covoiturage> mCovoiturageList;
    //CELLULE
    private RecyclerView mRecyclerView;

    /**
     * Contructeur qui prend en param la vue affichée.
     * Je recupere les 2 textview du layout list_cell.
     * responsable du clic sur les cellules.
     *
     * @param itemView : cellule d'une liste comprenant le nom du conducteur, la liste des passagers pour le covoiturage,
     *                 le type de vehicule et le nombre de place disponible.
     */
    public VehiculeViewHolder(View itemView) {
        super(itemView);
        // liaison des elements du layout recyclerview et list_cell avec les variables declarées ici
        ButterKnife.bind(this, itemView);

        mRecyclerView = itemView.findViewById(R.id.recyclerViewCovoitVehicules);
        mCovoiturageList = new ArrayList<>();

        // clic sur le nom du conducteur qui renvoi l'utilisateur à la page de reservation
        mNomConducteur.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO recuperer l'objet Covoiturage via l'id en requetant. Passer ensuite l'id à la liste depuis la creation de l'objet dans l'ecran conducteur.
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                db.collection("covoiturage").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot documentSnapshots) {
                        System.out.println("get l'objet");
                    }
                });
            }
        });
    }

    /**
     * Methode appellée via l'adapter. Cette methode mettra à jour les differentes view du viewholder en fonction de l'utilisateur connecté
     *
     * @param covoiturage
     */
    public void updateWithCovoiturage(final Covoiturage covoiturage) {
        // ajout des Covoit dans une liste afin de les retrouver pour l'affichage de chaque cours particulier sous forme de notification
        mCovoiturageList.add(covoiturage);

        for (int i = 0; i < mCovoiturageList.size(); i++) {
            mNomConducteur.setText(covoiturage.getNomConducteur() + "  " + covoiturage.getPrenomConducteur());
            if (covoiturage.getListPassagers() != null)
                mPassagerSpinner.setSelection(getIndexSpinner(mPassagerSpinner, covoiturage.getListPassagers().get(i).getNom()));
            mTypeVehicule.setText(covoiturage.getTypeVehicule());
            mNbPlaceDispo.setText(covoiturage.getNbPlacesDispo());
        }
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
