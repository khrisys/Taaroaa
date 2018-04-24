package fr.drochon.christian.taaroaa.covoiturage;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import fr.drochon.christian.taaroaa.R;
import fr.drochon.christian.taaroaa.model.Covoiturage;

public class VehiculeViewHolder extends RecyclerView.ViewHolder {

    //DATA
    List<Covoiturage> mCovoiturageList;
    //CELLULE
    @BindView(R.id.vehicle_linear_layout) LinearLayout mLinearLayoutGlobal;
    @BindView(R.id.covoit_conducteur_nom)
    TextView mNomConducteur;
    @BindView(R.id.covoit_passager_framelayout)
    FrameLayout mFrameLayoutPassagers;
    @BindView(R.id.passager_titre_txt) TextView mTitrePassager;
    @BindView(R.id.passager_spinner)
    Spinner mPassagerSpinner;
    @BindView(R.id.covoit_vehicule_framelayout) FrameLayout mFrameLayoutVehicules;
    @BindView(R.id.vehicule_titre_txt) TextView mTitreVehicule;
    @BindView(R.id.vehicule_spinner) Spinner mVehiculeSpinner;
    @BindView(R.id.covoit_places_framelayout) FrameLayout mFrameLayoutPlaces;
    @BindView(R.id.places_titre_txt) TextView mTitrePlace;
    @BindView(R.id.places_spinner) Spinner mPlaceSpinner;

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

        mCovoiturageList = new ArrayList<>();

        // clic sur le nom du conducteur qui renvoi l'utilisateur à la page de reservation
        mNomConducteur.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO
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

        for(int i = 0; i < mCovoiturageList.size(); i++) {
            mPassagerSpinner.setSelection(Integer.parseInt(covoiturage.getListPassagers().get(i).getNom()));
            mVehiculeSpinner.setSelection(Integer.parseInt(covoiturage.getTypeVehicule()));
            mPlaceSpinner.setSelection(covoiturage.getNbPlacesDispo());
        }

    }
}
